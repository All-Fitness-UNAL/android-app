package com.allfitness.app.progress

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.allfitness.app.core.ui.DataCell
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.FormTextField
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.RouteStation
import com.allfitness.app.core.ui.ScreenHeader
import com.allfitness.app.core.ui.SectionTitle
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.StationProgress
import com.allfitness.app.core.ui.StationState
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.numberStyle
import com.allfitness.app.core.ui.parseOneDecimal
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.onboarding.ProfileValidation
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
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
import kotlin.math.ceil
import kotlin.math.floor

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
        topBar = { ScreenHeader(stringResource(R.string.progress_title)) },
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
                        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            SectionTitle(stringResource(R.string.weight_trend_title))
                            when {
                                view.weights.isEmpty() -> Text(stringResource(R.string.weight_empty), style = MaterialTheme.typography.bodyMedium)
                                canDrawTrend(view.weights) -> WeightChart(view.weights)
                                // Un punto suelto no es una tendencia.
                                else -> NeedMoreWeights()
                            }
                            Button(
                                onClick = viewModel::openDialog,
                                enabled = online,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                                Text(stringResource(R.string.action_add_weight), modifier = Modifier.padding(start = Spacing.s))
                            }
                            if (!online) OfflineHint()
                        }
                    }
                }
                if (view.weights.isNotEmpty()) item { WeightHistory(view.weights) }
            }
        }
    }

    dialog?.let { WeightDialog(it, viewModel) }
}

/** Rango "Entre X y Y kg" solo con dos pesos distintos: "Entre 62 y 62 kg" no dice nada. */
fun showWeightRange(weights: List<WeightEntry>): Boolean = weights.map { it.weightKg }.distinct().size >= 2

/** Una tendencia necesita al menos dos registros. */
fun canDrawTrend(weights: List<WeightEntry>): Boolean = weights.size >= 2


