package com.allfitness.app.auth

import com.allfitness.app.BuildConfig
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.SafeApiCall
import com.allfitness.app.core.network.map
import com.allfitness.app.core.session.Session
import com.allfitness.app.core.session.SessionManager
import com.allfitness.app.core.session.User
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

// ---- DTO del contrato (contracts/auth.md) ----

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String, val acceptedLegalVersion: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class UserDto(val id: String, val name: String, val email: String)

@Serializable
data class SessionDto(val token: String, val user: UserDto, val onboardingCompleted: Boolean)

@Serializable
data class MeDto(val user: UserDto, val onboardingCompleted: Boolean)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): SessionDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): SessionDto

    @POST("auth/logout")
    suspend fun logout()

    @GET("me")
    suspend fun me(): MeDto

    @DELETE("me")
    suspend fun deleteMe()
}

private fun UserDto.toDomain() = User(id, name, email)

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val safeApiCall: SafeApiCall,
    private val sessionManager: SessionManager,
) {
    suspend fun register(name: String, email: String, password: String): AppResult<Unit> =
        safeApiCall { api.register(RegisterRequest(name.trim(), email.trim(), password, BuildConfig.LEGAL_VERSION)) }
            .map { start(it) }

    suspend fun login(email: String, password: String): AppResult<Unit> =
        safeApiCall(isLogin = true) { api.login(LoginRequest(email.trim(), password)) }.map { start(it) }

    /**
     * Abre la app con la sesión guardada aunque no haya conexión y luego la confirma con el servidor.
     * Un 401 lo maneja SafeApiCall cerrando la sesión.
     */
    suspend fun restoreSession() {
        val stored = sessionManager.restore() ?: return
        val me = safeApiCall { api.me() }
        if (me is AppResult.Success) {
            // El onboarding nunca vuelve a "pendiente"; se conserva lo más avanzado.
            sessionManager.start(
                stored.copy(user = me.data.user.toDomain(), onboardingCompleted = stored.onboardingCompleted || me.data.onboardingCompleted),
            )
        }
    }

    suspend fun logout() {
        safeApiCall { api.logout() }
        sessionManager.logOut()
    }

    /** Solo se borran los datos locales cuando el servidor confirma la eliminación (FR-034). */
    suspend fun deleteAccount(): AppResult<Unit> = safeApiCall { api.deleteMe() }.map { sessionManager.logOut() }

    private suspend fun start(dto: SessionDto) =
        sessionManager.start(Session(dto.token, dto.user.toDomain(), dto.onboardingCompleted))
}
