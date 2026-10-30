---

description: "Task list for All Fitness MVP (App Android)"
---

# Tasks: All Fitness MVP (App Android)

**Input**: Design documents from `specs/001-allfitness-mvp/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: No se generan tareas de pruebas automatizadas: la spec no las pide y la constitución establece la verificación manual (`quickstart.md`) como estándar.

**Organization**: Las tareas se agrupan por historia de usuario para implementar y verificar cada una por separado.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Se puede hacer en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario de la spec (US1–US7)

## Path Conventions

- Código principal: `app/src/main/java/com/allfitness/app/`
- Solo flavor mock: `app/src/mock/java/com/allfitness/app/` y `app/src/mock/assets/mock/`
- Solo flavor real: `app/src/real/java/com/allfitness/app/`
- Textos: `app/src/main/res/values/strings.xml`. **Todo** texto visible se añade aquí, en español, en la misma tarea que lo usa.

## Reglas para todas las tareas

- MVVM simple: Screen (Composable sin estado) → ViewModel (`StateFlow<UiState>`) → Repository. Sin UseCases.
- Los DTO no salen del Repository; se mapean a los modelos de `data-model.md`.
- Toda llamada de red pasa por `safeApiCall`. Las pantallas solo convierten `AppError` en texto con `AppError.messageRes()`.
- Colores, tipografía y espaciados salen de `AllFitnessTheme`. Nada hardcodeado.
- Íconos e imágenes con `contentDescription`, y áreas táctiles de al menos 48dp.
- Cada Repository nuevo es una interfaz en `main` con implementación real en `main`. Su Fake vive en `mock`, y se enlaza en `MockRepositoryModule` / `RealRepositoryModule`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Crear el proyecto Android y su configuración

- [X] T001 Create the single-module Android project: `settings.gradle.kts` (module `:app`), root `build.gradle.kts`, `gradle.properties` (`android.useAndroidX=true`, `kotlin.code.style=official`), Gradle wrapper, and `app/build.gradle.kts` with `namespace`/`applicationId` `com.allfitness.app`, `minSdk 26`, `targetSdk`/`compileSdk` latest stable, JVM target 17, Compose enabled, `buildConfig = true`
- [X] T002 Create `gradle/libs.versions.toml` with the latest stable versions of ONLY these: Compose BOM, material3, activity-compose, lifecycle-runtime-compose, lifecycle-viewmodel-compose, navigation-compose, hilt-android + hilt-compiler + hilt-navigation-compose, retrofit + retrofit converter-kotlinx-serialization, okhttp, kotlinx-serialization-json, kotlinx-coroutines-android, room-runtime + room-ktx + room-compiler, datastore-preferences, coil-compose + coil-network-okhttp; plugins: android-application, kotlin-android, kotlin-compose, kotlin-serialization, ksp, hilt. Apply them in `app/build.gradle.kts`
- [X] T003 Add product flavors in `app/build.gradle.kts`: dimension `backend`, flavors `mock` (`applicationIdSuffix ".mock"`, `BASE_URL="https://mock.invalid/"`) and `real` (`BASE_URL` read from `local.properties` key `allfitness.baseUrl`). For both: `buildConfigField` `TERMS_URL`, `PRIVACY_URL` and `LEGAL_VERSION="2026-10"`
- [X] T004 [P] Create `app/src/main/AndroidManifest.xml`: permissions `INTERNET` and `ACCESS_NETWORK_STATE`, `android:name=".AllFitnessApp"`, `android:allowBackup="false"`, `android:dataExtractionRules="@xml/data_extraction_rules"`, `MainActivity` as launcher; and `app/src/main/res/xml/data_extraction_rules.xml` excluding all domains from cloud backup and device transfer (research R6)
- [X] T005 [P] Create `app/src/main/res/values/strings.xml` with `app_name` "All Fitness" and the shared texts: AI disclaimer ("Este contenido es orientativo y no reemplaza la recomendación de un profesional de salud"), offline banner, "Reintentar", "Sin conexión", "Esta acción requiere conexión a internet", and one message per `AppError` from research R12 (e.g. `error_no_connection`, `error_timeout`, `error_server`, `error_ai_unavailable` "No pudimos generar tu contenido. Inténtalo de nuevo", `error_invalid_response`, `error_session_expired` "Tu sesión expiró. Inicia sesión de nuevo")
- [X] T006 [P] Create `.gitignore` for Android (`.gradle/`, `build/`, `local.properties`, `.idea/`, `*.iml`, `captures/`, `.kotlin/`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Red, errores, sesión, caché, navegación, componentes comunes y la capa de datos compartida de rutina, dieta y peso

**⚠️ CRITICAL**: Ninguna historia puede empezar hasta terminar esta fase

### Núcleo

- [X] T007 [P] Create Material 3 theme in `app/src/main/java/com/allfitness/app/core/ui/theme/Color.kt`, `Type.kt`, `Theme.kt` (`AllFitnessTheme`: light/dark color schemes that follow the system, no custom dark-mode toggle) and spacing tokens in `core/ui/theme/Spacing.kt`
- [X] T008 [P] Create `AppError` sealed interface in `app/src/main/java/com/allfitness/app/core/network/AppError.kt` with exactly: `NoConnection`, `Timeout`, `Validation(code: String, fields: List<String>)`, `InvalidCredentials`, `SessionExpired`, `NoActivePlan`, `NotFound`, `EmailTaken`, `ProposalExpired`, `AiUnavailable`, `Server`, `InvalidResponse`; plus `AppResult<T>` (`Success(data)` / `Failure(error)`) in `core/network/AppResult.kt`
- [X] T009 Implement `safeApiCall(isLogin: Boolean = false, block: suspend () -> T): AppResult<T>` and `ErrorBody(code, message, fields)` DTO in `app/src/main/java/com/allfitness/app/core/network/SafeApiCall.kt`, mapping exactly per research R12 (IOException→NoConnection, SocketTimeoutException→Timeout, 400 codes→Validation, 401+isLogin→InvalidCredentials, 401 otherwise→SessionExpired, 404 `NO_ACTIVE_PLAN`→NoActivePlan, other 404→NotFound, 409 `EMAIL_TAKEN`/`PROPOSAL_EXPIRED`, 502/503/504→AiUnavailable, other 5xx→Server, `SerializationException`/missing essential field→InvalidResponse). Never expose `message` (depends on T008)
- [X] T010 [P] Create Room cache in `app/src/main/java/com/allfitness/app/core/database/`: `SnapshotEntity(key: String PK, json: String, savedAt: Long)`, `SnapshotKey` enum `PROFILE, ROUTINE, WORKOUT_WEEK, DIET, WEIGHTS, SUMMARY`, `SnapshotDao` (`observe(key): Flow<SnapshotEntity?>`, `upsert(entity)`, `get(key)`), `AppDatabase`, and `DatabaseModule.kt` (Hilt). Add helper `SnapshotStore` with `inline fun <reified T> observe(key): Flow<Snapshot<T>?>` and `save(key, value)` using the shared `Json` (research R4)
- [X] T011 Create `app/src/main/java/com/allfitness/app/core/session/SessionStore.kt` (DataStore Preferences: key `session` = `Session` JSON, key `onboarding_draft` = draft JSON) and `SessionManager.kt` exposing `state: StateFlow<SessionState>` (`Loading`, `LoggedOut`, `Active(session)`, `Expired`), `start(session)`, `markOnboardingCompleted()`, `expire()` and `clearAll()` (clears DataStore keys, `AppDatabase.clearAllTables()`, Coil memory and disk cache) per research R6/R13 (depends on T010)
- [X] T012 Create `AuthInterceptor` in `app/src/main/java/com/allfitness/app/core/network/AuthInterceptor.kt`: adds `Authorization: Bearer <token>` from `SessionStore`; on HTTP 401 for any path not starting with `/api/v1/auth/` calls `SessionManager.expire()` (depends on T011)
- [X] T013 Create `app/src/main/java/com/allfitness/app/core/network/NetworkModule.kt` (Hilt): shared `Json { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true }`, default `OkHttpClient` (15 s timeouts) and AI `OkHttpClient` (90 s read timeout, research R1), both with `AuthInterceptor`, and two `Retrofit` instances qualified `@DefaultApi` / `@AiApi` with base URL `BuildConfig.BASE_URL + "api/v1/"` (depends on T012)
- [X] T014 [P] Create `ConnectivityObserver` in `app/src/main/java/com/allfitness/app/core/network/ConnectivityObserver.kt` exposing `isOnline: StateFlow<Boolean>` using `ConnectivityManager.registerDefaultNetworkCallback` and `NET_CAPABILITY_VALIDATED` (research R5), provided as a Hilt singleton
- [X] T015 [P] Create `app/src/main/java/com/allfitness/app/core/time/Today.kt` (injectable): `today(): LocalDate`, `currentWeekStart(): LocalDate` (Monday), `weekDates(): List<LocalDate>` (Monday–Sunday) (research R10)
- [X] T016 [P] Create `UiState<T>` in `app/src/main/java/com/allfitness/app/core/ui/UiState.kt`: `Loading`, `Content(data: T, isStale: Boolean, savedAt: Long?)`, `Empty`, `Error(error: AppError)` (data-model "Estado de UI común")
- [X] T017 [P] Create `AppError.messageRes(): Int` in `app/src/main/java/com/allfitness/app/core/ui/ErrorMessages.kt` mapping every `AppError` to the strings from T005
- [X] T018 [P] Create stateless shared components in `app/src/main/java/com/allfitness/app/core/ui/`: `LoadingState.kt` (optional message), `ErrorState.kt` (message + "Reintentar"), `EmptyState.kt` (message + optional action button), `OfflineBanner.kt` ("Sin conexión · datos de <fecha savedAt>; pueden no estar actualizados"), `AiDisclaimer.kt` (AI disclaimer card, FR-037)
- [X] T019 Create flavor bindings: empty Hilt modules `app/src/mock/java/com/allfitness/app/di/MockRepositoryModule.kt` and `app/src/real/java/com/allfitness/app/di/RealRepositoryModule.kt`; mock helpers `app/src/mock/java/com/allfitness/app/mock/MockAssets.kt` (reads `assets/mock/<name>.json` with the shared `Json`) and `mock/MockBehavior.kt` (`delayNormal()` 1 s, `delayAi(comment)` 6 s or 55 s if `comment` contains `#lento`; `failIfRequested(comment)` returns `AppError.AiUnavailable` for `#falla`) (research R3)
- [X] T020 Create `app/src/main/java/com/allfitness/app/AllFitnessApp.kt` (`@HiltAndroidApp`) and `MainActivity.kt` (`@AndroidEntryPoint`, `setContent { AllFitnessTheme { AppNavHost() } }`)
- [X] T021 Create navigation in `app/src/main/java/com/allfitness/app/navigation/`: `Routes.kt` (`@Serializable` routes `Login(sessionExpired: Boolean = false)`, `Register`, `Onboarding`, `Main`, `RoutineWeek`, `RoutineDay(date: String)`, `DietWeek`, `Recipe(dayOfWeek: String, mealId: String)`, `Progress`, `Profile`, `ProfileSection(section: String)`, `DeleteAccount`); `AppNavHost.kt` (root graph that observes `SessionManager.state`: `Loading`→splash with `LoadingState`, `LoggedOut`→`Login`, `Expired`→`Login(sessionExpired = true)`, `Active` + onboarding pending→`Onboarding`, `Active`→`Main`); `MainScaffold.kt` (Material 3 `NavigationBar` with Rutina, Dieta, Progreso, Perfil and a nested NavHost; temporary placeholder screens) (FR-005, FR-035)

