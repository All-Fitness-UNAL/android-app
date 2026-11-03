package com.allfitness.app.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Único esquema, oscuro (spec §4.1). Se usa el constructor completo para que el compilador exija
// los 48 roles: un rol sin definir lo rellena M3 con su paleta de referencia.
// Valores del Figma: background, surface*, outline*, primary, onPrimary, onSurface, onSurfaceVariant, error.
// Los roles que el Figma no define salen del mismo verde (#2ECC71) y de los neutros de la paleta:
//  - primaryContainer / errorContainer: el color al 12 % sobre background, precalculado como hex opaco.
//  - secondary: el neutro de onSurfaceVariant; secondaryContainer un paso más claro que surfaceContainerHighest.
//  - tertiary: el verde aclarado; tertiaryContainer repite primaryContainer.
//  - inverse*: claros, para el snackbar; inversePrimary es el verde oscurecido (5:1 sobre inverseSurface).
//  - surfaceBright / surfaceDim: surfaceContainerHighest / surfaceContainerLowest.
//  - *Fixed: los mismos tonos, que no cambian con el tema.

val AppColorScheme = ColorScheme(
    primary = Color(0xFF2ECC71),
    onPrimary = Color(0xFF11111E),
    // #2ECC71 al 12 % sobre #11111E.
    primaryContainer = Color(0xFF142728),
    onPrimaryContainer = Color(0xFF2ECC71),
    inversePrimary = Color(0xFF11703C),
    secondary = Color(0xFF9FA2B4),
    onSecondary = Color(0xFF11111E),
    secondaryContainer = Color(0xFF3A3B52),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF8EE6B3),
    onTertiary = Color(0xFF11111E),
    tertiaryContainer = Color(0xFF142728),
    onTertiaryContainer = Color(0xFF8EE6B3),
    background = Color(0xFF11111E),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C2C),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF25253A),
    onSurfaceVariant = Color(0xFF9FA2B4),
    surfaceTint = Color(0xFF2ECC71),
    inverseSurface = Color(0xFFE6E7F0),
    inverseOnSurface = Color(0xFF11111E),
    error = Color(0xFFFF5F57),
    onError = Color(0xFF11111E),
    // #FF5F57 al 12 % sobre #11111E.
    errorContainer = Color(0xFF2E1A25),
    onErrorContainer = Color(0xFFFF5F57),
    outline = Color(0xFF6B6F86),
    outlineVariant = Color(0xFF2A2A3E),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2A2A3E),
    surfaceContainer = Color(0xFF1C1C2C),
    surfaceContainerHigh = Color(0xFF25253A),
    surfaceContainerHighest = Color(0xFF2A2A3E),
    surfaceContainerLow = Color(0xFF161626),
    surfaceContainerLowest = Color(0xFF0B0B14),
    surfaceDim = Color(0xFF0B0B14),
    primaryFixed = Color(0xFF8EE6B3),
    primaryFixedDim = Color(0xFF2ECC71),
    onPrimaryFixed = Color(0xFF11111E),
    onPrimaryFixedVariant = Color(0xFF0F5A30),
    secondaryFixed = Color(0xFFD5D7E4),
    secondaryFixedDim = Color(0xFF9FA2B4),
    onSecondaryFixed = Color(0xFF11111E),
    onSecondaryFixedVariant = Color(0xFF25253A),
    tertiaryFixed = Color(0xFF8EE6B3),
    tertiaryFixedDim = Color(0xFF2ECC71),
    onTertiaryFixed = Color(0xFF11111E),
    onTertiaryFixedVariant = Color(0xFF0F5A30),
)
