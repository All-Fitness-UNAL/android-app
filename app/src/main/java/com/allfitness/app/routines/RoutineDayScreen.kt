package com.allfitness.app.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.allfitness.app.R
import com.allfitness.app.aifeedback.AlternativeHost
import com.allfitness.app.aifeedback.AlternativeTarget
import com.allfitness.app.aifeedback.AlternativeViewModel
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.time.displayName
import com.allfitness.app.core.time.shortLabel
import coil3.compose.AsyncImage
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AlternativeButton
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.IconTile
import com.allfitness.app.core.ui.LinearProgress
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.PrimaryButton
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.ScreenHeader
import com.allfitness.app.core.ui.SecondaryButton
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.sessionFraction
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.navigation.RoutineDayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayView(
    val day: RoutineDay,
    val completed: Set<String>,
    val finished: Boolean,
    val isToday: Boolean,
    val isPast: Boolean = false,
) {
    /** Solo se registra la sesión de hoy, en un día de entrenamiento y sin terminar (FR-013). */
    val editable get() = isToday && day.type == DayType.TRAINING && !finished

    /** Marcas de este día; las de otros días no cuentan. */
    val doneCount: Int get() = day.exercises.count { it.id in completed }
}

/** Botones del pie del modo sesión para una página. */
data class SessionControls(val canMark: Boolean, val canFinish: Boolean, val finishProminent: Boolean)

/** Marcar y terminar siguen a [DayView.editable] (FR-013); terminar se ofrece en toda página (FR-014) y destaca en la última. */
fun DayView.controls(page: Int): SessionControls =
    SessionControls(canMark = editable, canFinish = editable, finishProminent = editable && page == day.exercises.lastIndex)

/** Tras marcar se pasa al siguiente ejercicio; en el último se queda. */
fun nextPageAfterMark(page: Int, count: Int): Int = (page + 1).coerceIn(0, maxOf(count - 1, 0))

