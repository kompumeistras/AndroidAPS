package app.aaps.core.ui.compose.icons.library

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Icon for Wear Plugin.
 *
 * Bounding box: x: 4.8-19.2, y: 1.2-22.8 (viewport: 24x24, ~90% height)
 */
val IcPluginWear: ImageVector by lazy {
    ImageVector.Builder(
        name = "IcPluginWear",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f
        ) {
            moveTo(19.2f, 12f)
            curveToRelative(0f, -2.286f, -1.071f, -4.329f, -2.736f, -5.643f)
            lineTo(15.6f, 1.2f)
            horizontalLineTo(8.4f)
            lineTo(7.545f, 6.357f)
            curveTo(5.871f, 7.671f, 4.8f, 9.705f, 4.8f, 12f)
            reflectiveCurveToRelative(1.071f, 4.329f, 2.745f, 5.643f)
            lineTo(8.4f, 22.8f)
            horizontalLineToRelative(7.2f)
            lineToRelative(0.864f, -5.157f)
            curveTo(18.129f, 16.329f, 19.2f, 14.286f, 19.2f, 12f)
            close()

            moveTo(6.6f, 12f)
            curveToRelative(0f, -2.979f, 2.421f, -5.4f, 5.4f, -5.4f)
            reflectiveCurveToRelative(5.4f, 2.421f, 5.4f, 5.4f)
            reflectiveCurveToRelative(-2.421f, 5.4f, -5.4f, 5.4f)
            reflectiveCurveTo(6.6f, 14.979f, 6.6f, 12f)
            close()
        }
    }.build()
}

@Preview(showBackground = true)
@Composable
private fun IcPluginWearIconPreview() {
    Icon(
        imageVector = IcPluginWear,
        contentDescription = null,
        modifier = Modifier
            .padding(0.dp)
            .size(48.dp),
        tint = Color.Unspecified
    )
}

/*

<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="24px"
	 height="24px" viewBox="0 0 24 24" enable-background="new 0 0 24 24" xml:space="preserve">
<g id="ic_plugin_wear">
	<g id="Plugin_Wear" display="inline">
		<path fill="#FFFFFF" d="M19.2,12c0-2.286-1.071-4.329-2.736-5.643L15.6,1.2H8.4L7.545,6.357C5.871,7.671,4.8,9.705,4.8,12
			s1.071,4.329,2.745,5.643L8.4,22.8h7.2l0.864-5.157C18.129,16.329,19.2,14.286,19.2,12z M6.6,12c0-2.979,2.421-5.4,5.4-5.4
			s5.4,2.421,5.4,5.4s-2.421,5.4-5.4,5.4S6.6,14.979,6.6,12z"/>
	</g>
</g>
</svg>
 */