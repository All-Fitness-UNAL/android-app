# Data Model: All Fitness MVP

Este documento describe los modelos de dominio de la app, es decir, lo que reciben ViewModels y pantallas. Los DTO de red tienen la misma forma que el JSON de `contracts/`, y cada Repository los mapea a estos modelos. Las reglas de negocio (calorías, rachas, progresiones) son del backend; la app solo valida formato y rangos de entrada.

## Sesión y usuario

| Modelo | Campos | Notas |
|--------|--------|-------|
| `User` | `id: String`, `name: String`, `email: String` | El correo es de solo lectura en el Perfil. |
| `Session` | `token: String`, `user: User`, `onboardingCompleted: Boolean` | Se guarda en DataStore. Decide la ruta inicial: Login → Onboarding → Principal. |

**Estados de sesión**: `LoggedOut` → (login/registro) → `Active` → (logout, eliminar cuenta o 401) → `LoggedOut`. El 401 emite `Expired`, que muestra el mensaje "Tu sesión expiró" en el Login.

## Perfil

| Campo | Tipo | Validación (FR-007/008) |
|-------|------|-------------------------|
| `age` | Int | 16–100 |
| `sex` | `Sex` = `MALE`, `FEMALE` | obligatorio |
| `heightCm` | Int | 100–250 |
| `currentWeightKg` | Double? | Solo lectura: es el `WeightEntry` más reciente (FR-026a). En onboarding y edición se captura aparte y se guarda como `WeightEntry`. |
| `goal` | `Goal` = `LOSE_FAT`, `GAIN_MUSCLE`, `GENERAL_FITNESS` | obligatorio |
| `level` | `Level` = `BEGINNER`, `INTERMEDIATE`, `ADVANCED` | obligatorio |
| `daysPerWeek` | Int | 1–7 |
| `sessionMinutes` | Int | 15–120 |
| `location` | `Location` = `HOME`, `GYM` | obligatorio |
| `equipment` | Set<`Equipment`> | `BODYWEIGHT`, `DUMBBELLS`, `BARBELL`, `KETTLEBELL`, `RESISTANCE_BANDS`, `BENCH`, `PULL_UP_BAR`, `GYM_MACHINES`; mínimo 1 |
| `injuries` | String? | ≤ 300 caracteres |
| `dietType` | `DietType` = `OMNIVORE`, `VEGETARIAN`, `VEGAN` | obligatorio |
| `allergies` | Set<`Allergy`> | `GLUTEN`, `LACTOSE`, `TREE_NUTS`, `PEANUTS`, `SHELLFISH`, `EGG`, `SOY`, `OTHER`; puede estar vacío |
| `otherAllergy` | String? | obligatorio y ≤ 100 si `allergies` contiene `OTHER` |
| `dislikedFoods` | String? | ≤ 300 caracteres |
| `mealsPerDay` | Int | 3–5 |

**Secciones** (onboarding y Perfil): `PHYSICAL` (age, sex, heightCm, peso), `GOAL`, `TRAINING`, `NUTRITION`. El validador por sección vive en `onboarding/` y se usa en ambos flujos.

**Borrador de onboarding** (`OnboardingDraft`): los mismos campos, todos opcionales, más `weightKg: Double?` y `step: Int (0–3)`. Se guarda en DataStore y se borra al terminar o al cerrar sesión.

## Rutina

| Modelo | Campos |
|--------|--------|
| `Routine` | `id`, `version: Int`, `updatedAt: Instant`, `days: List<RoutineDay>` (exactamente 7, de lunes a domingo) |
| `RoutineDay` | `dayOfWeek: DayOfWeek`, `type: TRAINING \| REST`, `title: String?`, `exercises: List<Exercise>` (vacía si `REST`) |
| `Exercise` | `id`, `name`, `muscleGroup`, `sets: Int`, `reps: Int?`, `durationSeconds: Int?`, `restSeconds: Int`, `suggestedWeightKg: Double?`, `instructions`, `imageUrl: String?` |
| `WorkoutLog` | `date: LocalDate`, `completedExerciseIds: Set<String>`, `sessionFinished: Boolean` |

**Reglas**:
- `reps` o `durationSeconds`: viene exactamente uno. Si llegan ambos, se muestran las repeticiones; si no llega ninguno, falta un dato esencial y el ejercicio se omite. Si queda algún día inconsistente, toda la rutina pasa a `InvalidResponse`.
- Solo se marcan ejercicios y se termina la sesión cuando `date == Today` (FR-013).
- `sessionFinished` es `false → true` y no vuelve atrás; no puede haber una segunda sesión el mismo día.
- Los `Exercise.id` que se mantienen tras una actualización del servidor conservan su marca; los ids nuevos aparecen sin marca (FR-023a, caso límite de la spec). Por eso las marcas se guardan por id y no por posición.

