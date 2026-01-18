package app.aaps.ui.compose.graphs

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

/**
 * Shared utilities for Vico graphs in AndroidAPS.
 *
 * CRITICAL: All graphs MUST use the same x-coordinate system to ensure proper alignment.
 * This uses whole minutes from minTimestamp to avoid label repetition and precision errors.
 *
 * **X-Axis Range Alignment (Vico Pattern - Invisible Anchor Series):**
 * All graphs MUST show the same x-axis range from derivedTimeRange:
 * - Extract both minTimestamp and maxTimestamp from derivedTimeRange
 * - Calculate minX = 0.0 and maxX = timestampToX(maxTimestamp, minTimestamp)
 * - Add invisible anchor series: `val (anchorX, anchorY) = createInvisibleAnchorSeries(minX, maxX)`
 * - Then: `series(anchorX, anchorY)` (FIRST series for consistency)
 * - Configure anchor series in LineProvider as invisible: `createInvisibleLine()`
 * - Filter real data if needed: `filterToRange(dataPoints, minX, maxX)`
 * - This ensures ALL charts show identical x-axis range for perfect alignment
 *
 * **CRITICAL - xStep and GCD Issue:**
 * Vico calculates xStep using GCD (Greatest Common Divisor) of all x-value deltas:
 * - With only 2 anchor points [0, 1440]: xStep = 1440 → points are 1 unit apart → render stacked
 * - With 3 anchor points [0, 1, 1440]: deltas [1, 1439] → GCD = 1 → xStep = 1 → proper spacing
 * - ALWAYS use createInvisibleAnchorSeries(minX, maxX) which generates 3 points
 * - NEVER use just 2 points: series(listOf(minX, maxX), listOf(0, 0)) ❌
 *
 * **Scroll/Zoom Synchronization (Vico Pattern - Separate States + Explicit Sync):**
 * All graphs must stay aligned when scrolling/zooming:
 * - BG graph (primary): scrollEnabled = true, zoomEnabled = true
 * - Secondary graphs: scrollEnabled = false, zoomEnabled = false
 * - Use snapshotFlow { bgScrollState.value to bgZoomState.value }
 * - Update zoom first, delay(50), then update scroll
 * - See OverviewGraphsSection for full implementation
 *
 * **Point Connectors:**
 * - Adaptive step graphs (COB): Use `AdaptiveStep` - steps for steep angles (>45°), lines for gradual
 *   Uses fast ratio calculation (|dy/dx| > 1.0) instead of trigonometry
 * - Fixed step graphs (IOB, AbsIOB): Use `Square` PointConnector from core.graph.vico
 * - Smooth graphs (Activity, BGI, Ratio): Use default connector (no pointConnector parameter)
 * - Bar graphs (Deviations): N/A
 */

/**
 * Convert timestamp to x-value (whole minutes from minTimestamp).
 *
 * CRITICAL: This is the standard x-coordinate calculation for ALL graphs.
 * - Uses whole minutes (not milliseconds or fractional hours)
 * - Prevents label repetition (Vico increments by 1)
 * - Avoids precision errors with decimals
 *
 * @param timestamp The data point timestamp in milliseconds
 * @param minTimestamp The reference timestamp (start of graph time range)
 * @return X-value in whole minutes from minTimestamp
 */
fun timestampToX(timestamp: Long, minTimestamp: Long): Double =
    ((timestamp - minTimestamp) / 60000).toDouble()

/**
 * Creates a time formatter for X-axis labels showing hours (HH format).
 *
 * @param minTimestamp The reference timestamp for x-value calculation
 * @return CartesianValueFormatter that converts x-values back to time labels
 */
@Composable
fun rememberTimeFormatter(minTimestamp: Long): CartesianValueFormatter {
    return remember(minTimestamp) {
        val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
        CartesianValueFormatter { _, value, _ ->
            val timestamp = minTimestamp + (value * 60000).toLong()
            dateFormat.format(Date(timestamp))
        }
    }
}

/**
 * Creates an item placer for X-axis that shows labels at whole hour intervals.
 *
 * Calculates offset from minTimestamp to align labels with whole hours (e.g., 12:00, 13:00).
 *
 * @param minTimestamp The reference timestamp for calculating hour alignment
 * @return HorizontalAxis.ItemPlacer with 60-minute spacing aligned to whole hours
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun rememberBottomAxisItemPlacer(minTimestamp: Long): HorizontalAxis.ItemPlacer {
    return remember(minTimestamp) {
        val instant = Instant.fromEpochMilliseconds(minTimestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val minutesIntoHour = localDateTime.minute
        val offsetToNextHour = if (minutesIntoHour == 0) 0 else 60 - minutesIntoHour

        HorizontalAxis.ItemPlacer.aligned(
            spacing = { 60 },  // 60 minutes between labels
            offset = { offsetToNextHour }
        )
    }
}

/**
 * Default zoom level for graphs - shows 6 hours of data (360 minutes).
 */
