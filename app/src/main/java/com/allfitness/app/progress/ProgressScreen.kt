package com.allfitness.app.progress

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allfitness.app.R
import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.time.longLabel
import com.allfitness.app.core.time.shortLabel
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.FormTextField
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.parseOneDecimal
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.onboarding.ProfileValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressView(val weights: List<WeightEntry>, val summary: TrainingSummary?)

data class WeightDialogState(
    val date: LocalDate,
    val text: String = "",
    @StringRes val error: Int? = null,
    val confirmOverwrite: Boolean = false,
    val saving: Boolean = false,
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) { repository.refresh() }

    val state: StateFlow<UiState<ProgressView>> = combine(
        repository.weights, repository.summary, refresher.state, isOnline,
    ) { weights, summary, refresh, online ->
        val combined = weights?.let { Cached(ProgressView(it.data, summary?.data), it.savedAt) }
        uiStateOf(combined, refresh, online)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _dialog = MutableStateFlow<WeightDialogState?>(null)
    val dialog: StateFlow<WeightDialogState?> = _dialog.asStateFlow()
    private val _message = MutableStateFlow<UserMessage?>(null)
    val message: StateFlow<UserMessage?> = _message.asStateFlow()

    fun onResume() = refresher.run()

    fun openDialog() {
        _dialog.value = WeightDialogState(date = Today.date())
    }

    fun closeDialog() {
        if (_dialog.value?.saving != true) _dialog.value = null
    }

    fun onText(text: String) = _dialog.update { it?.copy(text = text, error = null) }

    fun onDate(date: LocalDate) = _dialog.update { it?.copy(date = minOf(date, Today.date()), confirmOverwrite = false) }

    /** Valida 30–300 kg con 1 decimal y fecha no futura; si ya hay registro ese día, pide confirmar (FR-024, FR-025). */
    fun save(confirmed: Boolean = false) {
        val dialog = _dialog.value ?: return
        if (dialog.saving) return
        val kg = parseOneDecimal(dialog.text)
        if (!ProfileValidation.isValidWeight(kg)) {
            _dialog.value = dialog.copy(error = R.string.error_weight_range)
            return
        }
        if (dialog.date.isAfter(Today.date())) {
            _dialog.value = dialog.copy(error = R.string.error_future_date)
            return
        }
        val existing = (state.value as? UiState.Content)?.data?.weights?.any { it.date == dialog.date } == true
        if (existing && !confirmed) {
            _dialog.value = dialog.copy(confirmOverwrite = true)
            return
        }
        _dialog.value = dialog.copy(saving = true, confirmOverwrite = false)
        viewModelScope.launch {
            when (val result = repository.upsertWeight(dialog.date, requireNotNull(kg))) {
                is AppResult.Success -> {
                    _dialog.value = null
                    _message.value = UserMessage(R.string.weight_saved)
                }
                is AppResult.Failure -> _dialog.update { it?.copy(saving = false, error = result.error.messageRes()) }
            }
        }
    }

    fun cancelOverwrite() = _dialog.update { it?.copy(confirmOverwrite = false) }

    fun messageShown() {
        _message.value = null
    }
}

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    MessageEffect(message, snackbar, viewModel::messageShown)

    Scaffold(
        topBar = { AppTopBar(stringResource(R.string.progress_title)) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.progress_empty),
            onRetry = viewModel::onResume,
            modifier = Modifier.padding(padding),
        ) { view ->
            LazyColumn(contentPadding = PaddingValues(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                item { SummaryCard(view.summary) }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            Text(stringResource(R.string.weight_history), style = MaterialTheme.typography.titleMedium)
                            if (view.weights.isEmpty()) {
                                Text(stringResource(R.string.weight_empty), style = MaterialTheme.typography.bodyMedium)
                            } else {
                                WeightChart(view.weights)
                            }
                            Button(onClick = viewModel::openDialog, enabled = online, modifier = Modifier.fillMaxWidth()) {
                                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                                Text(stringResource(R.string.action_add_weight), modifier = Modifier.padding(start = Spacing.s))
                            }
                            if (!online) OfflineHint()
                        }
                    }
                }
                items(view.weights, key = { it.date.toString() }) { entry ->
                    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.s), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.date.longLabel(), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.weight_value, formatNumber(entry.weightKg)), style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    dialog?.let { WeightDialog(it, viewModel) }
}