@HiltViewModel
class RoutineDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RoutineRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val date: LocalDate = LocalDate.parse(savedStateHandle.toRoute<RoutineDayRoute>().date)
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) { repository.refresh() }
    private val today = MutableStateFlow(Today.date())

    // Marcas optimistas mientras el servidor confirma (research R11).
    private val pending = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val state: StateFlow<UiState<DayView>> = combine(
        repository.routine, repository.weekLogs, refresher.state, isOnline, combine(pending, today, ::Pair),
    ) { routine, logs, refresh, online, (pending, today) ->
        uiStateOf(routine, refresh, online).mapContent { r ->
            val log = logs?.data.orEmpty().firstOrNull { it.date == date }
            val saved = log?.completedExerciseIds.orEmpty()
            DayView(
                day = r.days.first { it.dayOfWeek == date.dayOfWeek },
                completed = saved - pending.filterValues { !it }.keys + pending.filterValues { it }.keys,
                finished = log?.sessionFinished == true,
                isToday = date == today,
                isPast = date < today,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _finishing = MutableStateFlow(false)
    val finishing: StateFlow<Boolean> = _finishing.asStateFlow()
    private val _message = MutableStateFlow<UserMessage?>(null)
    val message: StateFlow<UserMessage?> = _message.asStateFlow()

    fun onResume() {
        today.value = Today.date()
        refresher.run()
    }

    fun retry() = refresher.run()

    fun toggle(exerciseId: String, completed: Boolean) {
        if (exerciseId in pending.value) return
        pending.update { it + (exerciseId to completed) }
        viewModelScope.launch {
            val result = repository.setExerciseCompleted(date, exerciseId, completed)
            pending.update { it - exerciseId }
            if (result is AppResult.Failure) {
                _message.value = UserMessage(result.error.messageRes(), retry = { toggle(exerciseId, completed) })
            }
        }
    }

    fun finishSession() {
        if (_finishing.value) return
        _finishing.value = true
        viewModelScope.launch {
            val result = repository.finishSession(date)
            _finishing.value = false
            // El éxito lo muestra (y anuncia) la SessionBar; un snackbar lo repetiría.
            if (result is AppResult.Failure) _message.value = UserMessage(result.error.messageRes(), ::finishSession)
        }
    }

    fun messageShown() {
        _message.value = null
    }
}

/** Figma `exercise-illustration-frame`: 362 × 180. */
private const val IMAGE_RATIO = 2f

/** Modo sesión (FR-035b): un ejercicio a la vez del día, abierto en [startIndex]. */
@Composable
fun RoutineDayScreen(
    onBack: () -> Unit,
    startIndex: Int = 0,
    viewModel: RoutineDayViewModel = hiltViewModel(),
    alternatives: AlternativeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val finishing by viewModel.finishing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val altDone by alternatives.doneMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    MessageEffect(message, snackbar, viewModel::messageShown)
    MessageEffect(altDone, snackbar, alternatives::doneShown)

    val date = stringResource(R.string.day_with_date, viewModel.date.dayOfWeek.displayName(), viewModel.date.shortLabel())
    val loaded = (state as? UiState.Content)?.data
    Scaffold(
        // Cargada la rutina, el título es el del día y la fecha pasa debajo.
        topBar = { ScreenHeader(loaded?.day?.dayTitle() ?: date, subtitle = date.takeIf { loaded != null }, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.routine_empty),
            offlineEmptyMessage = stringResource(R.string.routine_empty_offline),
            onRetry = viewModel::retry,
            modifier = Modifier.padding(padding),
        ) { view ->
            // Descanso (o un entreno sin ejercicios, que el mapeo ya descarta): sin pager.
            if (view.day.exercises.isEmpty()) {
                Column {
                    AiNote()
                    // "Hoy toca descansar" solo es cierto hoy; otro día de descanso ya se nombra en el título.
                    if (view.isToday) Text(stringResource(R.string.rest_day_message), Modifier.padding(Spacing.m), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Session(
                    view = view,
                    startIndex = startIndex,
                    online = online,
                    finishing = finishing,
                    onToggle = viewModel::toggle,
                    onFinish = viewModel::finishSession,
                    onAlternative = { alternatives.open(AlternativeTarget.Exercise(it)) },
                )
            }
        }
    }
    AlternativeHost(alternatives, online)
}

@Composable
private fun Session(
    view: DayView,
    startIndex: Int,
    online: Boolean,
    finishing: Boolean,
    onToggle: (String, Boolean) -> Unit,
    onFinish: () -> Unit,
    onAlternative: (String) -> Unit,
) {
    val exercises = view.day.exercises
    val pager = rememberPagerState(startIndex.coerceIn(0, exercises.lastIndex)) { exercises.size }
    // Si la lista se acorta, el pager corrige su página en el siguiente layout.
    val page = pager.currentPage.coerceAtMost(exercises.lastIndex)
    val scope = rememberCoroutineScope()
    val reduced = AppTheme.reducedMotion
    val go: (Int) -> Unit = { target ->
        scope.launch { if (reduced) pager.scrollToPage(target) else pager.animateScrollToPage(target) }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = Spacing.m, end = Spacing.m, bottom = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                stringResource(R.string.session_exercise_of, page + 1, exercises.size),
                Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgress(sessionFraction(view.doneCount, exercises.size))
        }
        AiNote()
        HorizontalPager(pager, Modifier.weight(1f), key = { exercises[it].id }) { index ->
            val exercise = exercises[index]
            ExercisePage(exercise, done = exercise.id in view.completed, alternativeEnabled = online, onAlternative = { onAlternative(exercise.id) })
        }
        SessionFooter(view, page, online, finishing, go, onToggle, onFinish)
    }
}

@Composable
private fun ExercisePage(exercise: Exercise, done: Boolean, alternativeEnabled: Boolean, onAlternative: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        if (exercise.imageUrl != null) {
            AsyncImage(
                model = exercise.imageUrl,
                // El nombre va justo debajo: decorativa.
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(IMAGE_RATIO)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            // Sin imagen del servidor, la placa: nunca una foto inventada (spec §3).
            if (exercise.imageUrl == null) IconTile(R.drawable.lc_dumbbell, Spacing.thumbExercise)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    MuscleTag(exercise.muscleGroup)
                    if (done) DoneTag()
                }
                Text(exercise.name, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
            }
        }
        DataStrip(exercise.dataCells())
        if (exercise.instructions.isNotBlank()) {
            Text(exercise.instructions, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AlternativeButton(onClick = onAlternative, enabled = alternativeEnabled)
    }
}

@Composable
private fun DoneTag() {
    Row(Modifier.semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(painterResource(R.drawable.lc_check), contentDescription = null, Modifier.size(Spacing.m), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.exercise_done), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Pie fijo sobre el fondo: "A continuación", Anterior y Saltar (la alternativa al deslizamiento para TalkBack)
 * y, solo hoy, marcar y terminar (FR-013, FR-014; sin conexión, deshabilitados con aviso: FR-039).
 */
@Composable
private fun SessionFooter(
    view: DayView,
    page: Int,
    online: Boolean,
    finishing: Boolean,
    go: (Int) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onFinish: () -> Unit,
) {
    val exercises = view.day.exercises
    val current = exercises[page]
    val done = current.id in view.completed
    val controls = view.controls(page)
    val haptics = LocalHapticFeedback.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenEdge, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        exercises.getOrNull(page + 1)?.let { UpNext(it, onSkip = { go(page + 1) }) }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SecondaryButton(stringResource(R.string.action_previous_exercise), { go(page - 1) }, Modifier.weight(1f), enabled = page > 0)
            SecondaryButton(stringResource(R.string.action_skip), { go(page + 1) }, Modifier.weight(1f), enabled = page < exercises.lastIndex)
        }
        if (controls.canMark) {
            PrimaryButton(
                text = stringResource(if (done) R.string.action_unmark_done else R.string.action_mark_done),
                onClick = {
                    haptics.performHapticFeedback(if (done) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                    onToggle(current.id, !done)
                    if (!done) go(nextPageAfterMark(page, exercises.size))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = online,
            )
        }
        if (controls.canFinish) {
            val finish = stringResource(R.string.action_finish_session)
            if (controls.finishProminent) {
                PrimaryButton(finish, onFinish, Modifier.fillMaxWidth(), enabled = online && !finishing)
            } else {
                SecondaryButton(finish, onFinish, Modifier.fillMaxWidth(), enabled = online && !finishing)
            }
        }
        if (!online && controls.canMark) OfflineHint()
        SessionStatus(view)
    }
}

/** Hoy terminada: el mensaje (anunciado solo si se terminó ahora). Otro día: "Terminada" si lo está y que solo se marca hoy. */
@Composable
private fun SessionStatus(view: DayView) {
    val openedFinished = remember { view.finished }
    val scheme = MaterialTheme.colorScheme
    when {
        view.isToday && view.finished -> Row(
            Modifier.semantics(mergeDescendants = true) { if (!openedFinished) liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Icon(painterResource(R.drawable.lc_trophy), contentDescription = null, Modifier.size(Spacing.l), tint = scheme.primary)
            Text(stringResource(R.string.session_finished_message), style = MaterialTheme.typography.titleMedium)
        }
        !view.isToday -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            if (view.finished) {
                Text(stringResource(R.string.session_finished), style = MaterialTheme.typography.labelLarge, color = scheme.primary)
            }
            Text(stringResource(R.string.only_today_message), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
        }
    }
}

/** Fila compacta del siguiente ejercicio (Figma `next-exercise-card`): tocarla es Saltar. */
@Composable
private fun UpNext(exercise: Exercise, onSkip: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = stringResource(R.string.action_skip), role = Role.Button, onClick = onSkip)
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ExerciseThumb(exercise, Spacing.thumbNext)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.session_up_next), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                Text(exercise.name, style = MaterialTheme.typography.titleSmall)
                Text(exercise.setsText(), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            }
        }
    }
}
