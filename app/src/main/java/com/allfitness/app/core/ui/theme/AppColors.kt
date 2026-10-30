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
    // Línea: se borra en la Task 12.
    val lineRoutine: Color,
    val lineDiet: Color,
    val lineDietText: Color,
    val lineProgress: Color,
    val here: Color,
    val signal: Color,
    val onSignal: Color,
    val signalOutline: Color,
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
    // Línea: se borra en la Task 12. Sin color de sección: el verde es el único acento.
    lineRoutine = AppColorScheme.primary,
    lineDiet = AppColorScheme.primary,
    lineDietText = AppColorScheme.primary,
    lineProgress = AppColorScheme.primary,
    here = AppColorScheme.primary,
    // Línea: se borra en la Task 12. La Señal pasa a ser un panel oscuro (no verde) para que el verde de línea se vea sobre ella.
    signal = AppColorScheme.surfaceContainerHigh,
    onSignal = AppColorScheme.onSurface,
    signalOutline = AppColorScheme.outlineVariant,
)

val LocalAppColors = staticCompositionLocalOf { DefaultAppColors }

// Línea: se borra en la Task 12.
enum class AppSection { ROUTINE, DIET, PROGRESS, PROFILE }

/** Perfil no tiene línea propia y usa la tinta. */
fun AppColors.lineFor(section: AppSection, ink: Color): Color = when (section) {
    AppSection.ROUTINE -> lineRoutine
    AppSection.DIET -> lineDiet
    AppSection.PROGRESS -> lineProgress
    AppSection.PROFILE -> ink
}

/** Color de línea para texto e íconos pequeños (Dieta tenía un tono de texto propio). */
fun AppColors.lineTextFor(section: AppSection, ink: Color): Color =
    if (section == AppSection.DIET) lineDietText else lineFor(section, ink)

/** Contraste WCAG 2.x entre dos colores opacos. */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}
