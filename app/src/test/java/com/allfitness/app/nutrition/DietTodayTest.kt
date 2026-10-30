package com.allfitness.app.nutrition

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DietTodayTest {
    private val wednesday = LocalDate.of(2026, 10, 28)

    private fun day(dayOfWeek: DayOfWeek) = DietDay(dayOfWeek, Nutrition(1850, 105.0, 206.0, 61.0), emptyList())
    private fun diet(vararg days: DayOfWeek) = Diet("d", 1, Instant.EPOCH, days.map(::day))

    @Test fun dietDayForMatchesDayOfWeek() =
        assertEquals(DayOfWeek.WEDNESDAY, dietDayFor(diet(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), wednesday)?.dayOfWeek)

    @Test fun dietDayForMissingDietIsNull() = assertNull(dietDayFor(null, wednesday))

    @Test fun dietDayForMissingDayIsNull() = assertNull(dietDayFor(diet(DayOfWeek.MONDAY), wednesday))
}
