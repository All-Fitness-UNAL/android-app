# Interfaz del prototipo de Figma: plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Llevar la app a la interfaz de los mockups de Figma: tema oscuro único, verde de acento, botones en píldora y anillos de progreso. Se añaden la pestaña Inicio y el modo sesión, sin inventar datos ni funciones.

**Architecture:**
1. Enmendar la spec 001 y preparar recursos: fuentes e íconos.
2. Reemplazar los tokens de `core/ui/theme`.
3. Reconstruir los componentes de `core/ui`.
4. Rehacer pantalla por pantalla, solo en Composables y en funciones puras de presentación.

Cada decisión de presentación con lógica (qué botón mostrar, qué día marcar, qué página sigue) se extrae a una función pura con test. Inicio solo combina los repositorios existentes en un ViewModel nuevo de lectura. El modo sesión reutiliza `RoutineDayViewModel`.

**Tech Stack:** Kotlin 2.4, Compose (BOM 2026.09.00), Material 3 1.4, Navigation 2.10, Hilt y Vico 3.3.1. Recursos nuevos: fuentes Outfit y Manrope (OFL) e íconos Lucide (ISC) como VectorDrawable. No se añaden librerías.

**Spec:**
- `docs/superpowers/specs/2026-10-30-interfaz-figma-design.md`: valores, componentes, mapa de pantallas y copia.
- `docs/design/figma/arbol-nodos.txt`: medidas exactas por nodo.
- Restricciones funcionales: `specs/001-allfitness-mvp/spec.md`, enmendada en la Task 0.

## Global Constraints

- **Solo frontend.** No se cambian:
  - contratos, DTOs, repositorios, `core/network`, `core/database`, `core/session` ni `app/src/mock/`;
  - la lógica de los ViewModels existentes.
- **ViewModels:** solo se permite añadir propiedades derivadas de solo lectura y el `HomeViewModel` nuevo, que únicamente lee.
- **Fuera de alcance (tabla §3 de la spec):** temporizadores, registro por serie, kcal consumidas o quemadas, agua, notificaciones, cambio de unidades, fotos inventadas y favoritos.
- **Tema:** solo oscuro. No hay hex, `dp` ni `sp` sueltos en pantallas: todo sale de `core/ui/theme`.
- **Copia:** todo texto visible va en `res/values/strings.xml`, en español (FR-042). Las claves nuevas son las de §8 de la spec. Los textos existentes no cambian.
- **Aviso de IA (FR-037):** `AiNote` con el texto completo visible sin desplazar en Inicio, Rutina, modo sesión, Dieta, Receta y Perfil. `AiBadge` no lo sustituye.
- **Marcar ejercicios y terminar la sesión:**
  - solo hoy, en día de entreno y sin terminar (`DayView.editable`, FR-013);
  - "Terminar" se permite aunque falten ejercicios (FR-014);
  - sin conexión se deshabilita con mensaje (FR-039).
- **Estados (FR-036):** toda pantalla y cada sección de Inicio maneja cargando, contenido, vacío y error con reintentar.
- **Accesibilidad:**
  - contraste: texto ≥ 4,5:1, controles y gráficos ≥ 3:1, fijados en `ContrastTest`;
  - áreas táctiles ≥ 48 dp;
  - fuente al 130 % y al 200 % sin recortes: las tarjetas tienen alto mínimo, nunca fijo;
  - con `ANIMATOR_DURATION_SCALE == 0` no se anima nada salvo la carga.
- **Build y lint:** `./gradlew :app:testMockDebugUnitTest :app:installMockDebug :app:lintMockDebug :app:lintRealDebug` sin errores ni avisos nuevos de lint.
- **Antes de cada commit:**
  - `git status` y revisión del diff;
  - `ponytail-review` del diff de la tarea; los hallazgos de correctitud se corrigen en esa misma tarea;
  - el mensaje termina en `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- **Capturas:** van en `visual-audit/figma/<tarea>/`.
  - Antes de tocar la escala de fuente, la rotación o el tamaño de pantalla del emulador, se lee su valor y se restaura al terminar.
  - Los toques usan coincidencia **exacta** de texto (lección de la ejecución anterior: "4" tocó "Paso 3 de 4").
- **Instalación de herramientas:** cualquier `npm install` se hace en una carpeta con su propio `package.json` (lección: sin él, npm escribió en `C:\Users\USER\package.json`).

## Review Focus

1. **Inicio con datos parciales:** con la rutina cargada y la dieta vacía, con error o sin conexión, cada sección muestra su propio estado y las demás siguen visibles. Test `dietDayForMissingDietIsNull` en la Task 5; captura con `#sindieta`.
2. **Día de entreno con 0 ejercicios:**
   - el CTA no abre una sesión vacía (`workoutCta() == NONE`);
   - los anillos muestran 0/0 sin dividir por cero.
   - Tests en las Tasks 3 y 5.
