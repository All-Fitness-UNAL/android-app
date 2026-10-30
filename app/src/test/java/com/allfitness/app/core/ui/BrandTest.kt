package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class BrandTest {
    @Test fun initialSkipsLeadingSpacesAndIsUppercase() = assertEquals("M", initialOf("  maría"))

    @Test fun emptyNameHasNoInitial() = assertEquals("", initialOf(""))

    /** Un carácter fuera del BMP es un par sustituto: take(1) lo partía. */
    @Test fun initialKeepsTheWholeCodePoint() = assertEquals("𝒜", initialOf("𝒜na"))
}
