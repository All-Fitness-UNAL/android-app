# Rediseño visual "Línea": plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sustituir el aspecto Material 3 de plantilla por el sistema visual "Línea" (la semana como ruta de estaciones, panel Señal y líneas de color por sección) en todas las pantallas, sin cambiar ningún comportamiento funcional.

**Architecture:** Primero el tema (color, tipo, forma y movimiento en `core/ui/theme`). Después los componentes compartidos en `core/ui`. Por último, pantalla por pantalla, solo en los Composables de presentación. ViewModels, repositorios, DTOs, red, Room, DataStore y sesión no se tocan, salvo propiedades derivadas de solo lectura sobre modelos de vista que ya existen. La lógica nueva es pura y tiene test JVM; lo visual se verifica con capturas del emulador.

**Tech Stack:** Kotlin 2.4, Jetpack Compose (BOM 2026.09.00), Material 3, Navigation Compose 2.10, Hilt. Nuevas: Vico (gráfica), `androidx.core:core-splashscreen`, `material3-adaptive-navigation-suite` y JUnit 4 (tests JVM).

**Spec:** `docs/superpowers/specs/2026-10-30-rediseno-visual-design.md` (es la fuente de verdad de valores: HEX, tamaños, radios y duraciones). Restricciones funcionales: `specs/001-allfitness-mvp/spec.md` (FR-001 a FR-043) y `.specify/memory/constitution.md` v1.1.0. Contrato de dirección: `.impeccable/surfaces/app-src-main-java-com-allfitness-app.md`.

## Global Constraints

- No se cambian contratos, DTOs, repositorios, `core/network`, `core/database`, `core/session` ni `app/src/mock/`. En los ViewModels solo se permite añadir propiedades derivadas de solo lectura en clases de estado de vista (`DayView`, `WeekDayRow`).
- No hay hex ni `dp`/`sp` sueltos en pantallas: todo sale de `core/ui/theme` (constitución, Estructura y estilo).
- Todo texto visible nuevo va en `res/values/strings.xml`, en español. Los textos existentes no cambian salvo los marcados en la Task 3 (formato de datos).
- Dependencias nuevas, solo estas: Vico `compose-m3`, `core-splashscreen`, `material3-adaptive-navigation-suite` y `junit:junit:4.13.2` (`testImplementation`). Se registran en `libs.versions.toml`.
- Fuentes empaquetadas en `res/font` (sin descargas en tiempo de ejecución), con licencia OFL en `app/src/main/assets/licenses/`.
- Aviso de IA (FR-037): `AiNote` justo debajo del título de las pantallas de rutina, día, dieta, receta y perfil, con el texto completo `R.string.ai_disclaimer`, siempre a la vista sin desplazar.
- Marcar ejercicios y terminar la sesión: solo hoy, en día de entreno y sin terminar (`DayView.editable`, FR-013). "Terminar" se permite aunque falten ejercicios (FR-014).
- El amarillo `here` aparece como máximo una vez por pantalla.
- Contraste: texto ≥ 4.5:1 y gráficos/bordes ≥ 3:1, en claro y en oscuro (tabla 4.3 de la spec).
- Áreas táctiles ≥ 48 dp. Escalado de fuente sin recortes al 130 % y al 200 %.
- Con `ANIMATOR_DURATION_SCALE == 0` no se anima nada salvo los indicadores de carga.
- Build: `./gradlew :app:assembleMockDebug :app:testMockDebugUnitTest :app:lintMockDebug` sin errores ni warnings nuevos de lint.
- Cada tarea termina con `ponytail-review` del diff de la tarea antes del commit. Los hallazgos de correctitud se corrigen en esa misma tarea.
- Las capturas van en `visual-audit/redesign/<tarea>/`. Antes de cambiar el modo oscuro o la escala de fuente se lee el valor del emulador, y se restaura al terminar.

## Review Focus

1. **Día de descanso hoy:** el SignPanel muestra "Descanso" sin CTA de sesión, y la estación es `TODAY` (no `TODAY_DONE`). Test en la Task 5.
2. **Rutina con 0 ejercicios en un día de entreno** (dato raro del servidor): la SessionBar muestra "0 de 0" sin dividir por cero y "Terminar" sigue disponible. Test `sessionFraction(0, 0) == 0f` en la Task 5.
3. **Macros todos en 0 o ausentes:** la barra de macros no se dibuja (sin división por cero) y la DataStrip muestra "0 g". Test en la Task 7.
4. **Historial de peso con 1 registro, o con varios iguales:** no se muestra el rango "Entre X y X kg", y la gráfica se sustituye por el estado vacío hasta tener 2 registros. Tests en la Task 8.
5. **Fuente al 200 % en la SessionBar y en el SignPanel:** los textos hacen salto de línea y nada se recorta ni queda tapado por la barra fija (la lista reserva el alto de la barra con `contentPadding`). Se verifica con captura en la Task 6.

---

### Task 0: Dependencias, fuentes, splash y base de tests

**Files:**
- Modify: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Create: `app/src/main/res/font/archivo_variable.ttf`, `app/src/main/res/font/atkinson_next_variable.ttf`, `app/src/main/assets/licenses/OFL-Archivo.txt`, `app/src/main/assets/licenses/OFL-AtkinsonHyperlegibleNext.txt`
- Create: `app/src/main/res/values/colors.xml`, `app/src/main/res/values-night/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`, `app/src/main/AndroidManifest.xml` (tema de la actividad), `app/src/main/java/com/allfitness/app/MainActivity.kt`
- Create: `app/src/test/java/com/allfitness/app/SmokeTest.kt`

**Interfaces:**
- Produces: `R.font.archivo_variable`, `R.font.atkinson_next_variable`, `@color/brand_signal` (`#0F1F1A` / noche `#1D2F28`), `@color/brand_ground` (`#F3F5F4` / noche `#0B1210`), estilos `Theme.AllFitness` y `Theme.AllFitness.Starting`.