3. **Modo sesión al marcar el último ejercicio:** no avanza fuera de rango y deja "Terminar sesión" a la vista. Test `nextPageAfterMarkStaysOnLast` en la Task 7.
4. **Modo sesión en un día que no es editable** (pasado, futuro o ya terminado): no hay "Marcar como hecho", se ve el mensaje existente y "Pedir alternativa" sigue disponible. Test `readOnlyDayHasNoMarkNorFinish` en la Task 7.
5. **Fuente al 200 %:** con textos largos (nombres de receta de 3 líneas, "Resistance Bands") las tarjetas compactas crecen y los botones fijos no tapan contenido (la lista reserva su alto con `contentPadding`). Capturas al 200 % en cada tarea de pantalla.

---

### Task 0: Enmienda de la spec, fuentes e íconos

**Files:**
- Modify: `specs/001-allfitness-mvp/spec.md` (Clarifications y FR-035; nuevos FR-035a, FR-035b y FR-035c)
- Modify: `PRODUCT.md` (Operating Context: barra con 5 destinos; Fuera de alcance y Accessibility: tema oscuro único en lugar de "se sigue el tema del sistema")
- Create: `app/src/main/res/font/outfit_variable.ttf`, `app/src/main/res/font/manrope_variable.ttf`
- Create: `app/src/main/assets/licenses/OFL-Outfit.txt`, `OFL-Manrope.txt`, `ISC-Lucide.txt`
- Create: `app/src/main/res/drawable/lc_*.xml` (íconos Lucide)

**Interfaces:**
- Produces:
  - `R.font.outfit_variable` y `R.font.manrope_variable`;
  - los drawables `lc_zap`, `lc_house`, `lc_dumbbell`, `lc_apple`, `lc_chart_line`, `lc_user`, `lc_sparkles`, `lc_check`, `lc_arrow_right`, `lc_arrow_left`, `lc_flame`, `lc_calendar_check`, `lc_trophy`, `lc_trending_down`, `lc_heart`, `lc_activity`, `lc_circle_play`, `lc_clock`, `lc_utensils`, `lc_coffee`, `lc_salad`, `lc_cookie`, `lc_moon`, `lc_pencil`, `lc_refresh_cw`, `lc_wifi_off`, `lc_info`, `lc_triangle_alert`, `lc_shield`, `lc_log_out`, `lc_trash_2`, `lc_plus`, `lc_x`, `lc_target`, `lc_ruler`, `lc_scale`, `lc_cake`, `lc_timer` y `lc_skip_forward`.
  - Todos son de 24 dp, con trazo de 2, extremos redondeados y `strokeColor` blanco, para teñirlos con `Icon(tint)`.

- [ ] **Step 1: Enmendar la spec 001.** Añadir en `## Clarifications` una `### Session 2026-10-30` con las 3 decisiones del usuario (§2 de la spec de diseño). Sustituir y añadir:
  - **FR-035:** "La navegación principal MUST ser una barra inferior con Inicio, Rutina, Dieta, Progreso y Perfil; tras iniciar sesión u onboarding se abre Inicio."
  - **FR-035a:** "Inicio MUST mostrar, con datos existentes, el entreno de hoy con acceso al modo sesión, el plan de comidas de hoy con sus kcal y macros, y la actividad y racha de la semana; MUST NOT mostrar consumo, agua ni otros datos que el backend no envía."
  - **FR-035b:** "El modo sesión MUST mostrar un ejercicio a la vez del día elegido, con avance y retroceso, marcar o desmarcar el ejercicio y terminar la sesión bajo las mismas reglas de FR-013, FR-014 y FR-039; MUST NOT incluir temporizadores ni registro por serie."
  - **FR-035c:** "La interfaz MUST usar un único tema oscuro; no hay selector de tema."

  La enmienda se escribe a mano: `/speckit-specify` crearía una feature y una rama nuevas. En el mismo paso se actualiza PRODUCT.md con las mismas decisiones.

  Ejecutar `/speckit-analyze` (solo informa). Esperado: sin conflictos CRITICAL nuevos. Si los hay, se ledgerean como ruling.
- [ ] **Step 2: Descargar las fuentes** desde `github.com/google/fonts`:
  - `ofl/outfit/Outfit[wght].ttf` → `outfit_variable.ttf`;
  - `ofl/manrope/Manrope[wght].ttf` → `manrope_variable.ttf`;
  - cada `OFL.txt` → `assets/licenses/`.

  Comprobar `tnum` con fontTools, instalado con `pip install --target <scratch>/deps/fonttools fonttools` y ejecutado con `python -I`: el script lista las features GSUB de cada archivo. Esperado: Manrope tiene `tnum`. Si Outfit no lo tiene, ledgerear el ruling "cifras de display en Manrope ExtraBold" (spec §4.2).
- [ ] **Step 3: Íconos Lucide.**
  - Instalar `lucide-static` (última estable) en `<scratch>/deps/lucide/`, con su propio `package.json`.
  - Escribir `<scratch>/lucide2vd.py`, que convierte `path`, `circle`, `line`, `rect`, `polyline` y `polygon` a `pathData` de VectorDrawable con viewport 24×24.
  - Generar los íconos de la lista y copiar la `LICENSE` como `ISC-Lucide.txt`.
- [ ] **Step 4: Verificar.** `./gradlew :app:testMockDebugUnitTest :app:assembleMockDebug -q`. Esperado: BUILD SUCCESSFUL y 53 tests verdes; los recursos sin usar dan aviso de lint hasta la Task 12.
- [ ] **Step 5: Commit** `chore: amend spec 001 for home and session mode, add Outfit, Manrope and Lucide icons`.

