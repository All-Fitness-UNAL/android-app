package com.allfitness.app.core.ui

import androidx.annotation.StringRes
import com.allfitness.app.R
import com.allfitness.app.core.network.AppError

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Timeout -> R.string.error_timeout
    is AppError.Validation -> when (code) {
        "FUTURE_DATE" -> R.string.error_future_date
        "INVALID_DATE" -> R.string.error_invalid_date
        else -> R.string.error_validation
    }
    AppError.InvalidCredentials -> R.string.error_invalid_credentials
    AppError.SessionExpired -> R.string.error_session_expired
    AppError.NoActivePlan -> R.string.error_no_active_plan
    AppError.NotFound -> R.string.error_not_found
    AppError.EmailTaken -> R.string.error_email_taken
    AppError.ProposalExpired -> R.string.error_proposal_expired
    AppError.AiUnavailable -> R.string.error_ai_unavailable
    AppError.Server -> R.string.error_server
    AppError.InvalidResponse -> R.string.error_invalid_response
}
