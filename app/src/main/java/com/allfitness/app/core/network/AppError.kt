package com.allfitness.app.core.network

sealed interface AppError {
    data object NoConnection : AppError
    data object Timeout : AppError
    data class Validation(val code: String, val fields: List<String> = emptyList()) : AppError
    data object InvalidCredentials : AppError
    data object SessionExpired : AppError
    data object NoActivePlan : AppError
    data object NotFound : AppError
    data object EmailTaken : AppError
    data object ProposalExpired : AppError
    data object AiUnavailable : AppError
    data object Server : AppError
    data object InvalidResponse : AppError
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

fun AppResult<*>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error