### Capa de datos compartida (rutina, dieta, peso)

- [X] T022 [P] Create routine domain models in `app/src/main/java/com/allfitness/app/routines/RoutineModels.kt` exactly as data-model.md: `Routine(id, version: Int, updatedAt: Instant, days: List<RoutineDay>)` with "exactamente 7, de lunes a domingo", `RoutineDay(dayOfWeek, type: TRAINING | REST, title: String?, exercises)`, `Exercise(id, name, muscleGroup, sets: Int, reps: Int?, durationSeconds: Int?, restSeconds: Int, suggestedWeightKg: Double?, instructions, imageUrl: String?)`, `WorkoutLog(date: LocalDate, completedExerciseIds: Set<String>, sessionFinished: Boolean)`; all `@Serializable` for the snapshot cache
- [X] T023 [P] Create `app/src/main/java/com/allfitness/app/routines/RoutineDtos.kt` and `RoutineApi.kt` per `contracts/routine.md`: `GET routine`, `GET workout-logs?from&to`, `PUT workout-logs/{date}/exercises/{exerciseId}` (`{completed}`), `POST workout-logs/{date}/finish`; and `RoutineAiApi.kt` (on the `@AiApi` Retrofit): `POST routine/generate` (`{comment?}`), `POST routine/exercises/{exerciseId}/alternative` (`{reason, comment?}`), `POST routine/exercises/{exerciseId}/alternative/{proposalId}/accept`
- [X] T024 Create `app/src/main/java/com/allfitness/app/routines/RoutineMapper.kt` (DTO → domain): require 7 days in MONDAY–SUNDAY order; exercise with both `reps` and `durationSeconds` keeps `reps`; exercise with neither is dropped; if a `TRAINING` day ends up inconsistent the whole routine maps to `AppError.InvalidResponse` (data-model "Reglas") (depends on T022, T023)
- [X] T025 Create `RoutineRepository` interface and real implementation in `app/src/main/java/com/allfitness/app/routines/RoutineRepository.kt` / `RoutineRepositoryImpl.kt` with: `observeRoutine(): Flow<Snapshot<Routine>?>` (from `SnapshotStore` key `ROUTINE`), `refreshRoutine(): AppResult<Unit>` (GET + save snapshot; `NoActivePlan` is passed through), `generateRoutine(comment: String?): AppResult<Routine>` (saves snapshot only on success) (depends on T010, T024)
- [X] T026 [P] Create diet domain models in `app/src/main/java/com/allfitness/app/nutrition/DietModels.kt` exactly as data-model.md: `Diet(id, version: Int, updatedAt: Instant, days: List<DietDay>)` (7), `DietDay(dayOfWeek, totals: Nutrition, meals: List<Meal>)` (3–5), `Meal(id, type: BREAKFAST | LUNCH | DINNER | SNACK, recipe)`, `Recipe(name, ingredients, steps: List<String>, prepMinutes: Int, perServing: Nutrition)`, `Ingredient(name, amount: Double, unit: G | ML)`, `Nutrition(kcal: Int, proteinG: Double, carbsG: Double, fatG: Double)`; `@Serializable`
- [X] T027 [P] Create `app/src/main/java/com/allfitness/app/nutrition/DietDtos.kt`, `DietApi.kt` (`GET diet`) and `DietAiApi.kt` (`POST diet/generate`, `POST diet/meals/{mealId}/alternative` `{reason, comment?}`, `POST diet/meals/{mealId}/alternative/{proposalId}/accept`) per `contracts/diet.md`
- [X] T028 Create `app/src/main/java/com/allfitness/app/nutrition/DietMapper.kt` (7 days required, otherwise `InvalidResponse`; totals copied as-is, never summed, FR-017) and `DietRepository` + `DietRepositoryImpl.kt` with `observeDiet()`, `refreshDiet()`, `generateDiet(comment: String?)`, same rules as T025 (key `DIET`) (depends on T010, T026, T027)
- [X] T029 [P] Create progress models and API in `app/src/main/java/com/allfitness/app/progress/`: `ProgressModels.kt` (`WeightEntry(date: LocalDate, weightKg: Double)` with "1 decimal", `TrainingSummary(weekStart: LocalDate, completedSessions: Int, scheduledSessions: Int, weekStreak: Int)`), `ProgressDtos.kt`, `ProgressApi.kt` (`GET progress/weights`, `PUT progress/weights/{date}` `{weightKg}`, `GET progress/summary?weekStart`) per `contracts/progress.md`
- [X] T030 Create `ProgressRepository` + `ProgressRepositoryImpl.kt` in `app/src/main/java/com/allfitness/app/progress/` with `upsertWeight(date: LocalDate, weightKg: Double): AppResult<WeightEntry>` (after success, refreshes the `WEIGHTS` snapshot). US6 adds the remaining methods (depends on T010, T029)
- [X] T031 [P] Create mock data in `app/src/mock/assets/mock/` from the contract examples, complete (not trimmed): `routine.json` (7 days; TRAINING on MONDAY, WEDNESDAY, FRIDAY with 4–6 exercises each, at least one with `durationSeconds`, one without `suggestedWeightKg`, one with `imageUrl: null`), `diet.json` (7 days × 4 meals: BREAKFAST, LUNCH, SNACK, DINNER, Colombian-style recipes), `profile.json`, `weights.json` (3 entries), `summary.json`, `exercise_alternative.json`, `recipe_alternative.json`
- [X] T032 Create Fakes in `app/src/mock/java/com/allfitness/app/mock/`: `FakeRoutineRepository.kt` and `FakeDietRepository.kt` (load assets into `SnapshotStore` on refresh; `generate*` applies `MockBehavior.delayAi/failIfRequested` and returns the asset), `FakeProgressRepository.kt` (`upsertWeight` in memory; weight `299.9` → calls `SessionManager.expire()` and returns `SessionExpired`, quickstart). Bind all three in `MockRepositoryModule.kt` and the real ones in `RealRepositoryModule.kt` (depends on T019, T025, T028, T030, T031)

