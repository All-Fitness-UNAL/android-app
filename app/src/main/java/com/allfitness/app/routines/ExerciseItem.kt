package com.allfitness.app.routines

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import coil3.compose.AsyncImage
import com.allfitness.app.R
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.DataCell
import com.allfitness.app.core.ui.DataStrip
import com.allfitness.app.core.ui.RouteStation
import com.allfitness.app.core.ui.StationState
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.Spacing

/**
 * Tarjeta de un ejercicio (FR-012). [checked] = null no muestra estado (descanso, propuestas). Con
 * [onCheckedChange] hay casilla, activa según [enabled]; sin él, el estado va en una estación de solo
 * lectura (días que no son hoy). [trailing] permite añadir acciones como "Pedir alternativa".
 */
@Composable
fun ExerciseItem(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    enabled: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    past: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val line = AppTheme.colors.lineRoutine
    val done = checked == true
    val container by animateColorAsState(
        targetValue = if (done) scheme.background else scheme.surface,
        animationSpec = if (AppTheme.reducedMotion) snap() else tween(Motion.SHORT),
        label = "exercise",
    )
    val haptics = LocalHapticFeedback.current
    val doneLabel = stringResource(R.string.exercise_done)
    AppCard(modifier.fillMaxWidth(), color = container) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            // Toda la cabecera es la casilla: área táctil grande y TalkBack lee el ejercicio con su estado.
            val toggle = if (checked != null && onCheckedChange != null) {
                Modifier.clip(MaterialTheme.shapes.small).toggleable(checked, enabled = enabled, role = Role.Checkbox) {
                    haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    onCheckedChange(it)
                }
            } else {
                Modifier
            }
            Row(toggle, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                when {
                    checked == null -> Unit
                    onCheckedChange != null -> Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
                    else -> RouteStation(
                        when {
                            checked -> StationState.DONE
                            past -> StationState.PAST
                            else -> StationState.PENDING
                        },
                        line,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        exercise.name,
                        // Sin casilla, el "hecho" de la estación solo se ve; TalkBack lo lee con el nombre.
                        modifier = Modifier.semantics { if (done && onCheckedChange == null) stateDescription = doneLabel },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (done) scheme.onSurfaceVariant else scheme.onSurface,
                    )
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = container, contentColor = line, border = BorderStroke(Spacing.hairline, line)) {
                        Text(
                            exercise.muscleGroup,
                            Modifier.padding(horizontal = Spacing.s, vertical = Spacing.hairline),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                exercise.imageUrl?.let {
                    AsyncImage(
                        model = it,
                        // Junto al nombre: decorativa.
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(Spacing.imageSize).clip(MaterialTheme.shapes.small),
                    )
                }
            }
            DataStrip(exercise.dataCells())
            if (exercise.instructions.isNotBlank()) {
                Text(exercise.instructions, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun Exercise.dataCells(): List<DataCell> = buildList {
    add(DataCell(stringResource(R.string.label_sets), sets.toString()))
    add(
        if (reps != null) {
            DataCell(stringResource(R.string.label_reps), reps.toString())
        } else {
            DataCell(stringResource(R.string.label_duration), (durationSeconds ?: 0).toString(), stringResource(R.string.unit_seconds))
        },
    )
    add(DataCell(stringResource(R.string.label_rest), restSeconds.toString(), stringResource(R.string.unit_seconds)))
    suggestedWeightKg?.let { add(DataCell(stringResource(R.string.label_weight), formatNumber(it), stringResource(R.string.unit_kg))) }
}
