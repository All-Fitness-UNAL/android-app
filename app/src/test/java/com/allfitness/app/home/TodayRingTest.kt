package com.allfitness.app.home

import com.allfitness.app.routines.DayType
import com.allfitness.app.routines.Exercise
import com.allfitness.app.routines.RoutineDay
import com.allfitness.app.routines.WeekDayRow
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodayRingTest {
    private val date = LocalDate.of(2026, 10, 31)
    private val exercises = listOf("a", "b").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }
    private fun row(type: DayType, list: List<Exercise>) = WeekDayRow(date, RoutineDay(date.dayOfWeek, type, null, list), true, false, setOf("a"))

    @Test fun trainingShowsDoneOfTotal() = assertEquals(1 to 2, row(DayType.TRAINING, exercises).todayRing())

    // Descanso: "–" y no "0/0".
    @Test fun restShowsNothing() = assertNull(row(DayType.REST, emptyList()).todayRing())
}
