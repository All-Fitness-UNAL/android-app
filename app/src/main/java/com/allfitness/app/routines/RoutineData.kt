package com.allfitness.app.routines

import com.allfitness.app.core.network.LongRunning
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

// ---- Modelos de dominio (data-model.md) ----

enum class DayType { TRAINING, REST }

data class Routine(val id: String, val version: Int, val updatedAt: Instant, val days: List<RoutineDay>)

data class RoutineDay(val dayOfWeek: DayOfWeek, val type: DayType, val title: String?, val exercises: List<Exercise>)

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int,
    val suggestedWeightKg: Double?,
    val instructions: String,
    val imageUrl: String?,
)

data class WorkoutLog(val date: LocalDate, val completedExerciseIds: Set<String>, val sessionFinished: Boolean)

data class ExerciseProposal(val proposalId: String, val exercise: Exercise)

// ---- DTO del contrato (contracts/routine.md) ----

@Serializable
data class RoutineDto(val id: String, val version: Int, val updatedAt: String, val days: List<RoutineDayDto>)

@Serializable
data class RoutineDayDto(
    val dayOfWeek: String,
    val type: String,
    val title: String? = null,
    val exercises: List<ExerciseDto> = emptyList(),
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int,
    val suggestedWeightKg: Double? = null,
    val instructions: String = "",
    val imageUrl: String? = null,
)

@Serializable
data class WorkoutLogDto(val date: String, val completedExerciseIds: List<String> = emptyList(), val sessionFinished: Boolean = false)

@Serializable
data class CompletedRequest(val completed: Boolean)

@Serializable
data class GenerateRequest(val comment: String? = null)

@Serializable
data class AlternativeRequestDto(val reason: String, val comment: String? = null)

@Serializable
data class ExerciseProposalDto(val proposalId: String, val exercise: ExerciseDto)

interface RoutineApi {
    @GET("routine")
    suspend fun getRoutine(): RoutineDto

    @LongRunning
    @POST("routine/generate")
    suspend fun generate(@Body body: GenerateRequest): RoutineDto

    @GET("workout-logs")
    suspend fun workoutLogs(@Query("from") from: String, @Query("to") to: String): List<WorkoutLogDto>

    @PUT("workout-logs/{date}/exercises/{exerciseId}")
    suspend fun setExercise(@Path("date") date: String, @Path("exerciseId") exerciseId: String, @Body body: CompletedRequest): WorkoutLogDto

    @POST("workout-logs/{date}/finish")
    suspend fun finish(@Path("date") date: String): WorkoutLogDto

    @LongRunning
    @POST("routine/exercises/{exerciseId}/alternative")
    suspend fun alternative(@Path("exerciseId") exerciseId: String, @Body body: AlternativeRequestDto): ExerciseProposalDto

    @POST("routine/exercises/{exerciseId}/alternative/{proposalId}/accept")
    suspend fun accept(@Path("exerciseId") exerciseId: String, @Path("proposalId") proposalId: String): RoutineDto
}

// ---- Mapeo DTO -> dominio: lanza IllegalArgumentException si faltan datos esenciales ----

fun RoutineDto.toDomain(): Routine {
    val mapped = days.map { it.toDomain() }.sortedBy { it.dayOfWeek }
    require(mapped.map { it.dayOfWeek } == DayOfWeek.entries) { "La rutina debe tener 7 días, de lunes a domingo" }
    return Routine(id, version, Instant.parse(updatedAt), mapped)
}

private fun RoutineDayDto.toDomain(): RoutineDay {
    val dayType = DayType.valueOf(type)
    val mapped = if (dayType == DayType.REST) emptyList() else exercises.mapNotNull { it.toDomainOrNull() }
    require(dayType == DayType.REST || mapped.isNotEmpty()) { "Día de entrenamiento sin ejercicios válidos" }
    return RoutineDay(DayOfWeek.valueOf(dayOfWeek), dayType, title, mapped)
}

/** Debe venir reps o durationSeconds; si llegan ambos se usan las repeticiones; si no llega ninguno, se omite. */
fun ExerciseDto.toDomainOrNull(): Exercise? {
    if (reps == null && durationSeconds == null) return null
    return Exercise(
        id, name, muscleGroup, sets, reps,
        durationSeconds = if (reps != null) null else durationSeconds,
        restSeconds, suggestedWeightKg, instructions, imageUrl,
    )
}

fun WorkoutLogDto.toDomain() = WorkoutLog(LocalDate.parse(date), completedExerciseIds.toSet(), sessionFinished)
