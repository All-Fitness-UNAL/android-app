package com.allfitness.app.routines

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekStripTest {
    /** Martes y miércoles empiezan por M: el CLDR en español usa X para el miércoles. */
    @Test
    fun initialsAreSpanishNarrow() =
        assertEquals(listOf("L", "M", "X", "J", "V", "S", "D"), DayOfWeek.entries.map(::dayInitial))
}
