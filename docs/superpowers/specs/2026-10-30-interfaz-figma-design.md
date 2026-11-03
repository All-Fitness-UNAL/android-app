# Interfaz del prototipo de Figma: diseño

**Fecha:** 2026-10-30 · **Rama:** `redesign/figma-ui` (sale de `redesign/premium-ui`) · **Estado:** para revisión

## 1. Intención y fuente

El usuario entregó `All Fitness_prototype.fig` (exportado de Figma el 2026-09-17), con los mockups de una interfaz mejorada. Esta spec traduce esos mockups a la app Android existente, sin cambiar la lógica de negocio ni los contratos.

- **Fuente:** 7 frames de 402 dp de ancho: `all-fitness-onboarding`, `all-fitness-home-dashboard`, `all-fitness-workout`, `active-workout-screen`, `nutrition-dashboard-screen`, `lunch-recipe-screen` y `all-fitness-profile`.
- **Valores exactos:** el árbol de nodos decodificado está en `docs/design/figma/arbol-nodos.txt` (tamaño, posición, relleno, borde, radio, fuente y texto de cada nodo). La vista general está en `miniatura.png`, y los scripts `decode.mjs` y `tree.py` lo regeneran desde el `.fig`.
- **Mundo visual:** tema oscuro casi negro, tarjetas gris azulado, un verde de acento, botones en píldora con brillo verde, anillos de progreso, títulos display grandes y fotos en miniatura.

Esta dirección **reemplaza** al sistema "Línea" (`2026-10-30-rediseno-visual-design.md`). De "Línea" se conserva la base técnica:
- navegación adaptativa;
- estados de pantalla;
- aviso de IA;
- tests de contraste;
- movimiento reducido;
- gráfica Vico;
- reglas de accesibilidad.

## 2. Decisiones del usuario (2026-10-30)

| Tema | Decisión |
|---|---|
| Alcance | Se añaden la pestaña **Inicio** (panel del día) y el **modo sesión** (un ejercicio a la vez). Antes de construirlos se enmienda la spec 001 (FR-035 y FR nuevos) con Spec Kit, porque la constitución (principio V) prohíbe pantallas que no pida la spec. |
| Tema | **Solo oscuro**, como los mockups. La app deja de seguir el modo claro del sistema. No hay selector de tema, que está fuera de alcance. |
| Rama | `redesign/figma-ui`, nueva desde `redesign/premium-ui`. |

## 3. Lo que no se construye

Los mockups muestran funciones que la spec 001 deja fuera de alcance, o datos que el backend no tiene. La regla del proyecto es no inventar información ni funcionalidades.

| Elemento del Figma | Por qué no | Qué va en su lugar |
|---|---|---|
| Kcal consumidas, "remaining", agua, estado "Completed/Upcoming" de comidas | Fuera de alcance: "registro de lo comido, conteo de calorías" | Kcal y macros **del plan** del día |
| Temporizador de descanso, registro de peso y reps por serie, "Complete Set", "Pause", "Elapsed", "kcal Burned" | Fuera de alcance: "temporizador de descanso y registro de peso por serie" | Series × reps, descanso en texto y peso sugerido; marcar el ejercicio como hecho |
| Campana de notificaciones, ajuste "Notifications" | Fuera de alcance: notificaciones y su ajuste | Nada |
| Interruptor kg/lbs y cm/ft, ajuste "Units" | Las unidades son kg y cm (contrato) | Sufijo fijo "kg" / "cm" |
| Foto de perfil | El perfil no tiene foto | Inicial del nombre en un círculo |
| Fotos de comida, de ingredientes y de ejercicio del Figma | Contenido inventado: las fotos no corresponden a las recetas reales (PRODUCT.md) | `Exercise.imageUrl` cuando el backend la envía; si no, una placa con ícono por tipo |
| Imagen principal de la receta, favoritos, "Start Cooking" | No hay imagen de receta, favoritos ni modo cocina | Cabecera tipográfica y botón fijo "Pedir alternativa" (acción existente) |
| Objetivo "Gain Strength" | El contrato tiene 3 objetivos | Las 3 opciones reales en la cuadrícula |
| Dificultad por ejercicio | El ejercicio no la tiene | Nivel del perfil solo en la tarjeta de entreno de Inicio |
| "Daily calorie target", "Food preferences" | No existen en el perfil | Los campos reales: tipo de dieta, comidas al día, alergias y alimentos que no gustan |
| "Help & Support" | No existe | Nada |
| Textos en inglés | FR-042: toda la interfaz en español | Copia en español (§8) |