**Checkpoint**: La app compila en `mockDebug` y `realDebug`, arranca en el Login (placeholder) y la capa de datos está lista.

---

## Phase 3: User Story 1 - Registro, inicio de sesión y onboarding (Priority: P1) 🎯 MVP

**Goal**: Crear una cuenta con consentimiento, iniciar sesión, mantener la sesión y completar el onboarding de 4 pasos hasta recibir rutina y dieta.

**Independent Test**: En `mockDebug`: registro (sin casilla no deja; `existe@allfitness.co` → mensaje), onboarding con valores fuera de rango, cerrar en el paso 3 y retomar, terminar → carga → pestaña Rutina; `#sindieta` → entra con Dieta vacía; reabrir la app → entra directo (quickstart pasos 1–2).

- [X] T033 [P] [US1] Create `app/src/main/java/com/allfitness/app/auth/AuthModels.kt` (`User(id, name, email)`, `Session(token, user, onboardingCompleted)`; `@Serializable`), `AuthDtos.kt` and `AuthApi.kt` per `contracts/auth.md` (`POST auth/register` with `acceptedLegalVersion`, `POST auth/login`, `POST auth/logout`, `GET me`, `DELETE me`)
- [X] T034 [P] [US1] Create `app/src/main/java/com/allfitness/app/auth/AuthValidation.kt`: name not blank, email via `Patterns.EMAIL_ADDRESS`, password "mínimo 8 caracteres" (FR-002); return field error string resources
- [X] T035 [US1] Create `AuthRepository` + `AuthRepositoryImpl.kt` in `app/src/main/java/com/allfitness/app/auth/`: `register(name, email, password)` (sends `BuildConfig.LEGAL_VERSION`), `login(email, password)` (`safeApiCall(isLogin = true)`), both calling `SessionManager.start(session)`; `restoreSession()` (if a session is stored: `GET me` updates `onboardingCompleted`; on `NoConnection`/`Timeout` keep the stored session so the app opens offline; on `SessionExpired` → expired); `logout()` (best-effort `POST auth/logout`, then `SessionManager.clearAll()`); `deleteAccount()` (`DELETE me`; only on success `clearAll()`) (research R6, R13) (depends on T033)
- [X] T036 [P] [US1] Create `app/src/mock/java/com/allfitness/app/mock/FakeAuthRepository.kt`: `existe@allfitness.co` → `EmailTaken`; password `Incorrecta1` → `InvalidCredentials`; register → `onboardingCompleted = false`; login → `true` (seeded profile); `deleteAccount` succeeds; bind real/fake in both flavor modules (depends on T035)
- [X] T037 [US1] Call `AuthRepository.restoreSession()` on startup (while `SessionState.Loading`) from `app/src/main/java/com/allfitness/app/navigation/AppNavHost.kt` or an `AppViewModel.kt` in the same package (depends on T035)
- [X] T038 [P] [US1] Create `LoginViewModel.kt` and `LoginScreen.kt` in `app/src/main/java/com/allfitness/app/auth/`: email and password fields, validation on submit (T034), button disabled while loading or offline (`ConnectivityObserver`, FR-039), errors via `messageRes()` ("Correo o contraseña incorrectos" for `InvalidCredentials`), "Tu sesión expiró. Inicia sesión de nuevo" banner when `sessionExpired = true`, link to Register (depends on T035)
- [X] T039 [P] [US1] Create `RegisterViewModel.kt` and `RegisterScreen.kt` in `app/src/main/java/com/allfitness/app/auth/`: name, email, password, checkbox "Acepto los términos y la política de tratamiento de datos (incluye datos de salud)" that is "desmarcada por defecto", with two links that open `BuildConfig.TERMS_URL` / `PRIVACY_URL` in the system browser; account creation blocked while unchecked with the message to accept (FR-002a); "Este correo ya tiene una cuenta" for `EmailTaken`; disabled offline/in-flight (depends on T035)
- [X] T040 [P] [US1] Create profile domain in `app/src/main/java/com/allfitness/app/onboarding/ProfileModels.kt` exactly as data-model.md: enums `Sex` = `MALE`, `FEMALE`; `Goal` = `LOSE_FAT`, `GAIN_MUSCLE`, `GENERAL_FITNESS`; `Level` = `BEGINNER`, `INTERMEDIATE`, `ADVANCED`; `Location` = `HOME`, `GYM`; `Equipment` = `BODYWEIGHT`, `DUMBBELLS`, `BARBELL`, `KETTLEBELL`, `RESISTANCE_BANDS`, `BENCH`, `PULL_UP_BAR`, `GYM_MACHINES`; `DietType` = `OMNIVORE`, `VEGETARIAN`, `VEGAN`; `Allergy` = `GLUTEN`, `LACTOSE`, `TREE_NUTS`, `PEANUTS`, `SHELLFISH`, `EGG`, `SOY`, `OTHER`; `Profile(...)` with `currentWeightKg: Double?` read-only; `ProfileSection` = `PHYSICAL`, `GOAL`, `TRAINING`, `NUTRITION`; `OnboardingDraft` (all fields nullable + `weightKg: Double?` + `step: Int (0–3)`); Spanish labels for each enum value in strings.xml
- [X] T041 [P] [US1] Create `app/src/main/java/com/allfitness/app/onboarding/ProfileValidation.kt` with one validator per `ProfileSection` using the data-model ranges verbatim: `age` 16–100, `heightCm` 100–250, weight 30.0–300.0 with 1 decimal, `daysPerWeek` 1–7, `sessionMinutes` 15–120, `equipment` "mínimo 1", `injuries` "≤ 300 caracteres", `otherAllergy` "obligatorio y ≤ 100 si `allergies` contiene `OTHER`", `dislikedFoods` "≤ 300 caracteres", `mealsPerDay` 3–5, every enum mandatory; errors name the allowed range (FR-008)
- [X] T042 [US1] Create `app/src/main/java/com/allfitness/app/onboarding/ProfileDtos.kt`, `ProfileApi.kt` (`GET profile`, `PUT profile` without `currentWeightKg`) and `ProfileRepository` + `ProfileRepositoryImpl.kt` (`observeProfile()` from snapshot `PROFILE`, `refreshProfile()`, `saveProfile(profile)` saving the snapshot on success); `app/src/mock/java/com/allfitness/app/mock/FakeProfileRepository.kt` seeded from `profile.json` (`#falla` in `injuries` → `AppError.Server`); bind both (depends on T040)
- [X] T043 [US1] Add draft persistence to `app/src/main/java/com/allfitness/app/core/session/SessionStore.kt`: `saveDraft(OnboardingDraft)`, `draft(): OnboardingDraft?`, `clearDraft()` (research R7) (depends on T040)
- [X] T044 [P] [US1] Create stateless, reusable section composables in `app/src/main/java/com/allfitness/app/onboarding/sections/`: `PhysicalSection.kt` (age, sex, heightCm, weightKg; numeric keyboards), `GoalSection.kt`, `TrainingSection.kt` (level, daysPerWeek, sessionMinutes, location, equipment multi-select chips, injuries with 300 counter), `NutritionSection.kt` (dietType, allergies chips + `otherAllergy` field when `OTHER`, dislikedFoods with 300 counter, mealsPerDay); each receives values + field errors + `onChange` (shared with US7) (depends on T040)
- [X] T045 [US1] Create `app/src/main/java/com/allfitness/app/onboarding/OnboardingViewModel.kt`: restore draft and step on start (FR-009); "Siguiente" validates the current section (T041) and saves the draft; "Atrás"; finish runs research R8 in order: `ProfileRepository.saveProfile` → `ProgressRepository.upsertWeight(today, weightKg)` → `RoutineRepository.generateRoutine(null)` and `DietRepository.generateDiet(null)` in parallel. If profile or weight fails → error + retry from that step; if both generations fail → error + retry generations; otherwise `clearDraft()`, `SessionManager.markOnboardingCompleted()` (partial failure allowed: the failed tab shows its empty state) (depends on T025, T028, T030, T042, T043)
- [X] T046 [US1] Create `app/src/main/java/com/allfitness/app/onboarding/OnboardingScreen.kt`: progress indicator "Paso N de 4", hosts the section composables, Back/Next, finish button disabled offline, generating state with `LoadingState` "Estamos generando tu rutina y tu dieta…" (can take up to 60 s), error with retry (depends on T044, T045)
- [X] T047 [US1] Wire `Login`, `Register` and `Onboarding` routes in `app/src/main/java/com/allfitness/app/navigation/AppNavHost.kt`, replacing the placeholders (depends on T037, T038, T039, T046)
- [X] T048 [US1] Add `#sindieta` handling to `app/src/mock/java/com/allfitness/app/mock/FakeDietRepository.kt`: when the fake profile's `dislikedFoods` contains `#sindieta`, `generateDiet` returns `AiUnavailable` (quickstart) (depends on T032, T042)

