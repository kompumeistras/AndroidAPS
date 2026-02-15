package app.aaps.ui.compose.overview.graphs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.overview.graph.BgDataPoint
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.compose.overview.graphs.BucketedPointProvider
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

/** Series identifiers */
private const val SERIES_REGULAR = "regular"
private const val SERIES_BUCKETED = "bucketed"

/**
 * BG Graph using Vico.
 *
 * Architecture:
 * - Both series (regular, bucketed) collected and updated atomically
 * - Single LaunchedEffect handles both series to prevent race conditions
 * - Series registry tracks current data
 * - Chart rebuilds when any series or time range changes
 *
 * Rendering:
 * - Regular BG: White outlined circles (STROKE style)
 * - Bucketed BG: Filled circles colored by range
 *
 * Scroll/Zoom:
 * - Accepts external scroll/zoom states for synchronization with secondary graphs
 * - This is the primary interactive graph - user controls scroll/zoom here
 */
@Composable
fun BgGraphCompose(
    viewModel: GraphViewModel,
    scrollState: VicoScrollState,
    zoomState: VicoZoomState,
    modifier: Modifier = Modifier
) {
    // Collect flows independently - each triggers recomposition only when it changes
    val bgReadings by viewModel.bgReadingsFlow.collectAsState()
    val bucketedData by viewModel.bucketedDataFlow.collectAsState()
    val derivedTimeRange by viewModel.derivedTimeRange.collectAsState()
    val chartConfig by viewModel.chartConfigFlow.collectAsState()

    // Use derived time range or fall back to default (last 24 hours)
    val (minTimestamp, maxTimestamp) = derivedTimeRange ?: run {
        val now = System.currentTimeMillis()
        val dayAgo = now - 24 * 60 * 60 * 1000L
        dayAgo to now
    }

    // Single model producer shared by all series
    val modelProducer = remember { CartesianChartModelProducer() }

    // Series registry - tracks current data for each series
    // Using mutableStateMapOf for Compose-aware updates
    val seriesRegistry = remember { mutableStateMapOf<String, List<BgDataPoint>>() }

    // Colors from theme (stable - won't change)
    val regularColor = AapsTheme.generalColors.originalBgValue
    val lowColor = AapsTheme.generalColors.bgLow
    val inRangeColor = AapsTheme.generalColors.bgInRange
    val highColor = AapsTheme.generalColors.bgHigh

    // Calculate x-axis range (must match COB graph for alignment)
    val minX = 0.0
    val maxX = remember(minTimestamp, maxTimestamp) {
        timestampToX(maxTimestamp, minTimestamp)
    }

    // Track which series are currently included (for matching LineProvider)
    val activeSeriesState = remember { mutableStateOf(listOf<String>()) }

    // Stable time range - only changes when timestamps change by more than 1 minute
    // This prevents rapid re-triggering of LaunchedEffects
    val stableTimeRange = remember(minTimestamp / 60000, maxTimestamp / 60000) {
        minTimestamp to maxTimestamp
    }

    // Function to rebuild chart from registry
    suspend fun rebuildChart() {
        val regularPoints = seriesRegistry[SERIES_REGULAR] ?: emptyList()
        val bucketedPoints = seriesRegistry[SERIES_BUCKETED] ?: emptyList()

        if (regularPoints.isEmpty() && bucketedPoints.isEmpty()) return

        modelProducer.runTransaction {
            lineSeries {
                val activeSeries = mutableListOf<String>()

                // Series 1: REGULAR points (only if data exists)
                if (regularPoints.isNotEmpty()) {
                    val dataPoints = regularPoints
                        .map { timestampToX(it.timestamp, minTimestamp) to it.value }
                        .sortedBy { it.first }

                    series(
                        x = dataPoints.map { it.first },
                        y = dataPoints.map { it.second }
                    )
                    activeSeries.add(SERIES_REGULAR)
                }

                // Series 2: BUCKETED points (only if data exists)
                if (bucketedPoints.isNotEmpty()) {
                    val dataPoints = bucketedPoints
                        .map { timestampToX(it.timestamp, minTimestamp) to it.value }
                        .sortedBy { it.first }

                    series(
                        x = dataPoints.map { it.first },
                        y = dataPoints.map { it.second }
                    )
                    activeSeries.add(SERIES_BUCKETED)
                }

                // Normalizer series — ensures identical maxPointSize across all charts (see GraphUtils.kt)
                series(x = NORMALIZER_X, y = NORMALIZER_Y)

                // Update which series are active
                activeSeriesState.value = activeSeries.toList()
            }
        }
    }

    // Single LaunchedEffect for both series - ensures atomic updates and prevents
    // race conditions when stableTimeRange changes during navigation
    LaunchedEffect(bgReadings, bucketedData, stableTimeRange) {
        seriesRegistry[SERIES_REGULAR] = bgReadings
        seriesRegistry[SERIES_BUCKETED] = bucketedData
        rebuildChart()
    }

    // Build lookup map for BUCKETED points: x-value -> BgDataPoint (for PointProvider)
    val bucketedLookup = remember(bucketedData, minTimestamp) {
        bucketedData.associateBy { timestampToX(it.timestamp, minTimestamp) }
    }

    // Create point provider for BUCKETED series (colors by range)
    val bucketedPointProvider = remember(bucketedLookup, lowColor, inRangeColor, highColor) {
        BucketedPointProvider(bucketedLookup, lowColor, inRangeColor, highColor)
    }

    // Time formatter and axis configuration
    val timeFormatter = rememberTimeFormatter(minTimestamp)
    val bottomAxisItemPlacer = rememberBottomAxisItemPlacer(minTimestamp)

    // Line for REGULAR: White outlined circles (STROKE style)
    val regularLine = remember(regularColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent)),
            areaFill = null,
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(
                    component = ShapeComponent(
                        fill = Fill(Color.Transparent),
                        shape = CircleShape,
                        strokeFill = Fill(regularColor.copy(alpha = 0.3f)),
                        strokeThickness = 1.dp
                    ),
                    size = 6.dp
                )
            )
        )
    }

    // Line for BUCKETED: Filled circles with PointProvider for range coloring
    val bucketedLine = remember(bucketedPointProvider) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent)),
            areaFill = null,
            pointProvider = bucketedPointProvider
        )
    }

    // Normalizer line — invisible 22dp-point line that equalizes maxPointSize across all charts.
    // Without this, charts with different point sizes get different xSpacing and unscalableStartPadding,
    // breaking pixel-based scroll/zoom sync. See GraphUtils.kt for details.
    val normalizerLine = remember { createNormalizerLine() }

    // Build lines list dynamically - MUST match series order exactly
    // CRITICAL: Number and order of lines MUST match number and order of series
    val activeSeries by activeSeriesState
    val lines = remember(activeSeries, regularLine, bucketedLine, normalizerLine) {
        buildList {
            if (SERIES_REGULAR in activeSeries) add(regularLine)
            if (SERIES_BUCKETED in activeSeries) add(bucketedLine)
            add(normalizerLine)  // Always last — normalizes layout
        }
    }

    // Target range background (static - from chartConfig)
    val targetRangeColor = AapsTheme.generalColors.bgTargetRangeArea
    val targetRangeBoxComponent = rememberShapeComponent(fill = Fill(targetRangeColor))
    val targetRangeBox = remember(chartConfig.lowMark, chartConfig.highMark, targetRangeBoxComponent) {
        HorizontalBox(
            y = { chartConfig.lowMark..chartConfig.highMark },
            box = targetRangeBoxComponent
        )
    }
    val decorations = remember(targetRangeBox) { listOf(targetRangeBox) }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines),
                rangeProvider = remember(maxX) { CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = maxX) }
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    minWidth = TextComponent.MinWidth.fixed(30.dp)
                )
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = timeFormatter,
                itemPlacer = bottomAxisItemPlacer,
                label = rememberTextComponent(
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                )
            ),
            decorations = decorations,
            getXStep = { 1.0 }
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        scrollState = scrollState,
        zoomState = zoomState
    )
}