- [ ] **Step 1: Declarar las dependencias.** En `libs.versions.toml`, añadir:
  - `vico`: la última versión estable 2.x de `com.patrykandpatrick.vico:compose-m3` en Maven Central.
  - `coreSplashscreen`: la última estable de `androidx.core:core-splashscreen`.
  - Librería `compose-material3-adaptive-navigation-suite` (`androidx.compose.material3:material3-adaptive-navigation-suite`, sin versión, la pone el BOM).
  - `junit = "4.13.2"`.

  En `app/build.gradle.kts`: `implementation` de las tres primeras y `testImplementation(libs.junit)`.
- [ ] **Step 2: Crear un test de humo** en `SmokeTest.kt` (`@Test fun junitRuns() = assertEquals(2, 1 + 1)`) y ejecutar `./gradlew :app:testMockDebugUnitTest`. Esperado: BUILD SUCCESSFUL con 1 test pasado.
- [ ] **Step 3: Descargar las fuentes** del repositorio oficial `github.com/google/fonts`: `ofl/archivo/Archivo[wdth,wght].ttf` → `archivo_variable.ttf` y `ofl/atkinsonhyperlegiblenext/AtkinsonHyperlegibleNext[wght].ttf` → `atkinson_next_variable.ttf`, más el `OFL.txt` de cada carpeta en `assets/licenses/`. Las fuentes se guardan en un directorio temporal nuevo y se copian a `res/font` por ruta.
- [ ] **Step 4: Configurar el splash y el fondo de ventana.** `Theme.AllFitness` pasa a ser padre de `android:Theme.Material.Light.NoActionBar` con `android:windowBackground=@color/brand_ground`. `Theme.AllFitness.Starting` tiene como padre `Theme.SplashScreen`, con `windowSplashScreenBackground=@color/brand_signal`, `windowSplashScreenAnimatedIcon=@drawable/ic_launcher_foreground` y `postSplashScreenTheme=@style/Theme.AllFitness`. En el manifest, la actividad usa `Theme.AllFitness.Starting`. En `MainActivity.onCreate`, llamar a `installSplashScreen()` antes de `super.onCreate`.
- [ ] **Step 5: Verificar.** Ejecutar `./gradlew :app:installMockDebug` y abrir la app en el emulador. Esperado: splash oscuro con el ícono, sin destello blanco, y después la app igual que antes.
- [ ] **Step 6: Commit** `build: add vico, splashscreen, adaptive nav suite, junit and brand fonts`.

### Task 1: Tokens de color completos

**Files:**
- Modify: `app/src/main/java/com/allfitness/app/core/ui/theme/Color.kt`
- Create: `app/src/main/java/com/allfitness/app/core/ui/theme/AppColors.kt`
- Test: `app/src/test/java/com/allfitness/app/core/ui/theme/ContrastTest.kt`

**Interfaces:**
- Produces:
  - `val LightColors: ColorScheme` y `val DarkColors: ColorScheme`, con **todos** los roles M3.
  - `@Immutable data class AppColors(val lineRoutine: Color, val lineDiet: Color, val lineDietText: Color, val lineProgress: Color, val here: Color, val signal: Color, val onSignal: Color, val signalOutline: Color, val macroProtein: Color, val macroCarbs: Color, val macroFat: Color)`.
  - `val LightAppColors` y `val DarkAppColors`.
  - `val LocalAppColors = staticCompositionLocalOf { LightAppColors }`.
  - `enum class AppSection { ROUTINE, DIET, PROGRESS, PROFILE }` y `fun AppColors.lineFor(section: AppSection): Color` (PROFILE → `onSurface` de la tinta, que se pasa como parámetro: `fun AppColors.lineFor(section: AppSection, ink: Color): Color`).
  - `fun contrastRatio(a: Color, b: Color): Float`, en `AppColors.kt`, sin `@Composable`.

- [ ] **Step 1: Escribir el test que falla.** `ContrastTest` con un test por par de la tabla 4.3 de la spec (`assertTrue(contrastRatio(fg, bg) >= min)`):
  - En claro: `onSurface`/`surface` ≥ 4.5; `onSurfaceVariant`/`background` ≥ 4.5; `primary`/`surface` ≥ 4.5; `onPrimary`/`primary` ≥ 4.5; `lineDietText`/`surface` ≥ 4.5; `lineDiet`/`surface` ≥ 3; `lineProgress`/`surface` ≥ 4.5; `macroFat`/`surface` ≥ 3; `onSignal`/`signal` ≥ 4.5; `here`/`signal` ≥ 4.5; `onSecondaryContainer`/`secondaryContainer` ≥ 4.5; `error`/`surface` ≥ 4.5; `outline`/`surface` ≥ 3.
  - Los mismos pares en oscuro.
- [ ] **Step 2: Ejecutar** `./gradlew :app:testMockDebugUnitTest --tests "*ContrastTest*"`. Esperado: FAIL (`contrastRatio` no existe).
- [ ] **Step 3: Implementar.** `contrastRatio` usa `Color.luminance()` y aplica `(max + 0.05f) / (min + 0.05f)`. En `LightColors` y `DarkColors`, los valores HEX de la tabla 4.3 de la spec:
  - `background` = andén, `surface` = `#FFFFFF` / `#121A17`, `onSurface`, `onSurfaceVariant`, `outline`, `outlineVariant` (hilo), `primary` = Línea Rutina, `secondaryContainer` y `onSecondaryContainer` (botón tonal), `inverseSurface` = Señal, `error`.
  - El resto de roles (`surfaceContainerLowest` a `surfaceContainerHighest`, `surfaceBright`, `surfaceDim`, `surfaceTint`, `inverseOnSurface`, `inversePrimary`, `scrim`, `primaryContainer`, `tertiary*`) se generan con Material Theme Builder desde `#0A7A50` con los neutros fijados a la familia verde fría. **Prohibido dejar cualquier rol con el valor por defecto.**
  - `AppColors` con los valores de la spec, incluidos `macroFat` (`#B07F00` / `#E5B83A`) y `signalOutline` (`#2A3531` en oscuro, `Color.Transparent` en claro).
- [ ] **Step 4: Ejecutar el test.** Esperado: PASS en todos los pares.
- [ ] **Step 5: Commit** `feat(theme): complete M3 color roles and Línea colors`.

### Task 2: Tipografía, formas, movimiento y cableado del tema

