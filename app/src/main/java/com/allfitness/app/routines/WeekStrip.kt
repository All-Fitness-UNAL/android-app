package com.allfitness.app.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.allfitness.app.R
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.time.displayName
import com.allfitness.app.core.time.shortLabel
import com.allfitness.app.core.ui.StationState
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Inicial del día en español: L M X J V S D. Con es-CO (como es-419) el miércoles sale "M"; el idioma base da "X". */
fun dayInitial(day: DayOfWeek): String =
    day.getDisplayName(TextStyle.NARROW, Locale.forLanguageTag(Today.locale.language)).uppercase(Today.locale)

/**
 * Semana en 7 columnas: inicial y círculo con el estado del día (Figma `weekly-activity`). Con [onSelect]
 * cada día se puede elegir y [selected] lleva fondo; sin él es solo lectura.
 */
@Composable
fun WeekStrip(rows: List<WeekDayRow>, selected: LocalDate?, onSelect: ((LocalDate) -> Unit)?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().then(if (onSelect != null) Modifier.selectableGroup() else Modifier)) {
        rows.forEach { row -> DayColumn(row, row.date == selected, onSelect, Modifier.weight(1f)) }
    }
}

@Composable
private fun DayColumn(row: WeekDayRow, isSelected: Boolean, onSelect: ((LocalDate) -> Unit)?, modifier: Modifier) {
    val state = row.stationState()
    val today = row.isToday
    val done = state == StationState.DONE || state == StationState.TODAY_DONE
    // TalkBack lee el día completo y su estado, no la inicial.
    val description = listOfNotNull(
        "${row.date.dayOfWeek.displayName()} ${row.date.shortLabel()}",
        stringResource(R.string.today).takeIf { today },
        stringResource(R.string.session_finished).takeIf { done },
        stringResource(R.string.rest_day).takeIf { state == StationState.REST },
    ).joinToString(", ")
    val interaction = if (onSelect != null) {
        Modifier.selectable(isSelected, role = Role.Tab) { onSelect(row.date) }
    } else {
        Modifier
    }
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .then(if (isSelected) Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh) else Modifier)
            .then(interaction)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(vertical = Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(
            dayInitial(row.date.dayOfWeek),
            Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.labelMedium,
            color = if (today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DayDot(state)
    }
}

/** Hecho: verde con check. Hoy: aro verde. Pendiente: aro y punto. Pasado sin hacer: aro apagado. Descanso: luna. */
@Composable
private fun DayDot(state: StationState) {
    val scheme = MaterialTheme.colorScheme
    val muted = AppTheme.colors.muted
    val ring = Spacing.hairline * 2
    val dot = Modifier.size(Spacing.weekDot)
    Box(
        when (state) {
            StationState.DONE, StationState.TODAY_DONE -> dot.background(scheme.primary, CircleShape)
            StationState.TODAY -> dot.background(scheme.primaryContainer, CircleShape).border(ring, scheme.primary, CircleShape)
            StationState.PENDING -> dot.border(ring, scheme.outline, CircleShape)
            StationState.PAST -> dot.border(ring, muted, CircleShape)
            StationState.REST -> dot
        },
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            StationState.DONE, StationState.TODAY_DONE ->
                Icon(painterResource(R.drawable.lc_check), contentDescription = null, Modifier.size(Spacing.weekDot / 2), tint = scheme.onPrimary)
            StationState.PENDING -> Box(Modifier.size(Spacing.s).background(muted, CircleShape))
            StationState.REST ->
                Icon(painterResource(R.drawable.lc_moon), contentDescription = null, Modifier.size(Spacing.weekDot / 2), tint = scheme.onSurfaceVariant)
            else -> Unit
        }
    }
}
