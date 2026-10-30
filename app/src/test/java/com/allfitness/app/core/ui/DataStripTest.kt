package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStripTest {
    @Test fun allFitInOneRow() = assertEquals(4, stripColumns(count = 4, available = 347f, widest = 80f))

    /** 3 caben, pero 3 + 1 deja una celda sola: mejor 2 + 2. */
    @Test fun unevenSplitIsBalanced() = assertEquals(2, stripColumns(count = 4, available = 347f, widest = 100f))

    @Test fun neverLessThanOne() = assertEquals(1, stripColumns(count = 3, available = 100f, widest = 200f))

    @Test fun neverMoreThanCells() = assertEquals(2, stripColumns(count = 2, available = 400f, widest = 50f))

    @Test fun equalWidthsWhenAllFitTheShare() = assertEquals(List(4) { 86.75f }, stripWeights(listOf(60f, 45f, 75f, 50f), 347f))

    /** Macros: CARBOHIDRATOS no cabe en un tercio, pero las tres caben en una fila con anchos distintos. */
    @Test
    fun unequalWidthsBeforeWrapping() {
        val widths = listOf(100f, 137f, 89f)
        val weights = stripWeights(widths, 347f)!!
        assertTrue(weights.zip(widths).all { (w, min) -> w >= min })
        assertEquals(347f, weights.sum(), 0.01f)
    }

    @Test fun nullWhenTheyDoNotFitInOneRow() = assertNull(stripWeights(listOf(137f, 137f, 137f), 347f))
}