## 4. Tokens

El Figma no es uniforme: usa 4 verdes, 4 fondos y 4 familias tipográficas. Se unifican así, y cada valor sale de algún frame.

### 4.1 Color (único esquema, oscuro)

| Rol | Hex | Origen | Uso |
|---|---|---|---|
| `background` | `#11111E` | home, nutrition, recipe | Fondo de pantalla |
| `surface` / tarjeta | `#1C1C2C` | home, nutrition, barra | Tarjetas, barra inferior |
| `surfaceContainerHigh` | `#25253A` | filas de comida, pistas de anillo, etiquetas | Elementos dentro de tarjetas |
| `surfaceInput` | `#121B2A` | onboarding, active workout | Fondo de campos y steppers |
| `outlineVariant` | `#2A2A3E` | bordes de tarjeta y barra | Hilos decorativos (1,2:1, nunca como único límite de un control) |
| `outline` | `#6B6F86` (3,39:1 sobre `#1C1C2C`, 3,49:1 sobre `#121B2A`, 3,02:1 sobre `#25253A`) | (nuevo) | Borde de campos, chips y tarjetas de opción sin seleccionar |
| `primary` | `#2ECC71` | onboarding, active workout, nutrition | Acento, CTA, seleccionado, progreso |
| `onPrimary` | `#11111E` | botones | Texto sobre verde (8,9:1) |
| `primaryContainer` | `#2ECC71` al 10–12 % | seleccionado, etiquetas de IA | Fondo de opción elegida y etiquetas |
| `onSurface` | `#FFFFFF` | todos | Texto principal (16,8:1 sobre tarjeta) |
| `onSurfaceVariant` | `#9FA2B4` | home, nutrition, barra | Texto secundario (6,6:1 sobre tarjeta, 5,9:1 sobre `#25253A`) |
| `muted` | `#5F6175` | viñetas, "Upcoming" | **Solo decorativo** (2,76:1): viñetas, puntos y bordes de pendiente |
| `accent` | `#FFB300` | racha | Racha y su fondo al 12 % (8,5:1) |
| `error` | `#FF5F57` | barra de grasas en nutrition | Errores y acciones destructivas (5,6:1 sobre tarjeta) |
| Macros: proteína / carbohidratos / grasas | `#22A85E` / `#5B8DEF` / `#E8607A` | Ajustado del Figma | Validado con `validate_palette.js --mode dark --surface #1C1C2C`: pasa todo. El trío del Figma (`#2ECC71` / `#FFB300` / `#FF5F57`) falla en protanopía (ΔE 5,9) y en la banda de luminosidad |

Reglas:
- El texto nunca va en un color de macro.
- El verde es el único acento de interacción.
- El ámbar solo aparece en la racha.

### 4.2 Tipografía

| Rol M3 | Fuente | Peso / tamaño / interlínea | Origen |
|---|---|---|---|
| displaySmall | Outfit | ExtraBold 32/40 | "1,850 / 2,000 kcal" |
| headlineMedium | Outfit | ExtraBold 28/35 | Títulos de pantalla |
| headlineSmall | Outfit | ExtraBold 24/30 | Ejercicio en modo sesión |
| titleLarge | Outfit | ExtraBold 20/26 | Título de entreno, "Ingredientes" |
| titleMedium | Outfit | Bold 16/20 | Nombres de tarjeta, botones |
| titleSmall | Outfit | Bold 15/19 | Ejercicio y comida en lista |
| bodyLarge | Manrope | Medium 15/21 | Texto de lectura, pasos |
| bodyMedium | Manrope | Medium 14/19 | Texto general |
| bodySmall | Manrope | Regular 12/16 | Notas |
| labelLarge | Manrope | Bold 14/19 | Etiquetas de botón secundario |
| labelMedium | Manrope | SemiBold 12/16 | Rótulos, meta ("10 min de preparación") |
| labelSmall | Manrope | Bold 10/14 | Barra inferior, etiquetas de IA |

