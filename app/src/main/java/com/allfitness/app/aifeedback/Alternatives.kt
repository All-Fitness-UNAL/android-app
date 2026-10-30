package com.allfitness.app.aifeedback

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allfitness.app.R
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.ui.FREE_TEXT_MAX
import com.allfitness.app.core.ui.FormTextField
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.TonalActionButton
import com.allfitness.app.core.ui.UserMessage
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.nutrition.DietRepository
import com.allfitness.app.nutrition.RecipeContent
import com.allfitness.app.nutrition.RecipeProposal
import com.allfitness.app.routines.ExerciseItem
import com.allfitness.app.routines.ExerciseProposal
import com.allfitness.app.routines.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---- Modelos (data-model.md, sección Alternativas) ----

sealed interface AlternativeTarget {
    data class Exercise(val exerciseId: String) : AlternativeTarget
    data class Recipe(val mealId: String) : AlternativeTarget
    data object WholeRoutine : AlternativeTarget
    data object WholeDiet : AlternativeTarget

    val isWholePlan get() = this == WholeRoutine || this == WholeDiet
}

enum class ExerciseReason(@StringRes val label: Int) {
    DISLIKE(R.string.reason_dislike),
    NO_EQUIPMENT(R.string.reason_no_equipment),
    PAIN(R.string.reason_pain),
    TOO_HARD(R.string.reason_too_hard),
    TOO_EASY(R.string.reason_too_easy),
}

enum class RecipeReason(@StringRes val label: Int) {
    DISLIKE(R.string.reason_dislike),
    NO_INGREDIENTS(R.string.reason_no_ingredients),
    TOO_ELABORATE(R.string.reason_too_elaborate),
    DIET_MISMATCH(R.string.reason_diet_mismatch),
}

sealed interface Proposal {
    data class ForExercise(val value: ExerciseProposal) : Proposal
    data class ForRecipe(val value: RecipeProposal) : Proposal
}

/** Idle → Loading → Proposal → Accepting → (cierra); Failed conserva el contenido y permite reintentar. */
sealed interface AlternativePhase {
    data object Idle : AlternativePhase
    data object Loading : AlternativePhase
    data class Failed(@StringRes val message: Int) : AlternativePhase
    data class Proposed(val proposal: Proposal, @StringRes val error: Int? = null) : AlternativePhase
    data class Accepting(val proposal: Proposal) : AlternativePhase
}

data class AlternativeUiState(
    val target: AlternativeTarget? = null,
    val exerciseReason: ExerciseReason? = null,
    val recipeReason: RecipeReason? = null,
    val comment: String = "",
    val phase: AlternativePhase = AlternativePhase.Idle,
    val confirmReplace: Boolean = false,
    val reasonMissing: Boolean = false,
) {
    val busy get() = phase == AlternativePhase.Loading || phase is AlternativePhase.Accepting
}

/**
 * Una instancia por pantalla: al salir de la pantalla se descarta cualquier propuesta pendiente.
 * El contenido actual solo cambia cuando el servidor confirma (FR-020, FR-022, FR-022a).
 */
