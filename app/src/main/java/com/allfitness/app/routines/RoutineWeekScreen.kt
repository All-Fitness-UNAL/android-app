package com.allfitness.app.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allfitness.app.R
import com.allfitness.app.aifeedback.AlternativeHost
import com.allfitness.app.aifeedback.AlternativeTarget
import com.allfitness.app.aifeedback.AlternativeViewModel
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.time.displayName
import com.allfitness.app.core.time.shortLabel
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeekDayRow(val date: LocalDate, val day: RoutineDay, val isToday: Boolean, val finished: Boolean)

@HiltViewModel
class RoutineWeekViewModel @Inject constructor(
    private val repository: RoutineRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) { repository.refresh() }
    private val today = MutableStateFlow(Today.date())

    val state: StateFlow<UiState<List<WeekDayRow>>> = combine(
        repository.routine, repository.weekLogs, refresher.state, isOnline, today,
    ) { routine, logs, refresh, online, today ->
        uiStateOf(routine, refresh, online).mapContent { r ->
            Today.weekDates(today).map { date ->
                WeekDayRow(
                    date = date,
                    day = r.days.first { it.dayOfWeek == date.dayOfWeek },
                    isToday = date == today,
                    finished = logs?.data.orEmpty().any { it.date == date && it.sessionFinished },
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()
    private val _message = MutableStateFlow<UserMessage?>(null)
    val message: StateFlow<UserMessage?> = _message.asStateFlow()

    /** Al volver a primer plano: puede haber cambiado el día o el servidor pudo actualizar la rutina (FR-011a). */
    fun onResume() {
        today.value = Today.date()
        refresher.run()
    }

    fun retry() = refresher.run()

    fun generate() {
        if (_generating.value) return
        _generating.value = true
        viewModelScope.launch {
            val result = repository.generate(null)
            _generating.value = false
            if (result is AppResult.Failure) _message.value = UserMessage(result.error.messageRes(), ::generate)
        }
    }

    fun messageShown() {
        _message.value = null
    }
}

@Composable
fun RoutineWeekScreen(
    onOpenDay: (LocalDate) -> Unit,
    viewModel: RoutineWeekViewModel = hiltViewModel(),
    alternatives: AlternativeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val generating by viewModel.generating.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val altDone by alternatives.doneMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    MessageEffect(message, snackbar, viewModel::messageShown)
    MessageEffect(altDone, snackbar, alternatives::doneShown)

    Scaffold(
        topBar = { AppTopBar(stringResource(R.string.routine_title)) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (generating) {
            AiGeneratingState(stringResource(R.string.generating_routine), Modifier.padding(padding))
            return@Scaffold
        }
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.routine_empty),
            offlineEmptyMessage = stringResource(R.string.routine_empty_offline),
            onRetry = viewModel::retry,
            emptyActionLabel = stringResource(R.string.action_generate_routine),
            emptyActionEnabled = online,
            onEmptyAction = viewModel::generate,
            modifier = Modifier.padding(padding),
        ) { rows ->
            Column {
                AiNote()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    items(rows, key = { it.date.toString() }) { row -> DayCard(row) { onOpenDay(row.date) } }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            TonalActionButton(
                                text = stringResource(R.string.action_regenerate_routine),
                                onClick = { alternatives.open(AlternativeTarget.WholeRoutine) },
                                enabled = online,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (!online) OfflineHint()
                        }
                    }
                }
            }
        }
    }
    AlternativeHost(alternatives, online)
}

@Composable
private fun DayCard(row: WeekDayRow, onClick: () -> Unit) {
    val colors = if (row.isToday) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(onClick = onClick, colors = colors, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.day_with_date, row.date.dayOfWeek.displayName(), row.date.shortLabel()),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    if (row.day.type == DayType.REST) stringResource(R.string.rest_day) else row.day.title ?: stringResource(R.string.training_day),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            when {
                row.finished -> Text(stringResource(R.string.session_finished), color = MaterialTheme.colorScheme.primary)
                row.isToday -> Text(stringResource(R.string.today), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
