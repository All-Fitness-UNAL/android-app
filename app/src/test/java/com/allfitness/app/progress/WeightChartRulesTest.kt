package com.allfitness.app.progress

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightChartRulesTest {
    private val day = LocalDate.of(2026, 10, 20)
    private fun weights(vararg kg: Double) = kg.mapIndexed { i, w -> WeightEntry(day.plusDays(i.toLong()), w) }

    @Test
    fun oneEntryHasNoTrendNorRange() {
        assertFalse(canDrawTrend(weights(62.0)))
        assertFalse(showWeightRange(weights(62.0)))
    }

    /** Sin "Entre 62 y 62 kg". */
    @Test
    fun equalWeightsDrawTrendWithoutRange() {
        assertTrue(canDrawTrend(weights(62.0, 62.0)))
        assertFalse(showWeightRange(weights(62.0, 62.0)))
    }

    @Test
    fun differentWeightsDrawBoth() {
        assertTrue(canDrawTrend(weights(62.0, 61.5)))
        assertTrue(showWeightRange(weights(62.0, 61.5)))
    }
}
