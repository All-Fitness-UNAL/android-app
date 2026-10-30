package com.allfitness.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/** Colores de la dirección "Línea" que no tienen rol en Material 3. */
@Immutable
data class AppColors(
    val lineRoutine: Color,
    val lineDiet: Color,
    val lineDietText: Color,
    val lineProgress: Color,
    val here: Color,
    val signal: Color,
    val onSignal: Color,
    val signalOutline: Color,
    val macroProtein: Color,
    val macroCarbs: Color,
    val macroFat: Color,
)

val LightAppColors = AppColors(
    lineRoutine = Color(0xFF0A7A50),
    lineDiet = Color(0xFFD9661F),
    lineDietText = Color(0xFFA84A0E),
    lineProgress = Color(0xFF2C5BD6),
    here = Color(0xFFF2C230),
    signal = Color(0xFF0F1F1A),
    onSignal = Color(0xFFFFFFFF),
    signalOutline = Color.Transparent,
    // Macros validados con dataviz/validate_palette (todos los pares, daltonismo y visión normal).
    macroProtein = Color(0xFF2C5BD6),
    macroCarbs = Color(0xFFD9661F),
    macroFat = Color(0xFFC2389A),
)

val DarkAppColors = AppColors(
    lineRoutine = Color(0xFF4CC38A),
    lineDiet = Color(0xFFF08A45),
    lineDietText = Color(0xFFF08A45),
    lineProgress = Color(0xFF8FAEFF),
    here = Color(0xFFF5CD4C),
    signal = Color(0xFF1D2F28),
    onSignal = Color(0xFFE6ECE9),
    // En oscuro la Señal casi no contrasta con la superficie: se separa con un hilo.
    signalOutline = Color(0xFF2A3531),
    // Tonos propios, no los de línea: en oscuro estos quedan dentro de la banda de luminosidad de datos.
    macroProtein = Color(0xFF5E8BEF),
    macroCarbs = Color(0xFFDD7434),
    macroFat = Color(0xFFCF4E9C),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

enum class AppSection { ROUTINE, DIET, PROGRESS, PROFILE }

/** Perfil no tiene línea propia y usa la tinta. */
fun AppColors.lineFor(section: AppSection, ink: Color): Color = when (section) {
    AppSection.ROUTINE -> lineRoutine
    AppSection.DIET -> lineDiet
    AppSection.PROGRESS -> lineProgress
    AppSection.PROFILE -> ink
}

/** Color de línea para texto e íconos pequeños: el naranja de Dieta no llega a 4,5:1 y usa su tono de texto. */
fun AppColors.lineTextFor(section: AppSection, ink: Color): Color =
    if (section == AppSection.DIET) lineDietText else lineFor(section, ink)

/** Contraste WCAG 2.x entre dos colores opacos. */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}
