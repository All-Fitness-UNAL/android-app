package com.allfitness.app.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil3.SingletonImageLoader
import com.allfitness.app.core.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "allfitness")

@Serializable
data class User(val id: String, val name: String, val email: String)

@Serializable
data class Session(val token: String, val user: User, val onboardingCompleted: Boolean)

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data object Expired : SessionState
    data class Active(val session: Session) : SessionState
}

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val json: Json,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()
    private val mutex = Mutex()

    val token: String? get() = (_state.value as? SessionState.Active)?.session?.token

    suspend fun restore(): Session? {
        val session = context.appDataStore.data.first()[SESSION]
            ?.let { runCatching { json.decodeFromString<Session>(it) }.getOrNull() }
        _state.value = session?.let { SessionState.Active(it) } ?: SessionState.LoggedOut
        return session
    }

    suspend fun start(session: Session) {
        context.appDataStore.edit { it[SESSION] = json.encodeToString(Session.serializer(), session) }
        _state.value = SessionState.Active(session)
    }

    suspend fun markOnboardingCompleted() {
        val current = (_state.value as? SessionState.Active)?.session ?: return
        if (!current.onboardingCompleted) start(current.copy(onboardingCompleted = true))
    }

    /** Respuesta 401: se borra todo y el Login muestra "Tu sesión expiró". */
    suspend fun expire() = mutex.withLock {
        if (_state.value !is SessionState.Active) return@withLock
        clearLocalData()
        _state.value = SessionState.Expired
    }

    suspend fun logOut() = mutex.withLock {
        clearLocalData()
        _state.value = SessionState.LoggedOut
    }

    private suspend fun clearLocalData() {
        context.appDataStore.edit { it.clear() }
        withContext(Dispatchers.IO) { database.clearAllTables() }
        SingletonImageLoader.get(context).run {
            memoryCache?.clear()
            diskCache?.clear()
        }
    }

    private companion object {
        val SESSION = stringPreferencesKey("session")
    }
}
