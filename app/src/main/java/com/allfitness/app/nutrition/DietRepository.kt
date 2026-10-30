package com.allfitness.app.nutrition

import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.database.SnapshotKey
import com.allfitness.app.core.database.SnapshotStore
import com.allfitness.app.core.database.mapData
import com.allfitness.app.core.network.AppError
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.SafeApiCall
import com.allfitness.app.core.network.map
import com.allfitness.app.routines.AlternativeRequestDto
import com.allfitness.app.routines.GenerateRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietRepository @Inject constructor(
    private val api: DietApi,
    private val safeApiCall: SafeApiCall,
    private val snapshots: SnapshotStore,
) {
    val diet: Flow<Cached<Diet>?> = snapshots.observe<DietDto>(SnapshotKey.DIET).mapData { it.toDomain() }

    suspend fun refresh(): AppResult<Unit> {
        val result = safeApiCall { api.getDiet().also { it.toDomain() } }
        if (result is AppResult.Failure && result.error == AppError.NoActivePlan) snapshots.delete(SnapshotKey.DIET)
        return result.map { snapshots.save(SnapshotKey.DIET, it) }
    }

    /** La dieta actual solo se reemplaza si la nueva llega bien (FR-022). */
    suspend fun generate(comment: String?): AppResult<Unit> =
        safeApiCall { api.generate(GenerateRequest(comment)).also { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.DIET, it) }

    suspend fun requestAlternative(mealId: String, reason: String, comment: String?): AppResult<RecipeProposal> =
        safeApiCall {
            val dto = api.alternative(mealId, AlternativeRequestDto(reason, comment))
            RecipeProposal(dto.proposalId, dto.recipe.toDomain())
        }

    suspend fun acceptAlternative(mealId: String, proposalId: String): AppResult<Unit> =
        safeApiCall { api.accept(mealId, proposalId).also { it.toDomain() } }
            .map { snapshots.save(SnapshotKey.DIET, it) }
}
