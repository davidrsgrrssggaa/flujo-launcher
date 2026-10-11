package app.flujo.launcher.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FlujoColors(
    val bg: Color,
    val fg: Color,
    val dim: Color,
    val line: Color,
    val card: Color,
    val accent: Color,
)

fun flujoColors(dark: Boolean, accent: Color): FlujoColors =
    if (dark) FlujoColors(
        bg = Color(0xFF0D1118),
        fg = Color(0xFFE8EDF5),
        dim = Color(0xFF7F8A9C),
        line = Color(0xFF202938),
        card = Color(0xFF161D29),
        accent = accent,
    ) else FlujoColors(
        bg = Color(0xFFF3F5F9),
        fg = Color(0xFF141922),
        dim = Color(0xFF667187),
        line = Color(0xFFD5DBE5),
        card = Color(0xFFE6EAF1),
        accent = accent,
    )

val LocalFlujo = staticCompositionLocalOf {
    flujoColors(dark = true, accent = Color(0xFFA9CCFF))
}