### Task 1: Colores del tema oscuro único

**Files:**
- Modify: `app/src/main/java/com/allfitness/app/core/ui/theme/Color.kt`, `AppColors.kt`, `Theme.kt`
- Modify: `app/src/main/res/values/colors.xml` y `themes.xml`; borrar `values-night/colors.xml`
- Modify: `MainActivity.kt` (`enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))`)
- Test: `app/src/test/java/com/allfitness/app/core/ui/theme/ContrastTest.kt` (se reescribe)

**Interfaces:**
- Produces:
  - `val AppColorScheme: ColorScheme`: constructor completo de 48 roles, valores de §4.1 de la spec;
  - `data class AppColors(val macroProtein: Color, val macroCarbs: Color, val macroFat: Color, val accent: Color, val accentContainer: Color, val surfaceInput: Color, val muted: Color)` y `val DefaultAppColors`;
  - `AppTheme.colors` y `AllFitnessTheme`, que ya no lee `isSystemInDarkTheme()`;
  - `@color/brand_background` = `#11111E` y `@color/brand_primary` = `#2ECC71`, para el splash y el tema de la ventana.

- [ ] **Step 1: Reescribir `ContrastTest`.** Usa el helper `contrast(a, b)` existente y los tokens nuevos. Tests:
  - `textOnBackgroundAndCards()`: `onSurface` y `onSurfaceVariant` ≥ 4,5 sobre `background`, `surface` y `surfaceContainerHigh`;
  - `primaryAsTextAndOnPrimary()`: `primary` ≥ 4,5 sobre `background` y `surface`, y `onPrimary` ≥ 4,5 sobre `primary`;
  - `accentOnItsContainer()`: `accent` ≥ 4,5 sobre `accentContainer` mezclado con `background`;
  - `errorOnCard()`: `error` ≥ 4,5 sobre `surface`;
  - `outlineBoundsControls()`: `outline` ≥ 3 sobre `surface`, `surfaceInput` y `surfaceContainerHigh`;
  - `macrosAreGraphics()`: los 3 macros ≥ 3 sobre `surface`;
  - `mutedIsNeverText()`: documenta que `muted` < 4,5 sobre `surface`, para que nadie lo use en texto.
- [ ] **Step 2:** `./gradlew :app:testMockDebugUnitTest --tests '*ContrastTest'`. Esperado: FAIL de compilación por los tokens que faltan.
- [ ] **Step 3: Implementar los tokens.**
  - Valores de §4.1: `background #11111E`, `surface #1C1C2C`, `surfaceContainer #1C1C2C`, `surfaceContainerHigh #25253A`, `surfaceContainerHighest #2A2A3E`, `surfaceContainerLow #161626`, `surfaceContainerLowest #0B0B14`, `outline #6B6F86`, `outlineVariant #2A2A3E`, `primary #2ECC71`, `onPrimary #11111E`, `onSurface #FFFFFF`, `onSurfaceVariant #9FA2B4`, `error #FF5F57`.
  - Macros `#22A85E`, `#5B8DEF` y `#E8607A`; accent `#FFB300`; muted `#5F6175`; surfaceInput `#121B2A`.
  - `primaryContainer` es `#2ECC71` al 12 % sobre `background`, precalculado como hex opaco.
  - Los roles que el Figma no define salen del mismo tono verde y neutro, y se documentan en un comentario.
- [ ] **Step 4:** el mismo comando. Esperado: PASS. Instalar y capturar Inicio, que aún muestra Rutina, en `visual-audit/figma/t1/`: barras del sistema con íconos claros y splash oscuro.
- [ ] **Step 5: Commit** `feat(theme): single dark color scheme from the Figma mockups`.

### Task 2: Tipografía, formas, medidas y brillo

**Files:**
- Modify: `core/ui/theme/Type.kt`, `Shape.kt`, `Theme.kt` (`Spacing`)
- Create: `core/ui/theme/Glow.kt`
- Test: `app/src/test/java/com/allfitness/app/core/ui/theme/TypeTest.kt`

**Interfaces:**
- Consumes: `R.font.outfit_variable` y `R.font.manrope_variable` (Task 0).
- Produces:
  - `AppTypography` con los roles de §4.2;
  - `AppShapes`: `extraSmall` 6, `small` 8, `medium` 12, `large` 16, `extraLarge` 20 y `val PillShape = CircleShape`;
  - nuevos `Spacing`: `buttonHeight` 56 dp, `thumbExercise` 76, `thumbNext` 56, `thumbMeal` 64, `ring` 60, `ringLarge` 72, `iconTile` 36, `weekDot` 28, `checkCircle` 32, `avatarSmall` 40, `avatarLarge` 104, `logoMark` 28, `screenEdge` 24 y `progressHeight` 6;
  - `fun Modifier.primaryGlow(shape: Shape): Modifier`: `shadow(12.dp, shape, spotColor = primary, ambientColor = primary)`. Es estático, así que se aplica también con movimiento reducido; con `enabled = false` el botón no lleva brillo.

