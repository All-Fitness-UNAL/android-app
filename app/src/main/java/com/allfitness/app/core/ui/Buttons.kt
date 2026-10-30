package com.allfitness.app.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.allfitness.app.R
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion

/** Acción secundaria: tonal y rectangular, para que no se lea como deshabilitada. */
@Composable
fun TonalActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        interactionSource = interaction,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text)
    }
}

/** "Pedir alternativa" es siempre el mismo botón, con el ícono de desvío. */
@Composable
fun AlternativeButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    TonalActionButton(
        text = stringResource(R.string.action_request_alternative),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = R.drawable.ic_alt_route,
    )
}

/** Escala 0,97 mientras se pulsa: el control responde al dedo. Nada con movimiento reducido. */
@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    if (AppTheme.reducedMotion) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, Motion.gentle(), label = "press")
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