**Checkpoint**: US1 completa y verificable sola.

---

## Phase 4: User Story 2 - Ver y seguir la rutina semanal (Priority: P1)

**Goal**: Ver la semana con hoy destacado, revisar ejercicios, marcarlos y terminar la sesión de hoy.

**Independent Test**: En `mockDebug` con sesión iniciada: pestaña Rutina → 7 días, días de descanso y hoy destacado → día de hoy → marcar/desmarcar → terminar con ejercicios pendientes → días pasado y futuro bloqueados; aviso de IA visible (quickstart paso 3).

- [X] T049 [US2] Extend `RoutineRepository`/`RoutineRepositoryImpl.kt` in `app/src/main/java/com/allfitness/app/routines/` with `observeWeekLogs(): Flow<Snapshot<List<WorkoutLog>>?>` (key `WORKOUT_WEEK`), `refreshWeekLogs(from, to)`, `setExerciseCompleted(date, exerciseId, completed): AppResult<WorkoutLog>` and `finishSession(date): AppResult<WorkoutLog>`, both updating the `WORKOUT_WEEK` snapshot on success
- [X] T050 [P] [US2] Extend `app/src/mock/java/com/allfitness/app/mock/FakeRoutineRepository.kt` with in-memory logs: reject a date other than today, a finished session or a REST day with `Validation("INVALID_DATE")`; finishing twice returns the same log
- [X] T051 [US2] Create `app/src/main/java/com/allfitness/app/routines/RoutineWeekViewModel.kt`: combines `observeRoutine()`, `observeWeekLogs()` and `Today.weekDates()`; refreshes on start and with `LifecycleResumeEffect` (date change, research R10); `NoActivePlan` → `UiState.Empty`; `generateRoutine()` action for the empty state (disabled while running) (depends on T049)
- [X] T052 [US2] Create `app/src/main/java/com/allfitness/app/routines/RoutineWeekScreen.kt`: 7 rows Monday–Sunday with date, title or "Descanso", today highlighted, "Sesión terminada" indicator from logs, `AiDisclaimer`, Loading/Error/Empty states (empty: "Aún no tienes rutina" + "Generar rutina"); tapping a day navigates to `RoutineDay(date)` (FR-011) (depends on T051)
- [X] T053 [P] [US2] Create stateless `app/src/main/java/com/allfitness/app/routines/ExerciseItem.kt`: name, muscleGroup, "N series × M repeticiones" or "N series × S s", rest, suggested weight only if present, instructions, Coil `AsyncImage` only if `imageUrl` (with `contentDescription` = exercise name), optional checkbox and optional trailing action slot (reused by US4) (FR-012)
- [X] T054 [US2] Create `app/src/main/java/com/allfitness/app/routines/RoutineDayViewModel.kt`: day by `date`; `canEdit = date == today && !sessionFinished && type == TRAINING`; optimistic toggle that reverts on failure and emits a snackbar event with "Reintentar"; ignores taps on an exercise with a request in flight (research R11); `finishSession()` single-flight (depends on T049)
- [X] T055 [US2] Create `app/src/main/java/com/allfitness/app/routines/RoutineDayScreen.kt`: `ExerciseItem` list (checkboxes enabled only when `canEdit`), "Terminar sesión" button (only today, allowed with pending exercises, FR-014), message for past/future days "Solo puedes registrar la sesión de hoy", `AiDisclaimer`, snackbar host (depends on T053, T054)
- [X] T056 [US2] Replace the Rutina tab placeholder and add the `RoutineDay` route in `app/src/main/java/com/allfitness/app/navigation/MainScaffold.kt` (depends on T052, T055)

