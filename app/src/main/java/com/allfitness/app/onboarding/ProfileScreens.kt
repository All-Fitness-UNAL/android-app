package com.allfitness.app.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.allfitness.app.R
import com.allfitness.app.auth.AuthRepository
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.session.SessionManager
import com.allfitness.app.core.session.SessionState
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.AiNote
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.DataCell
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.ErrorState
import com.allfitness.app.core.ui.FormTextField
import com.allfitness.app.core.ui.LoadingState
import com.allfitness.app.core.ui.MessageEffect
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.Refresher
import com.allfitness.app.core.ui.StateContent
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UiState
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.mapContent
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.core.ui.uiStateOf
import com.allfitness.app.navigation.ProfileSectionRoute
import com.allfitness.app.nutrition.DietRepository
import com.allfitness.app.progress.ProgressRepository
import com.allfitness.app.routines.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- Perfil

@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    progressRepository: ProgressRepository,
    private val authRepository: AuthRepository,
    sessionManager: SessionManager,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline
    private val refresher = Refresher(viewModelScope, isOnline) {
        profileRepository.refresh().also { progressRepository.refresh() }
    }

    /** El peso actual es siempre el registro más reciente del historial (FR-026a). */
    val state: StateFlow<UiState<Profile>> = combine(
        profileRepository.profile, progressRepository.weights, refresher.state, isOnline,
    ) { profile, weights, refresh, online ->
        uiStateOf(profile, refresh, online).mapContent {
            it.copy(currentWeightKg = weights?.data?.firstOrNull()?.weightKg ?: it.currentWeightKg)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val user = sessionManager.state.map { (it as? SessionState.Active)?.session?.user }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onResume() = refresher.run()

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

@Composable
fun ProfileScreen(
    onEdit: (ProfileSection) -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

    Scaffold(topBar = { AppTopBar(stringResource(R.string.profile_title)) }) { padding ->
        StateContent(
            state = state,
            emptyMessage = stringResource(R.string.profile_empty),
            onRetry = viewModel::onResume,
            modifier = Modifier.padding(padding),
        ) { profile ->
            Column {
                AiNote()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    item { ProfileHeader(user?.name.orEmpty(), user?.email.orEmpty()) }
                    items(ProfileSection.entries) { section ->
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                val title = stringResource(section.titleRes())
                                // Cuatro botones "Editar": cada uno dice qué sección abre.
                                val editLabel = stringResource(R.string.action_edit_section, title)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                                    Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                                    TonalActionButton(
                                        text = stringResource(R.string.action_edit),
                                        onClick = { onEdit(section) },
                                        modifier = Modifier.semantics { contentDescription = editLabel },
                                        enabled = online,
                                        icon = R.drawable.ic_edit,
                                    )
                                }
                                SectionSummary(section, profile)
                            }
                        }
                    }
                    if (!online) item { OfflineHint() }
                    item {
                        Column(Modifier.padding(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            TonalActionButton(
                                text = stringResource(R.string.action_logout),
                                onClick = { confirmLogout = true },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedButton(
                                onClick = onDeleteAccount,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(Spacing.hairline, MaterialTheme.colorScheme.error),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) { Text(stringResource(R.string.action_delete_account)) }
                        }
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    viewModel.logout()
                }) { Text(stringResource(R.string.action_logout)) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

/** Inicial en un círculo Señal, nombre y correo (el correo no se edita aquí). */
@Composable
private fun ProfileHeader(name: String, email: String) {
    val colors = AppTheme.colors
    Row(Modifier.padding(bottom = Spacing.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Box(
            Modifier.size(Spacing.avatar).background(colors.signal, CircleShape).border(Spacing.hairline, colors.signalOutline, CircleShape).clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Text(name.trim().take(1).uppercase(Today.locale), style = MaterialTheme.typography.titleLarge, color = colors.onSignal)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(name, style = MaterialTheme.typography.headlineSmall)
            Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.profile_email_readonly),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Cifras en una DataStrip y datos de texto como etiqueta/valor; mismos datos que antes, sin "Etiqueta: valor". */
@Composable
private fun SectionSummary(section: ProfileSection, profile: Profile) {
    @Composable
    fun Enum<*>.text() = stringResource(labelRes())
    @Composable
    fun label(@StringRes id: Int) = stringResource(id)
    val (numbers, facts) = when (section) {
        ProfileSection.PHYSICAL -> listOfNotNull(
            DataCell(label(R.string.label_age), profile.age.toString(), label(R.string.unit_years)),
            DataCell(label(R.string.label_height), profile.heightCm.toString(), label(R.string.unit_cm)),
            profile.currentWeightKg?.let { DataCell(label(R.string.label_weight), formatNumber(it), label(R.string.unit_kg)) },
        ) to listOf(label(R.string.label_sex) to profile.sex.text())
        // El título de la tarjeta ya dice "Objetivo".
        ProfileSection.GOAL -> emptyList<DataCell>() to listOf(null to profile.goal.text())
        ProfileSection.TRAINING -> listOf(
            DataCell(label(R.string.label_days), profile.daysPerWeek.toString()),
            DataCell(label(R.string.label_session), profile.sessionMinutes.toString(), label(R.string.unit_minutes)),
        ) to listOf(
            label(R.string.label_level) to profile.level.text(),
            label(R.string.label_location) to profile.location.text(),
            label(R.string.label_equipment) to profile.equipment.map { it.text() }.joinToString(", "),
            label(R.string.label_injuries) to profile.injuries?.takeIf { it.isNotBlank() },
        )
        ProfileSection.NUTRITION -> listOf(
            DataCell(label(R.string.label_meals), profile.mealsPerDay.toString()),
        ) to listOf(
            label(R.string.label_diet) to profile.dietType.text(),
            label(R.string.label_allergies) to profile.allergies.filter { it != Allergy.OTHER }.map { it.text() }
                .plus(listOfNotNull(profile.otherAllergy?.takeIf { it.isNotBlank() }))
                .takeIf { it.isNotEmpty() }?.joinToString(", "),
            label(R.string.label_disliked) to profile.dislikedFoods?.takeIf { it.isNotBlank() },
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (numbers.isNotEmpty()) DataStrip(numbers)
        facts.forEach { (label, value) -> if (value != null) Fact(label, value) }
    }
}

@Composable
private fun Fact(label: String?, value: String) {
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        if (label != null) Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

// ---------------------------------------------------------------- Editar una sección

data class ProfileEditState(
    val loaded: Boolean = false,
    val missing: Boolean = false,
    val original: ProfileForm = ProfileForm(),
    val form: ProfileForm = ProfileForm(),
    val errors: Map<ProfileField, Int> = emptyMap(),
    val saving: Boolean = false,
    val askRegenerate: Boolean = false,
    val regenerating: Boolean = false,
    val done: Boolean = false,
) {
    val hasUnsavedChanges get() = form != original
}

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val progressRepository: ProgressRepository,
    private val routineRepository: RoutineRepository,
    private val dietRepository: DietRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val section = ProfileSection.valueOf(savedStateHandle.toRoute<ProfileSectionRoute>().section)
    val isOnline = connectivity.isOnline
    private val _state = MutableStateFlow(ProfileEditState())
    val state: StateFlow<ProfileEditState> = _state.asStateFlow()
    private val _message = MutableStateFlow<UserMessage?>(null)
    val message: StateFlow<UserMessage?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.profile.first()?.data
            val weight = progressRepository.weights.first()?.data?.firstOrNull()?.weightKg ?: profile?.currentWeightKg
            val form = profile?.toForm(weight)
            _state.value = ProfileEditState(loaded = true, missing = form == null, original = form ?: ProfileForm(), form = form ?: ProfileForm())
        }
    }

    fun update(form: ProfileForm) = _state.update { it.copy(form = form, errors = emptyMap()) }

    fun save() {
        val s = _state.value
        if (s.saving) return
        val errors = ProfileValidation.validate(section, s.form)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            // Si falla, los datos editados se quedan en pantalla (FR-031).
            val saved = profileRepository.save(s.form.toProfile())
            if (saved is AppResult.Failure) return@launch fail(saved.error, ::save)
            // Editar el peso crea o actualiza el registro de hoy (FR-029).
            if (section == ProfileSection.PHYSICAL && s.form.weight != s.original.weight) {
                val weight = progressRepository.upsertWeight(Today.date(), requireNotNull(s.form.weight))
                if (weight is AppResult.Failure) return@launch fail(weight.error, ::save)
            }
            _state.update { it.copy(saving = false, original = s.form, askRegenerate = true) }
        }
    }

    /** No se regenera nada automáticamente: el usuario elige (FR-030). */
    fun regenerate(routine: Boolean, diet: Boolean) {
        _state.update { it.copy(askRegenerate = false) }
        if (!routine && !diet) {
            _state.update { it.copy(done = true) }
            return
        }
        _state.update { it.copy(regenerating = true) }
        viewModelScope.launch {
            val results = coroutineScope {
                listOfNotNull(
                    if (routine) async { routineRepository.generate(null) } else null,
                    if (diet) async { dietRepository.generate(null) } else null,
                ).map { it.await() }
            }
            val failure = results.firstOrNull { it is AppResult.Failure } as AppResult.Failure?
            if (failure != null) {
                _state.update { it.copy(regenerating = false) }
                _message.value = UserMessage(failure.error.messageRes(), retry = { regenerate(routine, diet) })
            } else {
                _state.update { it.copy(regenerating = false, done = true) }
            }
        }
    }

    fun messageShown() {
        _message.value = null
    }

    private fun fail(error: AppError, retry: () -> Unit) {
        _state.update { it.copy(saving = false) }
        _message.value = UserMessage(error.messageRes(), retry)
    }
}

@Composable
fun ProfileEditScreen(onDone: () -> Unit, viewModel: ProfileEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmDiscard by remember { mutableStateOf(false) }
    val attemptBack = { if (state.hasUnsavedChanges && !state.saving) confirmDiscard = true else if (!state.saving) onDone() }

    MessageEffect(message, snackbar, viewModel::messageShown)
    LaunchedEffect(state.done) { if (state.done) onDone() }
    BackHandler(enabled = state.hasUnsavedChanges || state.saving || state.regenerating) {
        if (!state.saving && !state.regenerating) confirmDiscard = true
    }

    Scaffold(
        topBar = { AppTopBar(stringResource(viewModel.section.titleRes()), onBack = attemptBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            !state.loaded -> LoadingState(modifier)
            state.missing -> ErrorState(AppError.NotFound, onRetry = onDone, modifier = modifier)
            state.regenerating -> AiGeneratingState(stringResource(R.string.generating_plan), modifier)
            else -> Column(
                modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                ProfileSectionForm(viewModel.section, state.form, state.errors, viewModel::update)
                if (!online) OfflineHint()
                Button(onClick = viewModel::save, enabled = online && !state.saving, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text(stringResource(if (state.saving) R.string.loading else R.string.action_save))
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onDone()
                }) { Text(stringResource(R.string.action_discard)) }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.action_keep_editing)) } },
        )
    }

    if (state.askRegenerate) {
        AlertDialog(
            onDismissRequest = { viewModel.regenerate(routine = false, diet = false) },
            title = { Text(stringResource(R.string.regenerate_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.regenerate_message))
                    RegenerateOption(R.string.regenerate_routine, online) { viewModel.regenerate(routine = true, diet = false) }
                    RegenerateOption(R.string.regenerate_diet, online) { viewModel.regenerate(routine = false, diet = true) }
                    RegenerateOption(R.string.regenerate_both, online) { viewModel.regenerate(routine = true, diet = true) }
                    RegenerateOption(R.string.regenerate_none, true) { viewModel.regenerate(routine = false, diet = false) }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun RegenerateOption(@StringRes label: Int, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) }
}

// ---------------------------------------------------------------- Eliminar cuenta

const val DELETE_CONFIRMATION = "ELIMINAR"

data class DeleteAccountState(val text: String = "", val deleting: Boolean = false, @StringRes val error: Int? = null)

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    val isOnline = connectivity.isOnline
    private val _state = MutableStateFlow(DeleteAccountState())
    val state: StateFlow<DeleteAccountState> = _state.asStateFlow()

    fun onText(text: String) = _state.update { it.copy(text = text, error = null) }

    /** Solo con la palabra exacta; los datos locales se borran cuando el servidor confirma (FR-034). */
    fun delete() {
        val s = _state.value
        if (s.deleting || s.text != DELETE_CONFIRMATION) return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result is AppResult.Failure) _state.update { it.copy(deleting = false, error = result.error.messageRes()) }
        }
    }
}

@Composable
fun DeleteAccountScreen(onBack: () -> Unit, viewModel: DeleteAccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppTopBar(stringResource(R.string.delete_title), onBack) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text(stringResource(R.string.delete_message), style = MaterialTheme.typography.bodyLarge)
            FormTextField(
                value = state.text,
                onValueChange = viewModel::onText,
                label = stringResource(R.string.delete_field, DELETE_CONFIRMATION),
                modifier = Modifier.fillMaxWidth(),
            )
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            if (!online) OfflineHint()
            val error = MaterialTheme.colorScheme.error
            Button(
                onClick = viewModel::delete,
                enabled = online && !state.deleting && state.text == DELETE_CONFIRMATION,
                shape = MaterialTheme.shapes.small,
                // Deshabilitado sigue en rojo (al 38 %): se ve qué tipo de acción es antes de escribir la palabra.
                colors = ButtonDefaults.buttonColors(
                    containerColor = error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = error.copy(alpha = 0.12f),
                    disabledContentColor = error.copy(alpha = 0.38f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(if (state.deleting) R.string.loading else R.string.action_delete_my_account)) }
        }
    }
}
