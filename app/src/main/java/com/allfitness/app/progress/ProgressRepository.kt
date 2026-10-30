package com.allfitness.app.progress

import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.database.SnapshotKey
import com.allfitness.app.core.database.SnapshotStore
import com.allfitness.app.core.database.mapData
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.SafeApiCall
import com.allfitness.app.core.network.map
import com.allfitness.app.core.time.Today
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val api: ProgressApi,
    private val safeApiCall: SafeApiCall,
    private val snapshots: SnapshotStore,
) {
    /** Historial de peso, del más reciente al más antiguo. Es la única fuente del peso (FR-026a). */
    val weights: Flow<Cached<List<WeightEntry>>?> = snapshots.observe<List<WeightDto>>(SnapshotKey.WEIGHTS)
        .mapData { list -> list.map { it.toDomain() }.sortedByDescending { it.date } }

    val summary: Flow<Cached<TrainingSummary>?> =
        snapshots.observe<SummaryDto>(SnapshotKey.SUMMARY).mapData { it.toDomain() }

    suspend fun refresh(): AppResult<Unit> {
        val weightsResult = safeApiCall { api.weights().onEach { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.WEIGHTS, it) }
        if (weightsResult is AppResult.Failure) return weightsResult
        return safeApiCall { api.summary(Today.weekStart().toString()).also { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.SUMMARY, it) }
    }

    /** Crea o reemplaza el único registro de [date] (FR-025). */
    suspend fun upsertWeight(date: LocalDate, weightKg: Double): AppResult<Unit> =
        safeApiCall { api.putWeight(date.toString(), WeightRequest(weightKg)) }.map { saved ->
            val current = snapshots.get<List<WeightDto>>(SnapshotKey.WEIGHTS).orEmpty()
            snapshots.save(SnapshotKey.WEIGHTS, current.filterNot { it.date == saved.date } + saved)
        }
}
