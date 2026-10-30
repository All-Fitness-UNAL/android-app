package com.allfitness.app.nutrition

import org.junit.Assert.assertEquals
import org.junit.Test

class MealIconTest {
    @Test fun everyMealTypeHasAnIcon() =
        assertEquals(MealType.entries.size, MealType.entries.map { it.iconRes() }.toSet().size)
}
