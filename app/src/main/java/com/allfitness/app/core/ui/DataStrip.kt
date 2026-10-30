package com.allfitness.app.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.allfitness.app.core.ui.theme.Spacing

data class DataCell(val label: String, val value: String, val unit: String? = null)

/** Fila reglada de datos: etiqueta pequeña arriba y cifra tabular abajo, nunca "Etiqueta: valor". */
@Composable
fun DataStrip(cells: List<DataCell>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) VerticalDivider(thickness = Spacing.hairline, color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = if (index == 0) 0.dp else Spacing.sm, end = Spacing.s)
                    .semantics(mergeDescendants = true) {},
            ) {
                Text(cell.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    Text(cell.value, Modifier.alignByBaseline(), style = MaterialTheme.typography.titleLarge)
                    if (cell.unit != null) {
                        Text(
                            cell.unit,
                            Modifier.alignByBaseline().padding(start = Spacing.xs),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