**Files:**
- Create: `core/ui/theme/Type.kt`, `core/ui/theme/Shape.kt`, `core/ui/theme/Motion.kt`
- Modify: `core/ui/theme/Theme.kt`
- Test: `app/src/test/java/com/allfitness/app/core/ui/theme/MotionTest.kt`

(Rutas relativas a `app/src/main/java/com/allfitness/app/`.)

**Interfaces:**
- Consumes: `R.font.*` (Task 0), `LightColors`, `DarkColors`, `LightAppColors`, `DarkAppColors` y `LocalAppColors` (Task 1).
- Produces:
  - `val ArchivoFamily: FontFamily` (pesos 600, 650, 700 y 800 con `FontVariation.width(87.5f)`).
  - `val AtkinsonFamily: FontFamily` (400 y 700).
  - `val AppTypography: Typography` (escala de la tabla 4.4 de la spec, todo en `sp`; los roles de cifras con `fontFeatureSettings = "tnum"`).
  - `val AppShapes: Shapes` (4/8/12/16/24 dp).
  - `object Motion { const val SHORT = 150; const val MEDIUM = 300; const val LONG = 450; val Emphasized: Easing; fun <T> gentle(): SpringSpec<T> }` (`dampingRatio = 0.9f`, `stiffness = Spring.StiffnessMediumLow`).
  - `fun isReducedMotion(animatorDurationScale: Float): Boolean`.
  - `val LocalReducedMotion = staticCompositionLocalOf { false }`.
  - `object AppTheme { val colors: AppColors @Composable get(); val reducedMotion: Boolean @Composable get() }`.
  - `Spacing` amplía con `sm = 12.dp`, `xxl = 48.dp`, `contentMaxWidth = 640.dp`, `stationSize = 12.dp`, `stationToday = 16.dp`, `lineWidth = 3.dp`, `hairline = 1.dp`, `routeRowHeight = 56.dp`.

- [ ] **Step 1: Escribir el test que falla.** `MotionTest`:
  - `isReducedMotion(0f)` es true.
  - `isReducedMotion(1f)` es false.
  - `isReducedMotion(0.5f)` es false.
- [ ] **Step 2: Ejecutar** `./gradlew :app:testMockDebugUnitTest --tests "*MotionTest*"`. Esperado: FAIL.
- [ ] **Step 3: Implementar los cuatro archivos.** `AllFitnessTheme`:
  - Lee `Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)` con `LocalContext`.
  - Provee `LocalAppColors` (claro u oscuro según `isSystemInDarkTheme()`) y `LocalReducedMotion`.
  - Pasa `typography = AppTypography` y `shapes = AppShapes`.
  - Si `androidx.compose.material3.MotionScheme` es API pública estable en el BOM, pasa `motionScheme = MotionScheme.standard()`; si no, no lo pasa (los componentes propios usan `Motion`).
- [ ] **Step 4: Ejecutar test y build.** `./gradlew :app:testMockDebugUnitTest :app:assembleMockDebug`. Esperado: PASS y BUILD SUCCESSFUL.
- [ ] **Step 5: Verificación visual.** Instalar y capturar `visual-audit/redesign/t2/{rutina,dia,dieta,progreso,perfil}-{light,dark}.png` con `adb exec-out screencap -p`. El modo oscuro se cambia con `adb shell cmd uimode night yes|no`. Esperado: ninguna superficie lavanda ni púrpura, títulos en Archivo y texto en Atkinson.
- [ ] **Step 6: Commit** `feat(theme): Archivo/Atkinson type scale, shapes and motion tokens`.

### Task 3: Componentes base compartidos e íconos

**Files:**
- Modify: `core/ui/Components.kt`
- Create: `core/ui/DataStrip.kt`, `core/ui/Buttons.kt`
- Create: `res/drawable/ic_{fitness,restaurant,chart,person}_filled.xml`, `ic_alt_route.xml`, `ic_check.xml`, `ic_info.xml`, `ic_cloud_off.xml`, `ic_timer.xml`, `ic_scale.xml`, `ic_local_fire.xml`, `ic_expand_more.xml`; sustituir `ic_fitness`, `ic_restaurant`, `ic_chart` e `ic_person` por su versión outlined.
- Modify: `res/values/strings.xml`
- Modify: todos los usos de `AiDisclaimer` (grep) y de `OutlinedButton` / `TextButton` en "Pedir alternativa" y "Regenerar".

**Interfaces:**
- Produces:
  - `@Composable fun AiNote(modifier: Modifier = Modifier)`, que sustituye a `AiDisclaimer` (se elimina).
  - `data class DataCell(val label: String, val value: String, val unit: String? = null)`.
  - `@Composable fun DataStrip(cells: List<DataCell>, modifier: Modifier = Modifier)`.
  - `@Composable fun AppCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit)`: `Card` con `containerColor = surface` (blanco), borde de `Spacing.hairline` en `outlineVariant`, elevación 0 y `MaterialTheme.shapes.medium`. Sustituye a **todos** los `Card(` de las pantallas, salvo las tarjetas de error con `errorContainer` (el `Card` por defecto usa `surfaceContainerHighest`, que no es blanco).
  - `@Composable fun TonalActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, @DrawableRes icon: Int? = null)`.
  - `@Composable fun AlternativeButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier)`.
  - `@Composable fun LineIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary)`.
  - `@Composable fun AiGeneratingState(message: String, modifier: Modifier = Modifier, stages: List<String> = emptyList())`.
  - `AppTopBar(title, onBack)` con la misma firma.
  - `StateContent(...)` con la misma firma.

- [ ] **Step 1: Íconos.** Descargar de `github.com/google/material-design-icons` (`symbols/android/<nombre>/materialsymbolsrounded/<nombre>_24px.xml`, y la variante `_fill1_24px.xml` para `_filled`), con peso 400. Los cuatro de la barra inferior pasan a outlined y se crean sus variantes `_filled`.
- [ ] **Step 2: Strings nuevos** en `strings.xml`:
  - `label_sets` = "SERIES", `label_reps` = "REPS", `label_duration` = "DURACIÓN", `label_rest` = "DESCANSO", `label_weight` = "PESO"
  - `label_protein` = "PROTEÍNA", `label_carbs` = "CARBOHIDRATOS", `label_fat` = "GRASAS", `label_kcal` = "KCAL", `label_time` = "TIEMPO"
  - `value_seconds` = "%1$d s", `value_grams` = "%1$s g"
  - `cd_info` = "Información", `cd_offline` = "Sin conexión"
  - `generating_stage_profile` = "Leyendo tu perfil", `generating_stage_routine` = "Armando tu rutina", `generating_stage_diet` = "Armando tu dieta"

  `exercise_sets_reps`, `exercise_sets_duration`, `exercise_rest`, `exercise_weight` y `macros_value` dejan de usarse en pantalla, pero se conservan si algún `contentDescription` los usa. Si no queda ningún uso, se borran.
