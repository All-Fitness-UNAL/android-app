package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class EntranceTest {
    @Test
    fun firstItemStartsAtOnceAndEndsAfterMedium() {
        assertEquals(0f, entranceProgress(0f, 0), 0.001f)
        assertEquals(0.5f, entranceProgress(150f, 0), 0.001f)
        assertEquals(1f, entranceProgress(300f, 0), 0.001f)
    }

    @Test
    fun eachItemWaits30msMore() {
        assertEquals(0f, entranceProgress(30f, 1), 0.001f)
        assertEquals(0.5f, entranceProgress(180f, 1), 0.001f)
    }

    /** Del 7.º en adelante entran con el 6.º: ninguno aparece de golpe ni alarga la entrada. */
    @Test
    fun itemsPastTheSixthEnterWithIt() {
        assertEquals(entranceProgress(200f, 5), entranceProgress(200f, 9), 0.001f)
        assertEquals(1f, entranceProgress(ENTRANCE_TOTAL_MS.toFloat(), 9), 0.001f)
    }
}