- [ ] **Step 1: Test** `TypeTest.numberRolesAreTabular()`. Afirma que los estilos usados para cifras (`displaySmall`, `headlineMedium`, `titleLarge` y `numberStyle(...)`) tienen `"tnum"` en `fontFeatureSettings`. Si en la Task 0 se dictó el ruling de Manrope para display, que su `fontFamily` sea `ManropeFamily`.
- [ ] **Step 2:** `./gradlew :app:testMockDebugUnitTest --tests '*TypeTest'`. Esperado: FAIL.
- [ ] **Step 3: Implementar.** Mantener la API de `numberStyle(size)` en `Components.kt` y `pressScale`. Borrar `ArchivoFamily` y `AtkinsonFamily` y sus fuentes, junto con sus licencias.
- [ ] **Step 4:** el mismo comando más la suite completa. Esperado: PASS.
- [ ] **Step 5: Commit** `feat(theme): Outfit and Manrope type roles, shapes, sizes and primary glow`.

### Task 3: Componentes compartidos

**Files:**
- Modify: `core/ui/Buttons.kt`, `Components.kt` (`AppCard`, `AiNote`, `AppTopBar`→`ScreenHeader`, estados), `FormFields.kt`, `DataStrip.kt`
- Create:
  - `core/ui/Progress.kt`: `sessionFraction`, que se mueve desde `Route.kt`, `ProgressRing`, `LinearProgress` y `SegmentedProgress`;
  - `core/ui/WeekStrip.kt`;
  - `core/ui/Brand.kt`: `LogoMark`, `Avatar` y `AiBadge`;
  - `core/ui/Cards.kt`: `StatCard`, `PreferenceRow`, `IconTile`, `ChoiceCard` y `PillChip`.
- Modify: `strings.xml` (`ai_badge`)
- Test: renombrar `RouteTest.kt` → `ProgressTest.kt`; crear `WeekStripTest.kt`.

**Interfaces:**
- Produces, todas en `com.allfitness.app.core.ui`:
  - `PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, @DrawableRes trailingIcon: Int? = null)` y `SecondaryButton(...)`, con la misma firma;
  - `AppCard(modifier, onClick: (() -> Unit)? = null, highlighted: Boolean = false, content)`;
  - `ScreenHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null)`;
  - `AiBadge()`, `LogoMark(modifier)` y `Avatar(name: String, size: Dp, modifier)`, que usa `fun initialOf(name: String): String`;
  - `ProgressRing(fraction: Float, size: Dp, modifier, center: @Composable () -> Unit)`;
  - `LinearProgress(fraction: Float, modifier)` y `SegmentedProgress(total: Int, reached: Int, modifier)`;
  - `WeekStrip(rows: List<WeekDayRow>, selected: LocalDate?, onSelect: ((LocalDate) -> Unit)?, modifier)` y `fun dayInitial(day: DayOfWeek): String`;
  - `StatCard(label: String, value: String, unit: String? = null, modifier)`;
  - `PreferenceRow(@DrawableRes icon: Int, label: String, value: String)` e `IconTile(@DrawableRes icon: Int, size: Dp = Spacing.iconTile)`;
  - `ChoiceCard(text: String, @DrawableRes icon: Int, selected: Boolean, onClick: () -> Unit, modifier)` y `PillChip(text: String, selected: Boolean, onClick: () -> Unit, enabled: Boolean = true)`;
  - `FormTextField`, con la misma firma y el estilo de §5: relleno `surfaceInput`, borde `outline` y radio 12.
- Se conservan `StateContent`, `MessageEffect`, `entrance`, `SectionTitle`, `numberStyle` y `DataStrip`.
- Los estados de `WeekStrip` salen de `WeekDayRow.stationState()`, que ya existe y está cubierto por `StationStateTest`.

- [ ] **Step 1: Tests.**
  - `ProgressTest`: los 3 casos existentes de `sessionFraction`, incluido `noExercisesIsZero` (0/0 → 0).
  - `WeekStripTest.initialsAreSpanishNarrow()`: `listOf(MONDAY..SUNDAY).map(::dayInitial) == listOf("L","M","X","J","V","S","D")`.
  - `BrandTest.initialOf`: `"  maría"` → `"M"`, `""` → `""` y `"𝒜na"` → `"𝒜"`. El último es un par sustituto entero; hoy `take(1)` lo parte, y es un pendiente heredado.
- [ ] **Step 2:** `--tests '*ProgressTest' --tests '*WeekStripTest' --tests '*BrandTest'`. Esperado: FAIL por `dayInitial` e `initialOf`.
- [ ] **Step 3: Implementar.**
  - Medidas del árbol de nodos (§5 de la spec).
  - `dayInitial` con `getDisplayName(TextStyle.NARROW, Today.locale)` en mayúsculas.
  - Áreas táctiles: `PillChip` y la casilla de 32 dp llevan `minimumInteractiveComponentSize()`.
  - `ProgressRing` dibuja con `drawArc` desde −90°, con el barrido animado según `Motion`, y lleva `progressBarRangeInfo`. `LinearProgress` y `SegmentedProgress` también.
  - `ScreenHeader` y `SectionTitle` marcan su título con `heading()`.
  - `WeekStrip` es un `selectableGroup` cuando hay `onSelect`.
