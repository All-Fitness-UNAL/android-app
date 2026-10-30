package com.allfitness.app.core.network

import com.allfitness.app.core.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.time.DateTimeException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ErrorBody(val code: String? = null, val message: String? = null, val fields: List<String> = emptyList())

/** Único punto que traduce fallos de red, HTTP y datos a [AppError] (constitución IV, research R12). */
@Singleton
class SafeApiCall @Inject constructor(
    private val sessionManager: SessionManager,
    private val json: Json,
) {
    suspend operator fun <T> invoke(isLogin: Boolean = false, block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        AppResult.Failure(mapHttp(e, isLogin))
    } catch (_: InterruptedIOException) {
        AppResult.Failure(AppError.Timeout)
    } catch (_: IOException) {
        AppResult.Failure(AppError.NoConnection)
    } catch (_: SerializationException) {
        AppResult.Failure(AppError.InvalidResponse)
    } catch (_: IllegalArgumentException) {
        // Mapeos DTO -> dominio que fallan por datos esenciales faltantes o inconsistentes.
        AppResult.Failure(AppError.InvalidResponse)
    } catch (_: DateTimeException) {
        AppResult.Failure(AppError.InvalidResponse)
    }

    private suspend fun mapHttp(e: HttpException, isLogin: Boolean): AppError {
        val body = e.response()?.errorBody()?.string()
            ?.let { runCatching { json.decodeFromString<ErrorBody>(it) }.getOrNull() }
        return when (e.code()) {
            400 -> AppError.Validation(body?.code ?: "VALIDATION_ERROR", body?.fields.orEmpty())
            401 -> if (isLogin) {
                AppError.InvalidCredentials
            } else {
                sessionManager.expire()
                AppError.SessionExpired
            }
            404 -> if (body?.code == "NO_ACTIVE_PLAN") AppError.NoActivePlan else AppError.NotFound
            409 -> when (body?.code) {
                "EMAIL_TAKEN" -> AppError.EmailTaken
                "PROPOSAL_EXPIRED" -> AppError.ProposalExpired
                else -> AppError.Server
            }
            502, 503, 504 -> AppError.AiUnavailable
            else -> AppError.Server
        }
    }
}
