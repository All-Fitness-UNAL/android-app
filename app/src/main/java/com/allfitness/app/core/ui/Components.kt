package com.allfitness.app.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
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

/** Tarjeta `surface` de radio 16 con hilo y sin sombra; [highlighted] (Figma `goal-card`): radio 20 y borde verde. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = CardDefaults.cardColors(containerColor = scheme.surface)
    val border = BorderStroke(Spacing.hairline, if (highlighted) scheme.primary else scheme.outlineVariant)
    val shape = if (highlighted) MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large
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
        animationSpec = infiniteRepeatable(tween(Motion.LOADER_CYCLE, easing = Motion.Emphasized)),
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
 * Espera de la IA: marca, mensaje vivo, indicador y etapas. Las etapas son orientativas: ninguna se marca
 * como hecha, porque no hay progreso real que mostrar. [summary] son pares etiqueta/valor de lo elegido.
 */
@Composable
fun AiGeneratingState(
    message: String,
    modifier: Modifier = Modifier,
    stages: List<String> = emptyList(),
    summary: List<Pair<String?, String>> = emptyList(),
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxSize().background(scheme.background).padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
    ) {
        LogoMark(Modifier.padding(bottom = Spacing.xl))
        Text(
            text = message,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.headlineSmall,
        )
        LineIndicator(Modifier.padding(top = Spacing.l))
        if (stages.isNotEmpty()) {
            Column(Modifier.padding(top = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                stages.forEach { stage ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(Spacing.sm).border(Spacing.hairline * 2, scheme.primary, CircleShape))
                        Text(
                            text = stage,
                            modifier = Modifier.padding(start = Spacing.sm),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        if (summary.isNotEmpty()) {
            Column(Modifier.padding(top = Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                summary.forEach { (label, value) ->
                    Column(Modifier.semantics(mergeDescendants = true) {}) {
                        if (label != null) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        }
                        Text(value, style = MaterialTheme.typography.bodyLarge)
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
        Icon(
            painterResource(if (isError) R.drawable.lc_triangle_alert else R.drawable.lc_info),
            contentDescription = null,
            modifier = Modifier.size(Spacing.xl),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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

/** Título de sección: Outfit y marcado como encabezado para TalkBack. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
}

/** Cifra en Outfit tabular (kg, kcal) con el tamaño de [size]: las columnas de números se alinean. */
@Composable
fun numberStyle(size: TextStyle): TextStyle = numberTextStyle(MaterialTheme.typography.titleLarge, size)

internal fun numberTextStyle(base: TextStyle, size: TextStyle): TextStyle =
    base.copy(fontSize = size.fontSize, lineHeight = size.lineHeight, fontFeatureSettings = "tnum")

/** Aviso de IA (FR-037) bajo la cabecera: el texto completo, sin caja de color, y un hilo que lo separa de la lista. */
@Composable
fun AiNote(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = Spacing.m, end = Spacing.m, bottom = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Icon(
                painterResource(R.drawable.lc_info),
                contentDescription = null,
                modifier = Modifier.size(Spacing.m),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.ai_disclaimer),
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

/**
 * Cabecera de pantalla (Figma `title-block`, `today-workout-title`): título Outfit 28 (20 con [onBack]),
 * [subtitle] debajo y [trailing] a la derecha (etiqueta de IA o duración). El título se parte en líneas
 * con fuente grande en vez de cortarse.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(start = if (onBack != null) Spacing.xs else Spacing.m, end = Spacing.m, top = Spacing.s, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.lc_arrow_left), contentDescription = stringResource(R.string.action_back))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = if (onBack == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
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

private const val ENTRANCE_ITEMS = 6
private const val ENTRANCE_STAGGER_MS = 30

/** Duración total de la entrada escalonada: el último escalón más una entrada MEDIUM. */
const val ENTRANCE_TOTAL_MS = Motion.MEDIUM + (ENTRANCE_ITEMS - 1) * ENTRANCE_STAGGER_MS

/** Avance (0–1) del ítem [index] cuando el reloj va por [elapsedMs]; del 7.º en adelante entran con el 6.º. */
fun entranceProgress(elapsedMs: Float, index: Int): Float {
    val delay = index.coerceAtMost(ENTRANCE_ITEMS - 1) * ENTRANCE_STAGGER_MS
    return ((elapsedMs - delay) / Motion.MEDIUM).coerceIn(0f, 1f)
}

/**
 * Reloj de la entrada de una lista: corre una sola vez por pantalla (al volver atrás o cambiar de
 * pestaña no se repite) y nunca con movimiento reducido.
 */
@Composable
fun rememberEntranceClock(): State<Float> {
    val reduced = AppTheme.reducedMotion
    var played by rememberSaveable { mutableStateOf(false) }
    val clock = remember { Animatable(if (played || reduced) ENTRANCE_TOTAL_MS.toFloat() else 0f) }
    LaunchedEffect(Unit) {
        played = true
        clock.animateTo(ENTRANCE_TOTAL_MS.toFloat(), tween(ENTRANCE_TOTAL_MS, easing = LinearEasing))
    }
    return clock.asState()
}

/** Fundido y subida de 8 dp del ítem [index]; el reloj se lee en la capa, sin recomponer la lista. */
fun Modifier.entrance(clock: State<Float>, index: Int): Modifier = graphicsLayer {
    val progress = Motion.Emphasized.transform(entranceProgress(clock.value, index))
    alpha = progress
    translationY = (1f - progress) * Spacing.s.toPx()
}
