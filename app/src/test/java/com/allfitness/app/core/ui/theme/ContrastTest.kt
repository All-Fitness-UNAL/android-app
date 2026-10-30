package com.allfitness.app.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import kotlin.math.pow
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    private val themes = listOf(
        Triple("claro", LightColors, LightAppColors),
        Triple("oscuro", DarkColors, DarkAppColors),
    )

    private fun check(min: Float, pair: (ColorScheme, AppColors) -> Pair<Color, Color>) =
        themes.forEach { (name, scheme, app) ->
            val (fg, bg) = pair(scheme, app)
            val ratio = contrastRatio(fg, bg)
            assertTrue("$name: $ratio < $min", ratio >= min)
        }

    @Test fun inkOnSurface() = check(4.5f) { s, _ -> s.onSurface to s.surface }
    @Test fun secondaryInkOnGround() = check(4.5f) { s, _ -> s.onSurfaceVariant to s.background }
    @Test fun primaryOnSurface() = check(4.5f) { s, _ -> s.primary to s.surface }
    @Test fun onPrimaryOnPrimary() = check(4.5f) { s, _ -> s.onPrimary to s.primary }
    // Chip de grupo muscular con contorno: tinta de línea también sobre la tarjeta hecha (andén). Con tinte al 12 % no llega.
    @Test fun routineChipOnGround() = check(4.5f) { s, a -> a.lineRoutine to s.background }
    @Test fun dietTextOnSurface() = check(4.5f) { s, a -> a.lineDietText to s.surface }
    @Test fun dietGraphicOnSurface() = check(3f) { s, a -> a.lineDiet to s.surface }
    @Test fun progressOnSurface() = check(4.5f) { s, a -> a.lineProgress to s.surface }
    @Test
    fun macrosOnSurface() {
        check(3f) { s, a -> a.macroProtein to s.surface }
        check(3f) { s, a -> a.macroCarbs to s.surface }
        check(3f) { s, a -> a.macroFat to s.surface }
    }
    @Test fun inkOnSignal() = check(4.5f) { _, a -> a.onSignal to a.signal }
    // La Señal es oscura en los dos temas: el tramo de la SessionBar usa el tono de línea para fondo oscuro.
    @Test fun routineLineOnSignal() = check(3f) { _, a -> DarkAppColors.lineRoutine to a.signal }
    @Test fun hereOnSignal() = check(4.5f) { _, a -> a.here to a.signal }
    @Test fun tonalButton() = check(4.5f) { s, _ -> s.onSecondaryContainer to s.secondaryContainer }

    // Tonal sobre hojas y diálogos (surfaceContainerHigh): sin separación, "Mantener la actual" parece texto suelto.
    @Test
    fun tonalStandsOutOnSheet() = themes.forEach { (name, s, _) ->
        val distance = oklabDistance(s.secondaryContainer, s.surfaceContainerHigh)
        assertTrue("$name: ΔE $distance < 4.5", distance >= 4.5f)
    }

    /** Distancia OKLab ×100, la misma escala que el validador de paletas. */
    private fun oklabDistance(a: Color, b: Color): Float {
        val x = a.convert(ColorSpaces.Oklab)
        val y = b.convert(ColorSpaces.Oklab)
        return 100 * sqrt((x.red - y.red).pow(2) + (x.green - y.green).pow(2) + (x.blue - y.blue).pow(2))
    }
    // Pestaña activa: ícono y etiqueta en el color de la sección sobre la barra; el ícono, también sobre el indicador al 16 %.
    @Test
    fun navItemOnBar() = AppSection.entries.forEach { section ->
        check(4.5f) { s, a -> a.lineTextFor(section, s.onSurface) to s.surfaceContainer }
        check(3f) { s, a ->
            a.lineTextFor(section, s.onSurface) to a.lineFor(section, s.onSurface).copy(alpha = 0.16f).compositeOver(s.surfaceContainer)
        }
    }
    @Test fun errorOnSurface() = check(4.5f) { s, _ -> s.error to s.surface }
    @Test fun outlineOnSurface() = check(3f) { s, _ -> s.outline to s.surface }

    // Snackbar: texto y acción sobre inverseSurface, que es la Señal.
    @Test fun snackbarText() = check(4.5f) { s, _ -> s.inverseOnSurface to s.inverseSurface }
    @Test fun snackbarAction() = check(4.5f) { s, _ -> s.inversePrimary to s.inverseSurface }

    @Test
    fun contrastRatioMatchesWcag() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.White, Color.White), 0.001f)
    }

    @Test
    fun lineForProfileIsInk() {
        val ink = LightColors.onSurface
        assertEquals(ink, LightAppColors.lineFor(AppSection.PROFILE, ink))
        assertEquals(LightAppColors.lineRoutine, LightAppColors.lineFor(AppSection.ROUTINE, ink))
    }
}