**Checkpoint**: US1 y US2 funcionan.

---

## Phase 5: User Story 3 - Ver la dieta y sus recetas (Priority: P2)

**Goal**: Ver las comidas de cada día con totales y abrir recetas.

**Independent Test**: Pestaña Dieta → hoy por defecto, cambiar entre los 7 días, totales iguales a `diet.json`, abrir una receta de cada tipo; aviso de IA visible (quickstart paso 4).

- [X] T057 [US3] Create `app/src/main/java/com/allfitness/app/nutrition/DietWeekViewModel.kt`: `observeDiet()` + refresh on start/resume, selected day defaults to today's `DayOfWeek`, `NoActivePlan` → Empty, `generateDiet()` for the empty state
- [X] T058 [P] [US3] Create stateless `app/src/main/java/com/allfitness/app/nutrition/NutritionSummary.kt` (kcal, proteína, carbohidratos and grasas formatted with units, values shown exactly as received) and `RecipeContent.kt` (name, ingredients "amount g/ml", numbered steps, "≈ N min", perServing) (reused by US4)
- [X] T059 [US3] Create `app/src/main/java/com/allfitness/app/nutrition/DietWeekScreen.kt`: day selector (scrollable tabs Lun–Dom), day totals with `NutritionSummary`, meals list with meal type label and recipe name + kcal, `AiDisclaimer`, states (empty: "Aún no tienes dieta" + "Generar dieta"); tapping a meal → `Recipe(dayOfWeek, mealId)` (FR-015) (depends on T057, T058)
- [X] T060 [US3] Create `app/src/main/java/com/allfitness/app/nutrition/RecipeViewModel.kt` and `RecipeScreen.kt` (finds the meal in `observeDiet()`, shows `RecipeContent` + `AiDisclaimer`, NotFound → ErrorState) (FR-016) (depends on T058)
- [X] T061 [US3] Replace the Dieta tab placeholder and add the `Recipe` route in `app/src/main/java/com/allfitness/app/navigation/MainScaffold.kt` (depends on T059, T060)

