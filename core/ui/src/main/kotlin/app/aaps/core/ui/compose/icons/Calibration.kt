package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for Calibration action.
 * Based on ic_calibration.xml from core/objects.
 * Three drops of decreasing size.
 *
 * Viewport: 24x24
 */
val Calibration: ImageVector by lazy {
    ImageVector.Builder(
        name = "Calibration",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Large drop
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(7.305f, 4.482f)
            curveToRelative(1.782f, 2.914f, 4.332f, 5.382f, 3.916f, 9.05f)
            curveToRelative(-0.256f, 2.254f, -2.529f, 3.635f, -4.783f, 3.199f)
            curveToRelative(-2.145f, -0.415f, -3.625f, -2.546f, -3.171f, -4.728f)
            curveTo(3.858f, 9.168f, 5.537f, 6.894f, 7.305f, 4.482f)
            close()
            moveTo(5.08f, 10.061f)
            curveToRelative(-0.212f, 0.638f, -0.489f, 1.262f, -0.621f, 1.916f)
            curveToRelative(-0.205f, 1.017f, -0.065f, 1.994f, 0.764f, 2.731f)
            curveToRelative(0.297f, 0.264f, 0.68f, 0.354f, 1.038f, 0.077f)
            curveToRelative(0.242f, -0.187f, 0.269f, -0.46f, 0.096f, -0.692f)
            curveTo(5.468f, 12.903f, 5.032f, 11.589f, 5.08f, 10.061f)
            close()
        }
        // Medium drop
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(17.959f, 4.566f)
            curveToRelative(1.073f, 1.453f, 2.073f, 2.837f, 2.489f, 4.535f)
            curveToRelative(0.28f, 1.143f, -0.069f, 2.1f, -1.037f, 2.763f)
            curveToRelative(-0.943f, 0.645f, -1.961f, 0.655f, -2.904f, 0.009f)
            curveToRelative(-0.967f, -0.662f, -1.318f, -1.635f, -1.041f, -2.769f)
            curveTo(15.88f, 7.408f, 16.875f, 6.019f, 17.959f, 4.566f)
            close()
        }
        // Small drop
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(14.82f, 14.425f)
            curveToRelative(0.797f, 1.094f, 1.535f, 2.104f, 1.819f, 3.355f)
            curveToRelative(0.188f, 0.826f, -0.096f, 1.498f, -0.784f, 1.965f)
            curveToRelative(-0.669f, 0.454f, -1.393f, 0.467f, -2.067f, 0.014f)
            curveToRelative(-0.688f, -0.462f, -0.985f, -1.13f, -0.8f, -1.959f)
            curveTo(13.272f, 16.531f, 14.004f, 15.503f, 14.82f, 14.425f)
            close()
        }
    }.build()
}
