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
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent.MinWidth.Companion.fixed
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

/**
 * COB (Carbs On Board) Graph using Vico.
 *
 * Architecture:
 * - Collects cobGraphFlow independently
 * - Single orange line for COB values
 * - Uses same x-axis coordinate system as BG graph
 * - Same axis configuration as BG graph (Y-axis + X-axis with time labels)
 *
 * X-Axis Range Alignment ("Open to the Future"):
 * - MUST match BG graph's x-axis range exactly (derivedTimeRange: minTimestamp to maxTimestamp)
 * - Uses invisible anchor series: 3 points [minX, minX+1, maxX] to establish xStep = 1 via GCD
 * - CRITICAL: With only 2 points [minX, maxX], xStep = (maxX-minX), causing vertical stacking
 * - With 3 points [minX, minX+1, maxX]: deltas [1, maxX-minX-1] → GCD = 1 → proper spacing
 * - Start anchor: At y=0 if COB data starts later than time range (invisible in gradient)
 * - End anchor: Holds last COB value (not drop to 0) - shows ongoing COB status
 * - Graph is "open to the future" - extends horizontally at last value, not dropping prematurely
 * - This ensures perfect vertical alignment with BG graph when stacked
 *
 * Scroll/Zoom:
 * - Receives scroll/zoom state from BG graph (synchronized automatically)
 * - User cannot manually scroll/zoom this graph - it follows BG graph
 *
 * Rendering:
 * - COB line: Orange adaptive line (AdaptiveStep PointConnector)
 * - Steep changes (>45° angle): Step connector (horizontal→vertical, staircase effect)
 * - Gradual changes (≤45° angle): Straight line connector (smooth)
 * - Uses fast ratio calculation (|dy/dx| > 1.0) instead of trigonometry
 * - Gradient fill from semi-transparent orange to transparent
 * - Failover regions: Semi-transparent yellow/amber shaded areas (XRangeDecoration)
 *   Consecutive failover points (within 10 minutes) are grouped into ranges
 */
