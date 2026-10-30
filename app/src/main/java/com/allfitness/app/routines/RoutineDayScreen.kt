package com.allfitness.app.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AlternativeButton
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.SessionBar
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
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

@Composable
fun RoutineDayScreen(
    onBack: () -> Unit,
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

    val title = stringResource(R.string.day_with_date, viewModel.date.dayOfWeek.displayName(), viewModel.date.shortLabel())
    val loaded = (state as? UiState.Content)?.data
    Scaffold(
        topBar = { AppTopBar(title, onBack) },
        bottomBar = {
            // Fuera de la lista: "N de M" y "Terminar" siempre a la vista. El Scaffold reserva su alto.
            if (loaded != null && loaded.isToday && loaded.day.type == DayType.TRAINING) {
                SessionBar(
                    done = loaded.doneCount,
                    total = loaded.day.exercises.size,
                    finished = loaded.finished,
                    enabled = loaded.editable && online,
                    finishing = finishing,
                    onFinish = viewModel::finishSession,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.routine_empty),
            offlineEmptyMessage = stringResource(R.string.routine_empty_offline),
            onRetry = viewModel::retry,
            modifier = Modifier.padding(padding),
        ) { view ->
            val reduced = AppTheme.reducedMotion
            Column {
                AiNote()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    when {
                        // "Hoy toca descansar" solo es cierto hoy; otro día de descanso solo se nombra.
                        view.day.type == DayType.REST -> item {
                            Text(
                                stringResource(if (view.isToday) R.string.rest_day_message else R.string.rest_day),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        !view.isToday -> item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                if (view.finished) {
                                    Text(stringResource(R.string.session_finished), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(stringResource(R.string.only_today_message), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        !online && !view.finished -> item { OfflineHint() }
                    }
                    items(view.day.exercises, key = { it.id }) { exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            // Al cambiar un ejercicio por su alternativa, el nuevo entra con fundido.
                            modifier = if (reduced) Modifier else Modifier.animateItem(),
                            checked = if (view.day.type == DayType.TRAINING) exercise.id in view.completed else null,
                            enabled = online,
                            onCheckedChange = if (view.editable) ({ viewModel.toggle(exercise.id, it) }) else null,
                            past = view.isPast,
                            trailing = {
                                AlternativeButton(
                                    onClick = { alternatives.open(AlternativeTarget.Exercise(exercise.id)) },
                                    enabled = online,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
    AlternativeHost(alternatives, online)
}
