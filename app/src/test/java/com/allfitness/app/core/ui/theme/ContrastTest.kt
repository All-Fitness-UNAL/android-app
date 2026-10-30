package com.allfitness.app.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    private val s = AppColorScheme
    private val a = DefaultAppColors

    // Color es una clase de valor: no admite vararg.
    private fun assertContrast(min: Float, fg: Color, backgrounds: List<Color>) = backgrounds.forEach { bg ->
        val ratio = contrastRatio(fg, bg)
        assertTrue("$fg sobre $bg: $ratio < $min", ratio >= min)
    }

    private fun assertContrast(min: Float, fg: Color, bg: Color) = assertContrast(min, fg, listOf(bg))

    /** Un tinte al 12 % ya precalculado como hex opaco debe coincidir con la mezcla real (±1/255 por el redondeo). */
    private fun assertBlend(tint: Color, over: Color, precomputed: Color) {
        val blended = tint.copy(alpha = 0.12f).compositeOver(over)
        val tolerance = 1f / 255f
        assertTrue("rojo", abs(blended.red - precomputed.red) <= tolerance)
        assertTrue("verde", abs(blended.green - precomputed.green) <= tolerance)
        assertTrue("azul", abs(blended.blue - precomputed.blue) <= tolerance)
    }

    @Test
    fun textOnBackgroundAndCards() {
        val cards = listOf(s.background, s.surface, s.surfaceContainerHigh, a.surfaceInput)
        assertContrast(4.5f, s.onSurface, cards)
        assertContrast(4.5f, s.onSurfaceVariant, cards)
    }

    // TonalActionButton.
    @Test fun onSecondaryContainer() = assertContrast(4.5f, s.onSecondaryContainer, s.secondaryContainer)

    @Test
    fun primaryAsTextAndOnPrimary() {
        assertContrast(4.5f, s.primary, listOf(s.background, s.surface))
        assertContrast(4.5f, s.onPrimary, s.primary)
    }

    @Test
    fun accentOnItsContainer() {
        assertBlend(a.accent, s.background, a.accentContainer)
        assertContrast(4.5f, a.accent, a.accentContainer)
    }

    @Test
    fun primaryContainerIsPrimaryTint() {
        assertBlend(s.primary, s.background, s.primaryContainer)
        assertContrast(4.5f, s.onPrimaryContainer, s.primaryContainer)
    }

    @Test fun errorOnCard() = assertContrast(4.5f, s.error, s.surface)

    @Test fun outlineBoundsControls() = assertContrast(3f, s.outline, listOf(s.surface, a.surfaceInput, s.surfaceContainerHigh))

    @Test
    fun macrosAreGraphics() {
        assertContrast(3f, a.macroProtein, s.surface)
        assertContrast(3f, a.macroCarbs, s.surface)
        assertContrast(3f, a.macroFat, s.surface)
    }

    /** `muted` solo es decorativo: si algún día llega a 4,5:1 este test avisa de que ya puede ser texto. */
    @Test fun mutedIsNeverText() = assertTrue(contrastRatio(a.muted, s.surface) < 4.5f)

    // Snackbar: texto y acción sobre inverseSurface.
    @Test
    fun snackbar() {
        assertContrast(4.5f, s.inverseOnSurface, s.inverseSurface)
        assertContrast(4.5f, s.inversePrimary, s.inverseSurface)
    }

    @Test
    fun contrastRatioMatchesWcag() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.White, Color.White), 0.001f)
    }

    // Línea: se borra en la Task 12 junto con lineFor.
    @Test
    fun lineForProfileIsInk() {
        assertEquals(s.onSurface, a.lineFor(AppSection.PROFILE, s.onSurface))
        assertEquals(a.lineRoutine, a.lineFor(AppSection.ROUTINE, s.onSurface))
    }
}
