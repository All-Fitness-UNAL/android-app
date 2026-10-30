package com.allfitness.app.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.allfitness.app.R
import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.session.SessionManager
import com.allfitness.app.core.session.SessionState
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.AiBadge
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.Avatar
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.EmptyState
import com.allfitness.app.core.ui.ErrorState
import com.allfitness.app.core.ui.LoadingState
import com.allfitness.app.core.ui.OfflineBanner
import com.allfitness.app.core.ui.PrimaryButton
import com.allfitness.app.core.ui.ProgressRing
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.SectionTitle
import com.allfitness.app.core.ui.SegmentedProgress
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.numberStyle
import com.allfitness.app.core.ui.sessionFraction
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.PillShape
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.nutrition.DietDay
import com.allfitness.app.nutrition.DietRepository
import com.allfitness.app.nutrition.MacroBar
import com.allfitness.app.nutrition.MealCard
import com.allfitness.app.nutrition.dietDayFor
import com.allfitness.app.nutrition.macroCells
import com.allfitness.app.nutrition.macroShares
import com.allfitness.app.onboarding.Level
import com.allfitness.app.onboarding.Profile
import com.allfitness.app.onboarding.ProfileRepository
import com.allfitness.app.onboarding.labelRes
import com.allfitness.app.progress.ProgressRepository
import com.allfitness.app.progress.TrainingSummary
import com.allfitness.app.routines.DayType
import com.allfitness.app.routines.RoutineRepository
import com.allfitness.app.routines.WeekDayRow
import com.allfitness.app.routines.WeekStrip
import com.allfitness.app.routines.WorkoutCta
import com.allfitness.app.routines.dayTitle
import com.allfitness.app.routines.firstPendingIndex
import com.allfitness.app.routines.weekRows
import com.allfitness.app.routines.workoutCta
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private fun SessionState.userName(): String = (this as? SessionState.Active)?.session?.user?.name.orEmpty()