- [ ] **Step 4:** suite completa. Esperado: PASS.
- [ ] **Step 5: Commit** `feat(ui): Figma component set (buttons, cards, rings, week strip, brand)`.

### Task 4: Navegación de 5 destinos

**Files:**
- Modify: `navigation/AppNavigation.kt` y `core/ui/Route.kt`; `RoutineDayRoute` gana `index: Int = 0`.
- Create: `home/HomeScreen.kt` (marcador vacío, que rellena la Task 5)
- Modify: `res/values/strings.xml` (`tab_home`)

**Interfaces:**
- Produces:
  - `@Serializable data object HomeRoute`;
  - `RoutineDayRoute(date: String, index: Int = 0)`;
  - pestañas en orden Inicio, Rutina, Dieta, Progreso y Perfil, con los íconos `lc_house`, `lc_dumbbell`, `lc_apple`, `lc_chart_line` y `lc_user`;
  - `startDestination = HomeRoute`.

- [ ] **Step 1: Implementar.**
  - Ítems con `indicatorColor = Color.Transparent`, seleccionado en `primary` y sin seleccionar en `onSurfaceVariant`.
  - Contenedor `surface` con hilo superior `outlineVariant`. Se resuelve el ruling T11 de "Línea": se dibuja con `drawBehind` en el contenedor de la suite.
  - Las etiquetas mantienen `maxLines = 1` y `TextAutoSize`.
  - `HomeRoute` también cuenta como pestaña en `isDetailMove`.
- [ ] **Step 2: Verificar.** Suite completa e instalación. Capturar la barra con 5 destinos al 100 % y al 200 %, más el riel en `wm size 1800x2400` / `density 240` (restaurar después). Esperado: las 5 etiquetas en una línea.
- [ ] **Step 3: Commit** `feat(nav): five destinations with Home first and session index`.

### Task 5: Inicio

**Files:**
- Create: `home/HomeScreen.kt` (`HomeViewModel` y `HomeScreen`)
- Modify: `routines/RoutineWeekScreen.kt` (se extraen `weekRows`, `workoutCta` y `firstPendingIndex`; `RoutineWeekViewModel` pasa a usar `weekRows`)
- Create: `nutrition/DietToday.kt` (`dietDayFor`)
- Modify: `strings.xml` (`home_greeting`, `home_today_workout`, `home_today_diet`, `home_progress`, `workout_meta`, `stat_exercises_today`, `stat_week_sessions`, `stat_plan_kcal`, plurales `streak_weeks`, `action_start_workout` y `action_continue_workout`)
- Test: `routines/WeekRowsTest.kt`, `routines/WorkoutCtaTest.kt`, `nutrition/DietTodayTest.kt`

**Interfaces:**
- Consumes: `RoutineRepository.routine/weekLogs/refresh`, `DietRepository.diet/refresh`, `ProgressRepository.summary/refresh`, `ProfileRepository.profile`, `SessionManager.state` y los componentes de la Task 3.
- Produces:
  - `fun weekRows(routine: Routine, logs: List<WorkoutLog>, today: LocalDate): List<WeekDayRow>`: misma lógica que hoy en el ViewModel, con `isPast` incluido;
  - `enum class WorkoutCta { START, CONTINUE, FINISHED, NONE }` y `fun WeekDayRow.workoutCta(): WorkoutCta`;
  - `fun WeekDayRow.firstPendingIndex(): Int`;
  - `fun dietDayFor(diet: Diet?, today: LocalDate): DietDay?`;
  - `HomeViewModel` con `week: StateFlow<UiState<List<WeekDayRow>>>`, `diet: StateFlow<UiState<DietDay>>`, `summary: StateFlow<TrainingSummary?>`, `profile: StateFlow<Profile?>`, `userName: StateFlow<String>` e `isOnline`. Usa un `Refresher` por repositorio, como los ViewModels actuales.

- [ ] **Step 1: Tests.**
  - `WeekRowsTest.markTodayAndPast()`: con today = miércoles, el lunes `isPast`, el miércoles `isToday`, el viernes ni lo uno ni lo otro; un log terminado del lunes da `finished`.
  - `WorkoutCtaTest`:
    - hoy entreno sin marcas → `START`;
    - con 1 marca → `CONTINUE`;
    - terminado → `FINISHED`;
    - descanso hoy → `NONE`;
    - entreno otro día → `NONE`;
    - hoy entreno con 0 ejercicios → `NONE`;
    - `firstPendingIndex` con marcas en 0 y 1 → `2`; todo marcado → `0`.
  - `DietTodayTest`:
    - `dietDayForMatchesDayOfWeek()`;
    - `dietDayForMissingDietIsNull()`;
    - `dietDayForMissingDayIsNull()`.
