package app.aaps.workflow

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.aaps.core.data.configuration.Constants
import app.aaps.core.data.model.BS
import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.data.model.TE
import app.aaps.core.data.time.T
import app.aaps.core.graph.data.BolusDataPoint
import app.aaps.core.graph.data.CarbsDataPoint
import app.aaps.core.graph.data.DataPointWithLabelInterface
import app.aaps.core.graph.data.EffectiveProfileSwitchDataPoint
import app.aaps.core.graph.data.ExtendedBolusDataPoint
import app.aaps.core.graph.data.HeartRateDataPoint
import app.aaps.core.graph.data.PointsWithLabelGraphSeries
import app.aaps.core.graph.data.StepsDataPoint
import app.aaps.core.graph.data.TherapyEventDataPoint
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.overview.OverviewData
import app.aaps.core.interfaces.overview.graph.BolusGraphPoint
import app.aaps.core.interfaces.overview.graph.BolusType
import app.aaps.core.interfaces.overview.graph.CarbsGraphPoint
import app.aaps.core.interfaces.overview.graph.ExtendedBolusGraphPoint
import app.aaps.core.interfaces.overview.graph.OverviewDataCache
import app.aaps.core.interfaces.overview.graph.TherapyEventGraphPoint
import app.aaps.core.interfaces.overview.graph.TherapyEventType
import app.aaps.core.interfaces.overview.graph.EpsGraphPoint
import app.aaps.core.interfaces.overview.graph.TreatmentGraphData
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventIobCalculationProgress
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.interfaces.utils.Round
import app.aaps.core.interfaces.utils.Translator
import app.aaps.core.interfaces.workflow.CalculationWorkflow
import app.aaps.core.keys.UnitDoubleKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.objects.workflow.LoggingWorker
import app.aaps.core.utils.receivers.DataWorkerStorage
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.math.min

