package com.allfitness.app.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.allfitness.app.R
import com.allfitness.app.core.ui.theme.AppTheme
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val scheme = MaterialTheme.colorScheme
    val fill = AppTheme.colors.surfaceInput
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = fill,
        unfocusedContainerColor = fill,
        errorContainerColor = fill,
        unfocusedBorderColor = scheme.outline,
    )
    val interaction = remember { MutableInteractionSource() }
    val errorText = error?.let { stringResource(it) }
    val shape = MaterialTheme.shapes.medium
    // Etiqueta encima del borde (Figma `input-wrapper`) pero dentro del campo: TalkBack la lee con él,
    // como la etiqueta de OutlinedTextField, y tocarla enfoca el campo.
    BasicTextField(
        value = value,
        // Se corta también lo que se pega (caso límite de la spec).
        onValueChange = { onValueChange(if (maxLength != null) it.take(maxLength) else it) },
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { if (errorText != null) error(errorText) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        visualTransformation = visualTransformation,
        interactionSource = interaction,
        cursorBrush = SolidColor(if (errorText != null) scheme.error else scheme.primary),
    ) { field ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant)
            // La Column suelta el ancho mínimo: sin esto la caja se encoge al texto.
            Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) {
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = field,
                    enabled = true,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interaction,
                    isError = errorText != null,
                    // La unidad va como trailingIcon porque suffix solo se ve con el campo enfocado.
                    // El hueco de trailingIcon está pensado para un ícono de 24 dp: sin margen, el texto toca el borde.
                    trailingIcon = suffix?.let {
                        { Text(it, Modifier.padding(horizontal = Spacing.sm), color = scheme.onSurfaceVariant) }
                    },
                    supportingText = when {
                        errorText != null -> { { Text(errorText) } }
                        hint != null -> { { Text(hint) } }
                        // En los campos numéricos el contador "0/3" no aporta: el límite lo da el rango.
                        maxLength != null && keyboardType == KeyboardType.Text -> {
                            { Text(stringResource(R.string.char_counter, value.length, maxLength)) }
                        }
                        else -> null
                    },
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = errorText != null,
                            interactionSource = interaction,
                            colors = colors,
                            shape = shape,
                        )
                    },
                )
            }
        }
    }
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
        options.forEach { option -> PillChip(optionLabel(option), option == selected, { onSelect(option) }, role = Role.RadioButton) }
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
        options.forEach { option -> PillChip(optionLabel(option), option in selected, { onToggle(option) }) }
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
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val scheme = MaterialTheme.colorScheme
        // Como PillChip: elegido en verde sobre verde al 12 %; sin elegir, relleno de campo y borde `outline`.
        val colors = SegmentedButtonDefaults.colors(
            activeContainerColor = scheme.primaryContainer,
            activeContentColor = scheme.primary,
            activeBorderColor = scheme.primary,
            inactiveContainerColor = AppTheme.colors.surfaceInput,
            inactiveContentColor = scheme.onSurfaceVariant,
            inactiveBorderColor = scheme.outline,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size, MaterialTheme.shapes.small),
                    colors = colors,
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
private fun ChipGroup(label: String, @StringRes error: Int?, modifier: Modifier = Modifier, chips: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) { chips() }
        if (error != null) {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