- [ ] **Step 2:** `--tests '*WeekRowsTest' --tests '*WorkoutCtaTest' --tests '*DietTodayTest'`. Esperado: FAIL por compilación.
- [ ] **Step 3: Implementar las funciones** y cambiar `RoutineWeekViewModel` para que use `weekRows`. Los `StationStateTest` siguen verdes.
- [ ] **Step 4: Implementar `HomeScreen`** según el frame `home-dashboard` y §6 de la spec.
  - `LazyColumn` con:
    - saludo y `Avatar`;
    - `AiNote`;
    - fila de 4 indicadores: ejercicios de hoy y sesiones de la semana como `ProgressRing` con `sessionFraction`; racha y kcal del plan como cifra;
    - tarjeta de entreno: `AiBadge`, título del día y `workout_meta` (`Profile.sessionMinutes` y número de ejercicios); nivel con `SegmentedProgress(3, …)` desde `Profile.level`; miniatura del primer ejercicio solo si tiene `imageUrl`. CTA según `workoutCta()`: `START`/`CONTINUE` navegan a `RoutineDayRoute(today, firstPendingIndex())`;
    - sección de dieta: kcal del plan, barra de macros con `macroShares()` y leyenda, y comidas que navegan a `RecipeRoute`;
    - sección de progreso: racha y `WeekStrip` sin `onSelect`.
  - Cada sección usa `StateContent` o su estado propio (FR-036).
  - Sin conexión, la CTA lleva `OfflineHint`.
- [ ] **Step 5: Verificar.** Suite, lint e instalación. Capturas en `visual-audit/figma/t5/`, con login demo, `#sindieta` y modo avión:
  - Inicio normal;
  - Inicio con 1 ejercicio marcado;
  - sin dieta;
  - sin conexión;
  - al 200 %.
- [ ] **Step 6: Commit** `feat(home): today dashboard from existing routine, diet and progress data`.

### Task 6: Rutina

**Files:**
- Modify: `routines/RoutineWeekScreen.kt` (pantalla) y `routines/ExerciseItem.kt` → `ExerciseCard`
- Modify: `strings.xml` (`workout_percent`, `exercise_rest` y `exercise_sets_reps` = "%1$d × %2$d reps" / `exercise_sets_seconds` = "%1$d × %2$d s")
- Test: `routines/ExerciseLoadTest.kt`

**Interfaces:**
- Consumes: `weekRows`, `workoutCta`, `firstPendingIndex` (Task 5) y `WeekStrip` (Task 3).
- Produces:
  - `ExerciseCard(exercise: Exercise, done: Boolean?, onClick: () -> Unit, modifier)`: la casilla de 32 dp muestra el estado y no se toca; `done = null` la oculta (días futuros y de descanso);
  - `fun Exercise.loadRes(): Pair<Int, Int>`, que devuelve `R.string.exercise_sets_reps` con reps, o `exercise_sets_seconds` con `durationSeconds`, y el segundo valor.

- [ ] **Step 1: Tests de `ExerciseLoadTest`:**
  - `repsWin()`: reps 12 → (`exercise_sets_reps`, 12);
  - `durationWhenNoReps()`: reps null y 60 s → (`exercise_sets_seconds`, 60);
  - `neitherFallsBackToSets()`: ambos null → (`exercise_sets_reps`, 0); en ese caso la UI muestra solo las series.
- [ ] **Step 2:** ejecutar el test. Esperado: FAIL.
- [ ] **Step 3: Implementar** según el frame `all-fitness-workout`:
  - `ScreenHeader("Tu rutina", AiBadge)`, `AiNote` y `WeekStrip` con selección guardada en `rememberSaveable`;
  - cabecera del día: título, `workout_percent` y "N de M" con `LinearProgress`;
  - lista de `ExerciseCard` con `done` desde `WeekDayRow.completed`; tocar una tarjeta abre `RoutineDayRoute(date, index)`.
  - **Decisión:** se marca solo en el modo sesión, como antes se marcaba solo en la vista del día. La lista no tiene un segundo flujo de marcas, así que no hace falta otro `RoutineDayViewModel` ni otra cola de marcas optimistas.
  - Botón fijo según `workoutCta()` sobre un degradado; "Regenerar rutina" al final; mensaje existente en días no-hoy.
- [ ] **Step 4: Verificar.** Suite y capturas: hoy, día pasado, día futuro, descanso, regenerando y al 200 %.
- [ ] **Step 5: Commit** `feat(routine): week strip, exercise cards and start workout CTA`.

### Task 7: Modo sesión

**Files:**
- Modify: `routines/RoutineDayScreen.kt` (UI nueva con `HorizontalPager`; `DayView` gana funciones puras)
- Modify: `strings.xml` (`session_exercise_of`, `session_up_next`, `action_mark_done`, `action_unmark_done`, `action_skip` y `action_previous_exercise`)
- Test: `routines/SessionControlsTest.kt`

**Interfaces:**
- Consumes: `RoutineDayViewModel` (sin cambios de lógica), `RoutineDayRoute.index` (Task 4) y `Exercise.loadRes()` (Task 6). "A continuación" es una fila compacta: miniatura de `Spacing.thumbNext`, nombre y carga.
- Produces:
  - `data class SessionControls(val canMark: Boolean, val canFinish: Boolean, val finishProminent: Boolean)`;
  - `fun DayView.controls(page: Int): SessionControls`;
  - `fun nextPageAfterMark(page: Int, count: Int): Int`.

