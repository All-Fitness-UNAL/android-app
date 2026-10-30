package com.allfitness.app.core.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTest {
    @Test fun animationsOffIsReduced() = assertTrue(isReducedMotion(0f))
    @Test fun normalScaleIsNotReduced() = assertFalse(isReducedMotion(1f))
    @Test fun fasterScaleIsNotReduced() = assertFalse(isReducedMotion(0.5f))
}
