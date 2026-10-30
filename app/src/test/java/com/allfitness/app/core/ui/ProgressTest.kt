package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTest {
    @Test fun halfDone() = assertEquals(0.5f, sessionFraction(3, 6), 0f)
    @Test fun noExercisesIsZero() = assertEquals(0f, sessionFraction(0, 0), 0f)
    @Test fun moreDoneThanTotalIsCapped() = assertEquals(1f, sessionFraction(7, 6), 0f)

    /** 0f/0f llega como NaN y ProgressBarRangeInfo lo rechaza al leer la semántica. */
    @Test fun nanFractionIsZero() = assertEquals(0f, progressFraction(Float.NaN), 0f)

    @Test
    fun fractionIsClamped() {
        assertEquals(1f, progressFraction(1.5f), 0f)
        assertEquals(0f, progressFraction(-0.2f), 0f)
        assertEquals(0.4f, progressFraction(0.4f), 0f)
    }
}