- Las cifras usan cifras tabulares (`tnum`). En la Tarea 0 se comprueba que cada fuente tenga esa función; si Outfit no la tiene, las cifras de display van en Manrope ExtraBold.
- Geist e Inter, que aparecen en algunos frames, se sustituyen por Manrope.

### 4.3 Formas, medidas y profundidad

- **Radios:**
  - etiquetas: 6 dp;
  - controles pequeños y segmentos: 8 dp;
  - campos y miniaturas: 12 dp;
  - tarjetas: 16 dp;
  - tarjeta destacada: 20 dp;
  - botones y chips: píldora completa;
  - anillos y avatares: círculo.
- **Margen de pantalla:** 16 dp (24 dp en onboarding y en los botones fijos).
- **Separaciones:** entre tarjetas, 12–20 dp; dentro de una tarjeta, 12–16 dp.
- **Tamaños:**
  - miniaturas de ejercicio de 76 dp en la lista y 56 dp en "A continuación";
  - miniaturas de comida de 64 dp;
  - anillos de 60 dp en Inicio y 72 dp en Dieta;
  - avatar de 40 dp en Inicio y 104 dp en Perfil.
- **Profundidad:** plana. La única sombra es el brillo verde del botón principal (`DROP_SHADOW` del Figma), con `Modifier.shadow` y `spotColor = primary`. En API < 28 el brillo sale gris.
- **Botón fijo:** se apoya sobre un degradado del fondo, de transparente a opaco.

### 4.4 Movimiento

- Se reutilizan los tokens `Motion` existentes (150, 300 y 450 ms, curva enfatizada) y las transiciones de navegación.
- Animaciones nuevas:
  - anillos y barras de progreso: 450 ms;
  - paso de página en el modo sesión: el del pager;
  - casilla de comprobación del ejercicio: 150 ms.
- Con "Quitar animaciones": sin movimiento, salvo los indicadores de carga.

## 5. Componentes

| Componente | Figma | Notas |
|---|---|---|
| `PrimaryButton` | `continue-button`, `start-workout-cta` | Píldora verde de 56 dp, texto `onPrimary`, brillo verde, ícono opcional a la derecha |
| `SecondaryButton` | `pause-btn`, `skip-button` | Píldora `#25253A` con texto blanco |
| `AppCard` | tarjetas | `#1C1C2C`, radio 16, hilo `outlineVariant` |
| `ScreenHeader` | `title-block`, `today-workout-title` | Título Outfit 28, subtítulo y elemento a la derecha (etiqueta de IA o duración) |
| `AiBadge` | `ai-sparkle-badge` | Ícono de destellos y "Plan de IA" en verde al 12 %. **No reemplaza** al aviso de IA (FR-037), que sigue visible |
| `FormTextField` | `input-wrapper` | Etiqueta arriba, relleno `#121B2A`, radio 12, borde `outline`, sufijo de unidad |
| `ChoiceCard` | `goal-*` | Cuadrícula de 2 columnas, círculo con ícono de 36 dp, seleccionado en verde al 10 % con borde verde |
| `PillChip` | `pill-*` | Píldora de 33 dp de alto visual y 48 dp de área táctil; seleccionado con check verde |
| `SegmentedProgress` | `progress-bar-container` | N segmentos de 4 dp con 6 dp de separación |
| `ProgressRing` | `ring-container` | Pista `#25253A` y arco verde, con ícono o cifra en el centro |
| `LinearProgress` | `progress-track` | 6–8 dp, píldora |
| `WeekStrip` | `weekly-activity` | 7 columnas: inicial del día y círculo de 28 dp (hecho, hoy, pendiente, pasado o descanso) |
| `ExerciseCard` | `exercise-card-*` | Miniatura de 76 dp, etiqueta de músculo, nombre, "4 × 12 reps · Descanso: 60 s" y casilla de 32 dp solo hoy |
| `MealCard` | `meal-card-*` | Placa de 64 dp con ícono por tipo, tipo, kcal, nombre y "N min de preparación". Sin estado de consumo |
| `StatCard` | `stat-card` (perfil) | Rótulo, cifra Outfit 22 y unidad |
| `PreferenceRow` | `pref-row` | Placa de 36 dp con ícono, rótulo y valor, separadas por hilos |
| `Avatar` | `avatar-glow-ring` | Círculo con la inicial y anillo verde |
| `LogoMark` | `logo-mark` | Cuadro verde de 28 dp, radio 8, con el rayo y "All Fitness" en Outfit ExtraBold 18 |
| Íconos | Lucide (trazo de 2) | Los que usa el Figma, como VectorDrawable, con licencia ISC en `assets/licenses/` |

