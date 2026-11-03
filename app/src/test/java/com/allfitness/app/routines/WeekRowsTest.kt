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

    private val exercises = listOf("a", "b").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }
    private fun day(isToday: Boolean, isPast: Boolean, completed: Set<String>) =
        WeekDayRow(wednesday, RoutineDay(wednesday.dayOfWeek, DayType.TRAINING, null, exercises), isToday, false, completed, isPast)

    // Spec §5: la casilla vacía solo hoy; los días pasados solo muestran lo hecho.
    @Test
    fun doneMarkOnlyTodayHasEmptyBox() {
        val today = day(isToday = true, isPast = false, completed = setOf("a"))
        assertEquals(true, today.doneMark("a"))
        assertEquals(false, today.doneMark("b"))
        val past = day(isToday = false, isPast = true, completed = setOf("a"))
        assertEquals(true, past.doneMark("a"))
        assertEquals(null, past.doneMark("b"))
        assertEquals(null, day(isToday = false, isPast = false, completed = emptySet()).doneMark("a"))
    }

    // Tocar hoy no fija la fecha: tras medianoche la tira debe seguir al nuevo hoy.
    @Test
    fun selectingTodayKeepsFollowingToday() {
        val rows = weekRows(routine, emptyList(), wednesday)
        assertEquals(null, rows.selectionOf(wednesday))
        assertEquals(wednesday.minusDays(1), rows.selectionOf(wednesday.minusDays(1)))
    }
}
