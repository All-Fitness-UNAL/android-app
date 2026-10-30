package com.allfitness.app.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.allfitness.app.R
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Tarjeta blanca sobre el andén, separada por un hilo y sin sombra. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = color)
    val border = BorderStroke(Spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    val shape = MaterialTheme.shapes.medium
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}

/** Carga indeterminada: un tramo de línea que recorre el ancho. */
@Composable
fun LineIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val track = color.copy(alpha = 0.24f)
    if (AppTheme.reducedMotion) {
        LinearProgressIndicator(modifier.fillMaxWidth(), color = color, trackColor = track)
        return
    }
    val progress by rememberInfiniteTransition(label = "line").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = Motion.Emphasized)),
        label = "segment",
    )
    Canvas(
        modifier
            .fillMaxWidth()
            .height(Spacing.lineWidth)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
    ) {
        val y = size.height / 2
        drawLine(track, Offset(0f, y), Offset(size.width, y), strokeWidth = size.height)
        val segment = size.width / 3
        val start = (size.width + segment) * progress - segment
        val from = start.coerceAtLeast(0f)
        val to = (start + segment).coerceAtMost(size.width)
        if (to > from) drawLine(color, Offset(from, y), Offset(to, y), strokeWidth = size.height)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier, message: String? = null) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LineIndicator(Modifier.fillMaxWidth(0.5f))
        if (message != null) {
            Text(
                text = message,
                modifier = Modifier.padding(top = Spacing.m).semantics { liveRegion = LiveRegionMode.Polite },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Espera de la IA sobre la Señal. Las etapas son orientativas: ninguna se marca como hecha,
 * porque no hay progreso real que mostrar.
 */
@Composable
fun AiGeneratingState(message: String, modifier: Modifier = Modifier, stages: List<String> = emptyList()) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize().background(colors.signal).padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSignal,
        )
        LineIndicator(Modifier.padding(top = Spacing.l), color = colors.lineRoutine)
        if (stages.isNotEmpty()) {
            Column(Modifier.padding(top = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                stages.forEach { stage ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(Spacing.stationSize).border(Spacing.hairline * 2, colors.lineRoutine, CircleShape))
                        Text(
                            text = stage,
                            modifier = Modifier.padding(start = Spacing.sm),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSignal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        message = stringResource(error.messageRes()),
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier,
        isError = true,
    )
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {},
) {
    MessageState(message, actionLabel, onAction, modifier, actionEnabled)
}

@Composable
private fun MessageState(
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    isError: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Estación como ilustración mínima: llena en error, hueca en vacío.
        val station = Modifier.size(Spacing.stationToday)
        Box(
            if (isError) {
                station.background(MaterialTheme.colorScheme.error, CircleShape)
            } else {
                station.border(Spacing.hairline * 2, MaterialTheme.colorScheme.outline, CircleShape)
            },
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = Spacing.m),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (actionLabel != null) {
            TonalActionButton(actionLabel, onAction, Modifier.padding(top = Spacing.m), enabled = actionEnabled)
        }
    }
}

@Composable
fun OfflineBanner(savedAt: Long?, modifier: Modifier = Modifier) {
    val date = savedAt?.let {
        DateTimeFormatter.ofPattern("d MMM, h:mm a", Today.locale)
            .format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
    }
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_cloud_off),
                contentDescription = null,
                modifier = Modifier.size(Spacing.m),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (date != null) stringResource(R.string.offline_banner_with_date, date) else stringResource(R.string.offline_banner),
                modifier = Modifier.padding(start = Spacing.s),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Aviso de IA compacto bajo el título: el texto completo, sin caja de color. */
@Composable
fun AiNote(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)) {
            Icon(
                painterResource(R.drawable.ic_info),
                contentDescription = null,
                modifier = Modifier.size(Spacing.m),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.ai_disclaimer),
                modifier = Modifier.padding(start = Spacing.s),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(thickness = Spacing.hairline, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun OfflineHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.requires_connection),
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = if (onBack == null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

/**
 * Pinta los estados cargando / vacío / error y, con contenido, el aviso sin conexión encima.
 * Solo anima al cambiar de tipo de estado; contenido → contenido se actualiza en sitio.
 */
@Composable
fun <T> StateContent(
    state: UiState<T>,
    emptyMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    offlineEmptyMessage: String = stringResource(R.string.empty_offline),
    emptyActionLabel: String? = null,
    emptyActionEnabled: Boolean = true,
    onEmptyAction: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    val reduced = AppTheme.reducedMotion
    AnimatedContent(
        targetState = state,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            if (reduced) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(tween(Motion.SHORT, easing = LinearEasing)) togetherWith fadeOut(tween(Motion.SHORT, easing = LinearEasing))
            }
        },
        contentKey = { it::class },
        label = "state",
    ) { current ->
        when (current) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(current.error, onRetry)
            is UiState.Empty -> if (current.offline) {
                EmptyState(offlineEmptyMessage)
            } else {
                EmptyState(emptyMessage, actionLabel = emptyActionLabel, actionEnabled = emptyActionEnabled, onAction = onEmptyAction)
            }
            is UiState.Content -> Column(Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = current.isStale,
                    enter = if (reduced) EnterTransition.None else expandVertically() + fadeIn(),
                    exit = if (reduced) ExitTransition.None else shrinkVertically() + fadeOut(),
                ) { OfflineBanner(current.savedAt) }
                content(current.data)
            }
        }
    }
}

/** Muestra [message] una vez en el snackbar y avisa con [onShown] para que el ViewModel lo limpie. */
@Composable
fun MessageEffect(message: UserMessage?, snackbar: SnackbarHostState, onShown: () -> Unit) {
    val text = message?.let { stringResource(it.text) }
    val retryLabel = stringResource(R.string.action_retry)
    LaunchedEffect(message?.id) {
        if (message == null || text == null) return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = text,
            actionLabel = if (message.retry != null) retryLabel else null,
            duration = if (message.retry != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) message.retry?.invoke()
        onShown()
    }
}
