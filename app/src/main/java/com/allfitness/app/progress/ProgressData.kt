package com.allfitness.app.progress

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDate

// ---- Modelos de dominio (data-model.md) ----

data class WeightEntry(val date: LocalDate, val weightKg: Double)

data class TrainingSummary(
    val weekStart: LocalDate,
    val completedSessions: Int,
    val scheduledSessions: Int,
    val weekStreak: Int,
)

// ---- DTO del contrato (contracts/progress.md) ----

@Serializable
data class WeightDto(val date: String, val weightKg: Double)

@Serializable
data class WeightRequest(val weightKg: Double)

@Serializable
data class SummaryDto(val weekStart: String, val completedSessions: Int, val scheduledSessions: Int, val weekStreak: Int)

interface ProgressApi {
    @GET("progress/weights")
    suspend fun weights(): List<WeightDto>

    @PUT("progress/weights/{date}")
    suspend fun putWeight(@Path("date") date: String, @Body body: WeightRequest): WeightDto

    @GET("progress/summary")
    suspend fun summary(@Query("weekStart") weekStart: String): SummaryDto
}

fun WeightDto.toDomain() = WeightEntry(LocalDate.parse(date), weightKg)

fun SummaryDto.toDomain() = TrainingSummary(LocalDate.parse(weekStart), completedSessions, scheduledSessions, weekStreak)
