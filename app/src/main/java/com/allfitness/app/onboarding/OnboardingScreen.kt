package com.allfitness.app.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allfitness.app.R
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.session.SessionManager
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.AiGeneratingState
import com.allfitness.app.core.ui.EmptyState
import com.allfitness.app.core.ui.LoadingState
import com.allfitness.app.core.ui.LogoMark
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.PrimaryButton
import com.allfitness.app.core.ui.SegmentedProgress
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.nutrition.DietRepository
import com.allfitness.app.progress.ProgressRepository
import com.allfitness.app.routines.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val loaded: Boolean = false,
    val step: Int = 0,
    val form: ProfileForm = ProfileForm(),
    val errors: Map<ProfileField, Int> = emptyMap(),
    val submitting: Boolean = false,
    @StringRes val submitError: Int? = null,
) {
    val section get() = ProfileSection.entries[step]
    val isLastStep get() = step == ProfileSection.entries.lastIndex
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val draftStore: OnboardingDraftStore,
    private val profileRepository: ProfileRepository,
    private val progressRepository: ProgressRepository,
    private val routineRepository: RoutineRepository,
    private val dietRepository: DietRepository,
    private val sessionManager: SessionManager,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()
    val isOnline = connectivity.isOnline

    // Pasos ya completados del envío final, para que reintentar no repita lo que salió bien (research R8).
    private var profileSaved = false
    private var weightSaved = false

    init {
        viewModelScope.launch {
            val draft = draftStore.load()
            _state.update {
                it.copy(loaded = true, form = draft?.form ?: it.form, step = draft?.step?.coerceIn(0, 3) ?: 0)
            }
        }
    }

    fun update(form: ProfileForm) {
        profileSaved = false
        weightSaved = false
        _state.update { it.copy(form = form, errors = emptyMap()) }
    }

    fun back() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), errors = emptyMap()) }
        saveDraft()
    }

    fun next() {
        val current = _state.value
        val errors = ProfileValidation.validate(current.section, current.form)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        if (current.isLastStep) {
            finish()
        } else {
            _state.update { it.copy(step = it.step + 1) }
            saveDraft()
        }
    }

    private fun saveDraft() = viewModelScope.launch {
        _state.value.let { draftStore.save(OnboardingDraft(it.form, it.step)) }
    }

    fun finish() {
        if (_state.value.submitting) return
        _state.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            saveDraft().join()
            val form = _state.value.form
            if (!profileSaved) {
                val result = profileRepository.save(form.toProfile())
                if (result is AppResult.Failure) return@launch fail(result.error.messageRes())
                profileSaved = true
            }
            if (!weightSaved) {
                val result = progressRepository.upsertWeight(Today.date(), requireNotNull(form.weight))
                if (result is AppResult.Failure) return@launch fail(result.error.messageRes())
                weightSaved = true
            }
            val (routine, diet) = coroutineScope {
                val routine = async { routineRepository.generate(null) }
                val diet = async { dietRepository.generate(null) }
                routine.await() to diet.await()
            }
            if (routine is AppResult.Failure && diet is AppResult.Failure) {
                return@launch fail(R.string.onboarding_generation_failed)
            }
            // Si solo falló una, su pestaña muestra el estado vacío con "Generar".
            draftStore.clear()
            sessionManager.markOnboardingCompleted()
        }
    }

    private fun fail(@StringRes message: Int) {
        _state.update { it.copy(submitting = false, submitError = message) }
    }
}

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()

    Scaffold { padding ->
        val modifier = Modifier.padding(padding)
        when {
            !state.loaded -> LoadingState(modifier)
            state.submitting -> AiGeneratingState(
                message = stringResource(R.string.onboarding_generating),
                stages = listOf(
                    stringResource(R.string.generating_stage_profile),
                    stringResource(R.string.generating_stage_routine),
                    stringResource(R.string.generating_stage_diet),
                ),
                summary = listOfNotNull(
                    state.form.goal?.let { null to stringResource(it.labelRes()) },
                    state.form.daysPerWeek?.let { stringResource(R.string.label_days) to it.toString() },
                    state.form.equipment.takeIf { it.isNotEmpty() }
                        ?.let { set -> stringResource(R.string.label_equipment) to set.map { stringResource(it.labelRes()) }.joinToString(", ") },
                ),
                modifier = modifier,
            )
            state.submitError != null -> EmptyState(
                message = stringResource(state.submitError!!),
                modifier = modifier,
                actionLabel = stringResource(R.string.action_retry),
                actionEnabled = online,
                onAction = viewModel::finish,
            )
            else -> OnboardingForm(state, online, viewModel, modifier)
        }
    }
}

@Composable
private fun OnboardingForm(state: OnboardingUiState, online: Boolean, viewModel: OnboardingViewModel, modifier: Modifier) {
    val steps = ProfileSection.entries.size
    Column(modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screenEdge, vertical = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Volver va arriba, como en las demás pantallas: abajo queda una sola acción (Figma `bottom-sticky-cta`).
                    if (state.step > 0) {
                        // El 48 dp táctil deja la flecha 12 dp dentro del margen: se desplaza para alinearla con él.
                        IconButton(onClick = viewModel::back, Modifier.offset(x = -Spacing.sm)) {
                            Icon(painterResource(R.drawable.lc_arrow_left), contentDescription = stringResource(R.string.action_back))
                        }
                    }
                    // Con fuente grande el paso baja a otra línea en vez de apretar la marca.
                    FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        LogoMark(Modifier.align(Alignment.CenterVertically))
                        // El texto "Paso N de M" ya lo lee TalkBack; los segmentos lo dibujan.
                        Text(
                            stringResource(R.string.onboarding_step, state.step + 1, steps),
                            Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                SegmentedProgress(total = steps, reached = state.step + 1)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(
                    stringResource(state.section.titleRes()),
                    Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ProfileSectionForm(state.section, state.form, state.errors, viewModel::update)
        }
        Column(
            Modifier.padding(horizontal = Spacing.screenEdge, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            PrimaryButton(
                text = stringResource(if (state.isLastStep) R.string.action_finish else R.string.action_next),
                onClick = viewModel::next,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLastStep || online,
                trailingIcon = R.drawable.lc_arrow_right,
            )
            if (state.isLastStep && !online) OfflineHint()
        }
    }
}
