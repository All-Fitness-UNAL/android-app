package com.allfitness.app.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import com.allfitness.app.R
import com.allfitness.app.core.ui.ChoiceCard
import com.allfitness.app.core.ui.FREE_TEXT_MAX
import com.allfitness.app.core.ui.FormTextField
import com.allfitness.app.core.ui.MultiChoiceChips
import com.allfitness.app.core.ui.SegmentedChoice
import com.allfitness.app.core.ui.SingleChoiceChips
import com.allfitness.app.core.ui.stripColumns
import com.allfitness.app.core.ui.theme.Spacing

@StringRes
fun ProfileSection.titleRes(): Int = when (this) {
    ProfileSection.PHYSICAL -> R.string.section_physical
    ProfileSection.GOAL -> R.string.section_goal
    ProfileSection.TRAINING -> R.string.section_training
    ProfileSection.NUTRITION -> R.string.section_nutrition
}

@StringRes
fun Enum<*>.labelRes(): Int = when (this) {
    Sex.MALE -> R.string.sex_male
    Sex.FEMALE -> R.string.sex_female
    Goal.LOSE_FAT -> R.string.goal_lose_fat
    Goal.GAIN_MUSCLE -> R.string.goal_gain_muscle
    Goal.GENERAL_FITNESS -> R.string.goal_general_fitness
    Level.BEGINNER -> R.string.level_beginner
    Level.INTERMEDIATE -> R.string.level_intermediate
    Level.ADVANCED -> R.string.level_advanced
    Location.HOME -> R.string.location_home
    Location.GYM -> R.string.location_gym
    Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
    Equipment.DUMBBELLS -> R.string.equipment_dumbbells
    Equipment.BARBELL -> R.string.equipment_barbell
    Equipment.KETTLEBELL -> R.string.equipment_kettlebell
    Equipment.RESISTANCE_BANDS -> R.string.equipment_bands
    Equipment.BENCH -> R.string.equipment_bench
    Equipment.PULL_UP_BAR -> R.string.equipment_pull_up_bar
    Equipment.GYM_MACHINES -> R.string.equipment_machines
    DietType.OMNIVORE -> R.string.diet_omnivore
    DietType.VEGETARIAN -> R.string.diet_vegetarian
    DietType.VEGAN -> R.string.diet_vegan
    Allergy.GLUTEN -> R.string.allergy_gluten
    Allergy.LACTOSE -> R.string.allergy_lactose
    Allergy.TREE_NUTS -> R.string.allergy_tree_nuts
    Allergy.PEANUTS -> R.string.allergy_peanuts
    Allergy.SHELLFISH -> R.string.allergy_shellfish
    Allergy.EGG -> R.string.allergy_egg
    Allergy.SOY -> R.string.allergy_soy
    Allergy.OTHER -> R.string.allergy_other
    else -> error("Sin etiqueta para $this")
}

@Composable
private fun Enum<*>.label() = stringResource(labelRes())

/** Formulario de una sección; sin estado, lo usan el onboarding y la edición del Perfil. */
@Composable
fun ProfileSectionForm(
    section: ProfileSection,
    form: ProfileForm,
    errors: Map<ProfileField, Int>,
    onChange: (ProfileForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        when (section) {
            ProfileSection.PHYSICAL -> PhysicalSection(form, errors, onChange)
            ProfileSection.GOAL -> GoalGrid(form.goal, { onChange(form.copy(goal = it)) }, errors[ProfileField.GOAL])
            ProfileSection.TRAINING -> TrainingSection(form, errors, onChange)
            ProfileSection.NUTRITION -> NutritionSection(form, errors, onChange)
        }
    }
}

@DrawableRes
private fun Goal.iconRes(): Int = when (this) {
    Goal.LOSE_FAT -> R.drawable.lc_trending_down
    Goal.GAIN_MUSCLE -> R.drawable.lc_trophy
    Goal.GENERAL_FITNESS -> R.drawable.lc_heart
}

/**
 * Figma `goals-grid`: tarjetas de elección única en 2 columnas (la tercera sola en su fila). Si con fuente
 * grande una palabra no cabe en media fila, pasa a 1 columna en vez de partirla.
 */
