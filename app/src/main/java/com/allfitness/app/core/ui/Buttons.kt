package com.allfitness.app.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.allfitness.app.R

/** Acción secundaria: tonal y rectangular, para que no se lea como deshabilitada. */
@Composable
fun TonalActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
) {
    FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = MaterialTheme.shapes.small) {
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
