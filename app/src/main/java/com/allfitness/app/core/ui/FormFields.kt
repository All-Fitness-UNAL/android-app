package com.allfitness.app.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    hint: String? = null,
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
        // La unidad va como trailingIcon porque suffix solo se ve con el campo enfocado.
        // El hueco de trailingIcon está pensado para un ícono de 24 dp: sin margen, el texto toca el borde.
        trailingIcon = suffix?.let {
            { Text(it, Modifier.padding(horizontal = Spacing.sm), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = when {
            error != null -> { { Text(stringResource(error)) } }
            hint != null -> { { Text(hint) } }
            // En los campos numéricos el contador "0/3" no aporta: el límite lo da el rango.
            maxLength != null && keyboardType == KeyboardType.Text -> {
                { Text(stringResource(R.string.char_counter, value.length, maxLength)) }
            }
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
    ChipGroup(label, error, Modifier.selectableGroup()) {
        options.forEach { option -> CheckChip(option == selected, { onSelect(option) }, optionLabel(option)) }
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
        options.forEach { option -> CheckChip(option in selected, { onToggle(option) }, optionLabel(option)) }
    }
}

/** Conteos cortos (días, comidas) en una sola fila de segmentos iguales. */
@Composable
fun <T> SegmentedChoice(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    @StringRes error: Int? = null,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size, MaterialTheme.shapes.small),
                    // Sin check: con 7 segmentos no cabe y el relleno ya marca la selección.
                    icon = {},
                    label = { Text(optionLabel(option)) },
                )
            }
        }
        if (error != null) {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CheckChip(selected: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        },
    )
}

@Composable
private fun ChipGroup(label: String, @StringRes error: Int?, modifier: Modifier = Modifier, chips: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) { chips() }
        if (error != null) {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