@Composable
private fun SummaryCard(summary: TrainingSummary?) {
    val colors = AppTheme.colors
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SectionTitle(stringResource(R.string.summary_title))
            if (summary == null) {
                Text(stringResource(R.string.summary_unavailable), style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            // Sin sesiones programadas no hay estaciones, pero "0/0" se sigue mostrando (FR-027).
            StationProgress(summary.scheduledSessions, summary.completedSessions, colors.lineProgress)
            val sessions = stringResource(R.string.summary_sessions, summary.completedSessions, summary.scheduledSessions)
            Row(Modifier.clearAndSetSemantics { contentDescription = sessions }) {
                Text(
                    stringResource(R.string.value_fraction, summary.completedSessions, summary.scheduledSessions),
                    Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    stringResource(R.string.label_sessions),
                    Modifier.alignByBaseline().padding(start = Spacing.s),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (summary.completedSessions == 0 && summary.weekStreak == 0) {
                Text(stringResource(R.string.summary_empty), style = MaterialTheme.typography.bodyMedium)
            } else {
                DataStrip(
                    listOf(
                        DataCell(
                            stringResource(R.string.label_streak),
                            summary.weekStreak.toString(),
                            pluralStringResource(R.plurals.unit_weeks, summary.weekStreak),
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun NeedMoreWeights() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        RouteStation(StationState.PENDING, AppTheme.colors.lineProgress)
        Text(
            stringResource(R.string.chart_need_more),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Historial: filas regladas con la fecha y los kg tabulares a la derecha. */
// shortcut: compone todas las filas de una vez; con cientos de registros, pasarlas a items del LazyColumn.
@Composable
private fun WeightHistory(weights: List<WeightEntry>) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m)) {
            SectionTitle(stringResource(R.string.weight_history))
            weights.sortedByDescending { it.date }.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(thickness = Spacing.hairline, color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = Spacing.sm).semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                ) {
                    Text(entry.date.longLabel(), Modifier.weight(1f).alignByBaseline(), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.weight_value, formatNumber(entry.weightKg)),
                        Modifier.alignByBaseline(),
                        style = numberStyle(MaterialTheme.typography.titleMedium),
                    )
                }
            }
        }
    }
}

/**
 * Evolución del peso con Vico: x = días desde el primer registro, y = kg tal como llegan (research R9).
 * Tocar un punto muestra fecha y kg; el historial de debajo es la vista en tabla.
 */
@Composable
fun WeightChart(weights: List<WeightEntry>) {
    val points = remember(weights) { weights.sortedBy { it.date } }
    val first = points.first()
    val last = points.last()
    val min = points.minOf { it.weightKg }
    val max = points.maxOf { it.weightKg }
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(points) {
        producer.runTransaction {
            lineSeries { series(points.map { it.date.toEpochDay() - first.date.toEpochDay() }, points.map { it.weightKg }) }
        }
    }
    // 1 kg de aire arriba y abajo: el eje no empieza en 0 y la línea no toca los bordes.
    val range = remember(min, max) { CartesianLayerRangeProvider.fixed(minY = floor(min) - 1, maxY = ceil(max) + 1) }
    val dates = remember(first) { CartesianValueFormatter { _, x, _ -> first.date.plusDays(x.toLong()).shortLabel() } }
    val kilos = remember { CartesianValueFormatter { _, y, _ -> formatNumber(y) } }
    val resources = LocalResources.current
    val markerText = remember(first, resources) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val target = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
            val y = target?.points?.firstOrNull()?.entry?.y
            if (target == null || y == null) {
                ""
            } else {
                resources.getString(R.string.chart_marker, first.date.plusDays(target.x.toLong()).shortLabel(), formatNumber(y))
            }
        }
    }
    val axisLabel = rememberAxisLabelComponent(style = numberStyle(typography.labelMedium).copy(color = scheme.onSurfaceVariant))
    val rule = rememberAxisGuidelineComponent(fill = Fill(scheme.outlineVariant))
    val line = Fill(colors.lineProgress)
    val reduced = AppTheme.reducedMotion

    val description = stringResource(
        R.string.chart_description,
        formatNumber(first.weightKg), formatNumber(last.weightKg), first.date.shortLabel(), last.date.shortLabel(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        if (showWeightRange(weights)) {
            Text(
                stringResource(R.string.chart_range, formatNumber(min), formatNumber(max)),
                style = numberStyle(MaterialTheme.typography.labelMedium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = remember(line) { LineCartesianLayer.LineFill.single(line) },
                            pointProvider = LineCartesianLayer.PointProvider.single(
                                LineCartesianLayer.Point(rememberShapeComponent(line, CircleShape), Spacing.chartPoint),
                            ),
                        ),
                    ),
                    rangeProvider = range,
                ),
                startAxis = VerticalAxis.rememberStart(
                    line = null,
                    label = axisLabel,
                    valueFormatter = kilos,
                    tick = null,
                    guideline = rule,
                    itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 3 }) },
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    line = rememberAxisLineComponent(fill = Fill(scheme.outlineVariant)),
                    label = axisLabel,
                    valueFormatter = dates,
                    tick = null,
                    guideline = null,
                    itemPlacer = remember { HorizontalAxis.ItemPlacer.extremes() },
                ),
                marker = rememberDefaultCartesianMarker(
                    label = rememberTextComponent(
                        style = typography.labelLarge.copy(color = scheme.inverseOnSurface),
                        padding = Insets(start = Spacing.s, top = Spacing.xs, end = Spacing.s, bottom = Spacing.xs),
                        background = rememberShapeComponent(Fill(scheme.inverseSurface), MaterialTheme.shapes.extraSmall),
                    ),
                    valueFormatter = markerText,
                    guideline = rule,
                ),
                // Un paso de un día: los registros quedan a la distancia real entre fechas.
                getXStep = { _, _, _ -> 1.0 },
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer = producer,
            modifier = Modifier.fillMaxWidth().height(Spacing.chartHeight).semantics { contentDescription = description },
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            animationSpec = if (reduced) null else tween(Motion.MEDIUM, easing = Motion.Emphasized),
        )
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
