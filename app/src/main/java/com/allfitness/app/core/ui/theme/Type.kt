package com.allfitness.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.allfitness.app.R

private fun archivo(weight: Int) = Font(
    R.font.archivo_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(87.5f)),
)

private fun atkinson(weight: Int) = Font(
    R.font.atkinson_next_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val ArchivoFamily = FontFamily(archivo(600), archivo(650), archivo(700), archivo(800))
val AtkinsonFamily = FontFamily(atkinson(400), atkinson(700))

// Archivo lleva cifras tabulares en todos sus roles: series, kg y kcal se alinean en columna.
private fun sign(weight: Int, size: Int, line: Int) = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    fontFeatureSettings = "tnum",
)

private fun read(weight: Int, size: Int, line: Int, tracking: Float = 0f) = TextStyle(
    fontFamily = AtkinsonFamily,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

// labelSmall es el rótulo de estación en mayúsculas: TextStyle no transforma, se aplica uppercase() al usarlo.
val AppTypography = Typography(
    displayLarge = sign(700, 57, 64),
    displayMedium = sign(700, 45, 52),
    displaySmall = sign(700, 40, 44),
    headlineLarge = sign(700, 32, 38),
    headlineMedium = sign(700, 28, 34),
    headlineSmall = sign(650, 24, 30),
    titleLarge = sign(600, 20, 26),
    titleMedium = read(700, 16, 22),
    titleSmall = read(700, 14, 20),
    bodyLarge = read(400, 16, 24),
    bodyMedium = read(400, 14, 20),
    bodySmall = read(400, 12, 16),
    labelLarge = read(700, 14, 20),
    labelMedium = read(700, 12, 16, tracking = 0.4f),
    labelSmall = read(700, 11, 14, tracking = 0.8f),
)
