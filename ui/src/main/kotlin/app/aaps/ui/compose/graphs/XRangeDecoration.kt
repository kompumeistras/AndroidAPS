package app.aaps.ui.compose.graphs

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration

/**
 * Custom Vico decoration that shades arbitrary x-ranges on a graph.
 *
 * Use case: Highlighting specific time periods (e.g., failover data regions, predicted data, etc.)
 *
 * Architecture:
 * - Takes x-ranges in data coordinates (same as graph data)
 * - Converts to canvas coordinates during rendering accounting for scroll/zoom
 * - Draws semi-transparent rectangles over specified ranges
 * - Automatically updates position when chart is scrolled or zoomed
 *
 * Scroll/Zoom Aware:
 * - Calculates visible x-range from scroll offset and zoom level
 * - Skips ranges completely outside visible area (performance optimization)
 * - Clamps partially visible ranges to layer bounds
 * - Redraws automatically on every frame (stateless)
 *
 * @param xRanges List of x-ranges to shade (in data coordinates, e.g., minutes from minTimestamp)
 * @param color Color of the shading
 * @param alpha Transparency of the shading (0.0 = transparent, 1.0 = opaque)
 *
 * Example usage:
 * ```
 * val failoverRanges = listOf(100.0..150.0, 300.0..350.0)  // Minutes from start
 * val decoration = XRangeDecoration(
 *     xRanges = failoverRanges,
 *     color = Color.Yellow,
 *     alpha = 0.15f
 * )
 *
 * CartesianChartHost(
 *     chart = rememberCartesianChart(
 *         ...,
 *         decorations = listOf(decoration)
 *     ),
 *     ...
 * )
 * ```
 */
class XRangeDecoration(
    private val xRanges: List<ClosedFloatingPointRange<Double>>,
    private val color: Color,
    private val alpha: Float = 0.2f
) : Decoration {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    override fun drawOverLayers(context: CartesianDrawingContext) {
        with(context) {
            // Set paint color with alpha
            paint.color = color.copy(alpha = alpha).toArgb()

            // Calculate visible x-range after scroll/zoom
            // Similar to how layers calculate visible range
            val layoutDirectionMultiplier = if (isLtr) 1f else -1f
            val layerWidth = layerBounds.right - layerBounds.left
            val visibleStartX = ranges.minX +
                layoutDirectionMultiplier * scroll / layerDimensions.xSpacing * ranges.xStep
            val visibleEndX = visibleStartX +
                layerWidth / layerDimensions.xSpacing * ranges.xStep

            xRanges.forEach { range ->
                // Skip ranges completely outside visible area
                if (range.endInclusive < visibleStartX || range.start > visibleEndX) {
                    return@forEach
                }

                // Convert x-values from data coordinates to canvas coordinates
                // Must account for scroll offset like layers do
                // Formula: canvasX = layerBounds.left + (dataX - visibleStartX) / xStep * xSpacing
                val startX = layerBounds.left +
                    ((range.start - visibleStartX) / ranges.xStep).toFloat() *
                    layerDimensions.xSpacing

                val endX = layerBounds.left +
                    ((range.endInclusive - visibleStartX) / ranges.xStep).toFloat() *
                    layerDimensions.xSpacing

                // Clamp to visible bounds (handles partial visibility)
                val clampedStartX = startX.coerceIn(layerBounds.left, layerBounds.right)
                val clampedEndX = endX.coerceIn(layerBounds.left, layerBounds.right)

                // Only draw if there's visible width
                if (clampedEndX > clampedStartX) {
                    canvas.drawRect(
                        clampedStartX,
                        layerBounds.top,
                        clampedEndX,
                        layerBounds.bottom,
                        paint
                    )
                }
            }
        }
    }
}
