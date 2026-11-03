package com.allfitness.app.core.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Brillo verde del botón principal (única sombra de la app). Estático: también con movimiento reducido.
 * El tema es un solo esquema, así que el verde sale de [AppColorScheme] sin leer la composición.
 */
fun Modifier.primaryGlow(shape: Shape): Modifier =
    shadow(12.dp, shape, spotColor = AppColorScheme.primary, ambientColor = AppColorScheme.primary)