@Composable
fun CobGraphCompose(
    viewModel: GraphViewModel,
    scrollState: VicoScrollState,
    zoomState: VicoZoomState,
    modifier: Modifier = Modifier
) {
    // Collect flows independently
    val cobGraphData by viewModel.cobGraphFlow.collectAsState()
    val derivedTimeRange by viewModel.derivedTimeRange.collectAsState()

    // Use derived time range or fall back to default (last 24 hours)
    // With anchor series, we can render immediately even without data
    val (minTimestamp, maxTimestamp) = derivedTimeRange ?: run {
        val now = System.currentTimeMillis()
        val dayAgo = now - 24 * 60 * 60 * 1000L
        dayAgo to now
    }

    // Single model producer for COB line
    val modelProducer = remember { CartesianChartModelProducer() }

    // Colors from theme
    val cobColor = AapsTheme.generalColors.cobPrediction

    // Calculate x-axis range (must match BG graph for alignment)
    val minX = 0.0
    val maxX = remember(minTimestamp, maxTimestamp) {
        timestampToX(maxTimestamp, minTimestamp)
    }

    // Track which series are currently included (for matching LineProvider)
    val hasCobDataState = remember { mutableStateOf(false) }
    val hasFailoverDataState = remember { mutableStateOf(false) }

    // LaunchedEffect for COB series - only runs when cobGraphData or time range changes significantly
    // Use remember to cache and only update when time range changes by more than 1 minute
    val stableTimeRange = remember(minTimestamp / 60000, maxTimestamp / 60000) {
        minTimestamp to maxTimestamp
    }

    LaunchedEffect(cobGraphData, stableTimeRange) {
        val cobPoints = cobGraphData.cob
        val failoverPoints = cobGraphData.failOverPoints

        modelProducer.runTransaction {
            lineSeries {
                // Invisible anchor series (ALWAYS FIRST for x-axis range normalization)
                // CRITICAL: 3 points [minX, minX+1, maxX] establish xStep = 1 via GCD
                // With only 2 points [minX, maxX]: xStep = (maxX-minX) → stacked rendering
                // With 3 points: deltas [1, maxX-minX-1] → GCD = 1 → xStep = 1.0 → proper spacing
                val (anchorX, anchorY) = createInvisibleAnchorSeries(minX, maxX)
                series(anchorX, anchorY)

                var hasCobData = false
                var hasFailoverData = false

                // COB data series (only if data exists after filtering)
                if (cobPoints.isNotEmpty()) {
                    val dataPoints = cobPoints
                        .map { timestampToX(it.timestamp, minTimestamp) to it.value }
                    val filteredPoints = filterToRange(dataPoints, minX, maxX)

                    if (filteredPoints.isNotEmpty()) {
                        series(
                            x = filteredPoints.map { it.first },
                            y = filteredPoints.map { it.second }
                        )
                        hasCobData = true
                    }
                }

                // Failover points series (for testing - shows as dots)
                if (failoverPoints.isNotEmpty()) {
                    val dataPoints = failoverPoints
                        .map { timestampToX(it.timestamp, minTimestamp) to it.cobValue }
                    val filteredPoints = filterToRange(dataPoints, minX, maxX)

                    if (filteredPoints.isNotEmpty()) {
                        series(
                            x = filteredPoints.map { it.first },
                            y = filteredPoints.map { it.second }
                        )
                        hasFailoverData = true
                    }
                }

                // Update state
                hasCobDataState.value = hasCobData
                hasFailoverDataState.value = hasFailoverData
            }
        }
    }

    // Time formatter and axis configuration
    val timeFormatter = rememberTimeFormatter(minTimestamp)
    val bottomAxisItemPlacer = rememberBottomAxisItemPlacer(minTimestamp)

    // Line style for COB: solid orange line with adaptive step connector
    val cobLine = remember(cobColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(cobColor.toArgb())),
            areaFill = LineCartesianLayer.AreaFill.single(
                Fill(
                    ShaderProvider.verticalGradient(
                        cobColor.copy(alpha = 1f).toArgb(),
                        Color.Transparent.toArgb()
                    )
                )
            ),
            pointConnector = AdaptiveStep  // Adaptive: step for steep angles (>45°), line for gradual
        )
    }

    // Invisible line for anchor series (always added, corresponds to last series)
    val invisibleLine = remember { createInvisibleDots() }

    // Failover dots style - same color as COB line, dots only, no line
    val failoverDotsLine = remember(cobColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent.toArgb())),
            areaFill = null,
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(
                    component = ShapeComponent(
                        fill = Fill(cobColor.toArgb()),
                        shape = CorneredShape.Pill
                    ),
                    sizeDp = 6f
                )
            )
        )
    }

    // Build lines list dynamically - MUST match series order exactly
    // Series order: anchor FIRST, then COB data, then failover dots
    val hasCobData by hasCobDataState
    val hasFailoverData by hasFailoverDataState
    val lines = remember(hasCobData, hasFailoverData, cobLine, failoverDotsLine, invisibleLine) {
        buildList {
            add(invisibleLine)                          // Series 1: Anchor (always first)
            if (hasCobData) add(cobLine)               // Series 2: COB data (if exists)
            if (hasFailoverData) add(failoverDotsLine) // Series 3: Failover dots (if exists)
        }
    }

    // Calculate failover x-ranges for shading decoration
    val failoverRanges = remember(cobGraphData, minTimestamp) {
        val failoverTimestamps = cobGraphData.failOverPoints
            .map { it.timestamp }
            .sorted()

        if (failoverTimestamps.isEmpty()) {
            emptyList()
        } else {
            // Group consecutive timestamps into ranges
            val timestampRanges = groupConsecutiveTimestamps(failoverTimestamps, maxGapMinutes = 10)

            // Convert to x-coordinates (minutes from minTimestamp)
            timestampRanges.map { (startTs, endTs) ->
                val startX = timestampToX(startTs, minTimestamp)
                val endX = timestampToX(endTs, minTimestamp)
                startX..endX
            }
        }
    }

    // Failover shading decoration - same color as COB line
    val failoverDecoration = remember(failoverRanges, cobColor) {
        XRangeDecoration(
            xRanges = failoverRanges,
            color = cobColor,
            alpha = 0.15f
        )
    }

    val decorations = remember(failoverDecoration) {
        listOf(failoverDecoration)
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
            ),
            decorations = decorations
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp),
        scrollState = scrollState,
        zoomState = zoomState
    )
}