**Checkpoint**: US1–US3 funcionan.

---

## Phase 6: User Story 4 - Pedir alternativas y regenerar planes (Priority: P2)

**Goal**: Pedir una alternativa para un ejercicio o receta (con vista previa) o regenerar el plan completo, sin perder nunca el contenido actual.

**Independent Test**: Cada motivo de ejercicio (PAIN muestra la recomendación), "Usar esta" / "Mantener la actual" / salir sin decidir, receta con `#roto`, regenerar con `#lento` y con `#falla`, doble toque = una sola petición (quickstart paso 5).

- [X] T062 [P] [US4] Create `app/src/main/java/com/allfitness/app/aifeedback/AlternativeModels.kt` exactly as data-model.md: `AlternativeTarget` = `Exercise(exerciseId)`, `Recipe(mealId)`, `WholeRoutine`, `WholeDiet`; `ExerciseReason` = `DISLIKE`, `NO_EQUIPMENT`, `PAIN`, `TOO_HARD`, `TOO_EASY`; `RecipeReason` = `DISLIKE`, `NO_INGREDIENTS`, `TOO_ELABORATE`, `DIET_MISMATCH`; `AlternativeRequest(target, reason (obligatorio para ejercicio y receta; nulo para plan completo), comment: String? (≤ 300))`; `ExerciseProposal(proposalId, exercise)`, `RecipeProposal(proposalId, recipe)`. Spanish labels in strings.xml exactly as spec US4: "No me gusta", "No tengo el equipo", "Me causa molestia o dolor", "Muy difícil", "Muy fácil", "No tengo los ingredientes", "Es muy elaborada", "No encaja con mi dieta o alergias"
- [X] T063 [US4] Create `AlternativeRepository` + `AlternativeRepositoryImpl.kt` in `app/src/main/java/com/allfitness/app/aifeedback/`: `requestExercise(exerciseId, reason, comment)`, `acceptExercise(exerciseId, proposalId)` (saves the returned routine to the `ROUTINE` snapshot), `requestRecipe(mealId, reason, comment)`, `acceptRecipe(mealId, proposalId)` (saves `DIET`); whole plans delegate to `RoutineRepository.generateRoutine(comment)` / `DietRepository.generateDiet(comment)`. Nothing changes in the cache until an accept/generate succeeds (FR-022) (depends on T062)
- [X] T064 [P] [US4] Create `app/src/mock/java/com/allfitness/app/mock/FakeAlternativeRepository.kt`: proposals from `exercise_alternative.json` / `recipe_alternative.json`, `MockBehavior` delays and `#lento`/`#falla`; `#roto` in a recipe request → `InvalidResponse`; accept replaces the element in the fake plan and saves the snapshot; bind real/fake (depends on T063)
- [X] T065 [US4] Create `app/src/main/java/com/allfitness/app/aifeedback/AlternativeViewModel.kt` implementing the data-model state machine `Idle → Loading → Proposal → Accepting → Done`, `Failed` with retry, `Proposal + error` when an accept fails; `ProposalExpired` → discard + "La propuesta expiró, pide otra alternativa"; single-flight (FR-020); comment limited to 300; submit disabled when offline (depends on T063)
- [X] T066 [US4] Create `app/src/main/java/com/allfitness/app/aifeedback/AlternativeSheet.kt` (Material 3 `ModalBottomSheet`): reasons as radio options per target (none for whole plans), "¿Cómo lo preferirías?" field with "N/300" counter that rejects longer input (including pasted text); PAIN → card "Si sientes dolor, consulta a un profesional de salud antes de continuar" (FR-019); whole plans → confirmation dialog "Esto reemplazará tu plan actual" before sending (FR-023); loading "Generando alternativa… puede tardar hasta un minuto"; proposal view using `ExerciseItem` (T053) or `RecipeContent` (T058) with "Usar esta" / "Mantener la actual" (FR-022a); error + "Reintentar" (depends on T065)
- [X] T067 [US4] Add a "Pedir alternativa" action to each `ExerciseItem` in `app/src/main/java/com/allfitness/app/routines/RoutineDayScreen.kt` and "Regenerar rutina" to `RoutineWeekScreen.kt`, opening `AlternativeSheet` while the current content stays visible behind it; leaving the screen discards a pending proposal (depends on T055, T066)
- [X] T068 [US4] Add "Pedir alternativa" to `app/src/main/java/com/allfitness/app/nutrition/RecipeScreen.kt` and "Regenerar dieta" to `DietWeekScreen.kt`, same behavior as T067 (depends on T060, T066)

**Checkpoint**: US1–US4 funcionan.

---

## Phase 7: User Story 5 - Consultar sin conexión (Priority: P2)

**Goal**: Rutina, dieta y progreso cargados se consultan sin red, con indicador, y las acciones de red se deshabilitan con mensaje.

**Independent Test**: Cargar datos, activar modo avión y reabrir → contenido + indicador; acciones deshabilitadas; instalación limpia en modo avión → estado vacío; recuperar la red → se actualiza (quickstart paso 6).

- [X] T069 [US5] In `RoutineWeekViewModel.kt` and `RoutineDayViewModel.kt` (`app/src/main/java/com/allfitness/app/routines/`) combine `ConnectivityObserver.isOnline`: offline with a snapshot → `Content(isStale = true, savedAt)`; offline without a snapshot → Empty "Necesitas conexión para cargar tu rutina"; `canEdit`, "Terminar sesión", "Generar" and "Regenerar" disabled offline; refresh when connectivity returns (FR-038, FR-039)
- [X] T070 [P] [US5] Apply the same rules to `DietWeekViewModel.kt` and `RecipeViewModel.kt` in `app/src/main/java/com/allfitness/app/nutrition/`
- [X] T071 [US5] Show `OfflineBanner` when `isStale` and the message "Esta acción requiere conexión a internet" on disabled actions in `RoutineWeekScreen.kt`, `RoutineDayScreen.kt`, `DietWeekScreen.kt`, `RecipeScreen.kt` and `aifeedback/AlternativeSheet.kt` (depends on T069, T070)
- [X] T072 [US5] Verify the offline startup path in `app/src/main/java/com/allfitness/app/auth/AuthRepositoryImpl.kt` and `navigation/AppNavHost.kt`: a stored session + no connection opens `Main` with cached tabs, and Login/Register show their disabled-offline message (depends on T037)

