package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {
    @Test fun halfDone() = assertEquals(0.5f, sessionFraction(3, 6), 0f)
    @Test fun noExercisesIsZero() = assertEquals(0f, sessionFraction(0, 0), 0f)
    @Test fun moreDoneThanTotalIsCapped() = assertEquals(1f, sessionFraction(7, 6), 0f)
}
