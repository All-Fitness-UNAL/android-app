package com.allfitness.app.core.ui

import androidx.annotation.StringRes
import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Content<T>(val data: T, val isStale: Boolean = false, val savedAt: Long? = null) : UiState<T>

    /** [offline] = no hay nada guardado y no hay conexión para cargarlo. */
    data class Empty(val offline: Boolean = false) : UiState<Nothing>
    data class Error(val error: AppError) : UiState<Nothing>
}

fun <T, R> UiState<T>.mapContent(transform: (T) -> R): UiState<R> = when (this) {
    is UiState.Content -> UiState.Content(transform(data), isStale, savedAt)
    UiState.Loading -> UiState.Loading
    is UiState.Empty -> this
    is UiState.Error -> this
}

sealed interface RefreshState {
    data object Idle : RefreshState
    data object Running : RefreshState
    data object Done : RefreshState
    data class Failed(val error: AppError) : RefreshState
}

/** Lo guardado se muestra siempre; sin conexión se marca como posiblemente desactualizado (FR-036, FR-038). */
fun <T> uiStateOf(cached: Cached<T>?, refresh: RefreshState, online: Boolean): UiState<T> = when {
    cached != null -> UiState.Content(cached.data, isStale = !online, savedAt = cached.savedAt)
    refresh is RefreshState.Failed && refresh.error == AppError.NoActivePlan -> UiState.Empty()
    !online -> UiState.Empty(offline = true)
    refresh is RefreshState.Failed && refresh.error == AppError.NoConnection -> UiState.Empty(offline = true)
    refresh is RefreshState.Failed -> UiState.Error(refresh.error)
    refresh == RefreshState.Done -> UiState.Empty()
    else -> UiState.Loading
}

/** Recarga desde el servidor al iniciar, al recuperar la conexión y cuando se pide; nunca dos a la vez. */
class Refresher(
    private val scope: CoroutineScope,
    online: Flow<Boolean>,
    private val block: suspend () -> AppResult<*>,
) {
    private val _state = MutableStateFlow<RefreshState>(RefreshState.Idle)
    val state: StateFlow<RefreshState> = _state.asStateFlow()

    init {
        scope.launch { online.collect { if (it) run() } }
    }

    fun run() {
        if (_state.value == RefreshState.Running) return
        _state.value = RefreshState.Running
        scope.launch {
            _state.value = when (val result = block()) {
                is AppResult.Success -> RefreshState.Done
                is AppResult.Failure -> RefreshState.Failed(result.error)
            }
        }
    }
}

/** Mensaje de una sola vez para un snackbar, con acción "Reintentar" opcional. */
data class UserMessage(
    @StringRes val text: Int,
    val retry: (() -> Unit)? = null,
    val id: Long = System.nanoTime(),
)