**Checkpoint**: US1–US5 funcionan, también sin conexión.

---

## Phase 8: User Story 6 - Registrar y ver progreso (Priority: P3)

**Goal**: Registrar el peso, ver el historial en lista y gráfica, y el resumen semanal y la racha.

**Independent Test**: Peso del onboarding como primer registro, registrar hoy, fecha futura bloqueada, fecha repetida avisa y actualiza, gráfica con 1 y varios puntos, resumen vacío para usuario nuevo (quickstart paso 7).

- [X] T073 [US6] Extend `ProgressRepository`/`ProgressRepositoryImpl.kt` in `app/src/main/java/com/allfitness/app/progress/` with `observeWeights()` (key `WEIGHTS`, sorted by `date` descending), `refreshWeights()`, `observeSummary()` (key `SUMMARY`) and `refreshSummary(weekStart)`
- [X] T074 [P] [US6] Extend `app/src/mock/java/com/allfitness/app/mock/FakeProgressRepository.kt`: weights in memory, one per date, seeded from `weights.json` for the seeded login user and empty for new registrations (the onboarding weight becomes the first entry); summary from `summary.json`, all zeros for new users
- [X] T075 [US6] Create `app/src/main/java/com/allfitness/app/progress/ProgressViewModel.kt`: weights + summary + `isOnline` (stale/disabled rules as in US5); `saveWeight(date, kg)` validates "30.0–300.0" with 1 decimal and `date <= Today`; if an entry exists for that date, emits a confirmation "Ya tienes un registro el <fecha>. ¿Quieres actualizarlo?" before saving (FR-024, FR-025); single-flight
- [X] T076 [P] [US6] Create `app/src/main/java/com/allfitness/app/progress/WeightChart.kt` using Compose `Canvas`: line + points in ascending date order, first/last date labels, min/max weight labels, single point when there is one entry, colors from the theme, `contentDescription` summarizing "De X kg a Y kg entre <fecha> y <fecha>" (research R9)
- [X] T077 [P] [US6] Create `app/src/main/java/com/allfitness/app/progress/WeightEntryDialog.kt`: decimal field (kg) and a Material 3 `DatePicker` defaulting to today with `selectableDates` that block future dates
- [X] T078 [US6] Create `app/src/main/java/com/allfitness/app/progress/ProgressScreen.kt`: summary card "N de M sesiones esta semana" + "Racha: K semanas" (values as received, FR-027), empty summary "Completa tu primera sesión para ver tu progreso", `WeightChart`, weight list (most recent first), "Registrar peso" button disabled offline, `OfflineBanner`, states, overwrite confirmation dialog (depends on T075, T076, T077)
- [X] T079 [US6] Replace the Progreso tab placeholder in `app/src/main/java/com/allfitness/app/navigation/MainScaffold.kt` (depends on T078)

**Checkpoint**: US1–US6 funcionan.

---

## Phase 9: User Story 7 - Gestionar perfil y cuenta (Priority: P3)

**Goal**: Ver y editar el perfil por secciones, decidir si regenerar los planes, cerrar sesión y eliminar la cuenta.

**Independent Test**: Editar cada sección, las 4 opciones de regeneración, salir con cambios sin guardar, guardar con `#falla` en lesiones, cerrar sesión (otra cuenta no ve los datos), eliminar con `ELIMINAR` (quickstart paso 8).

- [X] T080 [US7] Create `app/src/main/java/com/allfitness/app/onboarding/ProfileViewModel.kt` and `ProfileScreen.kt`: name, email (read-only, FR-028), summary of each section with "Editar" → `ProfileSection(section)`, `currentWeightKg` from the latest `WeightEntry` (FR-026a), `AiDisclaimer`, "Cerrar sesión" with confirmation dialog → `AuthRepository.logout()`, "Eliminar cuenta" → `DeleteAccount`, states + `OfflineBanner`
- [X] T081 [US7] Create `app/src/main/java/com/allfitness/app/onboarding/ProfileEditViewModel.kt`: loads the profile into an editable copy for one `ProfileSection`; validates with `ProfileValidation` (T041); save sends the full profile with `ProfileRepository.saveProfile` and, only if the weight changed in `PHYSICAL`, `ProgressRepository.upsertWeight(today, kg)` (FR-029); on failure keeps the edited values and offers retry (FR-031); `hasUnsavedChanges`; after success emits the regeneration prompt; `regenerate(routine: Boolean, diet: Boolean)` calls `generateRoutine(null)` / `generateDiet(null)` with a loading state (FR-030); save disabled offline
- [X] T082 [US7] Create `app/src/main/java/com/allfitness/app/onboarding/ProfileEditScreen.kt`: hosts the matching section composable (T044), "Guardar" button, `BackHandler` + top-bar back with dialog "Tienes cambios sin guardar. ¿Descartarlos?" (FR-032), post-save dialog "¿Quieres regenerar tu plan con los nuevos datos?" with options Rutina / Dieta / Ambas / Ninguna, and the regeneration loading/error states (depends on T081)
- [X] T083 [US7] Create `app/src/main/java/com/allfitness/app/onboarding/DeleteAccountViewModel.kt` and `DeleteAccountScreen.kt`: explanation that all data is deleted, text field that enables "Eliminar mi cuenta" only when it equals exactly `ELIMINAR`, disabled offline, calls `AuthRepository.deleteAccount()`; on failure the session stays and an error is shown (FR-034)
- [X] T084 [US7] Replace the Perfil tab placeholder and add the `ProfileSection` and `DeleteAccount` routes in `app/src/main/java/com/allfitness/app/navigation/MainScaffold.kt` (depends on T080, T082, T083)

**Checkpoint**: Las 7 historias funcionan.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Cumplimiento de la constitución y verificación final

