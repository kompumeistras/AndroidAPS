package app.aaps.ui.compose.graphs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import app.aaps.core.graph.vico.AdaptiveStep
import app.aaps.core.graph.vico.Square
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.compose.graphs.viewmodels.GraphViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.TextComponent.MinWidth.Companion.fixed
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider

/**
 * IOB (Insulin On Board) Graph using Vico.
 *
 * Architecture:
 * - Collects iobGraphFlow independently
 * - Two blue lines: regular IOB + IOB predictions
 * - Uses same x-axis coordinate system as BG graph
 * - Same axis configuration as BG graph (Y-axis + X-axis with time labels)
 *
 * X-Axis Range Alignment ("Open to the Future"):
 * - MUST match BG graph's x-axis range exactly (derivedTimeRange: minTimestamp to maxTimestamp)
 * - Uses invisible anchor series: 3 points [minX, minX+1, maxX] to establish xStep = 1 via GCD
 * - CRITICAL: With only 2 points [minX, maxX], xStep = (maxX-minX), causing vertical stacking
 * - With 3 points [minX, minX+1, maxX]: deltas [1, maxX-minX-1] → GCD = 1 → proper spacing
 * - Start anchor: At y=0 if IOB data starts later than time range (invisible in gradient)
 * - End anchor: Holds last IOB value (not drop to 0) - shows ongoing IOB status
 * - Graph is "open to the future" - extends horizontally at last value, not dropping prematurely
 * - This ensures perfect vertical alignment with BG graph when stacked
 *
 * Scroll/Zoom:
 * - Receives scroll/zoom state from BG graph (synchronized automatically)
 * - User cannot manually scroll/zoom this graph - it follows BG graph
 *
 * Rendering:
 * - IOB line: Blue step line (Square PointConnector) - horizontal→vertical staircase
 * - Predictions: Blue step line (Square PointConnector) - same style as regular IOB
 * - Gradient fill from semi-transparent blue to transparent
 * - No failover regions (IOB doesn't have failover points like COB)
 */
@Composable
fun IobGraphCompose(
    viewModel: GraphViewModel,
    scrollState: VicoScrollState,
    zoomState: VicoZoomState,
    modifier: Modifier = Modifier
) {
    // Collect flows independently
    val iobGraphData by viewModel.iobGraphFlow.collectAsState()
    val derivedTimeRange by viewModel.derivedTimeRange.collectAsState()

    // Use derived time range or fall back to default (last 24 hours)
    // With anchor series, we can render immediately even without data
    val (minTimestamp, maxTimestamp) = derivedTimeRange ?: run {
        val now = System.currentTimeMillis()
        val dayAgo = now - 24 * 60 * 60 * 1000L
        dayAgo to now
    }

    // Single model producer for IOB lines
    val modelProducer = remember { CartesianChartModelProducer() }

    // Colors from theme
    val iobColor = AapsTheme.generalColors.activeInsulinText

    // Calculate x-axis range (must match BG graph for alignment)
    val minX = 0.0
    val maxX = remember(minTimestamp, maxTimestamp) {
        timestampToX(maxTimestamp, minTimestamp)
    }

    // Track which series are currently included (for matching LineProvider)
    val hasIobDataState = remember { mutableStateOf(false) }
//    val hasPredictionsDataState = remember { mutableStateOf(false) }

    // LaunchedEffect for IOB series - only runs when iobGraphData or time range changes significantly
    // Use remember to cache and only update when time range changes by more than 1 minute
    val stableTimeRange = remember(minTimestamp / 60000, maxTimestamp / 60000) {
        minTimestamp to maxTimestamp
    }

    LaunchedEffect(iobGraphData, stableTimeRange) {
        val iobPoints = iobGraphData.iob
        iobGraphData.predictions

        modelProducer.runTransaction {
            lineSeries {
                // Invisible anchor series (ALWAYS FIRST for x-axis range normalization)
                // CRITICAL: 3 points [minX, minX+1, maxX] establish xStep = 1 via GCD
                // With only 2 points [minX, maxX]: xStep = (maxX-minX) → stacked rendering
                // With 3 points: deltas [1, maxX-minX-1] → GCD = 1 → xStep = 1.0 → proper spacing
                val (anchorX, anchorY) = createInvisibleAnchorSeries(minX, maxX)
                series(anchorX, anchorY)

                var hasIobData = false

                // IOB data series (only if data exists after filtering)
                if (iobPoints.isNotEmpty()) {
                    val dataPoints = iobPoints
                        .map { timestampToX(it.timestamp, minTimestamp) to it.value }
                    val filteredPoints = filterToRange(dataPoints, minX, maxX)

                    if (filteredPoints.isNotEmpty()) {
                        series(
                            x = filteredPoints.map { it.first },
                            y = filteredPoints.map { it.second }
                        )
                        hasIobData = true
                    }
                }

                // Predictions series (only if data exists after filtering)
                /*
                                if (predictionPoints.isNotEmpty()) {
                                    val dataPoints = predictionPoints
                                        .map { timestampToX(it.timestamp, minTimestamp) to it.value }
                                    val filteredPoints = filterToRange(dataPoints, minX, maxX)

                                    if (filteredPoints.isNotEmpty()) {
                                        series(
                                            x = filteredPoints.map { it.first },
                                            y = filteredPoints.map { it.second }
                                        )
                                        hasPredictionsData = true
                                    }
                                }
                */
                // Update state
                hasIobDataState.value = hasIobData
//                hasPredictionsDataState.value = hasPredictionsData
            }
        }
    }

    // Time formatter and axis configuration
    val timeFormatter = rememberTimeFormatter(minTimestamp)
    val bottomAxisItemPlacer = rememberBottomAxisItemPlacer(minTimestamp)

    // Line style for IOB: solid blue line with square step connector
    val iobLine = remember(iobColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(iobColor.toArgb())),
            areaFill = LineCartesianLayer.AreaFill.single(
                Fill(
                    ShaderProvider.verticalGradient(
                        iobColor.copy(alpha = 1f).toArgb(),
                        Color.Transparent.toArgb()
                    )
                )
            ),
            pointConnector = AdaptiveStep
        )
    }

    // Line style for predictions: same as IOB but without gradient fill
    val predictionsLine = remember(iobColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(iobColor.toArgb())),
            areaFill = null,  // No gradient for predictions
            pointConnector = Square  // Fixed step: horizontal→vertical staircase
        )
    }

    // Invisible line for anchor series (always added, corresponds to first series)
    val invisibleLine = remember { createInvisibleDots() }

    // Build lines list dynamically - MUST match series order exactly
    // Series order: anchor FIRST, then IOB data, then predictions
    val hasIobData by hasIobDataState
//    val hasPredictionsData by hasPredictionsDataState
//    val lines = remember(hasIobData, hasPredictionsData, iobLine, predictionsLine, invisibleLine) {
    val lines = remember(hasIobData, iobLine, predictionsLine, invisibleLine) {
        buildList {
            add(invisibleLine)                          // Series 1: Anchor (always first)
            if (hasIobData) add(iobLine)               // Series 2: IOB data (if exists)
//            if (hasPredictionsData) add(predictionsLine) // Series 3: Predictions (if exists)
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines)
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    minWidth = fixed(30.0f)
                )
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = timeFormatter,
                itemPlacer = bottomAxisItemPlacer,
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp),
        scrollState = scrollState,
        zoomState = zoomState
    )
}