## Dieta

| Modelo | Campos |
|--------|--------|
| `Diet` | `id`, `version: Int`, `updatedAt: Instant`, `days: List<DietDay>` (7) |
| `DietDay` | `dayOfWeek`, `totals: Nutrition`, `meals: List<Meal>` (3–5) |
| `Meal` | `id`, `type: BREAKFAST \| LUNCH \| DINNER \| SNACK`, `recipe: Recipe` |
| `Recipe` | `name`, `ingredients: List<Ingredient>`, `steps: List<String>`, `prepMinutes: Int`, `perServing: Nutrition` |
| `Ingredient` | `name`, `amount: Double`, `unit: G \| ML` |
| `Nutrition` | `kcal: Int`, `proteinG: Double`, `carbsG: Double`, `fatG: Double` |

`totals` y `perServing` se muestran tal como llegan (FR-017): la app no suma nada.

## Alternativas (aifeedback)

| Modelo | Campos |
|--------|--------|
| `AlternativeTarget` | `Exercise(exerciseId)`, `Recipe(mealId)`, `WholeRoutine`, `WholeDiet` |
| `ExerciseReason` | `DISLIKE`, `NO_EQUIPMENT`, `PAIN`, `TOO_HARD`, `TOO_EASY` |
| `RecipeReason` | `DISLIKE`, `NO_INGREDIENTS`, `TOO_ELABORATE`, `DIET_MISMATCH` |
| `AlternativeRequest` | `target`, `reason` (obligatorio para ejercicio y receta; nulo para plan completo), `comment: String?` (≤ 300) |
| `ExerciseProposal` / `RecipeProposal` | `proposalId`, `exercise` / `recipe` |

**Máquina de estados de la hoja de alternativa** (por pantalla):

```text
Idle ──enviar──▶ Loading ──éxito (ejercicio/receta)──▶ Proposal ──"Usar esta"──▶ Accepting ──éxito──▶ Done (reemplaza)
  ▲                │                                      │                          │
  │                ├──éxito (plan completo)──▶ Done (reemplaza)                      └──falla──▶ Proposal + error
  │                └──falla/timeout──▶ Failed (contenido actual intacto, "Reintentar")
  └────────────── "Mantener la actual" / salir de la pantalla (descarta la propuesta) ◀─┘
```

- En `Loading` y `Accepting` el botón de enviar está deshabilitado (FR-020).
- `reason == PAIN` muestra la recomendación de consultar a un profesional (FR-019).
- Si al aceptar llega `PROPOSAL_EXPIRED`, se descarta la propuesta y se muestra "La propuesta expiró, pide otra alternativa".

## Progreso

| Modelo | Campos | Validación |
|--------|--------|------------|
| `WeightEntry` | `date: LocalDate`, `weightKg: Double` (1 decimal) | 30.0–300.0; `date <= Today` (FR-024). Uno por fecha: guardar una fecha existente la actualiza (FR-025). |
| `TrainingSummary` | `weekStart: LocalDate` (lunes), `completedSessions: Int`, `scheduledSessions: Int`, `weekStreak: Int` | Calculado por el backend. |

El historial se ordena por `date` descendente para la lista y ascendente para la gráfica.

## Almacenamiento local

| Dónde | Clave / tabla | Contenido | Se borra |
|-------|---------------|-----------|----------|
| DataStore | `session` | `Session` en JSON | logout, eliminar cuenta, 401 |
| DataStore | `onboarding_draft` | `OnboardingDraft` en JSON | al terminar el onboarding, logout |
| Room | `snapshots(key PK, json, savedAt)` | claves `PROFILE`, `ROUTINE`, `WORKOUT_WEEK`, `DIET`, `WEIGHTS`, `SUMMARY` con el modelo de dominio en JSON | logout, eliminar cuenta, 401 (`clearAllTables`) |

`savedAt` sirve para el indicador "puede no estar actualizado" cuando se muestra la caché sin conexión.

## Estado de UI común

Cada pantalla que carga datos expone `UiState` con `Loading`, `Content(data, isStale: Boolean)`, `Empty` y `Error(AppError)`. `isStale = true` cuando lo mostrado viene de la caché y no hay conexión (FR-036, FR-038).
