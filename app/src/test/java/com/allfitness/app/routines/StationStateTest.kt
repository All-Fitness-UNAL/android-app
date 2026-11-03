package com.allfitness.app.routines

import com.allfitness.app.core.ui.StationState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StationStateTest {
    private val date = LocalDate.of(2026, 10, 30)

    private fun row(type: DayType, isToday: Boolean = false, finished: Boolean = false, isPast: Boolean = false) =
        WeekDayRow(date, RoutineDay(date.dayOfWeek, type, null, emptyList()), isToday, finished, isPast = isPast)

    @Test fun trainingNotTodayNotFinishedIsPending() =
        assertEquals(StationState.PENDING, row(DayType.TRAINING).stationState())

    @Test fun trainingNotTodayFinishedIsDone() =
        assertEquals(StationState.DONE, row(DayType.TRAINING, finished = true).stationState())

    @Test fun pastTrainingNotFinishedIsPast() =
        assertEquals(StationState.PAST, row(DayType.TRAINING, isPast = true).stationState())

    @Test
    fun pastFinishedOrRestKeepsItsState() {
        assertEquals(StationState.DONE, row(DayType.TRAINING, finished = true, isPast = true).stationState())
        assertEquals(StationState.REST, row(DayType.REST, isPast = true).stationState())
    }

    @Test fun restNotTodayIsRest() =
        assertEquals(StationState.REST, row(DayType.REST).stationState())

    @Test
    fun todayNotFinishedIsToday() {
        assertEquals(StationState.TODAY, row(DayType.TRAINING, isToday = true).stationState())
        assertEquals(StationState.TODAY, row(DayType.REST, isToday = true).stationState())
    }

    @Test fun todayFinishedIsTodayDone() =
        assertEquals(StationState.TODAY_DONE, row(DayType.TRAINING, isToday = true, finished = true).stationState())

    @Test
    fun doneCountOnlyCountsExercisesOfTheDay() {
        val exercises = listOf("a", "b", "c").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }
        val row = WeekDayRow(date, RoutineDay(date.dayOfWeek, DayType.TRAINING, null, exercises), true, false, setOf("a", "b", "zz"))
        assertEquals(2, row.doneCount)
    }
}
