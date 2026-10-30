package com.allfitness.app.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.allfitness.app.R
import com.allfitness.app.core.ui.theme.Spacing

const val FREE_TEXT_MAX = 300

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    @StringRes error: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        // Se corta también lo que se pega (caso límite de la spec).
        onValueChange = { onValueChange(if (maxLength != null) it.take(maxLength) else it) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        suffix = suffix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = when {
            error != null -> { { Text(stringResource(error)) } }
            maxLength != null -> { { Text(stringResource(R.string.char_counter, value.length, maxLength)) } }
            else -> null
        },
    )
}

@Composable
fun <T> SingleChoiceChips(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    @StringRes error: Int? = null,
) {
    ChipGroup(label, error) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(optionLabel(option)) },
            )
        }
    }
}

@Composable
fun <T> MultiChoiceChips(
    label: String,
    options: List<T>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    @StringRes error: Int? = null,
) {
    ChipGroup(label, error) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onToggle(option) },
                label = { Text(optionLabel(option)) },
            )
        }
    }
}

@Composable
private fun ChipGroup(label: String, @StringRes error: Int?, chips: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) { chips() }
        if (error != null) {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
