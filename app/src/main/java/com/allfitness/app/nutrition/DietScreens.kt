package com.allfitness.app.nutrition

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AlternativeButton
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.DataCell
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
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
import com.allfitness.app.core.ui.theme.AppTheme
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
        topBar = { ScreenHeader(stringResource(R.string.diet_title)) },
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
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    item { DaySummary(day) }
                    item(key = day.dayOfWeek) {
                        MealsCard(
                            day.meals,
                            onOpen = { onOpenMeal(day.dayOfWeek, it.id) },
                            entrance = entrance,
                            modifier = if (reduced) Modifier else Modifier.animateItem(),
                        )
                    }
                    item {
                        Column(Modifier.padding(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            TonalActionButton(
                                text = stringResource(R.string.action_regenerate_diet),
                                onClick = { alternatives.open(AlternativeTarget.WholeDiet) },
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

    val title = (state as? UiState.Content)?.data?.type?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.recipe_title)
    Scaffold(
        topBar = { ScreenHeader(title, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.diet_empty),
            offlineEmptyMessage = stringResource(R.string.diet_empty_offline),
            onRetry = viewModel::retry,
            modifier = Modifier.padding(padding),
        ) { meal ->
            Column {
                AiNote()
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                ) {
                    Text(meal.recipe.name, style = MaterialTheme.typography.headlineSmall)
                    RecipeContent(meal.recipe)
                    AlternativeButton(
                        onClick = { alternatives.open(AlternativeTarget.Recipe(meal.id)) },
                        enabled = online,
                    )
                    if (!online) OfflineHint()
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

/** Selector de días: desplaza a la pestaña elegida y deja ver que hay más días. */
@Composable
private fun DayTabs(selected: DayOfWeek, onSelect: (DayOfWeek) -> Unit) {
    val colors = AppTheme.colors
    val index = DayOfWeek.entries.indexOf(selected)
    SecondaryScrollableTabRow(
        selectedTabIndex = index,
        containerColor = MaterialTheme.colorScheme.background,
        edgePadding = Spacing.m,
        // Más estrechas que los 90 dp por defecto: a los lados se asoman días cortados y se ve que hay más.
        minTabWidth = Spacing.xxl + Spacing.l,
        indicator = {
            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(index, matchContentSize = false), color = colors.lineDiet)
        },
    ) {
        DayOfWeek.entries.forEach { day ->
            Tab(
                selected = day == selected,
                onClick = { onSelect(day) },
                text = { Text(day.displayName(java.time.format.TextStyle.SHORT)) },
                selectedContentColor = colors.lineDietText,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DaySummary(day: DietDay) {
    val totals = day.totals
    val kcal = stringResource(R.string.kcal_value, totals.kcal)
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SectionTitle(stringResource(R.string.diet_day_totals, day.dayOfWeek.displayName().lowercase(Today.locale)))
            Row(Modifier.clearAndSetSemantics { contentDescription = kcal }) {
                Text(totals.kcal.toString(), Modifier.alignByBaseline(), style = MaterialTheme.typography.displaySmall)
                Text(
                    stringResource(R.string.label_kcal),
                    Modifier.alignByBaseline().padding(start = Spacing.s),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            totals.macroShares()?.let { MacroBar(it) }
            DataStrip(macroCells(totals, legend = true))
        }
    }
}

/** Comidas del día en una tarjeta: filas regladas con el tipo, el plato y las kcal en la misma línea base. */
@Composable
private fun MealsCard(meals: List<Meal>, onOpen: (Meal) -> Unit, entrance: State<Float>, modifier: Modifier = Modifier) {
    val labelStyle = MaterialTheme.typography.labelMedium
    val labels = meals.map { stringResource(it.type.labelRes()) }
    // Con fuente muy grande el tipo va encima del plato: en columna le quitaría el ancho al nombre.
    val labelWidth = rememberMaxWidth(labels, labelStyle).takeIf { LocalDensity.current.fontScale < 1.5f }
    AppCard(modifier.fillMaxWidth()) {
        Column {
            meals.forEachIndexed { index, meal ->
                key(meal.id) {
                    Column(Modifier.entrance(entrance, index)) {
                        if (index > 0) {
                            HorizontalDivider(
                                Modifier.padding(horizontal = Spacing.m),
                                thickness = Spacing.hairline,
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        MealRow(meal, labels[index], labelWidth, onClick = { onOpen(meal) })
                    }
                }
            }
        }
    }
}

/** Fila tocable de una comida; sin [labelWidth] el tipo va en su propia línea. */
@Composable
private fun MealRow(meal: Meal, label: String, labelWidth: Dp?, onClick: () -> Unit) {
    val labelColor = AppTheme.colors.lineDietText
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.routeRowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.m, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterVertically),
    ) {
        if (labelWidth == null) Text(label, style = MaterialTheme.typography.labelMedium, color = labelColor)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            if (labelWidth != null) {
                Text(label, Modifier.width(labelWidth).alignByBaseline(), style = MaterialTheme.typography.labelMedium, color = labelColor)
            }
            Text(meal.recipe.name, Modifier.weight(1f).alignByBaseline(), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.kcal_value, meal.recipe.perServing.kcal),
                Modifier.alignByBaseline(),
                style = numberStyle(MaterialTheme.typography.titleMedium),
            )
        }
    }
}

/** Ancho del texto más ancho, para alinear una columna sin recortar con fuente grande. */
@Composable
private fun rememberMaxWidth(texts: List<String>, style: TextStyle): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(texts, style, density) {
        with(density) { (texts.maxOfOrNull { measurer.measure(it, style).size.width } ?: 0).toDp() }
    }
}

@Composable
fun RecipeContent(recipe: Recipe) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            DataStrip(
                listOf(
                    DataCell(stringResource(R.string.label_time), recipe.prepMinutes.toString(), stringResource(R.string.unit_minutes)),
                    DataCell(stringResource(R.string.label_kcal), recipe.perServing.kcal.toString()),
                ),
            )
            DataStrip(macroCells(recipe.perServing, legend = false))
            Text(stringResource(R.string.recipe_per_serving), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SectionTitle(stringResource(R.string.recipe_ingredients))
        val amounts = recipe.ingredients.map {
            stringResource(if (it.unit == MeasureUnit.G) R.string.value_grams else R.string.value_ml, formatNumber(it.amount))
        }
        val amountStyle = numberStyle(MaterialTheme.typography.titleMedium)
        val amountWidth = rememberMaxWidth(amounts, amountStyle)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                Row(
                    Modifier.semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Box(Modifier.size(Spacing.s).border(Spacing.hairline * 2, colors.lineDiet, CircleShape))
                    Text(amounts[index], Modifier.width(amountWidth), style = amountStyle, textAlign = TextAlign.End)
                    Text(ingredient.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        SectionTitle(stringResource(R.string.recipe_steps))
        val numbers = recipe.steps.indices.map { (it + 1).toString() }
        val numberWidth = rememberMaxWidth(numbers, MaterialTheme.typography.headlineSmall)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            recipe.steps.forEachIndexed { index, step ->
                Row(Modifier.semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        numbers[index],
                        Modifier.width(numberWidth).alignByBaseline(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.lineDietText,
                    )
                    Text(step, Modifier.weight(1f).alignByBaseline(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
