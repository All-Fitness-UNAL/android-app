package com.allfitness.app.core.ui.theme

import com.allfitness.app.core.ui.numberTextStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {
    private val t = AppTypography

    @Test
    fun numberRolesAreTabular() {
        listOf(t.displaySmall, t.headlineMedium, t.titleLarge, numberTextStyle(t.titleLarge, t.labelMedium))
            .forEach { assertEquals("tnum", it.fontFeatureSettings) }
    }

    @Test
    fun headingsAreOutfitAndTextIsManrope() {
        listOf(t.displaySmall, t.headlineMedium, t.headlineSmall, t.titleLarge, t.titleMedium, t.titleSmall)
            .forEach { assertEquals(OutfitFamily, it.fontFamily) }
        listOf(t.bodyLarge, t.bodyMedium, t.bodySmall, t.labelLarge, t.labelMedium, t.labelSmall)
            .forEach { assertEquals(ManropeFamily, it.fontFamily) }
    }
}
