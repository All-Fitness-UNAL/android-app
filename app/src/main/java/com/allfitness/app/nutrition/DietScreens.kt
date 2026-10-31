package com.allfitness.app.nutrition

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.time.displayName
import com.allfitness.app.core.time.shortLabel
import com.allfitness.app.core.ui.AiBadge
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.DataCell
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.PrimaryButton
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.ScreenHeader
import com.allfitness.app.core.ui.SectionTitle
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.entrance
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.numberStyle
import com.allfitness.app.core.ui.rememberEntranceClock
import com.allfitness.app.core.ui.stripColumns
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.PillShape
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.navigation.RecipeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@StringRes
fun MealType.labelRes(): Int = when (this) {
    MealType.BREAKFAST -> R.string.meal_breakfast
    MealType.LUNCH -> R.string.meal_lunch
    MealType.DINNER -> R.string.meal_dinner
    MealType.SNACK -> R.string.meal_snack
}

@HiltViewModel
class DietWeekViewModel @Inject constructor(
    private val repository: DietRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) { repository.refresh() }

    val state: StateFlow<UiState<Diet>> = combine(repository.diet, refresher.state, isOnline, ::uiStateOf)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _selectedDay = MutableStateFlow(Today.date().dayOfWeek)
    val selectedDay: StateFlow<DayOfWeek> = _selectedDay.asStateFlow()
    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()
    private val _message = MutableStateFlow<UserMessage?>(null)
    val message: StateFlow<UserMessage?> = _message.asStateFlow()

    fun select(day: DayOfWeek) {
        _selectedDay.value = day
    }

    fun onResume() = refresher.run()

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
fun DietWeekScreen(
    onOpenMeal: (DayOfWeek, String) -> Unit,
    viewModel: DietWeekViewModel = hiltViewModel(),
    alternatives: AlternativeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDay.collectAsStateWithLifecycle()
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
        topBar = {
            val today = Today.date()
            ScreenHeader(
                stringResource(R.string.diet_title),
                subtitle = stringResource(R.string.day_with_date, today.dayOfWeek.displayName(), today.shortLabel()),
                trailing = { AiBadge() },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (generating) {
            AiGeneratingState(stringResource(R.string.generating_diet), Modifier.padding(padding))
            return@Scaffold
        }
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.diet_empty),
            offlineEmptyMessage = stringResource(R.string.diet_empty_offline),
            onRetry = viewModel::onResume,
            emptyActionLabel = stringResource(R.string.action_generate_diet),
            emptyActionEnabled = online,
            onEmptyAction = viewModel::generate,
            modifier = Modifier.padding(padding),
        ) { diet ->
            val day = diet.days.first { it.dayOfWeek == selected }
            val reduced = AppTheme.reducedMotion
            val entrance = rememberEntranceClock()
            Column {
                AiNote()
                DayTabs(selected, viewModel::select)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    item { DaySummary(day) }
                    itemsIndexed(day.meals, key = { _, meal -> meal.id }) { index, meal ->
                        MealCard(
                            meal,
                            onClick = { onOpenMeal(day.dayOfWeek, meal.id) },
                            modifier = (if (reduced) Modifier else Modifier.animateItem()).entrance(entrance, index),
                        )
                    }
                    item {
                        Column(Modifier.padding(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            TonalActionButton(
                                text = stringResource(R.string.action_regenerate_diet),
                                onClick = { alternatives.open(AlternativeTarget.WholeDiet) },
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

@HiltViewModel
class RecipeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: DietRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<RecipeRoute>()
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) { repository.refresh() }

    val state: StateFlow<UiState<Meal>> = combine(repository.diet, refresher.state, isOnline) { diet, refresh, online ->
        when (val ui = uiStateOf(diet, refresh, online)) {
            is UiState.Content -> ui.data.days.firstOrNull { it.dayOfWeek.name == route.dayOfWeek }
                ?.meals?.firstOrNull { it.id == route.mealId }
                ?.let { UiState.Content(it, ui.isStale, ui.savedAt) }
                ?: UiState.Error(AppError.NotFound)
            UiState.Loading -> UiState.Loading
            is UiState.Empty -> ui
            is UiState.Error -> ui
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() = refresher.run()
}

@Composable
fun RecipeScreen(
    onBack: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel(),
    alternatives: AlternativeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val altDone by alternatives.doneMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    MessageEffect(altDone, snackbar, alternatives::doneShown)

    val shown = (state as? UiState.Content)?.data
    val title = shown?.type?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.recipe_title)
    Scaffold(
        topBar = { ScreenHeader(title, onBack = onBack) },
        bottomBar = {
            // Figma `sticky-footer`: la única acción de la receta, fija tras un degradado del fondo. Opaca debajo:
            // el aviso sin conexión no puede quedar sobre el texto que pasa por detrás.
            if (shown != null) {
                // Más allá de endY el degradado se queda en el fondo opaco (TileMode.Clamp).
                val fade = with(LocalDensity.current) { Spacing.l.toPx() }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background), endY = fade))
                        // Pantalla de detalle sin barra inferior: el botón queda encima de la barra de gestos.
                        .navigationBarsPadding()
                        .padding(start = Spacing.screenEdge, top = Spacing.l, end = Spacing.screenEdge, bottom = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.action_request_alternative),
                        onClick = { alternatives.open(AlternativeTarget.Recipe(shown.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = online,
                        trailingIcon = R.drawable.ic_alt_route,
                    )
                    if (!online) OfflineHint()
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.diet_empty),
            offlineEmptyMessage = stringResource(R.string.diet_empty_offline),
            onRetry = viewModel::retry,
            // Sin el margen de abajo: el contenido pasa bajo el degradado del botón y lo reserva al final.
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(direction),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(direction),
            ),
        ) { meal ->
            Column {
                AiNote()
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = Spacing.m, top = Spacing.m, end = Spacing.m, bottom = padding.calculateBottomPadding() + Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                ) {
                    RecipeTitle(meal.recipe)
                    RecipeContent(meal.recipe)
                }
            }
        }
    }
    AlternativeHost(alternatives, online)
}

/** Proporción de los gramos recibidos, solo para dibujar la barra; no calcula nutrición (FR-017). */
data class MacroShares(val protein: Float, val carbs: Float, val fat: Float)

fun Nutrition.macroShares(): MacroShares? {
    val total = proteinG + carbsG + fatG
    if (total <= 0.0) return null
    return MacroShares((proteinG / total).toFloat(), (carbsG / total).toFloat(), (fatG / total).toFloat())
}

/**
 * Barra apilada de macros con un hueco de 2 dp entre segmentos. Sin semántica propia: la DataStrip
 * de debajo lee los mismos gramos y hace de leyenda.
 */
@Composable
fun MacroBar(shares: MacroShares, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier.fillMaxWidth().height(Spacing.s).clip(MaterialTheme.shapes.extraSmall).clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(Spacing.hairline * 2),
    ) {
        listOf(shares.protein to colors.macroProtein, shares.carbs to colors.macroCarbs, shares.fat to colors.macroFat)
            .filter { (share, _) -> share > 0f } // weight(0f) no es válido
            .forEach { (share, color) -> Box(Modifier.weight(share).fillMaxHeight().background(color)) }
    }
}

/** Gramos tal como los envía el servidor (FR-017); [legend] marca cada celda con el color de su segmento. */
@Composable
internal fun macroCells(nutrition: Nutrition, legend: Boolean): List<DataCell> {
    val colors = AppTheme.colors
    val grams = stringResource(R.string.unit_grams)
    return listOf(
        DataCell(stringResource(R.string.label_protein), formatNumber(nutrition.proteinG), grams, colors.macroProtein.takeIf { legend }),
        DataCell(stringResource(R.string.label_carbs), formatNumber(nutrition.carbsG), grams, colors.macroCarbs.takeIf { legend }),
        DataCell(stringResource(R.string.label_fat), formatNumber(nutrition.fatG), grams, colors.macroFat.takeIf { legend }),
    )
}

/** Arcos (inicio, barrido) en grados, seguidos desde las 12 en sentido horario, de proporciones 0–1. Solo dibujo (FR-017). */
fun ringArcs(fractions: List<Float>): List<Pair<Float, Float>> {
    var start = -90f
    return fractions.map { fraction ->
        val sweep = 360f * fraction.coerceIn(0f, 1f)
        (start to sweep).also { start += sweep }
    }
}

/**
 * Selector de días: 7 columnas iguales con los 7 días a la vista. Solo si una columna quedara por debajo de
 * 48 dp o de su rótulo (fuente grande) pasa a desplazarse, y entonces lleva el día elegido a la vista.
 */
@Composable
private fun DayTabs(selected: DayOfWeek, onSelect: (DayOfWeek) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.labelLarge
    val labels = DayOfWeek.entries.map { it.displayName(java.time.format.TextStyle.SHORT) }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val widest = with(density) { labels.maxOf { measurer.measure(it, style).size.width }.toDp() } + Spacing.s * 2
    val column = maxOf(Spacing.xxl, widest)
    Column {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = Spacing.s)) {
            val fixed = maxWidth / DayOfWeek.entries.size >= column
            val scroll = rememberScrollState()
            if (!fixed) {
                val offset = with(density) { (column * DayOfWeek.entries.indexOf(selected)).roundToPx() }
                LaunchedEffect(offset) { scroll.animateScrollTo(offset) }
            }
            Row(Modifier.fillMaxWidth().then(if (fixed) Modifier else Modifier.horizontalScroll(scroll)).selectableGroup()) {
                DayOfWeek.entries.forEachIndexed { index, day ->
                    val isSelected = day == selected
                    Box(
                        (if (fixed) Modifier.weight(1f) else Modifier.width(column))
                            .heightIn(min = Spacing.xxl)
                            .selectable(isSelected, role = Role.Tab, onClick = { onSelect(day) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(labels[index], style = style, color = if (isSelected) scheme.primary else scheme.onSurfaceVariant, maxLines = 1)
                        if (isSelected) TabRowDefaults.SecondaryIndicator(Modifier.align(Alignment.BottomCenter), color = scheme.primary)
                    }
                }
            }
        }
        HorizontalDivider(thickness = Spacing.hairline, color = scheme.outlineVariant)
    }
}

/** Figma `nutrition-summary-card`: kcal del plan en grande, barra de proporción y una fila por macro con sus gramos. */
@Composable
private fun DaySummary(day: DietDay) {
    val scheme = MaterialTheme.colorScheme
    val totals = day.totals
    val kcal = stringResource(R.string.kcal_value, totals.kcal)
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    stringResource(R.string.diet_day_totals, day.dayOfWeek.displayName().lowercase(Today.locale)),
                    Modifier.semantics { heading() },
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Row(Modifier.clearAndSetSemantics { contentDescription = kcal }) {
                    Text(totals.kcal.toString(), Modifier.alignByBaseline(), style = numberStyle(MaterialTheme.typography.displaySmall))
                    Text(
                        stringResource(R.string.label_kcal),
                        Modifier.alignByBaseline().padding(start = Spacing.s),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(thickness = Spacing.hairline, color = scheme.outlineVariant)
            totals.macroShares()?.let { MacroBar(it) }
            macroCells(totals, legend = true).forEach { cell ->
                Row(
                    Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    cell.color?.let { Box(Modifier.size(Spacing.s).background(it, CircleShape)) }
                    Text(cell.label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(stringResource(R.string.value_grams, cell.value), style = numberStyle(MaterialTheme.typography.titleSmall))
                }
            }
        }
    }
}

/** Cabecera tipográfica de la receta (sin foto, spec §3): píldora con los minutos y el nombre en grande. */
@Composable
private fun RecipeTitle(recipe: Recipe) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(
            Modifier
                .background(scheme.surfaceContainerHigh, PillShape)
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(painterResource(R.drawable.lc_clock), contentDescription = null, Modifier.size(Spacing.m), tint = scheme.primary)
            Text(stringResource(R.string.meal_prep_minutes, recipe.prepMinutes), style = MaterialTheme.typography.labelLarge, color = scheme.primary)
        }
        Text(recipe.name, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
    }
}

/** Placas, ingredientes y pasos de una receta; también la muestra la propuesta de alternativa. */
@Composable
fun RecipeContent(recipe: Recipe) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            MacroTiles(recipe.perServing)
            Text(stringResource(R.string.recipe_per_serving), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }

        val count = recipe.ingredients.size
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectionTitle(stringResource(R.string.recipe_ingredients))
            Text(
                pluralStringResource(R.plurals.recipe_ingredients_count, count, count),
                Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.primary,
            )
        }
        Column {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                if (index > 0) HorizontalDivider(thickness = Spacing.hairline, color = scheme.outlineVariant)
                Column(
                    Modifier.fillMaxWidth().padding(vertical = Spacing.sm).semantics(mergeDescendants = true) {},
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(if (ingredient.unit == MeasureUnit.G) R.string.value_grams else R.string.value_ml, formatNumber(ingredient.amount)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.recipe_steps))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            recipe.steps.forEachIndexed { index, step ->
                Row(Modifier.semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    // Figma `step-badge`: círculo de 28 dp; tamaño mínimo para que la cifra crezca con la fuente.
                    Box(
                        Modifier
                            .sizeIn(minWidth = Spacing.weekDot, minHeight = Spacing.weekDot)
                            .background(scheme.surfaceContainerHigh, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text((index + 1).toString(), style = numberStyle(MaterialTheme.typography.titleSmall), color = scheme.primary)
                    }
                    Text(step, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Figma `macros-row`: kcal, proteína, carbohidratos y grasas, cada una con un anillo de la proporción de
 * gramos (el de kcal reparte los tres). Tantas columnas como quepan sin partir "CARBOHIDRATOS": 4, 2 o 1.
 */
@Composable
private fun MacroTiles(nutrition: Nutrition) {
    val colors = AppTheme.colors
    val cells = listOf(DataCell(stringResource(R.string.label_kcal), nutrition.kcal.toString())) + macroCells(nutrition, legend = false)
    val macros = nutrition.macroShares()
        ?.let { listOf(it.protein to colors.macroProtein, it.carbs to colors.macroCarbs, it.fat to colors.macroFat) }
        .orEmpty()
    val measurer = rememberTextMeasurer()
    val typography = MaterialTheme.typography
    val density = LocalDensity.current
    val widest = remember(cells, typography, density) {
        // Margen de la placa a cada lado más el hueco entre placas.
        val padding = with(density) { (Spacing.sm * 3).toPx() }
        cells.maxOf {
            maxOf(measurer.measure(it.label, typography.labelMedium).size.width, measurer.measure(it.value, typography.titleSmall).size.width)
        } + padding
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = stripColumns(cells.size, constraints.maxWidth.toFloat(), widest)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            cells.indices.chunked(columns).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    row.forEach { index ->
                        val segments = if (index == 0) macros else listOfNotNull(macros.getOrNull(index - 1))
                        MacroTile(cells[index], segments, Modifier.weight(1f).fillMaxHeight())
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun MacroTile(cell: DataCell, segments: List<Pair<Float, Color>>, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    AppCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(Spacing.sm).semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            MacroRing(segments)
            Row {
                Text(cell.value, Modifier.alignByBaseline(), style = numberStyle(MaterialTheme.typography.titleSmall))
                if (cell.unit != null) {
                    Text(
                        cell.unit,
                        Modifier.alignByBaseline().padding(start = Spacing.xs),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Text(cell.label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

/** Anillo de 36 dp con [segments] (proporción y color) seguidos sobre la pista. Sin semántica: las cifras van debajo. */
@Composable
private fun MacroRing(segments: List<Pair<Float, Color>>) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val arcs = ringArcs(segments.map { it.first })
    Canvas(Modifier.size(Spacing.iconTile)) {
        val stroke = Stroke(Spacing.xs.toPx())
        val inset = stroke.width / 2
        val topLeft = Offset(inset, inset)
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        arcs.forEachIndexed { index, (start, sweep) ->
            if (sweep > 0f) drawArc(segments[index].second, start, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        }
    }
}
