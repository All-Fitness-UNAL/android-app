package com.allfitness.app.onboarding

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.allfitness.app.R
import com.allfitness.app.core.session.appDataStore
import com.allfitness.app.core.ui.FREE_TEXT_MAX
import com.allfitness.app.core.ui.formatNumber
import com.allfitness.app.core.ui.parseOneDecimal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

enum class ProfileSection { PHYSICAL, GOAL, TRAINING, NUTRITION }

enum class ProfileField {
    AGE, SEX, HEIGHT, WEIGHT, GOAL, LEVEL, DAYS, MINUTES, LOCATION, EQUIPMENT, INJURIES,
    DIET_TYPE, OTHER_ALLERGY, DISLIKED, MEALS,
}

const val OTHER_ALLERGY_MAX = 100

/** Estado editable del perfil: lo comparten el onboarding y la edición en el Perfil. */
@Serializable
data class ProfileForm(
    val age: String = "",
    val sex: Sex? = null,
    val heightCm: String = "",
    val weightKg: String = "",
    val goal: Goal? = null,
    val level: Level? = null,
    val daysPerWeek: Int? = null,
    val sessionMinutes: String = "",
    val location: Location? = null,
    val equipment: Set<Equipment> = emptySet(),
    val injuries: String = "",
    val dietType: DietType? = null,
    val allergies: Set<Allergy> = emptySet(),
    val otherAllergy: String = "",
    val dislikedFoods: String = "",
    val mealsPerDay: Int? = null,
) {
    val weight: Double? get() = parseOneDecimal(weightKg)

    /** Solo se llama con todas las secciones validadas. */
    fun toProfile() = Profile(
        age = age.trim().toInt(),
        sex = requireNotNull(sex),
        heightCm = heightCm.trim().toInt(),
        currentWeightKg = weight,
        goal = requireNotNull(goal),
        level = requireNotNull(level),
        daysPerWeek = requireNotNull(daysPerWeek),
        sessionMinutes = sessionMinutes.trim().toInt(),
        location = requireNotNull(location),
        equipment = equipment,
        injuries = injuries.trim(),
        dietType = requireNotNull(dietType),
        allergies = allergies,
        otherAllergy = otherAllergy.trim().takeIf { Allergy.OTHER in allergies },
        dislikedFoods = dislikedFoods.trim(),
        mealsPerDay = requireNotNull(mealsPerDay),
    )
}

fun Profile.toForm(weightKg: Double?) = ProfileForm(
    age = age.toString(),
    sex = sex,
    heightCm = heightCm.toString(),
    weightKg = weightKg?.let { formatNumber(it) }.orEmpty(),
    goal = goal,
    level = level,
    daysPerWeek = daysPerWeek,
    sessionMinutes = sessionMinutes.toString(),
    location = location,
    equipment = equipment,
    injuries = injuries.orEmpty(),
    dietType = dietType,
    allergies = allergies,
    otherAllergy = otherAllergy.orEmpty(),
    dislikedFoods = dislikedFoods.orEmpty(),
    mealsPerDay = mealsPerDay,
)

/** Rangos de data-model.md (FR-007, FR-008). Devuelve el error de cada campo inválido de la sección. */
object ProfileValidation {
    fun validate(section: ProfileSection, form: ProfileForm): Map<ProfileField, Int> = buildMap {
        when (section) {
            ProfileSection.PHYSICAL -> {
                if (form.age.trim().toIntOrNull() !in 16..100) put(ProfileField.AGE, R.string.error_age_range)
                if (form.sex == null) put(ProfileField.SEX, R.string.error_required_choice)
                if (form.heightCm.trim().toIntOrNull() !in 100..250) put(ProfileField.HEIGHT, R.string.error_height_range)
                if (!isValidWeight(form.weight)) put(ProfileField.WEIGHT, R.string.error_weight_range)
            }
            ProfileSection.GOAL -> if (form.goal == null) put(ProfileField.GOAL, R.string.error_required_choice)
            ProfileSection.TRAINING -> {
                if (form.level == null) put(ProfileField.LEVEL, R.string.error_required_choice)
                if (form.daysPerWeek !in 1..7) put(ProfileField.DAYS, R.string.error_days_range)
                if (form.sessionMinutes.trim().toIntOrNull() !in 15..120) put(ProfileField.MINUTES, R.string.error_minutes_range)
                if (form.location == null) put(ProfileField.LOCATION, R.string.error_required_choice)
                if (form.equipment.isEmpty()) put(ProfileField.EQUIPMENT, R.string.error_equipment_required)
                if (form.injuries.length > FREE_TEXT_MAX) put(ProfileField.INJURIES, R.string.error_text_too_long)
            }
            ProfileSection.NUTRITION -> {
                if (form.dietType == null) put(ProfileField.DIET_TYPE, R.string.error_required_choice)
                if (Allergy.OTHER in form.allergies && form.otherAllergy.trim().length !in 1..OTHER_ALLERGY_MAX) {
                    put(ProfileField.OTHER_ALLERGY, R.string.error_other_allergy)
                }
                if (form.dislikedFoods.length > FREE_TEXT_MAX) put(ProfileField.DISLIKED, R.string.error_text_too_long)
                if (form.mealsPerDay !in 3..5) put(ProfileField.MEALS, R.string.error_meals_range)
            }
        }
    }

    fun isValidWeight(kg: Double?) = kg != null && kg in 30.0..300.0
}

@Serializable
data class OnboardingDraft(val form: ProfileForm, val step: Int)

/** Borrador del onboarding en el dispositivo para retomarlo donde quedó (FR-009, research R7). */
@Singleton
class OnboardingDraftStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    suspend fun load(): OnboardingDraft? = context.appDataStore.data.first()[KEY]
        ?.let { runCatching { json.decodeFromString<OnboardingDraft>(it) }.getOrNull() }

    suspend fun save(draft: OnboardingDraft) {
        context.appDataStore.edit { it[KEY] = json.encodeToString(OnboardingDraft.serializer(), draft) }
    }

    suspend fun clear() {
        context.appDataStore.edit { it.remove(KEY) }
    }

    private companion object {
        val KEY = stringPreferencesKey("onboarding_draft")
    }
}
