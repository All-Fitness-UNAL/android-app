package com.allfitness.app.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil3.compose.AsyncImage
import com.allfitness.app.R
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.theme.Spacing

/**
 * Tarjeta de un ejercicio (FR-012). [checked] = null oculta la casilla; [trailing] permite añadir
 * acciones como "Pedir alternativa".
 */
@Composable
fun ExerciseItem(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(Spacing.m), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            if (checked != null) {
                // TalkBack anuncia qué ejercicio se marca.
                Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.semantics { contentDescription = exercise.name })
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(exercise.muscleGroup, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (exercise.reps != null) {
                        stringResource(R.string.exercise_sets_reps, exercise.sets, exercise.reps)
                    } else {
                        stringResource(R.string.exercise_sets_duration, exercise.sets, exercise.durationSeconds ?: 0)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(stringResource(R.string.exercise_rest, exercise.restSeconds), style = MaterialTheme.typography.bodyMedium)
                exercise.suggestedWeightKg?.let {
                    Text(stringResource(R.string.exercise_weight, formatNumber(it)), style = MaterialTheme.typography.bodyMedium)
                }
                if (exercise.instructions.isNotBlank()) {
                    Text(exercise.instructions, style = MaterialTheme.typography.bodySmall)
                }
                trailing?.invoke()
            }
            exercise.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(Spacing.imageSize).clip(MaterialTheme.shapes.small).align(Alignment.Top),
                )
            }
        }
    }
}
