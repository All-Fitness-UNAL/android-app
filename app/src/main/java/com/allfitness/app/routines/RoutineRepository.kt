package com.allfitness.app.routines

import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.database.SnapshotKey
import com.allfitness.app.core.database.SnapshotStore
import com.allfitness.app.core.database.mapData
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.SafeApiCall
import com.allfitness.app.core.network.map
import com.allfitness.app.core.time.Today
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val api: RoutineApi,
    private val safeApiCall: SafeApiCall,
    private val snapshots: SnapshotStore,
) {
    val routine: Flow<Cached<Routine>?> = snapshots.observe<RoutineDto>(SnapshotKey.ROUTINE).mapData { it.toDomain() }

    val weekLogs: Flow<Cached<List<WorkoutLog>>?> =
        snapshots.observe<List<WorkoutLogDto>>(SnapshotKey.WORKOUT_WEEK).mapData { logs -> logs.map { it.toDomain() } }

    /** Rutina y registros de la semana; con 404 NO_ACTIVE_PLAN se borra la caché para mostrar el estado vacío. */
    suspend fun refresh(): AppResult<Unit> {
        val result = safeApiCall { api.getRoutine().also { it.toDomain() } }
        return when (result) {
            is AppResult.Success -> {
                snapshots.save(SnapshotKey.ROUTINE, result.data)
                refreshWeekLogs()
            }
            is AppResult.Failure -> {
                if (result.error == AppError.NoActivePlan) snapshots.delete(SnapshotKey.ROUTINE)
                result
            }
        }
    }

    private suspend fun refreshWeekLogs(): AppResult<Unit> {
        val week = Today.weekDates()
        return safeApiCall { api.workoutLogs(week.first().toString(), week.last().toString()) }
            .map { snapshots.save(SnapshotKey.WORKOUT_WEEK, it) }
    }

    /** La rutina actual solo se reemplaza si la nueva llega bien (FR-022). */
    suspend fun generate(comment: String?): AppResult<Unit> =
        safeApiCall { api.generate(GenerateRequest(comment)).also { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.ROUTINE, it) }

    suspend fun setExerciseCompleted(date: LocalDate, exerciseId: String, completed: Boolean): AppResult<Unit> =
        safeApiCall { api.setExercise(date.toString(), exerciseId, CompletedRequest(completed)) }.map { saveLog(it) }

    suspend fun finishSession(date: LocalDate): AppResult<Unit> =
        safeApiCall { api.finish(date.toString()) }.map { saveLog(it) }

    suspend fun requestAlternative(exerciseId: String, reason: String, comment: String?): AppResult<ExerciseProposal> =
        safeApiCall {
            val dto = api.alternative(exerciseId, AlternativeRequestDto(reason, comment))
            ExerciseProposal(dto.proposalId, requireNotNull(dto.exercise.toDomainOrNull()))
        }

    suspend fun acceptAlternative(exerciseId: String, proposalId: String): AppResult<Unit> =
        safeApiCall { api.accept(exerciseId, proposalId).also { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.ROUTINE, it) }

    private suspend fun saveLog(log: WorkoutLogDto) {
        val current = snapshots.get<List<WorkoutLogDto>>(SnapshotKey.WORKOUT_WEEK).orEmpty()
        snapshots.save(SnapshotKey.WORKOUT_WEEK, current.filterNot { it.date == log.date } + log)
    }
}
