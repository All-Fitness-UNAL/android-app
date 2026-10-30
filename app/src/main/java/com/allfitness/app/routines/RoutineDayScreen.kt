package com.allfitness.app.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.allfitness.app.core.ui.AiDisclaimer
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.navigation.RoutineDayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DayView(
    val day: RoutineDay,
    val completed: Set<String>,
    val finished: Boolean,
    val isToday: Boolean,
) {
    /** Solo se registra la sesión de hoy, en un día de entrenamiento y sin terminar (FR-013). */
    val editable get() = isToday && day.type == DayType.TRAINING && !finished
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
            _message.value = when (result) {
                is AppResult.Success -> UserMessage(R.string.session_finished_message)
                is AppResult.Failure -> UserMessage(result.error.messageRes(), ::finishSession)
            }
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
    Scaffold(
        topBar = { AppTopBar(title, onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.routine_empty),
            offlineEmptyMessage = stringResource(R.string.routine_empty_offline),
            onRetry = viewModel::retry,
            modifier = Modifier.padding(padding),
        ) { view ->
            val canEdit = view.editable && online
            LazyColumn(contentPadding = PaddingValues(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                item { AiDisclaimer() }
                when {
                    view.day.type == DayType.REST -> item { Text(stringResource(R.string.rest_day_message), style = MaterialTheme.typography.bodyLarge) }
                    !view.isToday -> item { Text(stringResource(R.string.only_today_message), style = MaterialTheme.typography.bodyMedium) }
                    !online && !view.finished -> item { OfflineHint() }
                }
                items(view.day.exercises, key = { it.id }) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        checked = if (view.day.type == DayType.TRAINING) exercise.id in view.completed else null,
                        onCheckedChange = if (canEdit) ({ viewModel.toggle(exercise.id, it) }) else null,
                        trailing = {
                            TextButton(
                                onClick = { alternatives.open(AlternativeTarget.Exercise(exercise.id)) },
                                enabled = online,
                            ) { Text(stringResource(R.string.action_request_alternative)) }
                        },
                    )
                }
                if (view.day.type == DayType.TRAINING) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
                            if (view.finished) {
                                Text(stringResource(R.string.session_finished), color = MaterialTheme.colorScheme.primary)
                            } else if (view.isToday) {
                                // Se puede terminar aunque falten ejercicios (FR-014).
                                Button(
                                    onClick = viewModel::finishSession,
                                    enabled = canEdit && !finishing,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.action_finish_session)) }
                            }
                        }
                    }
                }
            }
        }
    }
    AlternativeHost(alternatives, online)
}
