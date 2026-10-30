package com.allfitness.app.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.PillShape
import com.allfitness.app.core.ui.theme.Spacing

/** Estado de un día de la semana; lo calcula `WeekDayRow.stationState()` y lo pinta `WeekStrip`. */
enum class StationState { PENDING, PAST, DONE, REST, TODAY, TODAY_DONE }

fun sessionFraction(done: Int, total: Int): Float =
    if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)

/** Figma `progress-bar-container` del onboarding: segmentos separados 6 dp. */
private val SegmentGap = 6.dp

/** Avance animado 450 ms; con movimiento reducido salta al valor. */
@Composable
private fun animatedFraction(fraction: Float): Float {
    val value by animateFloatAsState(
        targetValue = fraction,
        animationSpec = if (AppTheme.reducedMotion) snap() else tween(Motion.LONG, easing = Motion.Emphasized),
        label = "progress",
    )
    return value
}

/** Anillo de [fraction] (0–1): pista `surfaceContainerHigh` y arco verde desde las 12, con [center] dentro. */
@Composable
fun ProgressRing(fraction: Float, size: Dp, modifier: Modifier = Modifier, center: @Composable () -> Unit) {
    val target = fraction.coerceIn(0f, 1f)
    val sweep = animatedFraction(target)
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val arc = MaterialTheme.colorScheme.primary
    Box(modifier.size(size).progressSemantics(target), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = Stroke(Spacing.progressHeight.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val topLeft = Offset(inset, inset)
            val arcSize = Size(this.size.width - stroke.width, this.size.height - stroke.width)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            if (sweep > 0f) drawArc(arc, -90f, 360f * sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        }
        center()
    }
}

/** Barra de progreso en píldora de 6 dp. */
@Composable
fun LinearProgress(fraction: Float, modifier: Modifier = Modifier) {
    val target = fraction.coerceIn(0f, 1f)
    val value = animatedFraction(target)
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val bar = MaterialTheme.colorScheme.primary
    Canvas(modifier.fillMaxWidth().height(Spacing.progressHeight).progressSemantics(target)) {
        val y = size.height / 2
        val end = size.width - y
        drawLine(track, Offset(y, y), Offset(end, y), size.height, StrokeCap.Round)
        if (value > 0f) drawLine(bar, Offset(y, y), Offset(y + (end - y) * value, y), size.height, StrokeCap.Round)
    }
}

/** [total] segmentos iguales; los primeros [reached] en verde (pasos del onboarding, nivel). */
@Composable
fun SegmentedProgress(total: Int, reached: Int, modifier: Modifier = Modifier) {
    if (total <= 0) return
    val done = reached.coerceIn(0, total)
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().progressSemantics(done.toFloat(), 0f..total.toFloat(), steps = total - 1),
        horizontalArrangement = Arrangement.spacedBy(SegmentGap),
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(Spacing.xs)
                    .background(if (index < done) scheme.primary else scheme.surfaceContainerHigh, PillShape),
            )
        }
    }
}