- [ ] **Step 3: Implementar los componentes** con la sección 4.8 de la spec:
  - `AiNote`: fila con `ic_info` de 16 dp, `bodySmall` en `onSurfaceVariant`, sin caja y con `HorizontalDivider(Spacing.hairline, outlineVariant)` debajo.
  - `DataStrip`: `Row` con `IntrinsicSize.Min` y celdas `weight(1f)` separadas por `VerticalDivider` de hilo. Etiqueta en `labelMedium` y valor en `titleLarge` (Archivo tnum) con unidad en `bodySmall`. Semántica: cada celda se fusiona con `mergeDescendants` y lee "etiqueta valor unidad".
  - `TonalActionButton`: `FilledTonalButton` con ícono de 18 dp.
  - `AlternativeButton`: `TonalActionButton` con `ic_alt_route` y `R.string.action_request_alternative`.
  - `LineIndicator`: tramo de línea de `Spacing.lineWidth` que recorre el ancho con `rememberInfiniteTransition` (1200 ms, `Motion.Emphasized`). Con `AppTheme.reducedMotion` es un `LinearProgressIndicator` indeterminado de M3.
  - `LoadingState`: usa `LineIndicator` con `semantics { liveRegion = LiveRegionMode.Polite }` sobre el mensaje.
  - `AiGeneratingState`: fondo `AppTheme.colors.signal`, `message` en `headlineSmall` `onSignal`, `LineIndicator(color = AppTheme.colors.lineRoutine)` y, si hay `stages`, la lista estática de etapas con una estación cada una. **No se marca ninguna etapa como completada**, porque no hay progreso real.
  - `ErrorState` y `EmptyState`: estación de 16 dp (error u hueca) encima del mensaje y acción con `TonalActionButton`.
  - `OfflineBanner`: `surfaceContainerHigh`, `ic_cloud_off` y `bodySmall`.
  - `AppTopBar`: título en `headlineLarge` (sin back) o `titleLarge` (con back), con `semantics { heading() }`; `TopAppBar` con `containerColor = background`.
  - `StateContent`: el `when` dentro de `AnimatedContent(targetState = state::class)` con `fadeIn(tween(Motion.SHORT)) togetherWith fadeOut(tween(Motion.SHORT))` (sin animación con `reducedMotion`). `OfflineBanner` dentro de `AnimatedVisibility(state.isStale)`.
- [ ] **Step 4: Sustituir los usos.** Cada `Card(` de las pantallas → `AppCard` (grep `Card(` en `routines/`, `nutrition/`, `progress/` y `onboarding/`). `AiDisclaimer` → `AiNote` (justo después del título, como primer ítem de la lista). Todos los "Pedir alternativa" → `AlternativeButton`. "Regenerar rutina/dieta" → `TonalActionButton`. Los `LoadingState` de generación (`RoutineWeekScreen.kt:142`, `DietScreens.kt:144`, `OnboardingScreen.kt:163`, `ProfileScreens.kt:352`) → `AiGeneratingState`. En el onboarding, con `stages` = las tres etapas; en los demás, sin etapas.
- [ ] **Step 5: Verificar.** Ejecutar `./gradlew :app:assembleMockDebug :app:lintMockDebug` e instalar. Capturas `visual-audit/redesign/t3/` de día, receta, sin conexión (`adb shell svc wifi disable` y `svc data disable`; restaurar después), error y generando. Esperado: aviso compacto bajo el título, "Pedir alternativa" igual en todas partes y estados con estación.
- [ ] **Step 6: Commit** `feat(ui): shared Línea components, icons and AI note`.

### Task 4: Campos y selección del formulario

**Files:**
- Modify: `core/ui/FormFields.kt`, `onboarding/ProfileSections.kt`, `res/values/strings.xml`

**Interfaces:**
- Produces:
  - `FormTextField(...)`: misma firma más `hint: String? = null`. `suffix` se pinta como `trailingIcon` siempre visible. El contador `char_counter` solo aparece si `keyboardType == KeyboardType.Text`. Si no, `supportingText` = error ?: hint.
  - `SingleChoiceChips` y `MultiChoiceChips`: misma firma, con `leadingIcon` `ic_check` (18 dp) cuando están seleccionados. Además, `SingleChoiceChips` envuelve el `FlowRow` en `Modifier.selectableGroup()`.
  - `@Composable fun <T> SegmentedChoice(label: String, options: List<T>, selected: T?, onSelect: (T) -> Unit, optionLabel: @Composable (T) -> String, @StringRes error: Int? = null)` con `SingleChoiceSegmentedButtonRow`.

- [ ] **Step 1: Añadir strings** `hint_age` = "Entre 16 y 100 años", `hint_height` = "Entre 100 y 250 cm", `hint_weight` = "Entre 30 y 300 kg", `hint_minutes` = "Entre 15 y 120 minutos".
- [ ] **Step 2: Implementar** las firmas de arriba en `FormFields.kt`.
- [ ] **Step 3: Aplicar en `ProfileSections.kt`.** Los campos edad, estatura, peso y minutos reciben su `hint`. Los días por semana (1–7) y las comidas por día (3–5) pasan de `SingleChoiceChips` a `SegmentedChoice`.
- [ ] **Step 4: Verificar.** Instalar y capturar los pasos 1, 3 y 4 del onboarding y la edición del perfil al 100 % y al 200 % de fuente (`adb shell settings put system font_scale 2.0`; leer el valor antes y restaurarlo). Esperado: sin "0/3", unidad visible sin foco, chip con check, los 7 días en una sola fila y sin recortes al 200 %. Si los 7 segmentos no caben al 200 %, `SegmentedChoice` pasa a `FlowRow` de chips solo cuando `LocalDensity.current.fontScale > 1.5f`.
- [ ] **Step 5: Commit** `feat(forms): visible units, range hints, checked chips, segmented counts`.

