package com.allfitness.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.allfitness.app.R

// Las dos TTF son variables y su instancia por defecto es la más fina: cada peso fija su eje.
private fun outfit(weight: Int) = Font(
    R.font.outfit_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun manrope(weight: Int) = Font(
    R.font.manrope_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val OutfitFamily = FontFamily(outfit(700), outfit(800))
val ManropeFamily = FontFamily(manrope(400), manrope(500), manrope(600), manrope(700))

// Outfit (títulos y cifras) lleva cifras tabulares: series, kg y kcal se alinean en columna.
private fun heading(weight: Int, size: Int, line: Int) = TextStyle(
    fontFamily = OutfitFamily,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    fontFeatureSettings = "tnum",
)

private fun text(weight: Int, size: Int, line: Int) = TextStyle(
    fontFamily = ManropeFamily,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
)

// Spec §4.2. displayLarge, displayMedium y headlineLarge no están en el Figma: Outfit ExtraBold a su tamaño de siempre.
// labelSmall se usa en mayúsculas: TextStyle no transforma, se aplica uppercase() al usarlo.
val AppTypography = Typography(
    displayLarge = heading(800, 57, 64),
    displayMedium = heading(800, 45, 52),
    displaySmall = heading(800, 32, 40),
    headlineLarge = heading(800, 32, 38),
    headlineMedium = heading(800, 28, 35),
    headlineSmall = heading(800, 24, 30),
    titleLarge = heading(800, 20, 26),
    titleMedium = heading(700, 16, 20),
    titleSmall = heading(700, 15, 19),
    bodyLarge = text(500, 15, 21),
    bodyMedium = text(500, 14, 19),
    bodySmall = text(400, 12, 16),
    labelLarge = text(700, 14, 19),
    labelMedium = text(600, 12, 16),
    labelSmall = text(700, 10, 14),
)

/** Suelo del auto-tamaño de la etiqueta de la barra inferior: con fuente al 200 % "Progreso" se reduce en vez de cortarse (6 sp × 2 > 10 sp × 1). */
val NavLabelMinSize = 6.sp
