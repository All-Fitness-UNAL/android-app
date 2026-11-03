package com.allfitness.app.routines

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionControlsTest {
    private val exercises = listOf("a", "b", "c").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }

    private fun view(finished: Boolean = false, isToday: Boolean = true, completed: Set<String> = emptySet()) =
        DayView(RoutineDay(DayOfWeek.FRIDAY, DayType.TRAINING, null, exercises), completed, finished, isToday)

    @Test fun todayEditableCanMarkAndFinish() = assertEquals(SessionControls(true, true, false), view().controls(0))

    @Test fun lastPageMakesFinishProminent() = assertTrue(view(completed = setOf("c")).controls(2).finishProminent)

    // Sin marcar el último, "Marcar como hecho" sigue siendo la única acción principal.
    @Test fun lastPageNotDoneKeepsFinishSecondary() = assertFalse(view().controls(2).finishProminent)

    @Test fun readOnlyDayHasNoMarkNorFinish() = assertEquals(SessionControls(false, false, false), view(isToday = false).controls(0))

    @Test fun finishedDayHasNoMarkNorFinish() = assertEquals(SessionControls(false, false, false), view(finished = true).controls(2))

    @Test fun nextPageAfterMarkAdvances() = assertEquals(1, nextPageAfterMark(0, 3))

    @Test fun nextPageAfterMarkStaysOnLast() = assertEquals(2, nextPageAfterMark(2, 3))
}
