package com.allfitness.app.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacroSharesTest {
    @Test fun sharesScaleTheReceivedGrams() =
        assertEquals(206f / 372f, Nutrition(1850, 105.0, 206.0, 61.0).macroShares()!!.carbs, 0.001f)

    @Test fun allZeroDrawsNoBar() = assertNull(Nutrition(0, 0.0, 0.0, 0.0).macroShares())
}
