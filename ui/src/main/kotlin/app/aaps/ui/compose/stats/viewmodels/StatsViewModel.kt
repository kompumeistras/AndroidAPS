package app.aaps.ui.compose.stats.viewmodels

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.configuration.Constants
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.stats.DexcomTIR
import app.aaps.core.interfaces.stats.DexcomTirCalculator
import app.aaps.core.interfaces.stats.TddCalculator
import app.aaps.core.interfaces.stats.TirCalculator
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.ui.R
import app.aaps.ui.activityMonitor.ActivityMonitor
import app.aaps.ui.activityMonitor.ActivityStats
import app.aaps.ui.compose.stats.TddStatsData
import app.aaps.ui.compose.stats.TirStatsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for StatsScreen managing statistics data loading and state.
 */
@Stable
class StatsViewModel @Inject constructor(
    private val tddCalculator: TddCalculator,
    private val tirCalculator: TirCalculator,
    private val dexcomTirCalculator: DexcomTirCalculator,
    private val activityMonitor: ActivityMonitor,
    private val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val uiInteraction: UiInteraction,
    private val uel: UserEntryLogger,
    val dateUtil: DateUtil,
    val profileUtil: ProfileUtil
) : ViewModel() {

    val uiState: StateFlow<StatsUiState>
        field = MutableStateFlow(StatsUiState())

    init {
        loadAllStats()
    }

    private fun loadAllStats() {
        loadTddStats()
        loadTirStats()
        loadDexcomTirStats()
        loadActivityStats()
    }

    private fun loadTddStats() {
        viewModelScope.launch {
            uiState.update { it.copy(tddLoading = true) }
            val data = withContext(Dispatchers.IO) {
                val tdds = tddCalculator.calculate(7, allowMissingDays = true)
                val averageTdd = tddCalculator.averageTDD(tdds)
                val todayTdd = tddCalculator.calculateToday()
                TddStatsData(tdds = tdds, averageTdd = averageTdd, todayTdd = todayTdd)
            }
            uiState.update { it.copy(tddStatsData = data, tddLoading = false) }
        }
    }

    private fun loadTirStats() {
        viewModelScope.launch {
            uiState.update { it.copy(tirLoading = true) }
            val data = withContext(Dispatchers.IO) {
                val lowTirMgdl = Constants.STATS_RANGE_LOW_MMOL * Constants.MMOLL_TO_MGDL
                val highTirMgdl = Constants.STATS_RANGE_HIGH_MMOL * Constants.MMOLL_TO_MGDL
                val lowTitMgdl = Constants.STATS_TARGET_LOW_MMOL * Constants.MMOLL_TO_MGDL
                val highTitMgdl = Constants.STATS_TARGET_HIGH_MMOL * Constants.MMOLL_TO_MGDL

                val tir7 = tirCalculator.calculate(7, lowTirMgdl, highTirMgdl)
                val tir30 = tirCalculator.calculate(30, lowTirMgdl, highTirMgdl)
                val tit7 = tirCalculator.calculate(7, lowTitMgdl, highTitMgdl)
                val tit30 = tirCalculator.calculate(30, lowTitMgdl, highTitMgdl)

                TirStatsData(
                    tir7 = tir7,
                    averageTir7 = tirCalculator.averageTIR(tir7),
                    averageTir30 = tirCalculator.averageTIR(tir30),
                    lowTirMgdl = lowTirMgdl,
                    highTirMgdl = highTirMgdl,
                    lowTitMgdl = lowTitMgdl,
                    highTitMgdl = highTitMgdl,
                    averageTit7 = tirCalculator.averageTIR(tit7),
                    averageTit30 = tirCalculator.averageTIR(tit30)
                )
            }
            uiState.update { it.copy(tirStatsData = data, tirLoading = false) }
        }
    }

    private fun loadDexcomTirStats() {
        viewModelScope.launch {
            uiState.update { it.copy(dexcomTirLoading = true) }
            val data = withContext(Dispatchers.IO) {
                dexcomTirCalculator.calculate()
            }
            uiState.update { it.copy(dexcomTirData = data, dexcomTirLoading = false) }
        }
    }

    private fun loadActivityStats() {
        viewModelScope.launch {
            uiState.update { it.copy(activityLoading = true) }
            val data = withContext(Dispatchers.IO) {
                activityMonitor.getActivityStats()
            }
            uiState.update { it.copy(activityStatsData = data, activityLoading = false) }
        }
    }

    fun toggleTddExpanded() {
        uiState.update { it.copy(tddExpanded = !it.tddExpanded) }
    }

    fun toggleTirExpanded() {
        uiState.update { it.copy(tirExpanded = !it.tirExpanded) }
    }

    fun toggleDexcomTirExpanded() {
        uiState.update { it.copy(dexcomTirExpanded = !it.dexcomTirExpanded) }
    }

    fun toggleActivityExpanded() {
        uiState.update { it.copy(activityExpanded = !it.activityExpanded) }
    }

    fun recalculateTdd(context: Context) {
        uiInteraction.showOkCancelDialog(
            context = context,
            message = rh.gs(R.string.do_you_want_recalculate_tdd_stats),
            ok = {
                uel.log(Action.STAT_RESET, Sources.Stats)
                viewModelScope.launch {
                    persistenceLayer.clearCachedTddData(0)
                    loadTddStats()
                }
            }
        )
    }

    fun resetActivityStats(context: Context) {
        uiInteraction.showOkCancelDialog(
            context = context,
            message = rh.gs(R.string.do_you_want_reset_stats),
            ok = {
                uel.log(Action.STAT_RESET, Sources.Stats)
                viewModelScope.launch(Dispatchers.IO) {
                    activityMonitor.reset()
                    loadActivityStats()
                }
            }
        )
    }
}

/**
 * UI state for StatsScreen
 */
@Immutable
data class StatsUiState(
    val tddStatsData: TddStatsData? = null,
    val tirStatsData: TirStatsData? = null,
    val dexcomTirData: DexcomTIR? = null,
    val activityStatsData: List<ActivityStats>? = null,
    val tddLoading: Boolean = true,
    val tirLoading: Boolean = true,
    val dexcomTirLoading: Boolean = true,
    val activityLoading: Boolean = true,
    val tddExpanded: Boolean = true,
    val tirExpanded: Boolean = false,
    val dexcomTirExpanded: Boolean = false,
    val activityExpanded: Boolean = false
)
