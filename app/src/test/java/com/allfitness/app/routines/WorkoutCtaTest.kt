package com.allfitness.app.routines

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutCtaTest {
    private val date = LocalDate.of(2026, 10, 28)
    private val exercises = listOf("a", "b", "c").map { Exercise(it, it, "Piernas", 3, 10, null, 60, null, "", null) }

    private fun row(
        type: DayType = DayType.TRAINING,
        isToday: Boolean = true,
        finished: Boolean = false,
        completed: Set<String> = emptySet(),
        exercises: List<Exercise> = this.exercises,
    ) = WeekDayRow(date, RoutineDay(date.dayOfWeek, type, null, exercises), isToday, finished, completed)

    @Test fun todayTrainingWithoutMarksStarts() = assertEquals(WorkoutCta.START, row().workoutCta())

    @Test fun oneMarkContinues() = assertEquals(WorkoutCta.CONTINUE, row(completed = setOf("a")).workoutCta())

    @Test fun finishedIsFinished() = assertEquals(WorkoutCta.FINISHED, row(finished = true, completed = setOf("a")).workoutCta())

    // Con ejercicios: así falla por la regla de descanso y no por la de lista vacía.
    @Test fun restTodayIsNone() = assertEquals(WorkoutCta.NONE, row(type = DayType.REST).workoutCta())

    @Test fun trainingAnotherDayIsNone() = assertEquals(WorkoutCta.NONE, row(isToday = false).workoutCta())

    @Test fun todayTrainingWithoutExercisesIsNone() = assertEquals(WorkoutCta.NONE, row(exercises = emptyList()).workoutCta())

    @Test
    fun firstPendingIndexSkipsMarked() {
        assertEquals(2, row(completed = setOf("a", "b")).firstPendingIndex())
        assertEquals(0, row(completed = setOf("a", "b", "c")).firstPendingIndex())
    }
}
