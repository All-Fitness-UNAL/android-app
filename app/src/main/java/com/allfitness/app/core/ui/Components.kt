package com.allfitness.app.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.allfitness.app.R
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.time.Today
import com.allfitness.app.core.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LoadingState(modifier: Modifier = Modifier, message: String? = null) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        if (message != null) {
            Text(
                text = message,
                modifier = Modifier.padding(top = Spacing.m),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun ErrorState(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        message = stringResource(error.messageRes()),
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {},
) {
    MessageState(message, actionLabel, onAction, modifier, actionEnabled)
}

@Composable
private fun MessageState(
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        if (actionLabel != null) {
            Button(onClick = onAction, enabled = actionEnabled, modifier = Modifier.padding(top = Spacing.m)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun OfflineBanner(savedAt: Long?, modifier: Modifier = Modifier) {
    val date = savedAt?.let {
        DateTimeFormatter.ofPattern("d MMM, h:mm a", Today.locale)
            .format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
    }
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (date != null) stringResource(R.string.offline_banner_with_date, date) else stringResource(R.string.offline_banner),
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
fun AiDisclaimer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = stringResource(R.string.ai_disclaimer),
            modifier = Modifier.padding(Spacing.m),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
fun OfflineHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.requires_connection),
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                }
            }
        },
    )
}

/**
 * Pinta los estados cargando / vacío / error y, con contenido, el aviso sin conexión encima.
 */
@Composable
fun <T> StateContent(
    state: UiState<T>,
    emptyMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    offlineEmptyMessage: String = stringResource(R.string.empty_offline),
    emptyActionLabel: String? = null,
    emptyActionEnabled: Boolean = true,
    onEmptyAction: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(state.error, onRetry)
            is UiState.Empty -> if (state.offline) {
                EmptyState(offlineEmptyMessage)
            } else {
                EmptyState(emptyMessage, actionLabel = emptyActionLabel, actionEnabled = emptyActionEnabled, onAction = onEmptyAction)
            }
            is UiState.Content -> Column(Modifier.fillMaxSize()) {
                if (state.isStale) OfflineBanner(state.savedAt)
                content(state.data)
            }
        }
    }
}

/** Muestra [message] una vez en el snackbar y avisa con [onShown] para que el ViewModel lo limpie. */
@Composable
fun MessageEffect(message: UserMessage?, snackbar: SnackbarHostState, onShown: () -> Unit) {
    val text = message?.let { stringResource(it.text) }
    val retryLabel = stringResource(R.string.action_retry)
    LaunchedEffect(message?.id) {
        if (message == null || text == null) return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = text,
            actionLabel = if (message.retry != null) retryLabel else null,
            duration = if (message.retry != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) message.retry?.invoke()
        onShown()
    }
}
