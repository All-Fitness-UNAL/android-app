package com.allfitness.app.routines

import com.allfitness.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseLoadTest {
    private fun exercise(reps: Int?, seconds: Int?) = Exercise("a", "Sentadilla", "Piernas", 4, reps, seconds, 60, null, "", null)

    @Test fun repsWin() = assertEquals(R.string.exercise_sets_reps to 12, exercise(reps = 12, seconds = 45).loadRes())

    @Test fun durationWhenNoReps() = assertEquals(R.string.exercise_sets_seconds to 60, exercise(reps = null, seconds = 60).loadRes())

    @Test fun neitherFallsBackToSets() = assertEquals(R.string.exercise_sets_reps to 0, exercise(reps = null, seconds = null).loadRes())
}
