package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for xDrip+ CGM source.
 * Based on ic_xdrip.xml from core/objects.
 *
 * Viewport: 24x24
 */
val XDrip: ImageVector by lazy {
    ImageVector.Builder(
        name = "XDrip",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(12.143f, 3.619f)
            curveToRelative(1.326f, 1.335f, 2.613f, 2.639f, 3.908f, 3.934f)
            curveToRelative(0.86f, 0.86f, 1.735f, 1.695f, 2.294f, 2.811f)
            curveToRelative(1.242f, 2.477f, 0.847f, 5.53f, -1.039f, 7.698f)
            curveToRelative(-1.763f, 2.026f, -4.813f, 2.845f, -7.35f, 1.973f)
            curveToRelative(-4.996f, -1.716f, -6.424f, -7.776f, -2.71f, -11.543f)
            curveTo(8.837f, 6.878f, 10.458f, 5.294f, 12.143f, 3.619f)
            close()
            moveTo(12.08f, 6.135f)
            curveToRelative(-1.258f, 1.251f, -2.447f, 2.427f, -3.628f, 3.61f)
            curveToRelative(-1.148f, 1.149f, -1.65f, 2.542f, -1.541f, 4.158f)
            curveToRelative(0.177f, 2.618f, 2.665f, 4.888f, 5.169f, 4.688f)
            curveTo(12.08f, 14.471f, 12.08f, 10.35f, 12.08f, 6.135f)
            close()
        }
    }.build()
}