- [ ] **Step 1: Tests de `SessionControlsTest`:**
  - `todayEditableCanMarkAndFinish()`: página 0 de 3 → (true, true, false);
  - `lastPageMakesFinishProminent()`: página 2 de 3 → `finishProminent == true`;
  - `readOnlyDayHasNoMarkNorFinish()`: `isToday = false` → (false, false, false);
  - `finishedDayHasNoMarkNorFinish()`;
  - `nextPageAfterMarkAdvances()`: (0, 3) → 1;
  - `nextPageAfterMarkStaysOnLast()`: (2, 3) → 2.
- [ ] **Step 2:** ejecutar el test. Esperado: FAIL.
- [ ] **Step 3: Implementar** según el frame `active-workout-screen`:
  - cabecera con título del día, `session_exercise_of` (`liveRegion`) y `LinearProgress`;
  - `AiNote`;
  - pager iniciado en `index.coerceIn(0, lastIndex)`;
  - por página: imagen (`AsyncImage` de Coil si hay `imageUrl`; si no, `IconTile` grande con `lc_dumbbell`), etiqueta de músculo, nombre `headlineSmall`, `DataStrip` (series, reps o tiempo, descanso, peso sugerido), instrucciones y "Pedir alternativa";
  - pie fijo:
    - `PrimaryButton` marcar/desmarcar con `canMark`; al marcar avanza a `nextPageAfterMark`;
    - "Anterior" y "Saltar";
    - tarjeta "A continuación" si hay siguiente;
    - `SecondaryButton`, o `PrimaryButton` si `finishProminent`, para "Terminar sesión" con `canFinish`, con el diálogo de confirmación existente;
  - mensaje de terminada y mensaje de solo hoy tal como hoy.
  - Descanso: muestra el texto existente.
  - Sin conexión: los botones se deshabilitan con `OfflineHint`.
- [ ] **Step 4: Verificar.** Suite y capturas:
  - ejercicio 1, ejercicio marcado, último ejercicio, terminar y confirmar, terminada;
  - día pasado y día futuro;
  - sin conexión;
  - al 200 %;
  - TalkBack: Anterior y Saltar leídos y "Ejercicio N de M" anunciado.
- [ ] **Step 5: Commit** `feat(session): one exercise at a time with mark, skip and finish`.

### Task 8: Dieta y receta

**Files:**
- Modify: `nutrition/DietScreens.kt`
- Modify: `strings.xml` (`meal_prep_minutes` y plurales `recipe_ingredients_count`)
- Test: `nutrition/MealIconTest.kt`

**Interfaces:**
- Consumes: `dietDayFor` (Task 5) y los componentes de la Task 3.
- Produces: `fun MealType.iconRes(): Int` (`BREAKFAST` → `lc_coffee`, `LUNCH` → `lc_salad`, `DINNER` → `lc_moon` y `SNACK` → `lc_cookie`) y `MealCard(meal, onClick)`.

- [ ] **Step 1: Test** `MealIconTest.everyMealTypeHasAnIcon()`: los 4 tipos dan íconos distintos.
- [ ] **Step 2:** ejecutar el test. Esperado: FAIL.
- [ ] **Step 3: Implementar** según los frames `nutrition-dashboard-screen` y `lunch-recipe-screen`, y §6 de la spec:
  - selector de día;
  - tarjeta resumen: kcal del plan en `displaySmall` y filas de macros con gramos;
  - barra de proporción con `macroShares()` existente;
  - `MealCard`;
  - "Regenerar dieta".
  - Receta:
    - cabecera tipográfica con píldora de minutos;
    - 4 placas con anillo de proporción;
    - ingredientes con contador;
    - pasos con insignia numerada;
    - botón fijo "Pedir alternativa".
- [ ] **Step 4: Verificar.** Suite y capturas: Dieta, otro día, receta arriba y abajo, al 200 % y `#sindieta`.
- [ ] **Step 5: Commit** `feat(diet): plan summary, meal cards and typographic recipe`.

### Task 9: Progreso

**Files:** Modify `progress/ProgressScreen.kt`. Test: los `WeightChartRulesTest` existentes.

**Interfaces:** Consumes `ProgressRing`, `StatCard`, `AppCard` y `ScreenHeader`.

- [ ] **Step 1: Implementar.**
  - Tarjeta semanal: `ProgressRing` con N/M y racha con el ícono `lc_flame` en `accent`.
  - Gráfica de Vico: línea `primary` de 2 dp, guías `outlineVariant`, ejes `onSurfaceVariant` con `numberStyle` y marcador sobre `surfaceContainerHigh`.
  - Historial y "Registrar peso" como `PrimaryButton`.
  - Diálogo de peso con `FormTextField`.
  - Se mantienen todas las reglas de la gráfica.
- [ ] **Step 2: Verificar.** Suite (`WeightChartRulesTest` verde) y capturas: un peso, varios, registrar, DatePicker, 200 % y sin conexión.
- [ ] **Step 3: Commit** `feat(progress): rings and dark chart`.

### Task 10: Perfil, editar sección y eliminar cuenta

**Files:** Modify `onboarding/ProfileScreens.kt` y `strings.xml` (`profile_section_account`).

