package com.allfitness.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.allfitness.app.core.ui.theme.Spacing

/** [color]: marca de la serie junto a la etiqueta (leyenda de una barra); el texto nunca va en ese color. */
data class DataCell(val label: String, val value: String, val unit: String? = null, val color: Color? = null)

/** Columnas por fila: las que caben sin partir palabras, repartidas en filas parejas (2 + 2, no 3 + 1). */
fun stripColumns(count: Int, available: Float, widest: Float): Int {
    val fit = (available / widest).toInt().coerceIn(1, count)
    val rows = (count + fit - 1) / fit
    return (count + rows - 1) / rows
}

/**
 * Pesos para una sola fila: iguales si cada celda cabe en su parte; si no, cada una con su ancho y lo
 * que sobra a partes iguales. Null si ni así caben (entonces se usan varias filas).
 */
fun stripWeights(widths: List<Float>, available: Float): List<Float>? {
    val share = available / widths.size
    if (widths.all { it <= share }) return List(widths.size) { share }
    val spare = available - widths.sum()
    if (spare < 0) return null
    return widths.map { it + spare / widths.size }
}

/**
 * Fila reglada de datos: etiqueta pequeña arriba y cifra tabular abajo, nunca "Etiqueta: valor". Con
 * fuente grande, si las celdas no caben enteras, pasan a varias filas en lugar de cortar "DESCA-NSO".
 */
@Composable
fun DataStrip(cells: List<DataCell>, modifier: Modifier = Modifier) {
    if (cells.isEmpty()) return
    val measurer = rememberTextMeasurer()
    val typography = MaterialTheme.typography
    val density = LocalDensity.current
    // Ancho mínimo de cada celda sin partir palabras, con su margen.
    val widths = remember(cells, typography, density) {
        cells.map { cell ->
            val value = measurer.measure(cell.value, typography.titleLarge).size.width +
                (cell.unit?.let { measurer.measure(it, typography.bodySmall).size.width + with(density) { Spacing.xs.roundToPx() } } ?: 0)
            val label = measurer.measure(cell.label, typography.labelMedium).size.width +
                (if (cell.color != null) with(density) { (Spacing.s + Spacing.xs).roundToPx() } else 0)
            maxOf(label, value) + with(density) { (Spacing.sm + Spacing.s + Spacing.hairline).toPx() }
        }
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val available = constraints.maxWidth.toFloat()
        val weights = stripWeights(widths, available)
        if (weights != null) {
            StripRow(cells, weights)
        } else {
            val columns = stripColumns(cells.size, available, widths.max())
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                cells.chunked(columns).forEach { row -> StripRow(row, List(columns) { 1f }) }
            }
        }
    }
}

@Composable
private fun StripRow(cells: List<DataCell>, weights: List<Float>) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) VerticalDivider(thickness = Spacing.hairline, color = MaterialTheme.colorScheme.outlineVariant)
            DataCellView(cell, first = index == 0, Modifier.weight(weights[index]))
        }
        // La última fila conserva el ancho de columna de las demás.
        weights.drop(cells.size).forEach { Spacer(Modifier.weight(it)) }
    }
}

@Composable
private fun DataCellView(cell: DataCell, first: Boolean, modifier: Modifier) {
    Column(modifier.padding(start = if (first) 0.dp else Spacing.sm, end = Spacing.s).semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            if (cell.color != null) Box(Modifier.size(Spacing.s).background(cell.color, CircleShape))
            Text(cell.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