@HiltViewModel
class AlternativeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val dietRepository: DietRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AlternativeUiState())
    val state: StateFlow<AlternativeUiState> = _state.asStateFlow()
    private val _doneMessage = MutableStateFlow<UserMessage?>(null)
    val doneMessage: StateFlow<UserMessage?> = _doneMessage.asStateFlow()

    fun open(target: AlternativeTarget) {
        _state.value = AlternativeUiState(target = target)
    }

    /** "Mantener la actual" o cerrar la hoja: descarta la propuesta (el servidor la expira solo). */
    fun dismiss() {
        if (!_state.value.busy) _state.value = AlternativeUiState()
    }

    fun onExerciseReason(reason: ExerciseReason) = _state.update { it.copy(exerciseReason = reason, reasonMissing = false) }
    fun onRecipeReason(reason: RecipeReason) = _state.update { it.copy(recipeReason = reason, reasonMissing = false) }
    fun onComment(text: String) = _state.update { it.copy(comment = text.take(FREE_TEXT_MAX)) }

    fun submit() {
        val s = _state.value
        val target = s.target ?: return
        if (s.busy) return
        val missingReason = (target is AlternativeTarget.Exercise && s.exerciseReason == null) ||
            (target is AlternativeTarget.Recipe && s.recipeReason == null)
        when {
            missingReason -> _state.update { it.copy(reasonMissing = true) }
            target.isWholePlan -> _state.update { it.copy(confirmReplace = true) } // FR-023
            else -> send()
        }
    }

    fun confirmReplace() {
        _state.update { it.copy(confirmReplace = false) }
        send()
    }

    fun cancelReplace() = _state.update { it.copy(confirmReplace = false) }

    private fun send() {
        val s = _state.value
        val target = s.target ?: return
        val comment = s.comment.trim().ifBlank { null }
        _state.update { it.copy(phase = AlternativePhase.Loading) }
        viewModelScope.launch {
            when (target) {
                is AlternativeTarget.Exercise -> routineRepository
                    .requestAlternative(target.exerciseId, requireNotNull(s.exerciseReason).name, comment)
                    .let { result -> propose(result) { Proposal.ForExercise(it) } }
                is AlternativeTarget.Recipe -> dietRepository
                    .requestAlternative(target.mealId, requireNotNull(s.recipeReason).name, comment)
                    .let { result -> propose(result) { Proposal.ForRecipe(it) } }
                AlternativeTarget.WholeRoutine -> finish(routineRepository.generate(comment), R.string.alt_done_routine)
                AlternativeTarget.WholeDiet -> finish(dietRepository.generate(comment), R.string.alt_done_diet)
            }
        }
    }

    fun accept() {
        val phase = _state.value.phase as? AlternativePhase.Proposed ?: return
        val target = _state.value.target ?: return
        _state.update { it.copy(phase = AlternativePhase.Accepting(phase.proposal)) }
        viewModelScope.launch {
            val result = when (val proposal = phase.proposal) {
                is Proposal.ForExercise -> routineRepository.acceptAlternative(
                    (target as AlternativeTarget.Exercise).exerciseId, proposal.value.proposalId,
                )
                is Proposal.ForRecipe -> dietRepository.acceptAlternative(
                    (target as AlternativeTarget.Recipe).mealId, proposal.value.proposalId,
                )
            }
            when {
                result is AppResult.Success -> done(R.string.alt_done_element)
                result.error() == AppError.ProposalExpired ->
                    _state.update { it.copy(phase = AlternativePhase.Failed(R.string.error_proposal_expired)) }
                else -> _state.update { it.copy(phase = AlternativePhase.Proposed(phase.proposal, result.error()?.messageRes())) }
            }
        }
    }

    fun doneShown() {
        _doneMessage.value = null
    }

    private fun <T> propose(result: AppResult<T>, wrap: (T) -> Proposal) {
        _state.update {
            it.copy(
                phase = when (result) {
                    is AppResult.Success -> AlternativePhase.Proposed(wrap(result.data))
                    is AppResult.Failure -> AlternativePhase.Failed(result.error.messageRes())
                },
            )
        }
    }

    private fun finish(result: AppResult<Unit>, @StringRes doneText: Int) = when (result) {
        is AppResult.Success -> done(doneText)
        is AppResult.Failure -> _state.update { it.copy(phase = AlternativePhase.Failed(result.error.messageRes())) }
    }

    private fun done(@StringRes text: Int) {
        _state.value = AlternativeUiState()
        _doneMessage.value = UserMessage(text)
    }

    private fun AppResult<*>.error(): AppError? = (this as? AppResult.Failure)?.error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeHost(viewModel: AlternativeViewModel, online: Boolean) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val target = state.target ?: return
    val busy by rememberUpdatedState(state.busy)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !busy },
    )

    ModalBottomSheet(
        onDismissRequest = viewModel::dismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.m)
                .padding(bottom = Spacing.l)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text(
                stringResource(
                    when (target) {
                        AlternativeTarget.WholeRoutine -> R.string.action_regenerate_routine
                        AlternativeTarget.WholeDiet -> R.string.action_regenerate_diet
                        else -> R.string.action_request_alternative
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            when (val phase = state.phase) {
                AlternativePhase.Loading -> Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.alt_loading), style = MaterialTheme.typography.bodyMedium)
                }
                is AlternativePhase.Proposed -> ProposalView(phase.proposal, phase.error, accepting = false, online, viewModel)
                is AlternativePhase.Accepting -> ProposalView(phase.proposal, null, accepting = true, online, viewModel)
                AlternativePhase.Idle, is AlternativePhase.Failed -> RequestForm(state, target, online, viewModel)
            }
        }
    }

    if (state.confirmReplace) {
        AlertDialog(
            onDismissRequest = viewModel::cancelReplace,
            title = { Text(stringResource(R.string.alt_confirm_title)) },
            text = { Text(stringResource(R.string.alt_confirm_message)) },
            confirmButton = { TextButton(onClick = viewModel::confirmReplace) { Text(stringResource(R.string.action_continue)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelReplace) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun RequestForm(state: AlternativeUiState, target: AlternativeTarget, online: Boolean, viewModel: AlternativeViewModel) {
    when (target) {
        is AlternativeTarget.Exercise -> ReasonList(ExerciseReason.entries, state.exerciseReason, { it.label }, viewModel::onExerciseReason)
        is AlternativeTarget.Recipe -> ReasonList(RecipeReason.entries, state.recipeReason, { it.label }, viewModel::onRecipeReason)
        else -> Text(stringResource(R.string.alt_whole_plan_hint), style = MaterialTheme.typography.bodyMedium)
    }
    if (state.reasonMissing) {
        Text(stringResource(R.string.alt_reason_required), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    if (state.exerciseReason == ExerciseReason.PAIN && target is AlternativeTarget.Exercise) {
        // FR-019
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(Modifier.padding(Spacing.m), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Text(stringResource(R.string.alt_pain_warning), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
    FormTextField(
        value = state.comment,
        onValueChange = viewModel::onComment,
        label = stringResource(R.string.alt_comment_label),
        maxLength = FREE_TEXT_MAX,
        singleLine = false,
    )
    (state.phase as? AlternativePhase.Failed)?.let {
        Text(stringResource(it.message), color = MaterialTheme.colorScheme.error)
    }
    if (!online) OfflineHint()
    Button(onClick = viewModel::submit, enabled = online, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
        Text(stringResource(if (state.phase is AlternativePhase.Failed) R.string.action_retry else R.string.action_send))
    }
}

@Composable
private fun <T> ReasonList(options: List<T>, selected: T?, label: (T) -> Int, onSelect: (T) -> Unit) {
    Column(Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Spacing.routeRowHeight)
                    .selectable(selected = option == selected, role = Role.RadioButton, onClick = { onSelect(option) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = option == selected, onClick = null)
                Text(stringResource(label(option)), modifier = Modifier.padding(start = Spacing.s))
            }
        }
    }
}

@Composable
private fun ProposalView(
    proposal: Proposal,
    @StringRes error: Int?,
    accepting: Boolean,
    online: Boolean,
    viewModel: AlternativeViewModel,
) {
    Text(stringResource(R.string.alt_proposal_title), style = MaterialTheme.typography.titleMedium)
    when (proposal) {
        is Proposal.ForExercise -> ExerciseItem(proposal.value.exercise)
        is Proposal.ForRecipe -> {
            Text(proposal.value.recipe.name, style = MaterialTheme.typography.titleMedium)
            RecipeContent(proposal.value.recipe)
        }
    }
    error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    if (!online) OfflineHint()
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        TonalActionButton(stringResource(R.string.action_keep_current), viewModel::dismiss, Modifier.weight(1f), enabled = !accepting)
        Button(onClick = viewModel::accept, enabled = online && !accepting, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
            Text(stringResource(if (accepting) R.string.loading else R.string.action_use_this))
        }
    }
}