@Composable
private fun GoalGrid(selected: Goal?, onSelect: (Goal) -> Unit, @StringRes error: Int?) {
    val labels = Goal.entries.map { it.label() }
    val measurer = rememberTextMeasurer()
    val typography = MaterialTheme.typography
    val density = LocalDensity.current
    val widest = remember(labels, typography, density, measurer) {
        // Margen de la tarjeta a cada lado más el hueco entre tarjetas.
        val padding = with(density) { (Spacing.m * 2 + Spacing.sm).toPx() }
        labels.flatMap { it.split(' ') }.maxOf { measurer.measure(it, typography.titleMedium).size.width } + padding
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(stringResource(R.string.field_goal), style = typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = minOf(2, stripColumns(Goal.entries.size, constraints.maxWidth.toFloat(), widest))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Goal.entries.chunked(columns).forEach { row ->
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        row.forEach { goal ->
                            ChoiceCard(
                                text = labels[goal.ordinal],
                                icon = goal.iconRes(),
                                selected = goal == selected,
                                onClick = { onSelect(goal) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        if (error != null) {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = typography.bodySmall)
        }
    }
}

@Composable
private fun PhysicalSection(form: ProfileForm, errors: Map<ProfileField, Int>, onChange: (ProfileForm) -> Unit) {
    FormTextField(
        value = form.age,
        onValueChange = { onChange(form.copy(age = it)) },
        label = stringResource(R.string.field_age),
        error = errors[ProfileField.AGE],
        keyboardType = KeyboardType.Number,
        suffix = stringResource(R.string.unit_years),
        hint = stringResource(R.string.hint_age),
        maxLength = 3,
    )
    SingleChoiceChips(
        label = stringResource(R.string.field_sex),
        options = Sex.entries,
        selected = form.sex,
        onSelect = { onChange(form.copy(sex = it)) },
        optionLabel = { it.label() },
        error = errors[ProfileField.SEX],
    )
    FormTextField(
        value = form.heightCm,
        onValueChange = { onChange(form.copy(heightCm = it)) },
        label = stringResource(R.string.field_height),
        error = errors[ProfileField.HEIGHT],
        keyboardType = KeyboardType.Number,
        suffix = stringResource(R.string.unit_cm),
        hint = stringResource(R.string.hint_height),
        maxLength = 3,
    )
    FormTextField(
        value = form.weightKg,
        onValueChange = { onChange(form.copy(weightKg = it)) },
        label = stringResource(R.string.field_weight),
        error = errors[ProfileField.WEIGHT],
        keyboardType = KeyboardType.Decimal,
        suffix = stringResource(R.string.unit_kg),
        hint = stringResource(R.string.hint_weight),
        maxLength = 5,
    )
}

@Composable
private fun TrainingSection(form: ProfileForm, errors: Map<ProfileField, Int>, onChange: (ProfileForm) -> Unit) {
    SingleChoiceChips(
        label = stringResource(R.string.field_level),
        options = Level.entries,
        selected = form.level,
        onSelect = { onChange(form.copy(level = it)) },
        optionLabel = { it.label() },
        error = errors[ProfileField.LEVEL],
    )
    SegmentedChoice(
        label = stringResource(R.string.field_days),
        options = (1..7).toList(),
        selected = form.daysPerWeek,
        onSelect = { onChange(form.copy(daysPerWeek = it)) },
        optionLabel = { it.toString() },
        error = errors[ProfileField.DAYS],
    )
    FormTextField(
        value = form.sessionMinutes,
        onValueChange = { onChange(form.copy(sessionMinutes = it)) },
        label = stringResource(R.string.field_minutes),
        error = errors[ProfileField.MINUTES],
        keyboardType = KeyboardType.Number,
        suffix = stringResource(R.string.unit_minutes),
        hint = stringResource(R.string.hint_minutes),
        maxLength = 3,
    )
    SingleChoiceChips(
        label = stringResource(R.string.field_location),
        options = Location.entries,
        selected = form.location,
        onSelect = { onChange(form.copy(location = it)) },
        optionLabel = { it.label() },
        error = errors[ProfileField.LOCATION],
    )
    MultiChoiceChips(
        label = stringResource(R.string.field_equipment),
        options = Equipment.entries,
        selected = form.equipment,
        onToggle = { onChange(form.copy(equipment = form.equipment.toggle(it))) },
        optionLabel = { it.label() },
        error = errors[ProfileField.EQUIPMENT],
    )
    FormTextField(
        value = form.injuries,
        onValueChange = { onChange(form.copy(injuries = it)) },
        label = stringResource(R.string.field_injuries),
        error = errors[ProfileField.INJURIES],
        maxLength = FREE_TEXT_MAX,
        singleLine = false,
    )
}

@Composable
private fun NutritionSection(form: ProfileForm, errors: Map<ProfileField, Int>, onChange: (ProfileForm) -> Unit) {
    SingleChoiceChips(
        label = stringResource(R.string.field_diet_type),
        options = DietType.entries,
        selected = form.dietType,
        onSelect = { onChange(form.copy(dietType = it)) },
        optionLabel = { it.label() },
        error = errors[ProfileField.DIET_TYPE],
    )
    MultiChoiceChips(
        label = stringResource(R.string.field_allergies),
        options = Allergy.entries,
        selected = form.allergies,
        onToggle = { onChange(form.copy(allergies = form.allergies.toggle(it))) },
        optionLabel = { it.label() },
    )
    if (Allergy.OTHER in form.allergies) {
        FormTextField(
            value = form.otherAllergy,
            onValueChange = { onChange(form.copy(otherAllergy = it)) },
            label = stringResource(R.string.field_other_allergy),
            error = errors[ProfileField.OTHER_ALLERGY],
            maxLength = OTHER_ALLERGY_MAX,
        )
    }
    FormTextField(
        value = form.dislikedFoods,
        onValueChange = { onChange(form.copy(dislikedFoods = it)) },
        label = stringResource(R.string.field_disliked_foods),
        error = errors[ProfileField.DISLIKED],
        maxLength = FREE_TEXT_MAX,
        singleLine = false,
    )
    SegmentedChoice(
        label = stringResource(R.string.field_meals),
        options = (3..5).toList(),
        selected = form.mealsPerDay,
        onSelect = { onChange(form.copy(mealsPerDay = it)) },
        optionLabel = { it.toString() },
        error = errors[ProfileField.MEALS],
    )
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
