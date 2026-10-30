package com.allfitness.app.routines

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekRowsTest {
    private val wednesday = LocalDate.of(2026, 10, 28)
    private val routine = Routine("r", 1, Instant.EPOCH, DayOfWeek.entries.map { RoutineDay(it, DayType.TRAINING, null, emptyList()) })

    @Test
    fun markTodayAndPast() {
        val monday = wednesday.minusDays(2)
        val rows = weekRows(routine, listOf(WorkoutLog(monday, emptySet(), sessionFinished = true)), wednesday)
            .associateBy { it.date.dayOfWeek }
        assertEquals(7, rows.size)
        assertTrue(rows.getValue(DayOfWeek.MONDAY).isPast)
        assertTrue(rows.getValue(DayOfWeek.MONDAY).finished)
        assertTrue(rows.getValue(DayOfWeek.WEDNESDAY).isToday)
        assertFalse(rows.getValue(DayOfWeek.WEDNESDAY).isPast)
        val friday = rows.getValue(DayOfWeek.FRIDAY)
        assertFalse(friday.isToday)
        assertFalse(friday.isPast)
        assertFalse(friday.finished)
    }
}
