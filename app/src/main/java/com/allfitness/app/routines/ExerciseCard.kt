package com.allfitness.app.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import coil3.compose.AsyncImage
import com.allfitness.app.R
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.IconTile
import com.allfitness.app.core.ui.theme.Spacing

/** Carga del ejercicio: con reps, "series × reps"; si no, "series × s". Sin ninguno, (reps, 0) y la tarjeta muestra solo las series. */
fun Exercise.loadRes(): Pair<Int, Int> = when {
    reps != null -> R.string.exercise_sets_reps to reps
    durationSeconds != null -> R.string.exercise_sets_seconds to durationSeconds
    else -> R.string.exercise_sets_reps to 0
}

/**
 * Tarjeta de un ejercicio de la lista de Rutina (Figma `exercise-card-*`): toda ella abre el modo sesión.
 * La casilla solo muestra [done] (marcar se hace en el modo sesión); null la oculta (días futuros y descanso).
 */
@Composable
fun ExerciseCard(exercise: Exercise, done: Boolean?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (loadRes, value) = exercise.loadRes()
    val sets = if (exercise.reps == null && exercise.durationSeconds == null) {
        pluralStringResource(R.plurals.exercise_sets, exercise.sets, exercise.sets)
    } else {
        stringResource(loadRes, exercise.sets, value)
    }
    val load = stringResource(R.string.status_pair, sets, stringResource(R.string.exercise_rest, exercise.restSeconds))
    val description = "${exercise.name}, ${exercise.muscleGroup}, $load"
    val doneLabel = stringResource(R.string.exercise_done)
    AppCard(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = stringResource(R.string.action_open_exercise), role = Role.Button, onClick = onClick)
                // Un solo nodo para TalkBack: nombre, músculo, carga y, si está hecho, "Hecho".
                .clearAndSetSemantics {
                    contentDescription = description
                    if (done == true) stateDescription = doneLabel
                }
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Sin imagen del servidor, una placa con ícono: nunca una foto inventada (spec §3).
            if (exercise.imageUrl != null) {
                AsyncImage(
                    model = exercise.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Spacing.thumbExercise)
                        .clip(MaterialTheme.shapes.medium)
                        .background(scheme.surfaceContainerHigh),
                )
            } else {
                IconTile(R.drawable.lc_dumbbell, Spacing.thumbExercise)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    exercise.muscleGroup,
                    Modifier
                        .background(scheme.primaryContainer, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = Spacing.s, vertical = Spacing.hairline),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary,
                )
                Text(exercise.name, style = MaterialTheme.typography.titleSmall)
                Text(load, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            }
            if (done != null) CheckCircle(done)
        }
    }
}

/** Hecho: círculo verde con check. Sin hacer: aro `outline` (≥ 3:1). */
@Composable
private fun CheckCircle(done: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val circle = Modifier.size(Spacing.checkCircle)
    Box(
        if (done) circle.background(scheme.primary, CircleShape) else circle.border(Spacing.hairline * 2, scheme.outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(painterResource(R.drawable.lc_check), contentDescription = null, Modifier.size(Spacing.checkCircle / 2), tint = scheme.onPrimary)
    }
}
