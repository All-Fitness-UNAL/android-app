package com.allfitness.app.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.allfitness.app.core.ui.AiBadge
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.LinearProgress
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.PrimaryButton
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.ScreenHeader
import com.allfitness.app.core.ui.SectionTitle
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.StationState
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.entrance
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.rememberEntranceClock
import com.allfitness.app.core.ui.sessionFraction
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeekDayRow(
    val date: LocalDate,
    val day: RoutineDay,
    val isToday: Boolean,
    val finished: Boolean,
    val completed: Set<String> = emptySet(),
    val isPast: Boolean = false,
) {
    /** Solo cuentan los ejercicios de este día. */
    val doneCount: Int get() = day.exercises.count { it.id in completed }
}

fun WeekDayRow.stationState(): StationState = when {
    isToday && finished -> StationState.TODAY_DONE
    isToday -> StationState.TODAY
    day.type == DayType.REST -> StationState.REST
    finished -> StationState.DONE
    isPast -> StationState.PAST
    else -> StationState.PENDING
}

/** Las 7 filas de la semana de [today] con la rutina y los registros de esa semana. */
fun weekRows(routine: Routine, logs: List<WorkoutLog>, today: LocalDate): List<WeekDayRow> =
    Today.weekDates(today).map { date ->
        WeekDayRow(
            date = date,
            day = routine.days.first { it.dayOfWeek == date.dayOfWeek },
            isToday = date == today,
            finished = logs.any { it.date == date && it.sessionFinished },
            completed = logs.firstOrNull { it.date == date }?.completedExerciseIds.orEmpty(),
            isPast = date < today,
        )
    }

enum class WorkoutCta { START, CONTINUE, FINISHED, NONE }

/** Acción del entreno de hoy en Inicio; solo hoy, en día de entreno con ejercicios (FR-013). */
fun WeekDayRow.workoutCta(): WorkoutCta = when {
    !isToday || day.type != DayType.TRAINING || day.exercises.isEmpty() -> WorkoutCta.NONE
    finished -> WorkoutCta.FINISHED
    doneCount > 0 -> WorkoutCta.CONTINUE
    else -> WorkoutCta.START
}

/** Primer ejercicio sin marcar; con todos marcados, el primero. */
fun WeekDayRow.firstPendingIndex(): Int = day.exercises.indexOfFirst { it.id !in completed }.coerceAtLeast(0)

/** Casilla de [exerciseId]: hoy marcada o vacía; un día pasado solo el visto de lo hecho (spec §5); otro día, nada. */
fun WeekDayRow.doneMark(exerciseId: String): Boolean? = when {
    isToday -> exerciseId in completed
    isPast && exerciseId in completed -> true
    else -> null
}

/** Lo que se guarda al tocar [date] en la tira: hoy se guarda como null para seguir a la fecha tras medianoche. */
internal fun List<WeekDayRow>.selectionOf(date: LocalDate): LocalDate? = date.takeUnless { d -> any { it.isToday && it.date == d } }