class PrepareTreatmentsDataWorker(
    context: Context,
    params: WorkerParameters
) : LoggingWorker(context, params, Dispatchers.Default) {

    // MIGRATION: KEEP - Core dependencies needed for calculation
    @Inject lateinit var dataWorkerStorage: DataWorkerStorage
    @Inject lateinit var profileUtil: ProfileUtil
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var translator: Translator
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var decimalFormatter: DecimalFormatter
    @Inject lateinit var preferences: Preferences

    // MIGRATION: KEEP - New cache for Compose graphs
    @Inject lateinit var overviewDataCache: OverviewDataCache

    // MIGRATION: DELETE - Remove after OverviewFragment converted to Compose
    class PrepareTreatmentsData(
        val overviewData: OverviewData // DELETE: This parameter goes away
    )

    override suspend fun doWorkAndLog(): Result {

        val data = dataWorkerStorage.pickupObject(inputData.getLong(DataWorkerStorage.STORE_KEY, -1)) as PrepareTreatmentsData?
            ?: return Result.failure(workDataOf("Error" to "missing input data"))

        // MIGRATION: Get time range from OLD cache for OLD GraphView system
        val endTimeOld = data.overviewData.endTime
        val fromTimeOld = data.overviewData.fromTime

        // MIGRATION: Calculate 24h range for NEW Compose system
        val toTimeNew = endTimeOld  // Same end time
        val fromTimeNew = toTimeNew - T.hours(Constants.GRAPH_TIME_RANGE_HOURS.toLong()).msecs()

        // MIGRATION: Fetch data using MIN of both ranges (ensures we get 24h of data)
        val endTime = endTimeOld
        val fromTime = min(fromTimeOld, fromTimeNew)

        rxBus.send(EventIobCalculationProgress(CalculationWorkflow.ProgressData.PREPARE_TREATMENTS_DATA, 0, null))

        // ========== MIGRATION: DELETE - Start GraphView-specific code ==========
        data.overviewData.maxTreatmentsValue = 0.0
        data.overviewData.maxTherapyEventValue = 0.0
        data.overviewData.maxEpsValue = 0.0
        val filteredTreatments: MutableList<DataPointWithLabelInterface> = ArrayList()
        val filteredTherapyEvents: MutableList<DataPointWithLabelInterface> = ArrayList()
        val filteredEps: MutableList<DataPointWithLabelInterface> = ArrayList()

        persistenceLayer.getBolusesFromTimeToTime(fromTime, endTime, true)
            .map { BolusDataPoint(it, rh, activePlugin.activePump.pumpDescription.bolusStep, preferences, decimalFormatter) }
            .filter { it.data.type == BS.Type.NORMAL || it.data.type == BS.Type.SMB }
            .forEach {
                it.y = getNearestBg(data.overviewData, it.x.toLong())
                filteredTreatments.add(it)
            }
        persistenceLayer.getCarbsFromTimeToTimeExpanded(fromTime, endTime, true)
            .map { CarbsDataPoint(it, rh) }
            .forEach {
                it.y = getNearestBg(data.overviewData, it.x.toLong())
                filteredTreatments.add(it)
            }

        // ProfileSwitch
        persistenceLayer.getEffectiveProfileSwitchesFromTimeToTime(fromTime, endTime, true)
            .map { EffectiveProfileSwitchDataPoint(it, rh, data.overviewData.epsScale) }
            .forEach {
                data.overviewData.maxEpsValue = maxOf(data.overviewData.maxEpsValue, it.data.originalPercentage.toDouble())
                filteredEps.add(it)
            }

        // Extended bolus
        if (!activePlugin.activePump.isFakingTempsByExtendedBoluses) {
            persistenceLayer.getExtendedBolusesStartingFromTimeToTime(fromTime, endTime, true)
                .map { ExtendedBolusDataPoint(it, rh) }
                .filter { it.duration != 0L }
                .forEach {
                    it.y = getNearestBg(data.overviewData, it.x.toLong())
                    filteredTreatments.add(it)
                }
        }

        // Careportal
        persistenceLayer.getTherapyEventDataFromToTime(fromTime - T.hours(6).msecs(), endTime)
            .map { TherapyEventDataPoint(it, rh, profileUtil, translator) }
            .filterTimeframe(fromTime, endTime)
            .forEach {
                if (it.y == 0.0) it.y = getNearestBg(data.overviewData, it.x.toLong())
                filteredTherapyEvents.add(it)
            }

        // increase maxY if a treatment forces it's own height that's higher than a BG value
        filteredTreatments.maxOfOrNull { it.y }
            ?.let(::addUpperChartMargin)
            ?.let { data.overviewData.maxTreatmentsValue = maxOf(data.overviewData.maxTreatmentsValue, it) }
        filteredTherapyEvents.maxOfOrNull { it.y }
            ?.let(::addUpperChartMargin)
            ?.let { data.overviewData.maxTherapyEventValue = maxOf(data.overviewData.maxTherapyEventValue, it) }

        data.overviewData.treatmentsSeries = PointsWithLabelGraphSeries(filteredTreatments.toTypedArray())
        data.overviewData.therapyEventSeries = PointsWithLabelGraphSeries(filteredTherapyEvents.toTypedArray())
        data.overviewData.epsSeries = PointsWithLabelGraphSeries(filteredEps.toTypedArray())

        data.overviewData.heartRateGraphSeries = PointsWithLabelGraphSeries<DataPointWithLabelInterface>(
            persistenceLayer.getHeartRatesFromTimeToTime(fromTime, endTime)
                .map { hr -> HeartRateDataPoint(hr, rh) }
                .toTypedArray()).apply { color = rh.gac(null, app.aaps.core.ui.R.attr.heartRateColor) }

        data.overviewData.stepsCountGraphSeries = PointsWithLabelGraphSeries<DataPointWithLabelInterface>(
            persistenceLayer.getStepsCountFromTimeToTime(fromTime, endTime)
                .map { steps -> StepsDataPoint(steps, rh) }
                .toTypedArray()).apply { color = rh.gac(null, app.aaps.core.ui.R.attr.stepsColor) }
        // ========== MIGRATION: DELETE - End GraphView-specific code ==========

        // ========== MIGRATION: KEEP - Start Compose/Vico code ==========
        val bolusStep = activePlugin.activePump.pumpDescription.bolusStep
        val lowMarkInUnits = preferences.get(UnitDoubleKey.OverviewLowMark)

        // Boluses and SMBs
        val bolusPoints = persistenceLayer.getBolusesFromTimeToTime(fromTimeNew, toTimeNew, true)
            .filter { it.type == BS.Type.NORMAL || it.type == BS.Type.SMB }
            .map { bs ->
                val nearestBg = getNearestBg(data.overviewData, bs.timestamp)
                BolusGraphPoint(
                    timestamp = bs.timestamp,
                    amount = bs.amount,
                    bolusType = if (bs.type == BS.Type.SMB) BolusType.SMB else BolusType.NORMAL,
                    yValue = if (bs.type == BS.Type.SMB) lowMarkInUnits else nearestBg,
                    isValid = bs.isValid,
                    label = decimalFormatter.toPumpSupportedBolus(bs.amount, bolusStep)
                )
            }

        // Carbs
        val carbsPoints = persistenceLayer.getCarbsFromTimeToTimeExpanded(fromTimeNew, toTimeNew, true)
            .map { ca ->
                CarbsGraphPoint(
                    timestamp = ca.timestamp,
                    amount = ca.amount,
                    yValue = getNearestBg(data.overviewData, ca.timestamp),
                    isValid = ca.isValid && ca.amount > 0,
                    label = rh.gs(app.aaps.core.ui.R.string.format_carbs, ca.amount.toInt())
                )
            }

        // Extended boluses
        val extendedBolusPoints = if (!activePlugin.activePump.isFakingTempsByExtendedBoluses) {
            persistenceLayer.getExtendedBolusesStartingFromTimeToTime(fromTimeNew, toTimeNew, true)
                .filter { it.duration != 0L }
                .map { eb ->
                    ExtendedBolusGraphPoint(
                        timestamp = eb.timestamp,
                        amount = eb.amount,
                        rate = eb.rate,
                        duration = eb.duration,
                        yValue = getNearestBg(data.overviewData, eb.timestamp),
                        label = rh.gs(app.aaps.core.ui.R.string.extended_bolus_data_point_graph, eb.amount, eb.rate)
                    )
                }
        } else emptyList()

        // Therapy events
        val therapyEventPoints = persistenceLayer.getTherapyEventDataFromToTime(fromTimeNew - T.hours(6).msecs(), toTimeNew)
            .filter { te -> te.timestamp + te.duration >= fromTimeNew && te.timestamp <= toTimeNew }
            .map { te ->
                val teYValue = computeTherapyEventY(te, data.overviewData)
                val teType = when {
                    te.type == TE.Type.NS_MBG                -> TherapyEventType.MBG
                    te.type == TE.Type.FINGER_STICK_BG_VALUE -> TherapyEventType.FINGER_STICK
                    te.type == TE.Type.ANNOUNCEMENT          -> TherapyEventType.ANNOUNCEMENT
                    te.type == TE.Type.SETTINGS_EXPORT       -> TherapyEventType.SETTINGS_EXPORT
                    te.type == TE.Type.EXERCISE              -> TherapyEventType.EXERCISE
                    te.duration > 0                          -> TherapyEventType.GENERAL_WITH_DURATION
                    else                                     -> TherapyEventType.GENERAL
                }
                val teLabel = if (!te.note.isNullOrBlank()) te.note!! else translator.translate(te.type)
                TherapyEventGraphPoint(
                    timestamp = te.timestamp,
                    eventType = teType,
                    yValue = teYValue,
                    label = teLabel,
                    duration = te.duration
                )
            }

        // Effective Profile Switches
        val epsPoints = persistenceLayer.getEffectiveProfileSwitchesFromTimeToTime(fromTimeNew, toTimeNew, true)
            .map { eps ->
                val label = buildString {
                    if (eps.originalPercentage != 100) append("${eps.originalPercentage}%")
                    if (eps.originalPercentage != 100 && eps.originalTimeshift != 0L) append(",")
                    if (eps.originalTimeshift != 0L) append("${T.msecs(eps.originalTimeshift).hours()}${rh.gs(app.aaps.core.interfaces.R.string.shorthour)}")
                }
                EpsGraphPoint(
                    timestamp = eps.timestamp,
                    originalPercentage = eps.originalPercentage,
                    originalTimeshift = eps.originalTimeshift,
                    profileName = eps.originalCustomizedName,
                    label = label
                )
            }

        overviewDataCache.updateTreatmentGraph(
            TreatmentGraphData(
                boluses = bolusPoints,
                carbs = carbsPoints,
                extendedBoluses = extendedBolusPoints,
                therapyEvents = therapyEventPoints,
                effectiveProfileSwitches = epsPoints
            )
        )
        // ========== MIGRATION: KEEP - End Compose/Vico code ==========

        rxBus.send(EventIobCalculationProgress(CalculationWorkflow.ProgressData.PREPARE_TREATMENTS_DATA, 100, null))
        return Result.success()
    }

    // MIGRATION: KEEP - Used by both old and new code
    private fun addUpperChartMargin(maxBgValue: Double) =
        if (profileUtil.units == GlucoseUnit.MGDL) Round.roundTo(maxBgValue, 40.0) + 80 else Round.roundTo(maxBgValue, 2.0) + 4

    // MIGRATION: KEEP - Used by both old and new code
    private fun getNearestBg(overviewData: OverviewData, date: Long): Double {
        overviewData.bgReadingsArray.let { bgReadingsArray ->
            for (reading in bgReadingsArray) {
                if (reading.timestamp > date) continue
                return profileUtil.fromMgdlToUnits(reading.value)
            }
            return if (bgReadingsArray.isNotEmpty()) profileUtil.fromMgdlToUnits(bgReadingsArray[0].value)
            else profileUtil.fromMgdlToUnits(100.0)
        }
    }

    /**
     * Compute Y value for therapy event, matching legacy TherapyEventDataPoint logic:
     * - NS_MBG: always use glucose value
     * - Events with glucose: convert and use glucose value
     * - Others: use nearest BG reading
     */
    private fun computeTherapyEventY(te: TE, overviewData: OverviewData): Double {
        if (te.type == TE.Type.NS_MBG && te.glucose != null) {
            return profileUtil.fromMgdlToUnits(te.glucose!!)
        }
        if (te.glucose != null && te.glucose != 0.0) {
            val mgdl = when (te.glucoseUnit) {
                GlucoseUnit.MGDL -> te.glucose!!
                GlucoseUnit.MMOL -> te.glucose!! * Constants.MMOLL_TO_MGDL
            }
            return profileUtil.fromMgdlToUnits(mgdl)
        }
        return getNearestBg(overviewData, te.timestamp)
    }

    // MIGRATION: DELETE - Only used by old GraphView code
    private fun <E : DataPointWithLabelInterface> List<E>.filterTimeframe(fromTime: Long, endTime: Long): List<E> =
        filter { it.x + it.duration >= fromTime && it.x <= endTime }
}
