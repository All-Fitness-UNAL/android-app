package com.allfitness.app.nutrition

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
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
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.navigation.RecipeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
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
        topBar = { AppTopBar(stringResource(R.string.diet_title)) },
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
            Column {
                AiNote()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            items(DayOfWeek.entries) { d ->
                                FilterChip(
                                    selected = d == selected,
                                    onClick = { viewModel.select(d) },
                                    label = { Text(d.displayName(TextStyle.SHORT)) },
                                )
                            }
                        }
                    }
                    item {
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(stringResource(R.string.diet_day_totals, day.dayOfWeek.displayName()), style = MaterialTheme.typography.titleMedium)
                                NutritionSummary(day.totals)
                            }
                        }
                    }
                    items(day.meals, key = { it.id }) { meal ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { onOpenMeal(day.dayOfWeek, meal.id) }) {
                            Row(Modifier.padding(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(stringResource(meal.type.labelRes()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    Text(meal.recipe.name, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(stringResource(R.string.kcal_value, meal.recipe.perServing.kcal), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
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
        topBar = { AppTopBar(title, onBack) },
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!online) OfflineHint()
                }
            }
        }
    }
    AlternativeHost(alternatives, online)
}

/** Valores tal como los envía el servidor (FR-017). */
@Composable
fun NutritionSummary(nutrition: Nutrition) {
    Text(stringResource(R.string.kcal_value, nutrition.kcal), style = MaterialTheme.typography.titleLarge)
    Text(
        stringResource(R.string.macros_value, formatNumber(nutrition.proteinG), formatNumber(nutrition.carbsG), formatNumber(nutrition.fatG)),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun RecipeContent(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(stringResource(R.string.recipe_prep_time, recipe.prepMinutes), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.recipe_per_serving), style = MaterialTheme.typography.titleSmall)
        NutritionSummary(recipe.perServing)
        Text(stringResource(R.string.recipe_ingredients), style = MaterialTheme.typography.titleSmall)
        recipe.ingredients.forEach { ingredient ->
            Text(
                stringResource(
                    if (ingredient.unit == MeasureUnit.G) R.string.ingredient_grams else R.string.ingredient_ml,
                    formatNumber(ingredient.amount),
                    ingredient.name,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(stringResource(R.string.recipe_steps), style = MaterialTheme.typography.titleSmall)
        recipe.steps.forEachIndexed { index, step ->
            Text(stringResource(R.string.recipe_step, index + 1, step), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