const val DEFAULT_GRAPH_ZOOM_MINUTES = 360.0

/**
 * Filters data points to only include those within the valid x-axis range.
 *
 * Use this when you have data that might extend beyond the visible time range
 * and you want to exclude out-of-range points from rendering.
 *
 * @param dataPoints List of (x, y) coordinate pairs
 * @param minX Minimum X value for the graph range
 * @param maxX Maximum X value for the graph range
 * @return Filtered and sorted list of (x, y) pairs within [minX, maxX]
 */
fun filterToRange(
    dataPoints: List<Pair<Double, Double>>,
    minX: Double,
    maxX: Double
): List<Pair<Double, Double>> {
    return dataPoints
        .filter { (x, _) -> x in minX..maxX }
        .sortedBy { (x, _) -> x }  // CRITICAL: Sort by x-value for Vico
}

/**
 * Creates an invisible anchor series for x-axis range normalization with proper xStep.
 *
 * CRITICAL: Vico calculates xStep using GCD of all x-value deltas. With only 2 anchor points
 * at [minX, maxX], xStep becomes (maxX - minX), making them only 1 unit apart in normalized
 * space. This causes them to render very close together or stacked.
 *
 * Solution: Use 3 points [minX, minX+1, maxX] to establish xStep = 1.0
 * - Deltas: [1, maxX - minX - 1]
 * - GCD(1, anything) = 1 → xStep = 1.0areas
 * - All y-values are 0.0 (invisible baseline)
 * - Forces all charts to show the same x-axis range AND scale
 * - Guarantees perfect alignment between multiple synchronized charts
 *
 * Example: minX=0, maxX=1440
 * - Creates x=[0, 1, 1440], y=[0, 0, 0]
 * - Deltas: [1, 1439]
 * - GCD(1, 1439) = 1 → xStep = 1.0
 * - Points are properly spaced across full range
 *
 * Usage:
 * ```
 * lineSeries {
 *     val (anchorX, anchorY) = createInvisibleAnchorSeries(minX, maxX)
 *     series(anchorX, anchorY)  // Anchor series FIRST
 *     if (hasData) series(actualXValues, actualYValues)  // Real data
 * }
 * ```
 *
 * @param minX Minimum X value for the graph range
 * @param maxX Maximum X value for the graph range
 * @return Pair of (x-values, y-values) for the invisible anchor series (3 points)
 */
fun createInvisibleAnchorSeries(
    minX: Double,
    maxX: Double
): Pair<List<Double>, List<Double>> {
    // 3 points: [minX, minX+1, maxX]
    // This establishes xStep = 1 via GCD while showing full range
    val xValues = listOf(minX, minX + 1.0, maxX)
    val yValues = listOf(0.0, 0.0, 0.0)
    return xValues to yValues
}

/**
 * Creates a transparent line configuration for invisible anchor series.
 *
 * Shows only points (dots), no line between them:
 * - Line fill is transparent (no line drawn between points)
 * - Points are transparent (invisible)
 * - This ensures y-axis labels appear even when only anchor series exists
 *
 * @return LineCartesianLayer.Line configured to show only dots, no line
 */
fun createInvisibleDots(): LineCartesianLayer.Line =
    LineCartesianLayer.Line(
        fill = LineCartesianLayer.LineFill.single(Fill(Color.TRANSPARENT)),  // No line
        areaFill = null,
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = ShapeComponent(
                    fill = Fill(Color.TRANSPARENT),
                    shape = CorneredShape.Pill
                ),
                sizeDp = 4f
            )
        )
    )

/**
 * Groups consecutive timestamps into ranges.
 *
 * Consecutive means timestamps within maxGapMinutes of each other.
 *
 * @param timestamps List of timestamps in milliseconds (must be sorted)
 * @param maxGapMinutes Maximum gap in minutes to consider timestamps consecutive (default: 10)
 * @return List of timestamp ranges (pairs of start/end timestamps)
 */
fun groupConsecutiveTimestamps(
    timestamps: List<Long>,
    maxGapMinutes: Int = 10
): List<Pair<Long, Long>> {
    if (timestamps.isEmpty()) return emptyList()

    val maxGapMs = maxGapMinutes * 60 * 1000L
    val ranges = mutableListOf<Pair<Long, Long>>()

    var rangeStart = timestamps.first()
    var rangeEnd = timestamps.first()

    for (i in 1 until timestamps.size) {
        val timestamp = timestamps[i]
        if (timestamp - rangeEnd <= maxGapMs) {
            // Extend current range
            rangeEnd = timestamp
        } else {
            // Close current range and start new one
            ranges.add(rangeStart to rangeEnd)
            rangeStart = timestamp
            rangeEnd = timestamp
        }
    }

    // Add final range
    ranges.add(rangeStart to rangeEnd)

    return ranges
}