### Task 5: Hoy en Rutina (SignPanel + RouteLine)

**Files:**
- Create: `core/ui/Route.kt`
- Modify: `routines/RoutineWeekScreen.kt`, `res/values/strings.xml`
- Test: `app/src/test/java/com/allfitness/app/routines/StationStateTest.kt`, `app/src/test/java/com/allfitness/app/core/ui/RouteTest.kt`

**Interfaces:**
- Consumes: `AppTheme.colors`, `Spacing.*` (Task 2), `DataStrip`, `AiNote`, `TonalActionButton` (Task 3).
- Produces:
  - `enum class StationState { PENDING, DONE, REST, TODAY, TODAY_DONE }`.
  - `fun sessionFraction(done: Int, total: Int): Float` (0f si `total <= 0`, y si no `done / total` limitado a 0..1).
  - `@Composable fun RouteStation(state: StationState, lineColor: Color, modifier: Modifier = Modifier)`.
  - `@Composable fun RouteRow(state: StationState, title: String, subtitle: String, lineColor: Color, isFirst: Boolean, isLast: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`.
  - `@Composable fun SignPanel(label: String, title: String, status: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null)`.
  - `@Composable fun StationProgress(total: Int, reached: Int, lineColor: Color, modifier: Modifier = Modifier)`.
  - En `RoutineWeekScreen.kt`: `fun WeekDayRow.stationState(): StationState`.

- [ ] **Step 1: Escribir los tests que fallan.**
  - `StationStateTest`:
    - entreno + no hoy + sin terminar → PENDING
    - entreno + no hoy + terminado → DONE
    - descanso + no hoy → REST
    - hoy + sin terminar (entreno o descanso) → TODAY
    - hoy + terminado → TODAY_DONE
  - `RouteTest`:
    - `sessionFraction(3, 6) == 0.5f`
    - `sessionFraction(0, 0) == 0f`
    - `sessionFraction(7, 6) == 1f`
- [ ] **Step 2: Ejecutar** `./gradlew :app:testMockDebugUnitTest --tests "*StationStateTest*" --tests "*RouteTest*"`. Esperado: FAIL.
- [ ] **Step 3: Implementar `Route.kt`** con la sección 4.8 de la spec:
  - `RouteRow`: alto `Spacing.routeRowHeight`. El segmento de línea se dibuja con `drawBehind` desde la estación hacia arriba (si no es `isFirst`) y hacia abajo (si no es `isLast`). La fila completa es `clickable` con `Role.Button`.
  - `RouteStation`: PENDING es un anillo de 12 dp; DONE, un círculo relleno con `ic_check`; REST, un punto hueco de 8 dp en `outline`; TODAY, un anillo de 16 dp con halo de 4 dp en `here`; TODAY_DONE, un relleno de 16 dp con halo.
  - `SignPanel`: `Surface(color = signal, shape = MaterialTheme.shapes.large, border = signalOutline)`. Contiene `label` en `labelSmall` color `here`, `title` en `headlineMedium` `onSignal` con `heading()`, `status` en `bodyMedium` y `action` a lo ancho.
  - `StationProgress`: fila horizontal de `total` estaciones unidas por línea; las primeras `reached` van rellenas.
- [ ] **Step 4: Strings:**
  - `today_station_label` = "HOY · %1$s"
  - `action_view_today_session` = "Ver sesión de hoy"
  - `session_status_not_started` = "Sin empezar"
  - `<plurals name="exercises_count">`: one "%d ejercicio", other "%d ejercicios"
  - `week_title` = "Tu semana"
- [ ] **Step 5: Recomponer `RoutineWeekScreen`.** El orden es `AiNote`, el `SignPanel` del día de hoy y después `week_title` + `RouteRow` × 7 + `TonalActionButton` "Regenerar rutina" (+ `OfflineHint`). Contenido del SignPanel:
  - `label`: `today_station_label` con `displayName()` + `shortLabel()` en mayúsculas.
  - `title`: título del día o `rest_day`.
  - `status`: `exercises_count` + estado (`session_status_not_started` o `session_finished`).
  - `action`: un `Button` `action_view_today_session` a `onOpenDay(today)`, solo si es día de entreno.

  `DayCard` se elimina. El `subtitle` de `RouteRow` es el título del día o `rest_day`. `lineColor` = `AppTheme.colors.lineRoutine`.
- [ ] **Step 6: Ejecutar los tests y verificar.** Ejecutar `./gradlew :app:testMockDebugUnitTest :app:assembleMockDebug` e instalar. Capturas `visual-audit/redesign/t5/rutina-{light,dark,font130,font200}.png`. Esperado: hoy visible sin desplazar en el emulador de 1080×2424 a 420 dpi, amarillo una sola vez y descanso visualmente más ligero que entreno.
- [ ] **Step 7: Commit** `feat(routine): today sign panel and week route line`.

### Task 6: Sesión del día (ExerciseCard + SessionBar)

**Files:**
- Modify: `routines/ExerciseItem.kt`, `routines/RoutineDayScreen.kt`, `core/ui/Route.kt` (añade `SessionBar`)
- Test: `app/src/test/java/com/allfitness/app/routines/DayViewTest.kt`