@Composable
private fun SummaryCard(summary: TrainingSummary?) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(R.string.summary_title), style = MaterialTheme.typography.titleMedium)
            when {
                summary == null -> Text(stringResource(R.string.summary_unavailable), style = MaterialTheme.typography.bodyMedium)
                summary.completedSessions == 0 && summary.weekStreak == 0 -> {
                    if (summary.scheduledSessions > 0) {
                        Text(stringResource(R.string.summary_sessions, 0, summary.scheduledSessions), style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(stringResource(R.string.summary_empty), style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    Text(
                        stringResource(R.string.summary_sessions, summary.completedSessions, summary.scheduledSessions),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(stringResource(R.string.summary_streak, summary.weekStreak), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/** Línea simple de evolución dibujada con Canvas; solo pinta los valores recibidos (research R9). */
@Composable
fun WeightChart(weights: List<WeightEntry>) {
    val points = weights.sortedBy { it.date }
    val first = points.first()
    val last = points.last()
    val min = points.minOf { it.weightKg }
    val max = points.maxOf { it.weightKg }
    val lineColor = MaterialTheme.colorScheme.primary
    val description = stringResource(
        R.string.chart_description,
        formatNumber(first.weightKg), formatNumber(last.weightKg), first.date.shortLabel(), last.date.shortLabel(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(stringResource(R.string.chart_range, formatNumber(min), formatNumber(max)), style = MaterialTheme.typography.labelMedium)
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(Spacing.chartHeight)
                .semantics { contentDescription = description },
        ) {
            val pad = Spacing.chartPoint.toPx() * 2
            val days = (last.date.toEpochDay() - first.date.toEpochDay()).coerceAtLeast(1)
            val range = (max - min).takeIf { it > 0 } ?: 1.0
            val offsets = points.map { p ->
                val x = if (points.size == 1) size.width / 2 else pad + (size.width - 2 * pad) * (p.date.toEpochDay() - first.date.toEpochDay()) / days
                val y = size.height - pad - (size.height - 2 * pad) * ((p.weightKg - min) / range).toFloat()
                Offset(x, if (min == max) size.height / 2 else y)
            }
            offsets.zipWithNext { a, b -> drawLine(lineColor, a, b, strokeWidth = Spacing.chartStroke.toPx()) }
            offsets.forEach { drawCircle(lineColor, Spacing.chartPoint.toPx(), it) }
            drawCircle(lineColor, Spacing.chartPoint.toPx(), offsets.last(), style = Stroke(Spacing.chartStroke.toPx()))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(first.date.shortLabel(), style = MaterialTheme.typography.labelMedium)
            if (points.size > 1) Text(last.date.shortLabel(), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightDialog(state: WeightDialogState, viewModel: ProgressViewModel) {
    var pickingDate by remember { mutableStateOf(false) }

    if (state.confirmOverwrite) {
        AlertDialog(
            onDismissRequest = viewModel::cancelOverwrite,
            title = { Text(stringResource(R.string.weight_overwrite_title)) },
            text = { Text(stringResource(R.string.weight_overwrite_message, state.date.longLabel())) },
            confirmButton = { TextButton(onClick = { viewModel.save(confirmed = true) }) { Text(stringResource(R.string.action_update)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelOverwrite) { Text(stringResource(R.string.action_cancel)) } },
        )
        return
    }

    if (pickingDate) {
        val todayMillis = Today.date().toUtcMillis()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    pickingDate = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(pickerState) }
        return
    }

    AlertDialog(
        onDismissRequest = viewModel::closeDialog,
        title = { Text(stringResource(R.string.action_add_weight)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                FormTextField(
                    value = state.text,
                    onValueChange = viewModel::onText,
                    label = stringResource(R.string.field_weight),
                    error = state.error,
                    keyboardType = KeyboardType.Decimal,
                    suffix = stringResource(R.string.unit_kg),
                    maxLength = 5,
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.weight_date, state.date.longLabel()), modifier = Modifier.weight(1f))
                    TextButton(onClick = { pickingDate = true }) { Text(stringResource(R.string.action_change)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.save() }, enabled = !state.saving) {
                Text(stringResource(if (state.saving) R.string.loading else R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = viewModel::closeDialog, enabled = !state.saving) { Text(stringResource(R.string.action_cancel)) } },
    )
}

private fun LocalDate.toUtcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