- [X] T085 [P] Search `app/src/main/java/` for hardcoded user-visible strings, colors (`Color(0x`) and sizes outside `core/ui/theme/`, and move them to `strings.xml` / the theme
- [X] T086 [P] Accessibility pass on all screens under `app/src/main/java/com/allfitness/app/`: `contentDescription` on meaningful icons and images, 48dp touch targets (checkboxes, chips, icon buttons), text contrast with the theme in light and dark
- [ ] T087 Verify the error paths with the mock flavor: no `message`, exception or HTTP code ever reaches the UI, and a malformed response (`#roto`) shows `ErrorState` without a crash (FR-040, FR-041)
- [X] T088 Run `./gradlew lintMockDebug lintRealDebug assembleMockDebug assembleRealDebug` and fix every error and every new warning
- [ ] T089 Run the full `specs/001-allfitness-mvp/quickstart.md` walkthrough on the API 26 and target-API emulators, and tick each item in the "Manual Verification" section of `specs/001-allfitness-mvp/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias.
- **Foundational (Phase 2)**: depende de Setup y bloquea todas las historias.
- **US1 (Phase 3)**: depende de Foundational. Es la entrada a la app (login y onboarding).
- **US2, US3 (Phases 4–5)**: dependen de Foundational. Se pueden desarrollar en paralelo con US1, pero para recorrerlas en la app hace falta el login de US1 (en mock, cualquier credencial).
- **US4 (Phase 6)**: depende de US2 (`ExerciseItem`, pantallas de rutina) y US3 (`RecipeContent`, pantallas de dieta).
- **US5 (Phase 7)**: depende de US2, US3 y US4 (añade el comportamiento sin conexión a sus pantallas).
- **US6 (Phase 8)**: depende de Foundational (y de US1 para el peso del onboarding). Incluye su propio manejo sin conexión.
- **US7 (Phase 9)**: depende de US1 (secciones y validación del onboarding) y de US6 (peso actual).
- **Polish (Phase 10)**: depende de todas las historias.

### User Story Dependencies

```text
Setup → Foundational ─┬─ US1 ──────────────┬─ US7
                      ├─ US2 ─┬─ US4 ─ US5 │
                      ├─ US3 ─┘            │
                      └─ US6 ──────────────┘
```

### Within Each User Story

- Modelos y DTO → Repository (real + Fake) → ViewModel → Screen → navegación.
- Una historia se termina y se verifica antes de pasar a la siguiente prioridad.

### Parallel Opportunities

- Setup: T004, T005 y T006 en paralelo tras T001–T003.
- Foundational: T007, T008, T010, T014, T015, T016, T017 y T018 en paralelo; luego T022/T023, T026/T027, T029 y T031 en paralelo.
- US1: T033, T034, T040, T041 en paralelo; luego T038, T039 y T044 en paralelo.
- US2 y US3 pueden avanzar en paralelo entre sí (archivos distintos), y US6 en paralelo con ambas.

---

## Parallel Example: User Story 1

```bash
# Modelos y validación (archivos distintos):
Task: "T033 AuthModels/AuthDtos/AuthApi in app/src/main/java/com/allfitness/app/auth/"
Task: "T034 AuthValidation in app/src/main/java/com/allfitness/app/auth/AuthValidation.kt"
Task: "T040 ProfileModels in app/src/main/java/com/allfitness/app/onboarding/ProfileModels.kt"
Task: "T041 ProfileValidation in app/src/main/java/com/allfitness/app/onboarding/ProfileValidation.kt"

# Pantallas, una vez listo AuthRepository (T035):
Task: "T038 LoginViewModel + LoginScreen"
Task: "T039 RegisterViewModel + RegisterScreen"
Task: "T044 Onboarding section composables"
```

## Parallel Example: Equipo de 3 tras Foundational

```bash
Developer A: US1 (T033–T048)
Developer B: US2 (T049–T056), luego US4
Developer C: US3 (T057–T061), luego US6
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2)

1. Phase 1 Setup → Phase 2 Foundational.
2. Phase 3 US1: registro, onboarding y generación.
3. Phase 4 US2: seguir la rutina (sin esto el MVP no da valor diario).
4. **STOP and VALIDATE**: quickstart pasos 1–3 en `mockDebug`.
5. Demo con el equipo de backend usando `contracts/`.

### Incremental Delivery

1. US3 Dieta → validar el paso 4.
2. US4 Alternativas → validar el paso 5.
3. US5 Sin conexión → validar el paso 6.
4. US6 Progreso → validar el paso 7.
5. US7 Perfil → validar el paso 8.
6. Polish → recorrido completo y lint limpio.

---

## Notes

- [P] = archivos distintos, sin dependencias pendientes.
- Sin tareas de tests automatizados (constitución); la verificación es T089.
- Si backend entrega algo distinto a `contracts/`, primero se actualiza el contrato y luego el código (constitución II).
- Hacer commit tras cada tarea o grupo lógico.

## Implementation Notes (2026-10-28)

Deviations from the literal task text, kept deliberately small:

- **Fakes at API level**: a single `app/src/mock/java/com/allfitness/app/mock/FakeBackend.kt` implements `AuthApi`, `ProfileApi`, `RoutineApi`, `ProgressApi` (and `DietApi` through an inner object, since its signatures clash with `RoutineApi`). It replaces the per-Repository Fakes, `MockAssets`/`MockBehavior` and `Mock/RealRepositoryModule` (T019, T032, T036, T042, T048, T050, T064, T074). The real Repositories, `SafeApiCall` and the DTO mappers run in both flavors. This departs from the wording of constitution II ("Fake per Repository") and should be ratified or reverted.
- Repositories are concrete classes (no interface/Impl pair); alternatives live in `RoutineRepository`/`DietRepository` (no `AlternativeRepository`); `SessionStore` merged into `SessionManager`; one OkHttp client with a `@LongRunning` annotation instead of two; 401 handled in `SafeApiCall` instead of an interceptor; own vector drawables instead of an icon dependency.
- Files are grouped per feature (e.g. `RoutineData.kt`, `DietScreens.kt`, `ProfileScreens.kt`) instead of one file per class.
- Mock delays: 800 ms normal, 5 s AI, 55 s with `#lento`.
- T087 and T089 remain open: they need the manual walkthrough of `quickstart.md` on the API 26 and target-API emulators. Only a smoke run was done (target API emulator: login → Rutina, Dieta, Progreso, Perfil load without crashes).
