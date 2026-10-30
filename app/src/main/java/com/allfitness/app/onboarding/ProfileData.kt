package com.allfitness.app.onboarding

import com.allfitness.app.core.database.Cached
import com.allfitness.app.core.database.SnapshotKey
import com.allfitness.app.core.database.SnapshotStore
import com.allfitness.app.core.database.mapData
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.SafeApiCall
import com.allfitness.app.core.network.map
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import javax.inject.Inject
import javax.inject.Singleton

// ---- Modelos de dominio (data-model.md) ----

enum class Sex { MALE, FEMALE }
enum class Goal { LOSE_FAT, GAIN_MUSCLE, GENERAL_FITNESS }
enum class Level { BEGINNER, INTERMEDIATE, ADVANCED }
enum class Location { HOME, GYM }
enum class Equipment { BODYWEIGHT, DUMBBELLS, BARBELL, KETTLEBELL, RESISTANCE_BANDS, BENCH, PULL_UP_BAR, GYM_MACHINES }
enum class DietType { OMNIVORE, VEGETARIAN, VEGAN }
enum class Allergy { GLUTEN, LACTOSE, TREE_NUTS, PEANUTS, SHELLFISH, EGG, SOY, OTHER }

data class Profile(
    val age: Int,
    val sex: Sex,
    val heightCm: Int,
    /** Solo lectura: es el registro de peso más reciente (FR-026a). */
    val currentWeightKg: Double?,
    val goal: Goal,
    val level: Level,
    val daysPerWeek: Int,
    val sessionMinutes: Int,
    val location: Location,
    val equipment: Set<Equipment>,
    val injuries: String?,
    val dietType: DietType,
    val allergies: Set<Allergy>,
    val otherAllergy: String?,
    val dislikedFoods: String?,
    val mealsPerDay: Int,
)

// ---- DTO del contrato (contracts/profile.md) ----

@Serializable
data class ProfileDto(
    val age: Int,
    val sex: Sex,
    val heightCm: Int,
    val currentWeightKg: Double? = null,
    val goal: Goal,
    val level: Level,
    val daysPerWeek: Int,
    val sessionMinutes: Int,
    val location: Location,
    val equipment: List<Equipment>,
    val injuries: String? = null,
    val dietType: DietType,
    val allergies: List<Allergy> = emptyList(),
    val otherAllergy: String? = null,
    val dislikedFoods: String? = null,
    val mealsPerDay: Int,
)

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): ProfileDto

    @PUT("profile")
    suspend fun putProfile(@Body body: ProfileDto): ProfileDto
}

fun ProfileDto.toDomain() = Profile(
    age, sex, heightCm, currentWeightKg, goal, level, daysPerWeek, sessionMinutes, location,
    equipment.toSet(), injuries, dietType, allergies.toSet(), otherAllergy, dislikedFoods, mealsPerDay,
)

/** El peso no viaja en el PUT: se guarda aparte como registro de peso (contracts/profile.md). */
fun Profile.toDto() = ProfileDto(
    age, sex, heightCm, null, goal, level, daysPerWeek, sessionMinutes, location,
    equipment.toList(), injuries?.ifBlank { null }, dietType, allergies.toList(),
    otherAllergy?.ifBlank { null }, dislikedFoods?.ifBlank { null }, mealsPerDay,
)

@Singleton
class ProfileRepository @Inject constructor(
    private val api: ProfileApi,
    private val safeApiCall: SafeApiCall,
    private val snapshots: SnapshotStore,
) {
    val profile: Flow<Cached<Profile>?> = snapshots.observe<ProfileDto>(SnapshotKey.PROFILE).mapData { it.toDomain() }

    suspend fun refresh(): AppResult<Unit> =
        safeApiCall { api.getProfile() }.map { snapshots.save(SnapshotKey.PROFILE, it) }

    /** Guarda el perfil completo; no regenera los planes (FR-030). */
    suspend fun save(profile: Profile): AppResult<Unit> =
        safeApiCall { api.putProfile(profile.toDto()) }.map { snapshots.save(SnapshotKey.PROFILE, it) }
}
