package com.allfitness.app.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import com.allfitness.app.R
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.DarkAppColors
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.Spacing

enum class StationState { PENDING, DONE, REST, TODAY, TODAY_DONE }

fun sessionFraction(done: Int, total: Int): Float =
    if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)

private val StationState.isToday get() = this == StationState.TODAY || this == StationState.TODAY_DONE
private val StationState.isDone get() = this == StationState.DONE || this == StationState.TODAY_DONE

/** Radio exterior, para que la línea llegue justo al borde de la estación. */
private fun StationState.radius(): Dp = when {
    isToday -> Spacing.stationToday / 2 + Spacing.xs
    this == StationState.REST -> Spacing.xs
    else -> Spacing.stationSize / 2
}

/**
 * Estación de la línea. Hoy lleva un halo del color de la línea (no amarillo: el amarillo solo va
 * sobre la Señal y una vez por pantalla).
 */
@Composable
fun RouteStation(state: StationState, lineColor: Color, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline
    val ring = Spacing.hairline * 2
    Box(modifier.size(state.radius() * 2), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(state.radius() * 2)) {
            val stroke = ring.toPx()
            val station = if (state.isToday) Spacing.stationToday.toPx() / 2 else Spacing.stationSize.toPx() / 2
            when (state) {
                StationState.PENDING -> drawCircle(lineColor, station - stroke / 2, style = Stroke(stroke))
                StationState.DONE -> drawCircle(lineColor, station)
                StationState.REST -> drawCircle(outline, size.minDimension / 2 - stroke / 4, style = Stroke(stroke / 2))
                StationState.TODAY -> {
                    drawCircle(lineColor.copy(alpha = 0.24f))
                    drawCircle(lineColor, station - stroke / 2, style = Stroke(stroke))
                }
                StationState.TODAY_DONE -> {
                    drawCircle(lineColor.copy(alpha = 0.24f))
                    drawCircle(lineColor, station)
                }
            }
        }
        if (state.isDone) {
            Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(if (state.isToday) Spacing.sm else Spacing.s),
                tint = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

/** Fila de la semana: estación sobre una línea vertical continua, toda la fila tocable. */
@Composable
fun RouteRow(
    state: StationState,
    title: String,
    subtitle: String,
    lineColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = stringResource(R.string.today)
    val finished = stringResource(R.string.session_finished)
    // El estado se ve en la estación; TalkBack lo necesita como texto.
    val status = listOfNotNull(today.takeIf { state.isToday }, finished.takeIf { state.isDone }).joinToString(", ")
    val gutter = Spacing.xl
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.routeRowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { if (status.isNotEmpty()) stateDescription = status }
            .drawBehind {
                val x = gutter.toPx() / 2
                val y = size.height / 2
                val gap = state.radius().toPx()
                val width = Spacing.lineWidth.toPx()
                if (!isFirst) drawLine(lineColor, Offset(x, 0f), Offset(x, y - gap), width)
                if (!isLast) drawLine(lineColor, Offset(x, y + gap), Offset(x, size.height), width)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(gutter), contentAlignment = Alignment.Center) { RouteStation(state, lineColor) }
        Column(Modifier.weight(1f).padding(start = Spacing.sm, top = Spacing.s, bottom = Spacing.s)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (state == StationState.REST) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Panel Señal: lo de hoy a un vistazo. */
@Composable
fun SignPanel(
    label: String,
    title: String,
    status: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.signal,
        contentColor = colors.onSignal,
        border = BorderStroke(Spacing.hairline, colors.signalOutline),
    ) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                text = label.uppercase(),
                // En mayúsculas TalkBack puede deletrear "OCT"; se lee la forma normal.
                modifier = Modifier.semantics { contentDescription = label },
                style = MaterialTheme.typography.labelSmall,
                color = colors.here,
            )
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
            Text(status, style = MaterialTheme.typography.bodyMedium)
            if (action != null) Box(Modifier.fillMaxWidth().padding(top = Spacing.xs)) { action() }
        }
    }
}

/** Fila de [total] estaciones unidas por línea; las primeras [reached] van llenas. */
@Composable
fun StationProgress(total: Int, reached: Int, lineColor: Color, modifier: Modifier = Modifier) {
    if (total <= 0) return
    val done = reached.coerceIn(0, total)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { index ->
            if (index > 0) {
                val reachedSegment = index < done
                Box(
                    Modifier
                        .weight(1f)
                        .height(Spacing.lineWidth)
                        .background(if (reachedSegment) lineColor else lineColor.copy(alpha = 0.24f)),
                )
            }
            RouteStation(if (index < done) StationState.DONE else StationState.PENDING, lineColor)
        }
    }
}

/**
 * Barra fija de la sesión de hoy sobre la Señal: un tramo de línea que se llena con "N de M" y
 * "Terminar" siempre a la vista (FR-014: se puede terminar aunque falten ejercicios).
 */
@Composable
fun SessionBar(
    done: Int,
    total: Int,
    finished: Boolean,
    enabled: Boolean,
    finishing: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val reduced = AppTheme.reducedMotion
    val fraction by animateFloatAsState(
        targetValue = sessionFraction(done, total),
        animationSpec = if (reduced) snap() else tween(Motion.LONG, easing = Motion.Emphasized),
        label = "session",
    )
    // Pulso de la estación solo al pasar a terminada, no al abrir una sesión ya terminada.
    // Animatable y no animateFloatAsState: empieza y acaba en 1.
    val pulse = remember { Animatable(1f) }
    val openedFinished = remember { finished }
    LaunchedEffect(finished) {
        if (finished && !openedFinished && !reduced) {
            pulse.animateTo(1f, keyframes { durationMillis = Motion.MEDIUM; 1.15f at Motion.MEDIUM / 2 })
        }
    }
    Surface(modifier.fillMaxWidth(), color = colors.signal, contentColor = colors.onSignal) {
        Column(
            Modifier.navigationBarsPadding().padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (finished) {
                Row(
                    Modifier.semantics(mergeDescendants = true) { if (!openedFinished) liveRegion = LiveRegionMode.Polite },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    RouteStation(StationState.TODAY_DONE, colors.lineRoutine, Modifier.graphicsLayer { scaleX = pulse.value; scaleY = pulse.value })
                    Text(stringResource(R.string.session_finished_message), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f).height(Spacing.lineWidth).background(colors.onSignal.copy(alpha = 0.24f))) {
                        // La Señal es oscura también en tema claro: el tono de línea para fondo oscuro se ve mejor.
                        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(DarkAppColors.lineRoutine))
                    }
                    Text(stringResource(R.string.session_progress, done, total), style = MaterialTheme.typography.titleLarge)
                }
                Button(
                    onClick = onFinish,
                    enabled = enabled && !finishing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    // Los colores deshabilitados por defecto son para fondo claro: sobre la Señal no se verían.
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = colors.onSignal.copy(alpha = 0.12f),
                        disabledContentColor = colors.onSignal.copy(alpha = 0.38f),
                    ),
                ) { Text(stringResource(R.string.action_finish_session)) }
            }
        }
    }
}
