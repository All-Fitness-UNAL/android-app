package com.allfitness.app.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

object Motion {
    const val SHORT = 150
    const val MEDIUM = 300
    const val LONG = 450
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    fun <T> gentle(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
}

/** "Quitar animaciones" de accesibilidad deja la escala del animador en 0. */
fun isReducedMotion(animatorDurationScale: Float): Boolean = animatorDurationScale == 0f

val LocalReducedMotion = staticCompositionLocalOf { false }
