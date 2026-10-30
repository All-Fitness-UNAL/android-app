# Graph Report - Allfitness  (2026-10-29)

## Corpus Check
- 171 files · ~235,995 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 24 file(s) not represented in the graph (top: .xml 13, (none) 3, .properties 3)

## Summary
- 2175 nodes · 5616 edges · 90 communities (77 shown, 13 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 282 edges (avg confidence: 0.86)
- Token cost: 726,112 input · 0 output

## Community Hubs (Navigation)
- Live Browser Core
- Live Param Panel
- Live Agent Bar
- UI Strings & Error Text
- Impeccable Polish & Quieter
- Screenshot Library
- Network & Session Layer
- Live Session Lifecycle
- Live Configure Bar
- Live Steer Chat
- UiState Screen States
- Progress Screen
- Alternative ViewModel
- Shared UI Components
- Live Design Panel
- Live Manual Edit Capture
- Profile Domain Enums
- Routine Data Layer
- Impeccable Doctor & Drift
- Auth Screens & Navigation
- Diet Data API
- Speckit Workflow YAML
- Room Snapshot DB
- Fake Diet Backend
- MVP Feature Instructions
- Auth Data API
- Speckit PowerShell Scripts
- Routine Week Screen
- Live Pending Apply Dock
- MD3 Color System
- Adaptive Layout & Edit Apply
- Impeccable Review Agents
- Constitution & Spec Template
- Tasks & Error Mapping
- Routine & Diet Contracts
- Impeccable Motion & Color
- Impeccable Live Generate
- AppResult & Week Dates
- Progress Data API
- Alternative Sheet UI
- Live DOM Helpers
- Live Session State
- Material 3 Skill Core
- Profile Form Fields
- Live Svelte Text Mapping
- Profile & Progress Contracts
- Impeccable Delight & Operate
- Connectivity Observer
- Onboarding Form State
- Compose M3 Theming
- Impeccable Harden & Distill
- Auth ViewModel & Validation
- Delete Account Flow
- MD3 Navigation Components
- AppError Types
- Profile Draft Storage
- Impeccable Audit
- Live Annotation Pins
- Auth Contract & Conventions
- AI Alternatives Rules
- Impeccable Asset Producer
- App Theme & MainActivity
- SafeApiCall Error Parsing
- Real API Module
- Domain Data Model
- Snapshot Keys
- Live Agent Target Claim
- MD3 Typography & Shape
- MD3 Component Catalog
- MD3 Layout & Spacing
- Repository Cache Mapping
- Mock API Module
- Compose Component Examples
- Android Navigation Patterns
- Onboarding ViewModel
- Quickstart & Mock Flavors
- Allergy Enum
- Equipment Enum
- Live Inline Text Edit
- Profile Sections & Validation
- MD3 Cards & Elevation
- Live Ignore Rules
- Manual Edit Apply Contract
- Hilt Application

## God Nodes (most connected - your core abstractions)
1. `Feature Spec All Fitness MVP` - 42 edges
2. `FakeBackend` - 40 edges
3. `AppResult` - 39 edges
4. `connectSSE()` - 34 edges
5. `setLiveState()` - 33 edges
6. `resumeSession()` - 33 edges
7. `AlternativeViewModel` - 33 edges
8. `showToast()` - 31 edges
9. `initGlobalBar()` - 30 edges
10. `el()` - 29 edges

## Surprising Connections (you probably didn't know these)
- `Adapt reference (native)` --semantically_similar_to--> `FoldableAwareLayout`  [INFERRED] [semantically similar]
  .claude/skills/impeccable/reference/adapt.native.md → .agents/skills/mobile-android-design/references/material3-theming.md
- `Android platform reference` --semantically_similar_to--> `Tonal Elevation levels`  [INFERRED] [semantically similar]
  .claude/skills/impeccable/reference/android.md → .agents/skills/mobile-android-design/references/material3-theming.md
- `Android platform reference` --semantically_similar_to--> `AppTypography (M3 type scale)`  [INFERRED] [semantically similar]
  .claude/skills/impeccable/reference/android.md → .agents/skills/mobile-android-design/references/material3-theming.md
- `Material color roles and Dynamic Color` --semantically_similar_to--> `Dynamic Color (Material You)`  [INFERRED] [semantically similar]
  .claude/skills/impeccable/reference/android.md → .agents/skills/mobile-android-design/references/material3-theming.md
- `Material color roles and Dynamic Color` --semantically_similar_to--> `LightColorScheme / DarkColorScheme`  [INFERRED] [semantically similar]
  .claude/skills/impeccable/reference/android.md → .agents/skills/mobile-android-design/references/material3-theming.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Navigation transforms bar to rail to drawer across window size classes** — _agents_skills_material_3_references_navigation_patterns_responsive_navigation_pattern, _agents_skills_material_3_references_layout_and_responsive_window_size_classes, _agents_skills_material_3_references_component_catalog_navigation_bar, _agents_skills_material_3_references_component_catalog_navigation_rail, _agents_skills_material_3_references_component_catalog_navigation_drawer [EXTRACTED 1.00]
- **Seed color to tonal palettes to color roles to tokens** — _agents_skills_material_3_references_color_system_tonal_palette_system, _agents_skills_material_3_references_color_system_color_roles, _agents_skills_material_3_references_color_system_material_color_utilities, _agents_skills_material_3_references_theming_and_dynamic_color_generatetheme, _agents_skills_material_3_references_color_system_dynamic_color [INFERRED 0.85]
- **MD3 canonical layouts** — _agents_skills_material_3_references_layout_and_responsive_canonical_layouts, _agents_skills_material_3_references_layout_and_responsive_feed_layout, _agents_skills_material_3_references_layout_and_responsive_list_detail_layout, _agents_skills_material_3_references_layout_and_responsive_supporting_pane_layout [EXTRACTED 1.00]
- **Impeccable subagent roles** — _claude_agents_impeccable_asset_producer, _claude_agents_impeccable_documenter, _claude_agents_impeccable_finish_reviewer, _claude_agents_impeccable_manual_edit_applier [INFERRED 0.85]
- **Craft floor ban enforcement** — _claude_skills_impeccable_reference_craft_floor_refuse, _claude_skills_impeccable_reference_craft_floor_kicker_ban, _claude_agents_impeccable_finish_reviewer_floor_check, _claude_agents_impeccable_documenter_never_canonize_floor_refusal, _claude_skills_impeccable_skill [INFERRED 0.85]
- **Comp-led build pipeline (spec, plates, review, finish)** — _claude_agents_impeccable_asset_producer_comp_spec, _claude_agents_impeccable_asset_producer_plate, _claude_skills_impeccable_reference_component_review_plan_asset_review, _claude_skills_impeccable_reference_component_review_hero_manifest, _claude_agents_impeccable_finish_reviewer_fidelity_matrix [INFERRED 0.85]
- **Visitor-mode rule files for concept-seed** — _claude_skills_impeccable_reference_mode_operate, _claude_skills_impeccable_reference_mode_persuade, _claude_skills_impeccable_reference_mode_read, _claude_skills_impeccable_reference_new_work_concept_seed [EXTRACTED 1.00]
- **Live variant workflow (setup, generate lane, live loop, manual edit apply)** — _claude_skills_impeccable_reference_live_setup, _claude_skills_impeccable_reference_generate, _claude_skills_impeccable_reference_live, _claude_skills_impeccable_reference_degraded_manual_edit_applier [EXTRACTED 1.00]
- **Impeccable project context artifacts maintained by doctor** — _claude_skills_impeccable_reference_init_product_md, _claude_skills_impeccable_reference_document_design_md, _claude_skills_impeccable_reference_document_design_json_sidecar, _claude_skills_impeccable_reference_hooks_impeccable_config, _claude_skills_impeccable_reference_new_work_surface_brief [EXTRACTED 1.00]
- **Spec Kit Spec-Driven Development Workflow** — _claude_skills_speckit_specify_skill_speckit_specify, _claude_skills_speckit_clarify_skill_speckit_clarify, _claude_skills_speckit_plan_skill_speckit_plan, _claude_skills_speckit_tasks_skill_speckit_tasks, _claude_skills_speckit_analyze_skill_speckit_analyze, _claude_skills_speckit_implement_skill_speckit_implement, _claude_skills_speckit_converge_skill_speckit_converge [EXTRACTED 1.00]
- **All Fitness Constitution Core Principles I-V** — _specify_memory_constitution_principle_i_frontend_only, _specify_memory_constitution_principle_ii_data_contract, _specify_memory_constitution_principle_iii_ai_content_ux, _specify_memory_constitution_principle_iv_centralized_errors, _specify_memory_constitution_principle_v_zero_shadow_code [EXTRACTED 1.00]
- **Impeccable Comp-Led Build Pipeline** — _claude_skills_impeccable_reference_visualize_three_comps, _claude_skills_impeccable_reference_visualize_approval_point, _claude_skills_impeccable_reference_region_map_region_map, _claude_skills_impeccable_reference_region_map_comp_spec, _claude_skills_impeccable_reference_visualize_plate_provenance [INFERRED 0.85]
- **Onboarding completion flow (profile -> weight -> parallel generation)** — specs_001_allfitness_mvp_contracts_profile_put_profile, specs_001_allfitness_mvp_contracts_progress_put_progress_weights_date, specs_001_allfitness_mvp_contracts_routine_post_routine_generate, specs_001_allfitness_mvp_contracts_diet_post_diet_generate, specs_001_allfitness_mvp_research_r8_onboarding_orchestration, specs_001_allfitness_mvp_tasks_t045_onboarding_viewmodel [EXTRACTED 1.00]
- **AI alternative proposal / accept flow** — specs_001_allfitness_mvp_contracts_routine_post_exercise_alternative, specs_001_allfitness_mvp_contracts_routine_post_exercise_alternative_accept, specs_001_allfitness_mvp_contracts_diet_post_meal_alternative, specs_001_allfitness_mvp_contracts_diet_post_meal_alternative_accept, specs_001_allfitness_mvp_data_model_alternative_sheet_state_machine, specs_001_allfitness_mvp_spec_fr_022a, specs_001_allfitness_mvp_tasks_t065_alternative_viewmodel [INFERRED 0.85]
- **Centralized error mapping and 401 session expiry** — specs_001_allfitness_mvp_contracts_readme_error_codes, specs_001_allfitness_mvp_research_r12_error_mapping, specs_001_allfitness_mvp_research_r6_session_401, specs_001_allfitness_mvp_tasks_t009_safe_api_call, specs_001_allfitness_mvp_tasks_t011_session_manager, instruccionconstitutcion_centralized_error_mapping [INFERRED 0.85]

## Communities (90 total, 13 thin omitted)

### Community 0 - "Live Browser Core"
Cohesion: 0.03
Nodes (105): acceptedDomAlreadyClean(), applyGlobalBarLabelState(), applyPlaceholderSizingStyles(), attachSteerFocusGuard(), averageRgb01(), bindEditBadgeProxy(), bufferToBase64(), buildPlaceholderResizeHandles() (+97 more)

### Community 1 - "Live Param Panel"
Cohesion: 0.05
Nodes (85): applyParamDefaults(), applyParamValue(), applyPlaceholderDimensions(), applySavedParamValues(), applySavedSessionMeta(), buildParamsPanel(), checkpointPayload(), clampVariantIndex() (+77 more)

### Community 2 - "Live Agent Bar"
Cohesion: 0.07
Nodes (79): actOnAgentTarget(), agentStatusText(), applyLiveBarPreference(), barPaletteForTheme(), beginNewLiveConfiguration(), brandMarkSvg(), cancelEditing(), cancelEditingToPicking() (+71 more)

### Community 3 - "UI Strings & Error Text"
Cohesion: 0.07
Nodes (4): messageRes(), Spacing, OnboardingForm(), OnboardingScreen()

### Community 4 - "Impeccable Polish & Quieter"
Cohesion: 0.05
Nodes (30): critique-storage Snapshot Backlog (latest / close), Impeccable Polish Command, Impeccable Quieter Command, impeccable comp-spec Tool, Region Map, impeccable detect (bundled web-only detector), impeccable signals, Context-Aware No-Argument Menu (+22 more)

### Community 5 - "Screenshot Library"
Cohesion: 0.09
Nodes (53): ae(), be(), bt(), Ce(), Ct(), de(), dt(), _e() (+45 more)

### Community 6 - "Network & Session Layer"
Cohesion: 0.06
Nodes (10): ApiInterceptor, NetworkModule, Active, Expired, Loading, LoggedOut, Session, SessionManager (+2 more)

### Community 7 - "Live Session Lifecycle"
Cohesion: 0.07
Nodes (52): abandonForeignSession(), abandonSupersededGo(), abortSvelteComponentInjection(), applyOriginalAttrsToSvelteAnchor(), cleanup(), clearHandled(), clearMountErrorCard(), clearScrollY() (+44 more)

### Community 8 - "Live Configure Bar"
Cohesion: 0.08
Nodes (51): actionLabel(), applyConfigureBarChrome(), bindConfigureCountPillTooltip(), bindConfigureInlineControlHover(), bindConfigureModifierPillHover(), buildConfigureActionControl(), buildConfigureCountControl(), buildConfigureRow() (+43 more)

### Community 9 - "Live Steer Chat"
Cohesion: 0.09
Nodes (47): agentHasWorkInFlight(), armPageChatForTyping(), attachSteerFocusDebug(), buildSteerProcessingDots(), buildSteerQueueHint(), clearSteerAwaitTimer(), collapsePageChat(), expandPageChat() (+39 more)

### Community 10 - "UiState Screen States"
Cohesion: 0.08
Nodes (17): Content, Done, Empty, Failed, Idle, Loading, mapContent(), Refresher (+9 more)

### Community 11 - "Progress Screen"
Cohesion: 0.08
Nodes (10): Today, parseOneDecimal(), ProgressScreen(), ProgressViewModel, SummaryCard(), toUtcMillis(), WeightChart(), WeightDialog() (+2 more)

### Community 12 - "Alternative ViewModel"
Cohesion: 0.08
Nodes (22): Accepting, AlternativePhase, AlternativeUiState, AlternativeViewModel, ExerciseReason, DISLIKE, NO_EQUIPMENT, PAIN (+14 more)

### Community 13 - "Shared UI Components"
Cohesion: 0.13
Nodes (20): AiDisclaimer(), AppTopBar(), EmptyState(), ErrorState(), LoadingState(), MessageEffect(), MessageState(), OfflineBanner() (+12 more)

### Community 14 - "Live Design Panel"
Cohesion: 0.07
Nodes (40): buildCollapsible(), buildColorModels(), buildDesignHeader(), buildListHtml(), buildRadiiModels(), buildTypographyModels(), cssSafe(), designEmptyMessage() (+32 more)

### Community 15 - "Live Manual Edit Capture"
Cohesion: 0.08
Nodes (39): addManualContextText(), applyEditing(), buildInsertPlaceholderSnapshotFromDom(), buildLocatorForLeaf(), buildPickedAnchorSnapshot(), canRestoreManualEditElement(), captureAndEmit(), collectManualContextPieces() (+31 more)

### Community 16 - "Profile Domain Enums"
Cohesion: 0.11
Nodes (24): DietType, OMNIVORE, VEGAN, VEGETARIAN, Goal, GAIN_MUSCLE, GENERAL_FITNESS, LOSE_FAT (+16 more)

### Community 17 - "Routine Data Layer"
Cohesion: 0.12
Nodes (19): CompletedRequest, DayType, REST, TRAINING, Exercise, ExerciseDto, ExerciseProposal, ExerciseProposalDto (+11 more)

### Community 18 - "Impeccable Doctor & Drift"
Cohesion: 0.09
Nodes (29): Doctor Reference, design-md-drift Finding, Schema Drift, Doctor Finding Severities (auto/mention/route), Tool Version Drift, Truth Drift, Document Reference (DESIGN.md generation), Creative North Star (+21 more)

### Community 19 - "Auth Screens & Navigation"
Cohesion: 0.10
Nodes (13): AuthScaffold(), EmailField(), LoginScreen(), PasswordField(), RegisterScreen(), SubmitArea(), AppRoot(), AppViewModel (+5 more)

### Community 20 - "Diet Data API"
Cohesion: 0.12
Nodes (23): Diet, DietApi, DietDay, DietDayDto, Ingredient, IngredientDto, Meal, MealDto (+15 more)

### Community 21 - "Speckit Workflow YAML"
Cohesion: 0.06
Nodes (30): args, inputs, integration, spec, default, prompt, type, any (+22 more)

### Community 22 - "Room Snapshot DB"
Cohesion: 0.12
Nodes (4): AppDatabase, DatabaseModule, SnapshotDao, SnapshotEntity

### Community 23 - "Fake Diet Backend"
Cohesion: 0.13
Nodes (4): DietDto, AlternativeRequestDto, DietFake, FakeBackend

### Community 24 - "MVP Feature Instructions"
Cohesion: 0.10
Nodes (25): Instruccion Specify: features MVP, Feature 1: Registro, inicio de sesion y sesion, Feature 8: Comportamiento transversal, Feature 4: Nutricion / Dieta, Feature 2: Onboarding, Feature 7: Perfil de usuario, POST /auth/register, UiState comun (Loading/Content/Empty/Error) (+17 more)

### Community 25 - "Auth Data API"
Cohesion: 0.16
Nodes (9): AuthApi, AuthRepository, LoginRequest, MeDto, RegisterRequest, SessionDto, toDomain(), UserDto (+1 more)

### Community 26 - "Speckit PowerShell Scripts"
Cohesion: 0.16
Nodes (19): Find-SpecifyRoot(), Format-SpecKitCommand(), Get-CurrentBranch(), Get-FeaturePathsEnv(), Get-InvokeSeparator(), Get-NormalizedPriority(), Get-Python3Command(), Get-RepoRoot() (+11 more)

### Community 27 - "Routine Week Screen"
Cohesion: 0.10
Nodes (6): displayName(), longLabel(), shortLabel(), DayCard(), RoutineWeekViewModel, WeekDayRow

### Community 28 - "Live Pending Apply Dock"
Cohesion: 0.19
Nodes (24): clearStoredManualApplyState(), fetchPendingCount(), handleManualEditActivity(), hidePendingApplyDock(), manualApplyLoadingText(), manualApplyStateKey(), manualEditEventForCurrentPage(), numberOrNull() (+16 more)

### Community 29 - "MD3 Color System"
Cohesion: 0.13
Nodes (22): MD3 Color System Reference, Baseline Color Scheme (#6750A4), Color Harmonization (Blend.harmonize), MD3 Color Roles, Dynamic Color (wallpaper / content-based), Fixed Accent Colors, @material/material-color-utilities, Tonal Palette System (seed to 5 palettes) (+14 more)

### Community 30 - "Adaptive Layout & Edit Apply"
Cohesion: 0.09
Nodes (16): AdaptiveLayout (Window Size Classes), FoldableAwareLayout, Impeccable Manual Edit Applier agent, Canonical Apply result JSON (done/partial/error), Source hint evidence resolution order, Adapt reference (web), Adapt reference (native), iOS <-> Android idiom translation table (+8 more)

### Community 31 - "Impeccable Review Agents"
Cohesion: 0.11
Nodes (13): Impeccable Documenter agent, Impeccable Finish Reviewer agent, Contract seed reference check (impeccable concept-seed), Disposition (recapture / rebuild / fix / ship), Truth check (plates shipped, no invented claims), Verdict Pass (post-fix scoring), Craft floor, Craft floor Verify checks (+5 more)

### Community 32 - "Constitution & Spec Template"
Cohesion: 0.13
Nodes (10): Spec Template, [NEEDS CLARIFICATION] marker, Constitucion All Fitness (instruccion), Dependencia con backend, Non-Goals de la iteracion, Specification Quality Checklist, Implementation Plan All Fitness MVP, Constitution Check (principios I-V, Stack, Calidad) (+2 more)

### Community 33 - "Tasks & Error Mapping"
Cohesion: 0.16
Nodes (13): Tasks Template, Room snapshots table, Research All Fitness MVP, FR-005 Todas las pantallas salvo Login/Registro requieren sesion, Tasks All Fitness MVP, Phase 1 Setup (T001-T006), Phase 2 Foundational (T007-T032), T008 AppError sealed interface + AppResult (+5 more)

### Community 34 - "Routine & Diet Contracts"
Cohesion: 0.11
Nodes (20): Feature 3: Rutinas (ver y seguir), Contrato Dieta, GET /diet, POST /diet/generate (IA, 90 s), POST /diet/meals/{mealId}/alternative, POST /diet/meals/{mealId}/alternative/{proposalId}/accept, Contrato Rutina, GET /routine (+12 more)

### Community 35 - "Impeccable Motion & Color"
Cohesion: 0.10
Nodes (9): Animate reference, Motion timing and easing table, Bolder reference, Colorize reference, Live-mode color-amount parameter, WCAG AA contrast minimums, OKLCH palettes and ramps, Impeccable skill (+1 more)

### Community 36 - "Impeccable Live Generate"
Cohesion: 0.12
Nodes (19): Generate Reference (live fast lane), Live Action Vocabulary Mapping, impeccable live-generate Command, Layout Reference, Density Live Parameter, Spatial Thesis, Squint Test, Live Mode Reference (+11 more)

### Community 37 - "AppResult & Week Dates"
Cohesion: 0.17
Nodes (5): AppResult, Failure, map(), Success, RoutineRepository

### Community 38 - "Progress Data API"
Cohesion: 0.16
Nodes (8): ProgressApi, SummaryDto, toDomain(), TrainingSummary, WeightDto, WeightEntry, WeightRequest, ProgressView

### Community 39 - "Alternative Sheet UI"
Cohesion: 0.13
Nodes (13): AlternativeHost(), AlternativeTarget, Exercise, ProposalView(), ReasonList(), Recipe, RequestForm(), WholeDiet (+5 more)

### Community 40 - "Live DOM Helpers"
Cohesion: 0.16
Nodes (15): createLiveBrowserDomHelpers(), activeElementDeep(), cloneWithoutChrome(), copyTextViaSelection(), cssId(), liveUiRoot(), makeFrozenAnchor(), raiseHitModal() (+7 more)

### Community 41 - "Live Session State"
Cohesion: 0.19
Nodes (17): createLiveBrowserSessionState(), clearHandled(), clearScrollY(), clearSession(), isHandled(), loadSession(), markHandled(), nextCheckpointRevision() (+9 more)

### Community 42 - "Material 3 Skill Core"
Cohesion: 0.15
Nodes (14): Shape Morphing (Expressive), Material 3 Skill, App Shell Pattern, Component Name Mapping (Web/Flutter/Compose), M3 Expressive Platform Availability Matrix, M3 Expressive Update (May 2025), @material/web (maintenance mode), MaterialTheme (Compose) (+6 more)

### Community 43 - "Profile Form Fields"
Cohesion: 0.29
Nodes (12): ChipGroup(), FormTextField(), MultiChoiceChips(), SingleChoiceChips(), label(), labelRes(), NutritionSection(), PhysicalSection() (+4 more)

### Community 44 - "Live Svelte Text Mapping"
Cohesion: 0.16
Nodes (19): buildSvelteExpressionTextMap(), buildSveltePropValuesFromLiveElement(), buildSveltePropValuesV2(), cloneWithoutElements(), collectTextNodes(), collectVisibleTexts(), cssEscapeIdent(), elementMatchesOriginalMarkup() (+11 more)

### Community 45 - "Profile & Progress Contracts"
Cohesion: 0.15
Nodes (17): Feature 6: Progreso, Contrato Perfil, GET /profile, Orden al terminar el onboarding (R8), PUT /profile, Contrato Progreso, GET /progress/summary?weekStart, GET /progress/weights (+9 more)

### Community 46 - "Impeccable Delight & Operate"
Cohesion: 0.12
Nodes (11): Delight Reference, Operate Mode Rules (concept-seed), Signature Move, Persuade and Experience Mode Rules, Read Mode Rules (concept-seed), concept-seed Roll, Visitor Modes (Persuade/Operate/Read/Experience), Optimize Reference (+3 more)

### Community 48 - "Onboarding Form State"
Cohesion: 0.11
Nodes (17): OnboardingUiState, ProfileField, AGE, DAYS, DIET_TYPE, DISLIKED, EQUIPMENT, GOAL (+9 more)

### Community 49 - "Compose M3 Theming"
Cohesion: 0.19
Nodes (13): Material Design 3 Theming, AppShapes (M3 shape scale), AppTheme composable, AppTypography (M3 type scale), ExtendedColors, LightColorScheme / DarkColorScheme, LocalExtendedColors, SuccessBanner (+5 more)

### Community 50 - "Impeccable Harden & Distill"
Cohesion: 0.12
Nodes (13): Distill Reference, Progressive Disclosure, Harden Reference, API Error Handling by Status Code, Arabic-script Specifics (ZWNJ, Yeh/Kaf, digits, calendars), Internationalization Hardening, Interrupted Gesture Handling, Text Overflow and Wrapping (+5 more)

### Community 51 - "Auth ViewModel & Validation"
Cohesion: 0.19
Nodes (3): AuthFormState, AuthValidation, AuthViewModel

### Community 52 - "Delete Account Flow"
Cohesion: 0.19
Nodes (4): DeleteAccountState, DeleteAccountViewModel, ProfileEditViewModel, ProfileViewModel

### Community 53 - "MD3 Navigation Components"
Cohesion: 0.13
Nodes (16): Navigation Bar Component, Navigation Drawer Component, Navigation Rail Component, Top App Bar Variants, Adaptive Component Behavior, MD3 Window Size Classes (5 breakpoints), MD3 Navigation Patterns Reference, Navigation Component Selection Decision Tree (+8 more)

### Community 54 - "AppError Types"
Cohesion: 0.13
Nodes (15): AiUnavailable, AppError, EmailTaken, errorOrNull(), InvalidCredentials, InvalidResponse, NoActivePlan, NoConnection (+7 more)

### Community 55 - "Profile Draft Storage"
Cohesion: 0.21
Nodes (5): OnboardingDraft, OnboardingDraftStore, ProfileForm, toForm(), ProfileEditState

### Community 56 - "Impeccable Audit"
Cohesion: 0.16
Nodes (13): Audit reference (web), Desktop app audit via CDP (--remote-debugging-port), Audit Health Score (5 dimensions, /20), Implementation Integrity (impeccable detect), Audit reference (native), Adaptivity dimension, Platform Conformance (CRITICAL), Critique reference (+5 more)

### Community 57 - "Live Annotation Pins"
Cohesion: 0.24
Nodes (15): beginEditPin(), buildAnnotationsForCapture(), buildPinElement(), cancelEditingPin(), finalizeEditingPin(), initAnnotOverlay(), localCoords(), onAnnotDown() (+7 more)

### Community 58 - "Auth Contract & Conventions"
Cohesion: 0.18
Nodes (11): Contrato Auth, DELETE /me, GET /me, POST /auth/login, POST /auth/logout, Contrato API README, Convenciones API (base URL /api/v1, camelCase, enums MAYUSCULAS, Bearer), Catalogo de errores (code, message, fields) (+3 more)

### Community 59 - "AI Alternatives Rules"
Cohesion: 0.17
Nodes (9): Feature 5: Alternativas y feedback a la IA, Tiempos: 15 s normales, 90 s endpoints IA, FR-019 Recomendacion profesional ante dolor, FR-020 Contenido visible, carga y accion deshabilitada durante IA, FR-021 Esperar la IA al menos 60 s, FR-022 Reemplazar solo cuando la alternativa llega bien, FR-023a Rutina regenerada aplica desde hoy, US4 Pedir alternativas y regenerar planes (P2) (+1 more)

### Community 60 - "Impeccable Asset Producer"
Cohesion: 0.21
Nodes (7): Impeccable Asset Producer agent, Measured spec (.impeccable/build/spec.json, impeccable comp-spec), Plan and asset review (component-review), impeccable component-review plan/capture/serve/verify, Hero first-viewport manifest (schemaVersion 2), Receipt actions (approve / revise / split / reclassify / missing), Degraded inline Asset Producer

### Community 64 - "Domain Data Model"
Cohesion: 0.24
Nodes (13): Data Model All Fitness MVP, AlternativeRequest, AlternativeTarget, Diet, DietDay, Exercise, Ingredient, Meal (+5 more)

### Community 65 - "Snapshot Keys"
Cohesion: 0.22
Nodes (8): SnapshotKey, DIET, PROFILE, ROUTINE, SUMMARY, WEIGHTS, WORKOUT_WEEK, SnapshotStore

### Community 66 - "Live Agent Target Claim"
Cohesion: 0.35
Nodes (13): agentTargetBusyReason(), agentTargetOverlayGone(), agentTargetTaken(), claimAgentTarget(), claimAndActOnAgentTarget(), declineAgentTargetBusy(), declineAgentTargetUnresolvable(), handleAgentTarget() (+5 more)

### Community 67 - "MD3 Typography & Shape"
Cohesion: 0.20
Nodes (10): Surface Container Hierarchy, MD3 Typography, Shape, Elevation, Motion Reference, Brand and Plain Typeface Roles, Component Type Style Usage Map, Corner Radius Shape Scale, Easing and Duration Motion Tokens, Roboto Flex Variable Font, Spring-Based Motion Physics (+2 more)

### Community 68 - "MD3 Component Catalog"
Cohesion: 0.24
Nodes (10): MD3 Component Catalog, MD3 Buttons (5 emphasis types), MD3 Chips, MD3 Dialog, Expressive Button Sizes (XS-XL), Floating Action Button, I/O 2026 Expressive Component Updates, MD3 List (+2 more)

### Community 69 - "MD3 Layout & Spacing"
Cohesion: 0.27
Nodes (11): MD3 Layout and Responsive Reference, 4dp Spacing Grid, Canonical Layouts, CSS Container Queries, Edge-to-Edge and WindowInsets, Feed Layout, Foldable Postures (tabletop, book), List-Detail Layout (+3 more)

### Community 70 - "Repository Cache Mapping"
Cohesion: 0.33
Nodes (4): Cached, mapData(), DietRepository, ProgressRepository

### Community 72 - "Compose Component Examples"
Cohesion: 0.20
Nodes (10): MD3 Text Field, Jetpack Compose Component Library, AsyncContent / AsyncState, DateTimePickerExample, LoginForm, OptionsBottomSheet, RefreshableList (pull to refresh), SettingsScreen (selection controls) (+2 more)

### Community 73 - "Android Navigation Patterns"
Cohesion: 0.24
Nodes (10): Android Navigation Patterns Reference, AnimatedNavigation, BottomNavWithBadges, DeepLinkNavigation, ModalDrawerNavigation, NavigationHandler, NavigationViewModel (event flow), NestedNavigation (graphs) (+2 more)

### Community 76 - "Quickstart & Mock Flavors"
Cohesion: 0.27
Nodes (7): Quickstart Verificacion MVP, Compilar: assembleMockDebug / lintMockDebug / assembleRealDebug, Escenarios especiales del flavor mock (#lento, #falla, #roto, #sindieta, 299.9), Recorrido de validacion en 9 pasos, FR-043 Funcionar con datos de ejemplo conformes al contrato, Manual Verification checklist, T087/T089 open: manual quickstart walkthrough pending

### Community 77 - "Allergy Enum"
Cohesion: 0.22
Nodes (9): Allergy, EGG, GLUTEN, LACTOSE, OTHER, PEANUTS, SHELLFISH, SOY (+1 more)

### Community 78 - "Equipment Enum"
Cohesion: 0.22
Nodes (9): Equipment, BARBELL, BENCH, BODYWEIGHT, DUMBBELLS, GYM_MACHINES, KETTLEBELL, PULL_UP_BAR (+1 more)

### Community 79 - "Live Inline Text Edit"
Cohesion: 0.22
Nodes (9): collectEditableTextRows(), visit(), own(), pickable(), stopFocusOutIntoChrome(), enableInlineEdit(), layoutFlowChildren(), onInlineInput() (+1 more)

### Community 80 - "Profile Sections & Validation"
Cohesion: 0.25
Nodes (6): ProfileSection, GOAL, NUTRITION, PHYSICAL, TRAINING, ProfileValidation

### Community 81 - "MD3 Cards & Elevation"
Cohesion: 0.29
Nodes (6): MD3 Cards (Filled, Outlined, Elevated), Elevation Levels 0-5, Mobile Android Design Detailed Sections, Android MD3 Core Concepts, Compose Layout System (Column/Row/Lazy), FeatureCard

### Community 82 - "Live Ignore Rules"
Cohesion: 0.52
Nodes (6): globToRegex(), matchesScope(), normalizeIgnoreRule(), normalizeIgnoreValue(), pageCandidates(), resolveDetectIgnores()

### Community 83 - "Manual Edit Apply Contract"
Cohesion: 0.40
Nodes (3): Impeccable Manual Edit Applier (degraded inline), Manual Edit Apply Output Contract (done/partial/error JSON), Handle manual_edit_apply Event

## Ambiguous Edges - Review These
- `8dp Spacing System` → `4dp Spacing Grid`  [AMBIGUOUS]
  .agents/skills/material-3/references/layout-and-responsive.md · relation: conceptually_related_to
- `R3 Flavors mock/real y Fakes` → `Implementation note: FakeBackend at API level`  [AMBIGUOUS]
  specs/001-allfitness-mvp/tasks.md · relation: conceptually_related_to

## Knowledge Gaps
- **186 isolated node(s):** `Exercise`, `Recipe`, `WholeRoutine`, `WholeDiet`, `DISLIKE` (+181 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 409 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppResult` connect `AppResult & Week Dates` to `UI Strings & Error Text`, `Repository Cache Mapping`, `Progress Data API`, `UiState Screen States`, `Progress Screen`, `Alternative ViewModel`, `Profile Domain Enums`, `AppError Types`, `Fake Diet Backend`, `Auth Data API`, `Routine Week Screen`, `SafeApiCall Error Parsing`?**
  _High betweenness centrality (0.013) - this node is a cross-community bridge._
- **What connects `Exercise`, `Recipe`, `WholeRoutine` to the rest of the system?**
  _186 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Live Browser Core` be split into smaller, more focused modules?**
  _Cohesion score 0.034561834561834565 - nodes in this community are weakly interconnected._
- **Why does `SessionManager` connect `Network & Session Layer` to `UI Strings & Error Text`, `Onboarding ViewModel`, `Auth Screens & Navigation`, `Room Snapshot DB`, `Auth Data API`, `SafeApiCall Error Parsing`?**
  _High betweenness centrality (0.012) - this node is a cross-community bridge._
- **Should `Live Param Panel` be split into smaller, more focused modules?**
  _Cohesion score 0.05350140056022409 - nodes in this community are weakly interconnected._

### Low-confidence Hints
_AMBIGUOUS edges — the extractor was unsure. Verify before acting on these._

- **What is the exact relationship between `8dp Spacing System` and `4dp Spacing Grid`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `R3 Flavors mock/real y Fakes` and `Implementation note: FakeBackend at API level`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._