/** Día elegido en la tira; sin elegir, o si la fecha ya no está en la semana, hoy. */
private fun List<WeekDayRow>.shown(selected: LocalDate?): WeekDayRow = firstOrNull { it.date == selected } ?: first { it.isToday }

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
        uiStateOf(routine, refresh, online).mapContent { weekRows(it, logs?.data.orEmpty(), today) }
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
    onOpenSession: (LocalDate, Int) -> Unit,
    viewModel: RoutineWeekViewModel = hiltViewModel(),
    alternatives: AlternativeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val generating by viewModel.generating.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val altDone by alternatives.doneMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    // Día elegido en la tira; null = hoy, así sigue a la fecha si cambia el día.
    var selected by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    MessageEffect(message, snackbar, viewModel::messageShown)
    MessageEffect(altDone, snackbar, alternatives::doneShown)

    val rows = (state as? UiState.Content)?.data
    val today = rows?.first { it.isToday }
    // El botón sigue al entreno de hoy y solo se ve con hoy elegido (FR-013).
    val cta = today?.workoutCta()?.takeIf { rows.shown(selected).isToday && !generating && (it == WorkoutCta.START || it == WorkoutCta.CONTINUE) }

    Scaffold(
        topBar = { ScreenHeader(stringResource(R.string.routine_title), trailing = { AiBadge() }) },
        bottomBar = {
            if (today != null && cta != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
                        .padding(horizontal = Spacing.screenEdge, vertical = Spacing.sm),
                ) {
                    PrimaryButton(
                        text = stringResource(if (cta == WorkoutCta.START) R.string.action_start_workout else R.string.action_continue_workout),
                        onClick = { onOpenSession(today.date, today.firstPendingIndex()) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = R.drawable.lc_circle_play,
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (generating) {
            AiGeneratingState(stringResource(R.string.generating_routine), Modifier.padding(padding))
            return@Scaffold
        }
        val direction = LocalLayoutDirection.current
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.routine_empty),
            offlineEmptyMessage = stringResource(R.string.routine_empty_offline),
            onRetry = viewModel::retry,
            emptyActionLabel = stringResource(R.string.action_generate_routine),
            emptyActionEnabled = online,
            onEmptyAction = viewModel::generate,
            // Sin el margen de abajo: la lista pasa bajo el degradado del botón y lo reserva con contentPadding.
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(direction),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(direction),
            ),
        ) { weekRows ->
            val row = weekRows.shown(selected)
            val entrance = rememberEntranceClock()
            val edge = Modifier.padding(horizontal = Spacing.m)
            Column {
                AiNote()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = Spacing.m, bottom = padding.calculateBottomPadding() + Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    // Margen menor que la lista: en 360 dp cada día sigue teniendo 48 dp de ancho para el dedo.
                    item { WeekStrip(weekRows, row.date, onSelect = { selected = weekRows.selectionOf(it) }, Modifier.padding(horizontal = Spacing.s)) }
                    item { DayHeader(row, edge.padding(vertical = Spacing.s)) }
                    itemsIndexed(row.day.exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            done = row.doneMark(exercise.id),
                            onClick = { onOpenSession(row.date, index) },
                            modifier = edge.entrance(entrance, index),
                        )
                    }
                    item {
                        Column(edge.padding(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            TonalActionButton(
                                text = stringResource(R.string.action_regenerate_routine),
                                onClick = { alternatives.open(AlternativeTarget.WholeRoutine) },
                                enabled = online,
                                modifier = Modifier.fillMaxWidth(),
                                icon = R.drawable.lc_refresh_cw,
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
internal fun RoutineDay.dayTitle(): String =
    if (type == DayType.REST) stringResource(R.string.rest_day) else title ?: stringResource(R.string.training_day)

/** Título, fecha y avance del día elegido (Figma `progress-bar-container`). Días que no son hoy: solo lectura (FR-013). */
@Composable
private fun DayHeader(row: WeekDayRow, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val date = stringResource(R.string.day_with_date, row.date.dayOfWeek.displayName(), row.date.shortLabel())
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        SectionTitle(row.day.dayTitle())
        Text(
            if (row.finished) stringResource(R.string.status_pair, date, stringResource(R.string.session_finished)) else date,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        if (row.day.type == DayType.REST) {
            // "Hoy toca descansar" solo es cierto hoy; otro día de descanso ya se nombra en el título.
            if (row.isToday) Text(stringResource(R.string.rest_day_message), style = MaterialTheme.typography.bodyLarge)
        } else {
            val total = row.day.exercises.size
            val fraction = sessionFraction(row.doneCount, total)
            FlowRow(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.workout_percent, (fraction * 100).roundToInt()), style = MaterialTheme.typography.labelMedium, color = scheme.primary)
                Text(stringResource(R.string.session_progress, row.doneCount, total), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            }
            LinearProgress(fraction)
            if (!row.isToday) {
                Text(stringResource(R.string.only_today_message), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        }
    }
}
