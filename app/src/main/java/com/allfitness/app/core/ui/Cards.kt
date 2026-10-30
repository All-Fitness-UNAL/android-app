package com.allfitness.app.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.allfitness.app.R
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.PillShape
import com.allfitness.app.core.ui.theme.Spacing

/** Figma `pill-*`: 33 dp de alto visual; el área táctil llega a 48 con minimumInteractiveComponentSize. */
private val ChipHeight = 33.dp

/**
 * Rótulo, cifra y unidad en una tarjeta (Figma `stat-card` del perfil). Alto según el contenido.
 * [modifier] va antes de [unit] porque lint (ModifierParameter) lo pide como primer parámetro opcional.
 */
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, unit: String? = null) {
    AppCard(modifier) {
        Column(
            Modifier.padding(Spacing.m).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row {
                Text(value, Modifier.alignByBaseline(), style = MaterialTheme.typography.titleLarge)
                if (unit != null) {
                    Text(
                        unit,
                        Modifier.alignByBaseline().padding(start = Spacing.xs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Placa con ícono, rótulo y valor (Figma `pref-row`). Los hilos entre filas los pone quien las lista. */
@Composable
fun PreferenceRow(@DrawableRes icon: Int, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Spacing.sm).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        IconTile(icon)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Placa cuadrada con un ícono verde a la mitad de su tamaño. Decorativa. */
@Composable
fun IconTile(@DrawableRes icon: Int, size: Dp = Spacing.iconTile) {
    Box(
        Modifier.size(size).background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, Modifier.size(size / 2), tint = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Opción de una cuadrícula de elección única (Figma `goal-*`): círculo con ícono y texto. Elegida: verde
 * al 12 % con borde verde; si no, `surface` con borde `outline` (≥ 3:1). El grupo lleva `selectableGroup`.
 */
@Composable
fun ChoiceCard(text: String, @DrawableRes icon: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    Column(
        modifier
            .clip(shape)
            .background(if (selected) scheme.primaryContainer else scheme.surface)
            .border(Spacing.hairline, if (selected) scheme.primary else scheme.outline, shape)
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(
            Modifier.size(Spacing.iconTile).background(if (selected) scheme.primary else scheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                Modifier.size(Spacing.iconTile / 2),
                tint = if (selected) scheme.onPrimary else scheme.onSurface,
            )
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Píldora de selección múltiple (Figma `pill-*`): elegida lleva check y texto verdes sobre verde al 12 %. */
@Composable
fun PillChip(text: String, selected: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = ChipHeight)
            .alpha(if (enabled) 1f else 0.38f)
            .clip(PillShape)
            .background(if (selected) scheme.primaryContainer else AppTheme.colors.surfaceInput)
            .border(Spacing.hairline, if (selected) scheme.primary else scheme.outline, PillShape)
            .selectable(selected, enabled = enabled, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (selected) {
            Icon(painterResource(R.drawable.lc_check), contentDescription = null, Modifier.size(Spacing.m), tint = scheme.primary)
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) scheme.primary else scheme.onSurfaceVariant)
    }
}