## 6. Pantallas

| Pantalla de la app | Frame | Datos (todos existentes) |
|---|---|---|
| **Inicio** (nueva) | `home-dashboard` | <ul><li>Saludo "Hola, {nombre}" (`SessionManager`) con avatar de inicial.</li><li>Fila de 4 indicadores: ejercicios de hoy N/M (anillo), sesiones de la semana N/M (anillo), racha en semanas (cifra) y kcal del plan de hoy (cifra).</li><li>Tarjeta de entreno de hoy: etiqueta de IA, título del día, "{min} min · N ejercicios" (`Profile.sessionMinutes`), nivel con 3 segmentos (`Profile.level`), miniatura del primer ejercicio si tiene `imageUrl` y CTA al modo sesión.</li><li>Dieta de hoy: kcal del plan, barra de macros con leyenda y comidas (tocables, abren la receta).</li><li>Tu progreso: racha y `WeekStrip` de la semana.</li></ul> |
| Rutina | `all-fitness-workout` | <ul><li>Cabecera "Tu rutina" con etiqueta de IA.</li><li>`WeekStrip` para elegir el día (hoy por defecto).</li><li>Del día elegido: título, "N % completado · N de M ejercicios" y barra.</li><li>Lista de `ExerciseCard`: tocar abre el modo sesión en ese ejercicio.</li><li>Botón fijo "Empezar/Continuar entrenamiento" (solo hoy, en día de entreno y sin terminar).</li><li>"Regenerar rutina" al final.</li><li>Días no-hoy: solo lectura, con el mensaje existente.</li></ul> |
| **Modo sesión** (nueva vista de `RoutineDayRoute`) | `active-workout-screen` | <ul><li>Título del día, "Ejercicio N de M" y barra.</li><li>Imagen del ejercicio (`imageUrl`) o placa, y etiqueta de músculo.</li><li>Nombre, "Serie × reps", descanso y peso sugerido, e instrucciones.</li><li>"Marcar como hecho" o "Desmarcar".</li><li>"A continuación" con "Saltar".</li><li>"Anterior".</li><li>En el último ejercicio, "Terminar sesión".</li><li>"Pedir alternativa".</li><li>Mismas reglas de FR-013/014.</li></ul> |
| Dieta | `nutrition-dashboard-screen` | <ul><li>Cabecera "Tu dieta", fecha y etiqueta de IA.</li><li>Selector de día.</li><li>Tarjeta resumen: kcal del plan en grande, filas de macros con gramos y barra de proporción.</li><li>`MealCard` por comida.</li><li>"Regenerar dieta".</li></ul> |
| Receta | `lunch-recipe-screen` | <ul><li>Cabecera tipográfica (sin foto): píldora "N min", nombre en Outfit 28.</li><li>4 placas: kcal, proteína, carbohidratos y grasas, con anillo de proporción de gramos solo para dibujar (FR-017).</li><li>"Ingredientes" con contador y filas con cantidad.</li><li>"Preparación" con pasos numerados.</li><li>Botón fijo "Pedir alternativa".</li></ul> |
| Progreso | Sin frame: sistema | <ul><li>Cabecera.</li><li>Tarjeta semanal con anillo de sesiones N/M y racha.</li><li>Gráfica de peso con Vico (línea verde sobre tarjeta).</li><li>Historial.</li><li>"Registrar peso".</li></ul> |
| Perfil | `all-fitness-profile` | <ul><li>Avatar con inicial, nombre y correo.</li><li>"Información personal": 4 `StatCard` (edad, peso, estatura y nivel).</li><li>"Objetivo principal": tarjeta destacada con "Editar".</li><li>"Preferencias de entreno" y "Alimentación": `PreferenceRow` con "Editar" por sección.</li><li>CTA "Regenerar mi plan" (acción existente) con su nota.</li><li>"Cuenta": política de privacidad, cerrar sesión y eliminar cuenta.</li></ul> |
| Onboarding | `all-fitness-onboarding` | <ul><li>`LogoMark`, "Paso N de 4" y `SegmentedProgress`.</li><li>Título y subtítulo.</li><li>Los 4 pasos y campos actuales, sin cambios de lógica, con `FormTextField`, `ChoiceCard` (objetivo) y `PillChip` (equipo y alergias).</li><li>Botón fijo "Siguiente/Terminar".</li></ul> |
| Login, registro, generando, alternativa, editar sección, eliminar cuenta | Sin frame: sistema | <ul><li>`LogoMark`, campos, botón principal y estados con los componentes de §5.</li><li>Generando: fondo oscuro, `LogoMark`, mensaje vivo, indicador y resumen de lo elegido (ya existe).</li></ul> |

