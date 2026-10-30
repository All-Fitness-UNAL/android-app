package com.allfitness.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DataStripTest {
    @Test fun allFitInOneRow() = assertEquals(4, stripColumns(count = 4, available = 347f, widest = 80f))

    /** 3 caben, pero 3 + 1 deja una celda sola: mejor 2 + 2. */
    @Test fun unevenSplitIsBalanced() = assertEquals(2, stripColumns(count = 4, available = 347f, widest = 100f))

    @Test fun neverLessThanOne() = assertEquals(1, stripColumns(count = 3, available = 100f, widest = 200f))

    @Test fun neverMoreThanCells() = assertEquals(2, stripColumns(count = 2, available = 400f, widest = 50f))
}
