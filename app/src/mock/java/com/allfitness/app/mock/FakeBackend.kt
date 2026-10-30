package com.allfitness.app.mock

import android.content.Context
import com.allfitness.app.auth.AuthApi
import com.allfitness.app.auth.LoginRequest
import com.allfitness.app.auth.MeDto
import com.allfitness.app.auth.RegisterRequest
import com.allfitness.app.auth.SessionDto
import com.allfitness.app.auth.UserDto
import com.allfitness.app.core.time.Today
import com.allfitness.app.nutrition.DietApi
import com.allfitness.app.nutrition.DietDto
import com.allfitness.app.nutrition.RecipeProposalDto
import com.allfitness.app.onboarding.ProfileApi
import com.allfitness.app.onboarding.ProfileDto
import com.allfitness.app.progress.ProgressApi
import com.allfitness.app.progress.SummaryDto
import com.allfitness.app.progress.WeightDto
import com.allfitness.app.progress.WeightRequest
import com.allfitness.app.routines.AlternativeRequestDto
import com.allfitness.app.routines.CompletedRequest
import com.allfitness.app.routines.ExerciseDto
import com.allfitness.app.routines.ExerciseProposalDto
import com.allfitness.app.routines.GenerateRequest
import com.allfitness.app.routines.RoutineApi
import com.allfitness.app.routines.RoutineDto
import com.allfitness.app.routines.WorkoutLogDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend en memoria que cumple los contratos de `specs/001-allfitness-mvp/contracts/`.
 * Responde con DTO y errores HTTP reales para que SafeApiCall y los mapeos se ejerciten igual que con el backend.
 * Los valores especiales (#lento, #falla, #roto, #sindieta, 299.9…) están documentados en quickstart.md.
 */
@Singleton
class FakeBackend @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : AuthApi, ProfileApi, RoutineApi, ProgressApi {

    private class State(
        var user: UserDto,
        var profile: ProfileDto?,
        var routine: RoutineDto?,
        var diet: DietDto?,
        val weights: MutableMap<String, Double>,
        var weekStreak: Int,
        val logs: MutableMap<String, WorkoutLogDto> = mutableMapOf(),
    )

    // Se crea al primer uso: una sesión guardada de un arranque anterior ve los datos de demo.
    private var state: State? = null
    private val s get() = state ?: demoState(UserDto("u_demo", "Usuario demo", "demo@allfitness.co")).also { state = it }
    private val exerciseProposals = mutableMapOf<String, ExerciseDto>()
    private val recipeProposals = mutableMapOf<String, RecipeProposalDto>()
    private var nextId = 1

    // ---------------------------------------------------------------- Auth

    override suspend fun register(body: RegisterRequest): SessionDto {
        delay(NORMAL_MS)
        if (body.email.equals("existe@allfitness.co", ignoreCase = true)) http(409, "EMAIL_TAKEN")
        val user = UserDto("u_${nextId++}", body.name, body.email)
        state = State(user, profile = null, routine = null, diet = null, weights = mutableMapOf(), weekStreak = 0)
        return SessionDto(TOKEN, user, onboardingCompleted = false)
    }

    override suspend fun login(body: LoginRequest): SessionDto {
        delay(NORMAL_MS)
        if (body.password == "Incorrecta1") http(401, "INVALID_CREDENTIALS")
        val user = UserDto("u_${nextId++}", body.email.substringBefore('@'), body.email)
        state = demoState(user)
        return SessionDto(TOKEN, user, onboardingCompleted = true)
    }

    override suspend fun logout() = delay(NORMAL_MS)

    // Nunca adelanta el onboarding: AuthRepository conserva el valor guardado si es más avanzado.
    override suspend fun me(): MeDto {
        delay(NORMAL_MS)
        return MeDto(s.user, onboardingCompleted = false)
    }

    override suspend fun deleteMe() {
        delay(NORMAL_MS)
        state = null
    }

    // ---------------------------------------------------------------- Perfil

    override suspend fun getProfile(): ProfileDto {
        delay(NORMAL_MS)
        val profile = s.profile ?: http(404, "NOT_FOUND")
        return profile.copy(currentWeightKg = latestWeight())
    }

    override suspend fun putProfile(body: ProfileDto): ProfileDto {
        delay(NORMAL_MS)
        if (body.injuries.orEmpty().contains("#falla")) http(500)
        s.profile = body.copy(currentWeightKg = null)
        return body.copy(currentWeightKg = latestWeight())
    }

    // ---------------------------------------------------------------- Rutina

    override suspend fun getRoutine(): RoutineDto {
        delay(NORMAL_MS)
        return s.routine ?: http(404, "NO_ACTIVE_PLAN")
    }

    // shortcut: siempre devuelve la misma rutina de 3 días sin mirar el perfil; basta para recorrer la UI.
    override suspend fun generate(body: GenerateRequest): RoutineDto {
        ai(body.comment)
        val current = s.routine
        return asset<RoutineDto>("routine")
            .copy(id = "r_${nextId++}", version = (current?.version ?: 0) + 1, updatedAt = Instant.now().toString())
            .also { s.routine = it }
    }

    override suspend fun workoutLogs(from: String, to: String): List<WorkoutLogDto> {
        delay(NORMAL_MS)
        return s.logs.values.filter { it.date in from..to }.sortedBy { it.date }
    }

    override suspend fun setExercise(date: String, exerciseId: String, body: CompletedRequest): WorkoutLogDto {
        delay(NORMAL_MS)
        val log = s.logs[date] ?: WorkoutLogDto(date)
        if (date != Today.date().toString() || log.sessionFinished) http(400, "INVALID_DATE")
        val day = s.routine?.days?.firstOrNull { it.dayOfWeek == LocalDate.parse(date).dayOfWeek.name }
        if (day?.exercises?.none { it.id == exerciseId } != false) http(404, "NOT_FOUND")
        val ids = if (body.completed) log.completedExerciseIds + exerciseId else log.completedExerciseIds - exerciseId
        return log.copy(completedExerciseIds = ids.distinct()).also { s.logs[date] = it }
    }

    override suspend fun finish(date: String): WorkoutLogDto {
        delay(NORMAL_MS)
        val log = s.logs[date] ?: WorkoutLogDto(date)
        if (log.sessionFinished) return log
        val day = s.routine?.days?.firstOrNull { it.dayOfWeek == LocalDate.parse(date).dayOfWeek.name }
        if (date != Today.date().toString() || day?.type != "TRAINING") http(400, "INVALID_DATE")
        return log.copy(sessionFinished = true).also { s.logs[date] = it }
    }

    override suspend fun alternative(exerciseId: String, body: AlternativeRequestDto): ExerciseProposalDto {
        ai(body.comment)
        val proposal = asset<ExerciseProposalDto>("exercise_alternative")
        val id = "p_${nextId++}"
        val exercise = proposal.exercise.copy(id = "e_alt_${nextId++}")
        exerciseProposals[id] = exercise
        return ExerciseProposalDto(id, exercise)
    }

    override suspend fun accept(exerciseId: String, proposalId: String): RoutineDto {
        delay(NORMAL_MS)
        val exercise = exerciseProposals.remove(proposalId) ?: http(409, "PROPOSAL_EXPIRED")
        val routine = s.routine ?: http(404, "NO_ACTIVE_PLAN")
        val days = routine.days.map { day -> day.copy(exercises = day.exercises.map { if (it.id == exerciseId) exercise else it }) }
        return routine.copy(version = routine.version + 1, updatedAt = Instant.now().toString(), days = days).also { s.routine = it }
    }

    // ---------------------------------------------------------------- Dieta

    // DietApi y RoutineApi comparten firmas (generate/alternative/accept), así que la dieta va en un objeto aparte.
    val diet: DietApi = DietFake()

    private inner class DietFake : DietApi {
        override suspend fun getDiet(): DietDto {
            delay(NORMAL_MS)
            return s.diet ?: http(404, "NO_ACTIVE_PLAN")
        }

        override suspend fun generate(body: GenerateRequest): DietDto {
            ai(body.comment)
            if (s.profile?.dislikedFoods.orEmpty().contains("#sindieta")) http(503, "AI_UNAVAILABLE")
            val current = s.diet
            return asset<DietDto>("diet")
                .copy(id = "d_${nextId++}", version = (current?.version ?: 0) + 1, updatedAt = Instant.now().toString())
                .also { s.diet = it }
        }

        override suspend fun alternative(mealId: String, body: AlternativeRequestDto): RecipeProposalDto {
            ai(body.comment)
            // Simula un JSON sin `recipe`: el parser lanza la misma excepción que con una respuesta real incompleta.
            if (body.comment.orEmpty().contains("#roto")) return json.decodeFromString("""{"proposalId":"p_roto"}""")
            val proposal = asset<RecipeProposalDto>("recipe_alternative").copy(proposalId = "p_${nextId++}")
            recipeProposals[proposal.proposalId] = proposal
            return proposal
        }

        override suspend fun accept(mealId: String, proposalId: String): DietDto {
            delay(NORMAL_MS)
            val proposal = recipeProposals.remove(proposalId) ?: http(409, "PROPOSAL_EXPIRED")
            val diet = s.diet ?: http(404, "NO_ACTIVE_PLAN")
            val days = diet.days.map { day -> day.copy(meals = day.meals.map { if (it.id == mealId) it.copy(recipe = proposal.recipe) else it }) }
            return diet.copy(version = diet.version + 1, updatedAt = Instant.now().toString(), days = days).also { s.diet = it }
        }
    }

    // ---------------------------------------------------------------- Progreso

    override suspend fun weights(): List<WeightDto> {
        delay(NORMAL_MS)
        return s.weights.entries.sortedByDescending { it.key }.map { WeightDto(it.key, it.value) }
    }

    override suspend fun putWeight(date: String, body: WeightRequest): WeightDto {
        delay(NORMAL_MS)
        if (body.weightKg == 299.9) http(401, "UNAUTHORIZED")
        if (LocalDate.parse(date).isAfter(Today.date())) http(400, "FUTURE_DATE")
        if (body.weightKg !in 30.0..300.0) http(400, "VALIDATION_ERROR")
        s.weights[date] = body.weightKg
        return WeightDto(date, body.weightKg)
    }

    override suspend fun summary(weekStart: String): SummaryDto {
        delay(NORMAL_MS)
        val weekEnd = LocalDate.parse(weekStart).plusDays(6).toString()
        return SummaryDto(
            weekStart = weekStart,
            completedSessions = s.logs.values.count { it.sessionFinished && it.date in weekStart..weekEnd },
            scheduledSessions = s.routine?.days?.count { it.type == "TRAINING" } ?: 0,
            weekStreak = s.weekStreak,
        )
    }

    // ---------------------------------------------------------------- Ayudas

    private fun demoState(user: UserDto) = State(
        user = user,
        profile = asset("profile"),
        routine = asset("routine"),
        diet = asset("diet"),
        weights = asset<List<WeightDto>>("weights").associate { it.date to it.weightKg }.toMutableMap(),
        weekStreak = asset<SummaryDto>("summary").weekStreak,
    )

    private fun latestWeight() = s.weights.maxByOrNull { it.key }?.value

    private inline fun <reified T> asset(name: String): T =
        json.decodeFromString(context.assets.open("mock/$name.json").bufferedReader().use { it.readText() })

    private suspend fun ai(comment: String?) {
        delay(if (comment.orEmpty().contains("#lento")) SLOW_AI_MS else AI_MS)
        if (comment.orEmpty().contains("#falla")) http(503, "AI_UNAVAILABLE")
    }

    private fun http(code: Int, errorCode: String? = null): Nothing {
        val body = errorCode?.let { """{"code":"$it"}""" }.orEmpty()
        throw HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))
    }

    private companion object {
        const val TOKEN = "mock-token"
        const val NORMAL_MS = 800L
        const val AI_MS = 5_000L
        const val SLOW_AI_MS = 55_000L
    }
}
