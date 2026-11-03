package com.allfitness.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import com.allfitness.app.R
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.theme.Spacing

/** Primer carácter visible de [name] en mayúscula; un carácter fuera del BMP sale entero, no medio par. */
fun initialOf(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return ""
    return trimmed.substring(0, trimmed.offsetByCodePoints(0, 1)).uppercase(Today.locale)
}

/** Cuadro verde con el rayo y el nombre de la app (Figma `logo-mark`); [large] en login y registro. */
@Composable
fun LogoMark(modifier: Modifier = Modifier, large: Boolean = false) {
    val box = if (large) Spacing.logoMarkLarge else Spacing.logoMark
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (large) Spacing.sm else Spacing.s)) {
        Box(
            Modifier.size(box).background(MaterialTheme.colorScheme.primary, if (large) MaterialTheme.shapes.medium else MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            // El rayo ocupa 16 de 28 dp, como en el Figma.
            Icon(painterResource(R.drawable.lc_zap), contentDescription = null, Modifier.size(box * 4 / 7), tint = MaterialTheme.colorScheme.onPrimary)
        }
        Text(stringResource(R.string.app_wordmark), style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
    }
}

/**
 * Inicial de [name] en un círculo con anillo verde (Figma `avatar-glow-ring`: 3 dp de anillo y 3 de hueco
 * en el de 104). Decorativo: el nombre siempre va al lado.
 */
@Composable
fun Avatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val ring = size * 3 / 104
    Box(
        modifier
            .size(size)
            .border(ring, scheme.primary, CircleShape)
            .padding(ring)
            .border(ring, scheme.surfaceContainerLowest, CircleShape)
            .background(scheme.surfaceContainerHigh, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initialOf(name),
            // Con fuente grande la letra puede pasar del círculo; mejor salirse que cortarse.
            Modifier.wrapContentSize(unbounded = true),
            style = if (size >= Spacing.avatarLarge) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
        )
    }
}

/** Etiqueta "Plan de IA" (Figma `ai-sparkle-badge`). No sustituye al aviso de IA (FR-037). */
@Composable
fun AiBadge() {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.s, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(painterResource(R.drawable.lc_sparkles), contentDescription = null, Modifier.size(Spacing.sm), tint = primary)
        Text(stringResource(R.string.ai_badge), style = MaterialTheme.typography.labelSmall, color = primary)
    }
}
