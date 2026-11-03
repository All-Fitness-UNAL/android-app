package com.allfitness.app.routines

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayViewTest {
    private val exercises = listOf("a", "b", "c").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }

    private fun view(type: DayType = DayType.TRAINING, completed: Set<String> = emptySet(), finished: Boolean = false, isToday: Boolean = true) =
        DayView(RoutineDay(DayOfWeek.FRIDAY, type, null, exercises), completed, finished, isToday)

    @Test fun doneCountIgnoresIdsFromOtherDays() = assertEquals(2, view(completed = setOf("a", "b", "zz")).doneCount)

    @Test fun todayTrainingNotFinishedIsEditable() = assertTrue(view().editable)

    @Test
    fun notEditableWhenNotTodayFinishedOrRest() {
        assertFalse(view(isToday = false).editable)
        assertFalse(view(finished = true).editable)
        assertFalse(view(type = DayType.REST).editable)
    }
}
