package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for BYODA (Build Your Own Dexcom App) CGM source.
 * Based on ic_byoda.xml from plugins/main.
 *
 * Viewport: 24x24
 */
val Byoda: ImageVector by lazy {
    ImageVector.Builder(
        name = "Byoda",
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
            moveTo(12.107f, 3.666f)
            curveToRelative(-4.603f, 0f, -8.335f, 3.732f, -8.335f, 8.335f)
            reflectiveCurveToRelative(3.732f, 8.335f, 8.335f, 8.335f)
            reflectiveCurveToRelative(8.335f, -3.731f, 8.335f, -8.335f)
            verticalLineTo(3.666f)
            horizontalLineTo(12.107f)
            close()
            moveTo(12.107f, 18.335f)
            curveToRelative(-3.498f, 0f, -6.334f, -2.836f, -6.334f, -6.334f)
            curveToRelative(0f, -3.498f, 2.836f, -6.334f, 6.334f, -6.334f)
            curveToRelative(3.498f, 0f, 6.334f, 2.836f, 6.334f, 6.334f)
            curveTo(18.442f, 15.499f, 15.606f, 18.335f, 12.107f, 18.335f)
            close()
        }
    }.build()
}