**Interfaces:**
- Consumes: `sessionFraction`, `RouteStation`, `StationState` (Task 5), `DataStrip`, `AlternativeButton` (Task 3).
- Produces:
  - `DayView` añade `val doneCount: Int get() = day.exercises.count { it.id in completed }` (solo lectura).
  - `ExerciseItem(exercise: Exercise, modifier: Modifier = Modifier, checked: Boolean? = null, enabled: Boolean = false, onCheckedChange: ((Boolean) -> Unit)? = null, trailing: (@Composable () -> Unit)? = null)`.
  - `@Composable fun SessionBar(done: Int, total: Int, finished: Boolean, enabled: Boolean, finishing: Boolean, onFinish: () -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Escribir el test que falla.** `DayViewTest`: `doneCount` es 2 con `completed = {a, b, zz}` y ejercicios `[a, b, c]` (los ids que no son del día no cuentan). Además, `editable` es false si `!isToday`, si `finished` o si es `REST`.
- [ ] **Step 2: Ejecutar** el test. Esperado: FAIL (no existe `doneCount`).
- [ ] **Step 3: Implementar `doneCount`, `ExerciseItem` y `SessionBar`.**
  - **ExerciseItem.** El checkbox se muestra con `enabled = enabled`. Cuando `checked == null`, en su lugar va `RouteStation(PENDING o DONE)`, o nada en descanso. El nombre va en `titleMedium` y el grupo muscular como chip de 4 dp en `lineRoutine`. Los datos van en `DataStrip`:
    - SERIES siempre.
    - REPS, o DURACIÓN con `value_seconds`.
    - DESCANSO con `value_seconds`.
    - PESO con kg, solo si existe.

    Las instrucciones van en `bodyMedium` y la imagen de 64 dp a la derecha. Con `checked == true`, el contenedor pasa a `background` con `animateColorAsState(Motion.SHORT)` y el nombre a `onSurfaceVariant`. Al marcar, `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.ToggleOn)` (o `ToggleOff`).
  - **SessionBar.** `Surface(signal)` fija, con `StationProgress`-like tramo lleno animado con `animateFloatAsState(sessionFraction(done, total), tween(Motion.LONG, easing = Motion.Emphasized))`. Muestra el texto `session_progress` = "%1$d de %2$d" y un `Button` "Terminar sesión de hoy" (`enabled && !finishing`). Si `finished`, muestra `session_finished_message` con `ic_check`.
- [ ] **Step 4: Recomponer `RoutineDayScreen`:**
  - `AiNote` primero.
  - Si no es hoy: el mensaje `only_today_message` como etiqueta (`labelLarge` en `onSurfaceVariant`).
  - Lista de `ExerciseItem` con `enabled = canEdit`.
  - Si `view.isToday && view.day.type == TRAINING`: `SessionBar` en el `bottomBar` del `Scaffold` (no dentro de la lista), y la lista con `contentPadding` inferior igual a la barra.
  - Se elimina el botón de terminar al final de la lista.
  - El pulso de la estación al terminar es un `animateFloatAsState` 1 → 1.15 → 1 (`keyframes`, `Motion.MEDIUM`) disparado por `finished`, sin animación con `reducedMotion`.
- [ ] **Step 5: Verificar.** Tests, build e instalar. Capturas `t6/`: día de hoy sin marcar, con 2 marcados, terminado, un día pasado, al 200 % y en oscuro. Esperado: "N de M" y "Terminar" siempre visibles, el checkbox de un día pasado anunciado como deshabilitado (comprobar con `adb shell uiautomator dump` que `enabled="false"`) y nada tapado por la barra.
- [ ] **Step 6: Commit** `feat(routine): session bar, done state and disabled past-day checks`.

### Task 7: Dieta y receta

**Files:**
- Modify: `nutrition/DietScreens.kt`, `res/values/strings.xml`
- Test: `app/src/test/java/com/allfitness/app/nutrition/MacroSharesTest.kt`

**Interfaces:**
- Consumes: `DataStrip`, `DataCell`, `AiNote`, `AlternativeButton`, `TonalActionButton`, `AppTheme.colors`.
- Produces:
  - `data class MacroShares(val protein: Float, val carbs: Float, val fat: Float)`.
  - `fun Nutrition.macroShares(): MacroShares?` (null si la suma de gramos ≤ 0). Solo escala visualmente los gramos recibidos; no calcula nutrición (FR-017).
  - `@Composable fun MacroBar(shares: MacroShares, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Escribir el test que falla.** `MacroSharesTest`:
  - `Nutrition(1850, 105.0, 206.0, 61.0).macroShares()!!.carbs` ≈ 206/372 (tolerancia 0.001).
  - `Nutrition(0, 0.0, 0.0, 0.0).macroShares() == null`.
- [ ] **Step 2: Ejecutar** el test. Esperado: FAIL.
- [ ] **Step 3: Implementar.** `MacroBar`: barra de 8 dp con radio extraSmall, en tres segmentos `weight(share)` con los colores `macroProtein`, `macroCarbs` y `macroFat`, y `contentDescription` = `macros_value` existente.
- [ ] **Step 4: Recomponer `DietWeekScreen`:**
  - Selector de días: `SecondaryScrollableTabRow` con la pestaña seleccionada = hoy al entrar (comportamiento actual), indicador en `lineDiet` y etiquetas `displayName(TextStyle.SHORT)`.
  - `AiNote`.
  - Tarjeta resumen: `day_total` existente en `titleLarge`, kcal en `displaySmall` tnum + `label_kcal`, `MacroBar` y `DataStrip` de 3 macros con `value_grams`.
  - Comidas: fila con la etiqueta de comida en `labelMedium` `lineDietText` encima del nombre en `titleMedium`, y kcal en `titleMedium` tnum alineado a la derecha con `alignByBaseline()` respecto al nombre.
  - "Regenerar dieta" como `TonalActionButton`.
- [ ] **Step 5: Recomponer `RecipeContent` / `RecipeScreen`:**
  - `AiNote` bajo el título.
  - Nombre en `headlineSmall`.
  - `DataStrip` de TIEMPO (min), KCAL y los 3 macros en gramos (dos filas si no caben: tiempo y kcal / macros).
  - Ingredientes: cada fila con una estación de 8 dp, la cantidad tabular en una columna de ancho fijo y el nombre.
  - Pasos: número en `headlineSmall` Archivo `lineDietText` y texto en `bodyLarge`, con `Spacing.l` entre pasos.
  - `AlternativeButton` al final.

  Los títulos "Ingredientes" y "Preparación" en `titleLarge` con `heading()`.
- [ ] **Step 6: Verificar.** Tests, build e instalar. Capturas `t7/`: dieta de hoy, otro día (que se vea que el selector hace scroll), receta, oscuro y 200 %. Esperado: ningún macro en prosa y kcal alineadas.
- [ ] **Step 7: Commit** `feat(diet): macro bar, aligned meals and readable recipe`.

### Task 8: Progreso (con Vico)

**Files:**
- Modify: `progress/ProgressScreen.kt`, `core/ui/theme/Theme.kt` (`chartHeight` se conserva y se eliminan `chartStroke` y `chartPoint` si quedan sin uso), `res/values/strings.xml`
- Test: `app/src/test/java/com/allfitness/app/progress/WeightChartRulesTest.kt`

**Interfaces:**
- Consumes: `StationProgress` (Task 5), `DataStrip`, `AppTheme.colors.lineProgress`, `ProgressView`, `TrainingSummary`.
- Produces:
  - `fun showWeightRange(weights: List<WeightEntry>): Boolean` (true solo si hay ≥ 2 pesos distintos).
  - `fun canDrawTrend(weights: List<WeightEntry>): Boolean` (≥ 2 registros).
  - `WeightChart(weights: List<WeightEntry>)` con la misma firma, reimplementado con Vico.

- [ ] **Step 1: Escribir el test que falla.** `WeightChartRulesTest`:
  - 1 registro → `canDrawTrend` false y `showWeightRange` false.
  - 2 registros de 62.0 → `canDrawTrend` true y `showWeightRange` false.
  - 62.0 y 61.5 → los dos true.
- [ ] **Step 2: Ejecutar** el test. Esperado: FAIL.
- [ ] **Step 3: Implementar `WeightChart` con Vico.** Se usa `CartesianChartModelProducer`. La serie X = días desde el primer registro e Y = kg (solo valores recibidos). La capa de línea va en `lineProgress` con puntos de 6 dp. El eje inferior lleva las fechas con `shortLabel()` y el inicial y final con kg formateados por `formatNumber`. El marcador al tocar muestra fecha y kg. Se mantiene la `contentDescription` `chart_description`. Con `!canDrawTrend`, en lugar de la gráfica se muestra `EmptyState`-like en línea con `chart_need_more` = "Registra otro peso para ver tu tendencia".
- [ ] **Step 4: Recomponer `ProgressScreen`:**
  - Tarjeta "Esta semana" con `StationProgress(total = scheduledSessions, reached = completedSessions, lineColor = lineProgress)`, la cifra "N/M" en `displaySmall` tnum y la racha en una `DataStrip`. Strings nuevos: `label_streak` = "RACHA", `value_weeks` = plurals "%d semana"/"%d semanas".
  - Gráfica y `chart_range` solo si `showWeightRange`.
  - "Registrar peso" como `Button` con `ic_add`.
  - Historial como tarjeta con filas separadas por hilos, la fecha `longLabel()` y kg tnum a la derecha.
- [ ] **Step 5: Verificar.** Tests, build e instalar. Capturas `t8/`: con 1 peso (estado del mock), después de registrar un segundo peso distinto, en oscuro y al 200 %. Esperado: sin el punto solitario, sin "Entre 62 y 62 kg" y la gráfica con ejes.
- [ ] **Step 6: Commit** `feat(progress): weekly stations and Vico weight chart`.

### Task 9: Perfil y eliminar cuenta

**Files:**
- Modify: `onboarding/ProfileScreens.kt`

**Interfaces:**
- Consumes: `AiNote`, `DataStrip`, `TonalActionButton`, `AppTheme.colors.signal`.

- [ ] **Step 1: Encabezado.** Círculo `signal` de 56 dp con la inicial del nombre en `titleLarge` `onSignal`, nombre en `headlineSmall` y correo en `bodyMedium` `onSurfaceVariant`.
- [ ] **Step 2: Secciones.** `SectionSummary`: título en `titleLarge` con `heading()`, valores en `DataStrip` o filas etiqueta/valor (etiqueta `labelMedium`, valor `bodyLarge`), y "Editar" como `TonalActionButton` pequeño con un ícono de lápiz (`ic_edit.xml`, Material Symbols Rounded, se añade en este paso). El peso actual sigue saliendo del último registro (FR-026a, sin cambios de datos).
- [ ] **Step 3: Acciones.** "Cerrar sesión" con `TonalActionButton`. "Eliminar cuenta" con `OutlinedButton` en `error` (borde y texto). En `DeleteAccountScreen`, el botón final es `Button(colors = error)`; deshabilitado, con `error.copy(alpha = 0.38f)`.
- [ ] **Step 4: Verificar.** Build e instalar. Capturas `t9/`: perfil, edición de una sección y eliminar cuenta, en claro y oscuro.
- [ ] **Step 5: Commit** `feat(profile): header, labeled sections and destructive styling`.

### Task 10: Login, registro, onboarding y alternativa

**Files:**
- Modify: `auth/AuthScreens.kt`, `onboarding/OnboardingScreen.kt`, `aifeedback/Alternatives.kt`, `res/values/strings.xml`

**Interfaces:**
- Consumes: `StationProgress` (Task 5), `AiGeneratingState` (Task 3), `SegmentedChoice` (Task 4), `AppTheme.colors`.
- Produces: `@Composable fun BrandHeader(modifier: Modifier = Modifier)` en `core/ui/Route.kt`: bloque `signal` con el wordmark "AllFitness" (string nuevo `app_wordmark` = "AllFitness") en Archivo 800 `displaySmall` `onSignal` y una línea `lineRoutine` con 3 estaciones (`StationProgress(3, 3, …)`).

- [ ] **Step 1: Login y registro.** `AuthScaffold` lleva `BrandHeader` arriba (≈ 30 % del alto en pantalla compacta, que se encoge con `imePadding`) y el formulario debajo. En `PasswordField` del login no aparece la ayuda "Mínimo 8 caracteres": se añade el parámetro `showHint: Boolean` y el login pasa `false`. Los enlaces legales del registro van en una sola columna.
- [ ] **Step 2: Onboarding.** `LinearProgressIndicator` se sustituye por `StationProgress(total = steps, reached = state.step + 1, lineColor = lineRoutine)` con `contentDescription` = `onboarding_step`. Título en `headlineLarge` con `heading()`. Al enviar, `AiGeneratingState(onboarding_generating, stages = 3 etapas)` (ya conectado en la Task 3).
- [ ] **Step 3: Sheet de alternativa.** `containerColor = surfaceContainerHigh` y título en `titleLarge`. Las razones como filas de 56 dp (ya son `selectable` con `Role.RadioButton`) dentro de `selectableGroup()`. Aviso de molestia (FR-019) en una `Card` `errorContainer` con `ic_info`. En la propuesta (FR-022a), "Usar esta" es `Button` y "Mantener la actual" es `TonalActionButton`.
- [ ] **Step 4: Verificar.** Build e instalar (cerrar sesión para ver el login). Capturas `t10/`: login, login con errores, registro con teclado, onboarding 1-4, generando y sheet con molestia y con propuesta. Esperado: "Crear cuenta" alcanzable con el teclado abierto desplazando, y onboarding con estaciones.
- [ ] **Step 5: Commit** `feat(entry): brand header, station onboarding and alternative sheet`.

### Task 11: Navegación adaptativa y transiciones

**Files:**
- Modify: `navigation/AppNavigation.kt`, `core/ui/Buttons.kt` (escala al pulsar)

**Interfaces:**
- Consumes: `AppSection`, `lineFor` (Task 1), `Motion`, `AppTheme.reducedMotion` (Task 2) e íconos `_filled` (Task 3).
- Produces: `Tab` añade `val section: AppSection` y `@DrawableRes val selectedIcon: Int`. `Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier` en `Buttons.kt`.

- [ ] **Step 1: `MainScaffold`** pasa a `NavigationSuiteScaffold` con los 4 `item(...)`. El ícono es `selectedIcon` cuando está seleccionado. Con `val line = AppTheme.colors.lineFor(tab.section, MaterialTheme.colorScheme.onSurface)`: `NavigationSuiteDefaults.itemColors(navigationBarItemColors = NavigationBarItemDefaults.colors(indicatorColor = line.copy(alpha = 0.16f), selectedIconColor = line, selectedTextColor = line), navigationRailItemColors = …mismos valores…)`, calculado por ítem. La barra se oculta en rutas que no son pestaña con `layoutType = NavigationSuiteType.None`, como hoy.
- [ ] **Step 2: Transiciones en `NavHost`.**
  - Entre pestañas: `fadeIn(tween(210, delayMillis = 90)) + scaleIn(initialScale = 0.96f)` / `fadeOut(tween(90))`.
  - Rutas de detalle (`RoutineDayRoute`, `RecipeRoute`, `ProfileSectionRoute`, `DeleteAccountRoute`): `slideInHorizontally { it / 10 } + fadeIn(tween(Motion.MEDIUM, easing = Motion.Emphasized))` y las inversas en `popEnterTransition` / `popExitTransition`.
  - Con `reducedMotion`, todo es `fadeIn` / `fadeOut(tween(Motion.SHORT))`.
- [ ] **Step 2b: Ancho máximo.** El `NavHost` va dentro de un `Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter)` con `Modifier.widthIn(max = Spacing.contentMaxWidth)` (spec 4.5). En el teléfono no cambia nada. La transición compartida estación → título (`SharedTransitionLayout`) queda **fuera de este plan**: era opcional en la spec, y se puede añadir después si se pide.
- [ ] **Step 3: `pressScale`.** `collectIsPressedAsState` + `animateFloatAsState(if (pressed) 0.97f else 1f, Motion.gentle())` → `graphicsLayer`; nada con `reducedMotion`. Se aplica en `TonalActionButton` y `AlternativeButton`, en el CTA del `SignPanel` y en `RouteRow`.
- [ ] **Step 4: Entrada de listas.** En `RoutineWeekScreen` (`RouteRow`) y `DietWeekScreen` (comidas), los primeros 6 ítems entran con `fadeIn` + `translationY` 8 dp → 0, con retardo `index * 30 ms`, **solo en la primera composición de la pantalla** (`rememberSaveable { mutableStateOf(false) }` como bandera de "ya animado"). Sin animación con `reducedMotion`. Las listas que cambian por alternativa usan `Modifier.animateItem()` en `items`.
- [ ] **Step 5: Verificar.** Build e instalar. Grabar `adb shell screenrecord --time-limit 15 /sdcard/nav.mp4` navegando entre pestañas, de la semana al día y de vuelta con el gesto atrás predictivo. Repetir con `adb shell settings put global animator_duration_scale 0` (leer y restaurar el valor). Esperado: transiciones suaves y reversibles con el gesto, y sin animaciones con la escala en 0. Captura de `NavigationSuiteScaffold` en un emulador de tablet o plegable si hay uno disponible (opcional, y anotarlo).
- [ ] **Step 6: Commit** `feat(nav): adaptive navigation suite, section-colored indicator and transitions`.

### Task 12: Cierre (revisión, documentación y verificación funcional)

**Files:**
- Create: `DESIGN.md`, `.impeccable/design.json` (lo genera el documentador)
- Modify: `PRODUCT.md` (sección Evidence on Hand: nuevas puntuaciones)

- [ ] **Step 1: Build completo.** `./gradlew :app:assembleMockDebug :app:assembleRealDebug :app:testMockDebugUnitTest :app:lintMockDebug`. Esperado: BUILD SUCCESSFUL y sin warnings nuevos de lint respecto a `main`.
- [ ] **Step 2: Recorrido funcional.** Seguir la lista de comprobación de `specs/001-allfitness-mvp/` (flujos felices, errores, sin conexión) en el emulador, con el flavor mock. Esperado: todos los FR se comportan igual que antes del rediseño. Anotar cualquier desviación como bloqueante.
- [ ] **Step 3: Capturas finales** de todas las pantallas en claro, oscuro, 130 % y 200 %, en `visual-audit/redesign/final/` y `.impeccable/review/phone.png` (Rutina en claro).
- [ ] **Step 4: Auditorías.** Ejecutar `/impeccable audit` (objetivo ≥ 17/20) y `/impeccable critique` (objetivo ≥ 30/40). Corregir los P0 y P1 que salgan, en un solo lote.
- [ ] **Step 5: Revisión final de impeccable** (el agente `impeccable-finish-reviewer` con el contrato de dirección, las capturas y `reference/android.md`; sin detector, plataforma nativa) y aplicar su veredicto. Después, `impeccable-documenter` para `DESIGN.md` y `.impeccable/design.json`.
- [ ] **Step 6:** `ponytail-review` de toda la rama contra `main`, y corregir los hallazgos de correctitud.
- [ ] **Step 7: Commit** `docs: DESIGN.md and redesign review evidence`.
