package com.allfitness.app.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacroSharesTest {
    @Test fun sharesScaleTheReceivedGrams() =
        assertEquals(206f / 372f, Nutrition(1850, 105.0, 206.0, 61.0).macroShares()!!.carbs, 0.001f)

    @Test fun allZeroDrawsNoBar() = assertNull(Nutrition(0, 0.0, 0.0, 0.0).macroShares())

    @Test fun oneShareStartsAtTwelve() = assertEquals(listOf(-90f to 90f), ringArcs(listOf(0.25f)))

    @Test fun sharesFollowEachOtherAroundTheRing() =
        assertEquals(listOf(-90f to 90f, 0f to 180f, 180f to 90f), ringArcs(listOf(0.25f, 0.5f, 0.25f)))

    @Test fun outOfRangeSharesAreClamped() = assertEquals(listOf(-90f to 0f, -90f to 360f), ringArcs(listOf(-1f, 2f)))
}
