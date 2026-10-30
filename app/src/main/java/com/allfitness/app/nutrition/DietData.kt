package com.allfitness.app.nutrition

import com.allfitness.app.core.network.LongRunning
import com.allfitness.app.routines.AlternativeRequestDto
import com.allfitness.app.routines.GenerateRequest
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.DayOfWeek
import java.time.Instant

// ---- Modelos de dominio (data-model.md) ----

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

enum class MeasureUnit { G, ML }

data class Diet(val id: String, val version: Int, val updatedAt: Instant, val days: List<DietDay>)

data class DietDay(val dayOfWeek: DayOfWeek, val totals: Nutrition, val meals: List<Meal>)

data class Meal(val id: String, val type: MealType, val recipe: Recipe)

data class Recipe(
    val name: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val prepMinutes: Int,
    val perServing: Nutrition,
)

data class Ingredient(val name: String, val amount: Double, val unit: MeasureUnit)

data class Nutrition(val kcal: Int, val proteinG: Double, val carbsG: Double, val fatG: Double)

data class RecipeProposal(val proposalId: String, val recipe: Recipe)

// ---- DTO del contrato (contracts/diet.md) ----

@Serializable
data class DietDto(val id: String, val version: Int, val updatedAt: String, val days: List<DietDayDto>)

@Serializable
data class DietDayDto(val dayOfWeek: String, val totals: NutritionDto, val meals: List<MealDto>)

@Serializable
data class MealDto(val id: String, val type: String, val recipe: RecipeDto)

@Serializable
data class RecipeDto(
    val name: String,
    val ingredients: List<IngredientDto>,
    val steps: List<String>,
    val prepMinutes: Int,
    val perServing: NutritionDto,
)

@Serializable
data class IngredientDto(val name: String, val amount: Double, val unit: String)

@Serializable
data class NutritionDto(val kcal: Int, val proteinG: Double, val carbsG: Double, val fatG: Double)

@Serializable
data class RecipeProposalDto(val proposalId: String, val recipe: RecipeDto)

interface DietApi {
    @GET("diet")
    suspend fun getDiet(): DietDto

    @LongRunning
    @POST("diet/generate")
    suspend fun generate(@Body body: GenerateRequest): DietDto

    @LongRunning
    @POST("diet/meals/{mealId}/alternative")
    suspend fun alternative(@Path("mealId") mealId: String, @Body body: AlternativeRequestDto): RecipeProposalDto

    @POST("diet/meals/{mealId}/alternative/{proposalId}/accept")
    suspend fun accept(@Path("mealId") mealId: String, @Path("proposalId") proposalId: String): DietDto
}

// ---- Mapeo DTO -> dominio: los totales se copian tal cual, la app no suma nada (FR-017) ----

fun DietDto.toDomain(): Diet {
    val mapped = days.map { day ->
        require(day.meals.isNotEmpty()) { "Día sin comidas" }
        DietDay(DayOfWeek.valueOf(day.dayOfWeek), day.totals.toDomain(), day.meals.map { it.toDomain() })
    }.sortedBy { it.dayOfWeek }
    require(mapped.map { it.dayOfWeek } == DayOfWeek.entries) { "La dieta debe tener 7 días, de lunes a domingo" }
    return Diet(id, version, Instant.parse(updatedAt), mapped)
}

private fun MealDto.toDomain() = Meal(id, MealType.valueOf(type), recipe.toDomain())

fun RecipeDto.toDomain() = Recipe(
    name = name,
    ingredients = ingredients.map { Ingredient(it.name, it.amount, MeasureUnit.valueOf(it.unit)) },
    steps = steps,
    prepMinutes = prepMinutes,
    perServing = perServing.toDomain(),
)

private fun NutritionDto.toDomain() = Nutrition(kcal, proteinG, carbsG, fatG)