/** Panel del día (FR-035a): solo lee; marcar y terminar la sesión siguen en la pantalla del día. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    routines: RoutineRepository,
    diets: DietRepository,
    progress: ProgressRepository,
    profiles: ProfileRepository,
    session: SessionManager,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline

    // Uno por repositorio: si una carga falla, las otras secciones siguen a la vista (FR-036).
    private val routineRefresher = Refresher(viewModelScope, isOnline) { routines.refresh() }
    private val dietRefresher = Refresher(viewModelScope, isOnline) { diets.refresh() }
    private val progressRefresher = Refresher(viewModelScope, isOnline) { progress.refresh() }
    private val profileRefresher = Refresher(viewModelScope, isOnline) { profiles.refresh() }
    private val today = MutableStateFlow(Today.date())

    val week: StateFlow<UiState<List<WeekDayRow>>> = combine(
        routines.routine, routines.weekLogs, routineRefresher.state, isOnline, today,
    ) { routine, logs, refresh, online, today ->
        uiStateOf(routine, refresh, online).mapContent { weekRows(it, logs?.data.orEmpty(), today) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Una dieta guardada sin el día de hoy cuenta como "nada guardado": vacío o error según la recarga. */
    val diet: StateFlow<UiState<DietDay>> = combine(diets.diet, dietRefresher.state, isOnline, today) { diet, refresh, online, today ->
        uiStateOf(diet?.let { cached -> dietDayFor(cached.data, today)?.let { Cached(it, cached.savedAt) } }, refresh, online)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val summary: StateFlow<TrainingSummary?> = progress.summary.map { it?.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val profile: StateFlow<Profile?> = profiles.profile.map { it?.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val userName: StateFlow<String> = session.state.map { it.userName() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), session.state.value.userName())

    /** Al volver a primer plano o al reintentar: puede haber cambiado el día o los datos en el servidor. */
    fun onResume() {
        today.value = Today.date()
        listOf(routineRefresher, dietRefresher, progressRefresher, profileRefresher).forEach(Refresher::run)
    }
}

@Composable
fun HomeScreen(
    onOpenSession: (LocalDate, Int) -> Unit,
    onOpenMeal: (DayOfWeek, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val week by viewModel.week.collectAsStateWithLifecycle()
    val diet by viewModel.diet.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

    val todayRow = (week as? UiState.Content)?.data?.first { it.isToday }
    val edge = Modifier.padding(horizontal = Spacing.m)
    // El aviso sin conexión va fijo sobre la lista, como en StateContent: siempre a la vista y sin mover el scroll.
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
        if (!online) OfflineBanner(savedAt = null)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item { Greeting(name, edge.padding(top = Spacing.s)) }
            item { AiNote() }
            item { Indicators(todayRow, summary, (diet as? UiState.Content)?.data?.totals?.kcal, edge) }
            item {
                Section(stringResource(R.string.home_today_workout), edge) {
                    SectionState(week, stringResource(R.string.routine_empty), stringResource(R.string.routine_empty_offline), viewModel::onResume) {
                        WorkoutCard(it.first { row -> row.isToday }, profile, onOpenSession)
                    }
                }
            }
            item { DietSection(diet, onOpenMeal, viewModel::onResume, edge) }
            item {
                Section(stringResource(R.string.progress_title), edge, trailing = { summary?.let { StreakBadge(it.weekStreak) } }) {
                    SectionState(week, stringResource(R.string.routine_empty), stringResource(R.string.routine_empty_offline), viewModel::onResume) { rows ->
                        AppCard(Modifier.fillMaxWidth()) { WeekStrip(rows, selected = null, onSelect = null, Modifier.padding(Spacing.s)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Greeting(name: String, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Avatar(name, Spacing.avatarSmall)
        Text(
            stringResource(R.string.home_greeting, name),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

/** Título de sección con [trailing] a la derecha; con fuente grande [trailing] baja a otra línea en vez de aplastar el título. */
@Composable
private fun Section(title: String, modifier: Modifier, trailing: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(title)
            trailing()
        }
        content()
    }
}

/** Cargando, vacío y error de una sección (FR-036): si una falla, las demás siguen a la vista. */
@Composable
private fun <T> SectionState(state: UiState<T>, emptyMessage: String, offlineEmptyMessage: String, onRetry: () -> Unit, content: @Composable (T) -> Unit) {
    when (state) {
        is UiState.Content -> content(state.data)
        UiState.Loading -> AppCard(Modifier.fillMaxWidth()) { LoadingState() }
        is UiState.Error -> AppCard(Modifier.fillMaxWidth()) { ErrorState(state.error, onRetry) }
        is UiState.Empty -> AppCard(Modifier.fillMaxWidth()) { EmptyState(if (state.offline) offlineEmptyMessage else emptyMessage) }
    }
}

/** Fila de 4 indicadores (Figma `progress-row`). Lo que aún no cargó se ve "–", nunca 0. */
@Composable
private fun Indicators(today: WeekDayRow?, summary: TrainingSummary?, kcal: Int?, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = AppTheme.colors
    AppCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = Spacing.m, horizontal = Spacing.s)) {
            val cell = Modifier.weight(1f)
            RingIndicator(stringResource(R.string.today), today?.doneCount, today?.day?.exercises?.size, R.drawable.lc_dumbbell, cell)
            RingIndicator(
                stringResource(R.string.stat_week_sessions),
                summary?.completedSessions,
                summary?.scheduledSessions,
                R.drawable.lc_calendar_check,
                cell,
            )
            Indicator(
                stringResource(R.string.label_streak),
                summary?.weekStreak?.toString(),
                summary?.let { pluralStringResource(R.plurals.streak_weeks, it.weekStreak, it.weekStreak) },
                cell,
            ) { Disc(R.drawable.lc_flame, colors.accentContainer, colors.accent) }
            Indicator(
                stringResource(R.string.stat_plan_kcal),
                kcal?.toString(),
                kcal?.let { "${stringResource(R.string.stat_plan_kcal)}, ${stringResource(R.string.kcal_value, it)}" },
                cell,
            ) { Disc(R.drawable.lc_utensils, scheme.surfaceContainerHigh, scheme.onSurface) }
        }
    }
}

/** Anillo N de M; TalkBack lee "Hoy, 2 de 5" en vez de un porcentaje. */
@Composable
private fun RingIndicator(label: String, done: Int?, total: Int?, @DrawableRes icon: Int, modifier: Modifier) {
    val known = done != null && total != null
    Indicator(
        label,
        if (known) stringResource(R.string.value_fraction, done, total) else null,
        if (known) "$label, ${stringResource(R.string.session_progress, done, total)}" else null,
        modifier,
    ) {
        ProgressRing(sessionFraction(done ?: 0, total ?: 0), Spacing.ring) {
            Icon(painterResource(icon), contentDescription = null, Modifier.size(Spacing.ring / 3), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun Indicator(label: String, value: String?, description: String?, modifier: Modifier, medallion: @Composable () -> Unit) {
    // Sin dato (cargando, vacío o error) solo se lee el rótulo: "Cargando" sería falso con la dieta vacía.
    val spoken = description ?: label
    Column(
        modifier.clearAndSetSemantics { contentDescription = spoken },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        medallion()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value ?: stringResource(R.string.value_missing), style = numberStyle(MaterialTheme.typography.titleMedium))
            // labelSmall va en mayúsculas (Type.kt).
            Text(
                label.uppercase(Today.locale),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Disc(@DrawableRes icon: Int, background: Color, tint: Color) {
    Box(Modifier.size(Spacing.ring).background(background, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = null, Modifier.size(Spacing.ring / 3), tint = tint)
    }
}

/** Tarjeta del entreno de hoy (Figma `workout-card`). Sin conexión se puede abrir, pero marcar pide red (FR-039). */
@Composable
private fun WorkoutCard(today: WeekDayRow, profile: Profile?, onOpenSession: (LocalDate, Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val training = today.day.type == DayType.TRAINING
    val count = today.day.exercises.size
    val exercises = pluralStringResource(R.plurals.exercises_count, count, count)
    val meta = when {
        !training -> stringResource(R.string.rest_day_message)
        profile != null -> stringResource(R.string.workout_meta, profile.sessionMinutes, exercises)
        else -> exercises
    }
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    AiBadge()
                    Text(today.dayTitle(), style = MaterialTheme.typography.headlineSmall)
                    Text(meta, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    if (training && profile != null) LevelRow(profile.level)
                }
                today.day.exercises.firstOrNull()?.imageUrl?.let {
                    AsyncImage(
                        model = it,
                        // Junto al título del día: decorativa.
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(Spacing.thumbExercise).clip(MaterialTheme.shapes.medium),
                    )
                }
            }
            when (val cta = today.workoutCta()) {
                WorkoutCta.START, WorkoutCta.CONTINUE -> PrimaryButton(
                    text = stringResource(if (cta == WorkoutCta.START) R.string.action_start_workout else R.string.action_continue_workout),
                    onClick = { onOpenSession(today.date, today.firstPendingIndex()) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = R.drawable.lc_circle_play,
                )
                WorkoutCta.FINISHED -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    Icon(painterResource(R.drawable.lc_check), contentDescription = null, Modifier.size(Spacing.m), tint = scheme.primary)
                    Text(stringResource(R.string.session_finished_message), style = MaterialTheme.typography.bodyLarge)
                }
                WorkoutCta.NONE -> Unit
            }
        }
    }
}

/** Nivel del perfil en 3 segmentos (Figma `difficulty-row`), con el nombre escrito: no solo por color. */
@Composable
private fun LevelRow(level: Level) {
    val label = stringResource(R.string.label_level)
    val name = stringResource(level.labelRes())
    val style = MaterialTheme.typography.labelMedium
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.clearAndSetSemantics { contentDescription = "$label, $name" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(label, style = style, color = color)
        SegmentedProgress(Level.entries.size, level.ordinal + 1, Modifier.width(Spacing.levelMeter))
        Text(name, style = style, color = color)
    }
}

/** Kcal y macros del plan de hoy y sus comidas, que abren la receta. Sin consumo ni agua (spec §3). */
@Composable
private fun DietSection(state: UiState<DietDay>, onOpenMeal: (DayOfWeek, String) -> Unit, onRetry: () -> Unit, modifier: Modifier) {
    val kcal = (state as? UiState.Content)?.data?.totals?.kcal
    Section(
        stringResource(R.string.home_today_diet),
        modifier,
        trailing = {
            if (kcal != null) {
                Text(
                    stringResource(R.string.kcal_value, kcal),
                    style = numberStyle(MaterialTheme.typography.titleSmall),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        SectionState(state, stringResource(R.string.diet_empty), stringResource(R.string.diet_empty_offline), onRetry) { day ->
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                day.totals.macroShares()?.let { shares ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            MacroBar(shares)
                            DataStrip(macroCells(day.totals, legend = true))
                        }
                    }
                }
                day.meals.forEach { meal -> MealCard(meal, onClick = { onOpenMeal(day.dayOfWeek, meal.id) }) }
            }
        }
    }
}

/** Racha en ámbar sobre su fondo al 12 % (Figma `streak-badge`); el ámbar solo se usa aquí. */
@Composable
private fun StreakBadge(weeks: Int) {
    val colors = AppTheme.colors
    Row(
        Modifier.background(colors.accentContainer, PillShape).padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(painterResource(R.drawable.lc_flame), contentDescription = null, Modifier.size(Spacing.sm), tint = colors.accent)
        Text(pluralStringResource(R.plurals.streak_weeks, weeks, weeks), style = MaterialTheme.typography.labelMedium, color = colors.accent)
    }
}
