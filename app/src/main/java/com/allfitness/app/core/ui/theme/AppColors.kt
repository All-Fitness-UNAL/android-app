package com.allfitness.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/** Colores del Figma que no tienen rol en Material 3. */
@Immutable
data class AppColors(
    /** Solo gráficos (≥ 3:1): el texto nunca va en un color de macro. */
    val macroProtein: Color,
    val macroCarbs: Color,
    val macroFat: Color,
    /** Ámbar: solo la racha. */
    val accent: Color,
    val accentContainer: Color,
    /** Fondo de campos y steppers. */
    val surfaceInput: Color,
    /** Solo decorativo (< 4,5:1): viñetas, puntos y bordes de pendiente. Nunca texto. */
    val muted: Color,
)

val DefaultAppColors = AppColors(
    macroProtein = Color(0xFF22A85E),
    macroCarbs = Color(0xFF5B8DEF),
    macroFat = Color(0xFFE8607A),
    accent = Color(0xFFFFB300),
    // #FFB300 al 12 % sobre #11111E.
    accentContainer = Color(0xFF2E241A),
    surfaceInput = Color(0xFF121B2A),
    muted = Color(0xFF5F6175),
)

val LocalAppColors = staticCompositionLocalOf { DefaultAppColors }

/** Contraste WCAG 2.x entre dos colores opacos. */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}