**Interfaces:** Consumes `Avatar`, `StatCard`, `PreferenceRow`, `PrimaryButton`, `ChoiceCard` y `PillChip`.

- [ ] **Step 1: Implementar** según el frame `all-fitness-profile`:
  - cabecera con `Avatar` grande, nombre y correo;
  - rótulos de sección en `primary` `labelMedium`;
  - 4 `StatCard`;
  - objetivo destacado con "Editar";
  - filas de entreno y alimentación con los campos reales de `SectionSummary`; cada sección con "Editar";
  - "Regenerar mi plan" como `PrimaryButton` (acción existente) con nota;
  - "Cuenta": política de privacidad (`BuildConfig.PRIVACY_URL`), cerrar sesión y eliminar cuenta en `error`.
  - Editar sección y eliminar cuenta usan los componentes nuevos sin cambios de lógica (FR-030 a FR-034).
- [ ] **Step 2: Verificar.** Suite y capturas: perfil arriba y abajo, cada edición, descartar, regenerar, eliminar (vacío y listo) y al 200 %.
- [ ] **Step 3: Commit** `feat(profile): stat cards, preference rows and account section`.

### Task 11: Login, registro, onboarding, generando y alternativa

**Files:** Modify `auth/AuthScreens.kt`, `onboarding/OnboardingScreen.kt`, `onboarding/ProfileSections.kt`, `core/ui/Components.kt` (`AiGeneratingState`), `aifeedback/Alternatives.kt` y `strings.xml` (`onboarding_subtitle`).

**Interfaces:** Consumes `LogoMark`, `SegmentedProgress`, `FormTextField`, `ChoiceCard`, `PillChip` y `PrimaryButton`.

- [ ] **Step 1: Implementar.**
  - **Onboarding** según el frame `all-fitness-onboarding`:
    - `LogoMark` y "Paso N de 4", con `SegmentedProgress(4, step)`;
    - título y `onboarding_subtitle`;
    - el objetivo como cuadrícula de `ChoiceCard` de 2 columnas (3 opciones, la tercera sola en su fila);
    - equipo y alergias con `PillChip`;
    - unidades fijas como sufijo;
    - botón fijo "Siguiente/Terminar" con `lc_arrow_right`.
    - La lógica de pasos y borradores no cambia.
  - **Login y registro:** `LogoMark` grande, campos, `PrimaryButton` y enlaces.
  - **Generando:** fondo `background`, `LogoMark`, el mensaje con `liveRegion`, indicador lineal `primary`, etapas y resumen. Se retiran el modo a sangre de la Señal y los íconos claros, que ahora son el estado normal.
  - **Hoja de alternativa:** contenedor `surfaceContainerHigh`, motivos con `selectableGroup` y botones nuevos. Al regenerar la rutina o la dieta completa (`WholeRoutine`/`WholeDiet`), la espera muestra el texto nuevo `alt_loading_plan`: "La IA está preparando tu nuevo plan. Puede tardar hasta un minuto." Hoy dice "…una alternativa…", un pendiente heredado.
- [ ] **Step 2: Verificar.** Suite y recorrido de registro con una cuenta nueva:
  - errores, 409 y términos;
  - los 4 pasos, retomar y generando;
  - alternativa de ejercicio y de receta (cargando, `#roto` y propuesta);
  - login 401 y sesión expirada.
  - Al 200 %.
- [ ] **Step 3: Commit** `feat(entry): logo mark, segmented onboarding, generating and alternative sheet`.

### Task 12: Limpieza y cierre

**Files:**
- Delete: componentes de "Línea" sin uso (`RouteRow`, `RouteStation` si ya no se usa, `SignPanel`, `SessionBar`, `BrandHeader` y `StationProgress`), los íconos Material sin uso y los tokens retirados.
- Modify: `DESIGN.md` y `.impeccable/design.json` (con `impeccable-documenter`), y `PRODUCT.md` (Evidence on Hand y Brand Commitments: logo-mark del Figma y tema oscuro único).

- [ ] **Step 1:** lint en mock y real. Esperado: 0 avisos; los recursos sin usar se borran. `grep -rnE "tween\([0-9]|[0-9]+\.sp" app/src/main/java --include=*.kt | grep -v core/ui/theme`: esperado vacío (pendiente heredado de literales fuera de los tokens).
- [ ] **Step 2: Recorrido funcional** del quickstart 1–8 en mock con las capturas en `visual-audit/figma/final/flujo/`, y juego de capturas de las 12 pantallas al 100 %, al 130 % y al 200 %.
- [ ] **Step 3:** `/impeccable audit` y `/impeccable critique`. Comparar con los 7 frames de Figma (`docs/design/figma/miniatura.png` y árbol de nodos) y anotar cada diferencia como ruling o como pendiente.
- [ ] **Step 4: Revisión final de toda la rama**, con el revisor de código en el modelo más capaz y `impeccable-finish-reviewer` contra la spec de Figma. Una sola pasada de arreglos: cada uno con test RED→GREEN cuando tiene lógica, y con captura si es solo visual.
- [ ] **Step 5:** documentar (`DESIGN.md`) y hacer el commit final; informe con tareas, rulings y pendientes.