## 7. Navegación

- Barra inferior con 5 destinos: **Inicio**, Rutina, Dieta, Progreso y Perfil.
  - Íconos Lucide: `house`, `dumbbell`, `apple`, `chart-line` y `user`.
  - Seleccionado en verde, sin píldora indicadora (como el Figma).
  - Etiquetas de 10 sp.
- En ventanas anchas se usa el riel, como ahora.
- El inicio tras login u onboarding es **Inicio**.
- `RoutineDayRoute(date, index = 0)` pasa a ser el modo sesión. Se abre desde Inicio, desde el botón de Rutina y al tocar una tarjeta de ejercicio.

## 8. Copia nueva (español, en `strings.xml`)

| Clave | Texto |
|---|---|
| `tab_home` | Inicio |
| `home_greeting` | Hola, %1$s |
| `home_today_workout` | Entreno de hoy |
| `home_today_diet` | Tu dieta de hoy |
| `home_progress` | Tu progreso |
| `ai_badge` | Plan de IA |
| `workout_meta` | %1$d min · %2$s |
| `label_level` | (existe) |
| `stat_exercises_today` | Hoy |
| `stat_week_sessions` | Semana |
| `stat_plan_kcal` | Kcal plan |
| `streak_weeks` (plurales) | %d semana de racha / %d semanas de racha |
| `workout_percent` | %1$d %% completado |
| `action_start_workout` | Empezar entrenamiento |
| `action_continue_workout` | Continuar entrenamiento |
| `session_exercise_of` | Ejercicio %1$d de %2$d |
| `session_up_next` | A continuación |
| `action_mark_done` | Marcar como hecho |
| `action_unmark_done` | Desmarcar |
| `action_skip` | Saltar |
| `action_previous_exercise` | Anterior |
| `exercise_rest` | Descanso: %1$d s |
| `meal_prep_minutes` | %1$d min de preparación |
| `recipe_ingredients_count` (plurales) | %d ingrediente / %d ingredientes |
| `profile_section_account` | Cuenta |
| `onboarding_subtitle` | Con estos datos la IA arma tu rutina y tu dieta. |

Los textos existentes se mantienen. Antes de añadir una clave se comprueba si ya existe una equivalente.

## 9. Accesibilidad

- Contraste: texto ≥ 4,5:1 y controles o gráficos ≥ 3:1, fijados en `ContrastTest`. `muted` y `outlineVariant` nunca son el único límite ni el texto.
- Áreas táctiles ≥ 48 dp: los chips de 33 dp y las casillas de 32 dp crecen su área con `minimumInteractiveComponentSize`.
- Fuente al 130 % y al 200 %: tarjetas con alto mínimo, no fijo (el Figma fija 100 y 88 dp).
- TalkBack:
  - `heading()` en títulos de sección;
  - anillos con `progressBarRangeInfo` o descripción textual "N de M";
  - "Ejercicio N de M" con `liveRegion` al cambiar de página;
  - botones Anterior y Saltar como alternativa al deslizamiento.
- Aviso de IA (FR-037) visible en Inicio, Rutina, modo sesión, Dieta, Receta y Perfil.

## 10. Lo que sale de "Línea"

Salen `RouteRow`, `RouteStation`, `SignPanel`, `SessionBar`, `BrandHeader`, `StationProgress` y la Señal. También el amarillo "here" y los colores de línea por sección.

Se conservan `StateContent`, `AiNote` (reestilizado), `DataStrip` (reestilizado), la entrada escalonada, `pressScale` y `numberStyle` (sobre Outfit/Manrope). También la lógica de `StationState`, que pasa a nutrir los estados del `WeekStrip`.
