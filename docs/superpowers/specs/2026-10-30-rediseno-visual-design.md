# AllFitness: dirección visual y criterios UI/UX (rediseño)

- **Fecha:** 2026-10-30
- **Rama:** `redesign/premium-ui`
- **Estado:** **dirección A (Línea) aprobada** el 2026-10-30. Pendiente: plan de implementación. No se ha modificado código.
- **Cambio de contexto (2026-10-30):** el usuario autorizó añadir librerías que mejoren la apariencia. La constitución se enmendó a la v1.1.0 (regla "Dependencias nuevas"). Las librerías elegidas están en la sección 0.1.
- **Naturaleza:** documento complementario. No modifica la constitución (`.specify/memory/constitution.md`) ni la spec funcional (`specs/001-allfitness-mvp/spec.md`), que siguen siendo restricciones obligatorias. Si este documento contradice alguna de las dos, mandan ellas.

## 0. Alcance y restricciones

**Qué cambia:** solo la capa visual y de interacción del frontend: tema (color, tipografía, forma, movimiento), componentes de `core/ui`, composición de pantallas, iconografía y textos nuevos de apoyo en `strings.xml`.

**Qué no cambia:**
- Backend, contratos de API, DTOs, Room/DataStore, repositorios, ViewModels y su lógica.
- Flujos y reglas funcionales: FR-001 a FR-043, sin excepción.
- Stack: Kotlin, Compose, Material 3, Navigation Compose, Hilt y Coil.
- La barra inferior con Rutina, Dieta, Progreso y Perfil (FR-035).

**Restricciones que guían el diseño:**

| Origen | Restricción | Efecto en el diseño |
|---|---|---|
| Constitución v1.1.0 | Se pueden añadir librerías de UI si son compatibles con Compose y M3, están mantenidas, no reemplazan el stack base y se registran en el plan | Ver la sección 0.1. Las fuentes siguen empaquetadas, porque es la opción que funciona sin conexión, no por restricción. |
| Constitución | Colores, tipografía y espaciado salen del tema | Los colores propios (las líneas por sección) se exponen como `CompositionLocal` en `core/ui/theme`, nunca como hex en pantallas. |
| Constitución | Componentes repetidos en `core/ui` | Los componentes nuevos (panel Hoy, línea de la semana, tira de datos, aviso compacto) viven en `core/ui`. |
| Constitución | Texto en `strings.xml`, todo en español | Cualquier texto nuevo se añade a `strings.xml`. Los textos existentes no se reescriben sin aprobación. |
| FR-037 | Aviso de IA visible en rutina, ejercicio, dieta, receta y perfil | El aviso se mantiene en todas esas pantallas, pero cambia de lugar y de peso (ver 8.6). |
| FR-013, FR-014 | Marcar solo hoy; terminar aunque falten ejercicios | El modo sesión solo se activa hoy, y "Terminar" está siempre disponible en esa barra. |
| FR-017, FR-027 | La app no calcula nada | Solo se muestran los valores del servidor. Contar ejercicios es formato, no lógica. **No se muestra duración estimada**, porque no llega del backend. |
| FR-020, FR-022 | La IA no destruye lo actual | Las animaciones de reemplazo solo se ejecutan cuando llega la alternativa aceptada. |
| FR-038, FR-039 | Sin conexión | El estado sin conexión es parte del sistema visual, no un parche (ver 8.5). |
| PRODUCT.md | Seguir el tema del sistema | Claro y oscuro son de primera clase. No hay selector propio. |

## 0.1 Librerías nuevas (constitución v1.1.0)

| Librería | Para qué | Por qué esta |
|---|---|---|
| **Vico** `com.patrykandpatrick.vico:compose-m3` | Gráfica de peso (FR-026): ejes, línea en Línea Progreso, marcador al tocar, animación de entrada y escalado automático | Es la librería de gráficas nativa de Compose más mantenida, toma los colores de `MaterialTheme` y sustituye el `Canvas` hecho a mano, que hoy dibuja un punto suelto sin ejes. |
| **Core SplashScreen** `androidx.core:core-splashscreen` | Splash de marca: fondo Señal con el ícono y transición suave a la app, igual en API 26+ | Es la API oficial: unifica el splash de Android 12+ con versiones anteriores y evita el destello blanco al abrir. |
| **Material 3 Adaptive Navigation Suite** `androidx.compose.material3:material3-adaptive-navigation-suite` | `NavigationSuiteScaffold`: barra inferior en teléfono y rail en ventanas anchas o plegables, sin código condicional | Es la pieza oficial de Material 3 para la navegación adaptativa (P3 #11 de la auditoría). La versión la fija el BOM. |

**Evaluadas y descartadas:**
- **Lottie:** exige crear animaciones JSON. La línea y el check se dibujan con Compose con el mismo resultado y sin assets.
- **material-icons-extended:** son Material Icons antiguos, no Symbols, y pesan mucho. Se mantienen los vector drawables de Material Symbols.
- **Fuentes descargables de Google:** dependen de la red al primer uso. Las fuentes empaquetadas son mejores para el uso sin conexión (FR-038).
- **Librerías de blur o shimmer:** no encajan con el lenguaje de A, y el esqueleto de carga se hace con `rememberInfiniteTransition`.

Si el BOM 2026.09 trae `MotionScheme` estable en Material 3, se usa para los springs de los componentes. Si no, se usan los tokens propios de `Motion.kt`.

## 1. Diagnóstico visual del estado actual

Evidencia: 31 capturas reales del emulador (`visual-audit/2026-10-29/`), la crítica de impeccable (22/40, `.impeccable/critique/`) y la auditoría técnica (13/20, `.impeccable/audit/2026-10-30__app.md`).

**Veredicto:** la app funciona y es robusta (estados de error, caché sin conexión, accesibilidad básica, escalado de fuente), pero visualmente es **Material 3 de plantilla**. Si se quitan los nombres de los platos, podría ser cualquier app.

**Causas técnicas del aspecto genérico:**
1. `core/ui/theme/Color.kt` define solo 23 roles. Faltan `surfaceContainer*`, `surfaceBright` y `surfaceDim`, `outlineVariant`, `inverse*` y `scrim`. M3 los rellena con su paleta de referencia (lavanda en claro, gris púrpura en oscuro). Por eso las tarjetas, la barra inferior, el bottom sheet y los diálogos no pertenecen a la marca.
2. `Theme.kt` usa `Typography()` de serie: Roboto en peso regular para títulos, cifras proporcionales y sin voz propia.
3. No hay sistema de formas, de movimiento ni de componentes de dominio: todo es `Card` + `Text`.

## 2. Problemas principales (ordenados por impacto)

| # | Problema | Pantallas | Severidad |
|---|---|---|---|
| 1 | "Hoy" no está a un vistazo: son 7 tarjetas iguales y hoy es la 5.ª, marcada con un menta fuera de paleta. | Rutina semana (16) | P1 |
| 2 | Entrenar no tiene forma propia: no hay progreso "3 de 6", "Terminar" está al final de la lista y marcar no cambia la tarjeta. El checkbox se ve activo en días que no son hoy pero no responde. | Rutina día (18, 19, 34) | P1 |
| 3 | No hay identidad visual: mezcla de verde, lavanda y menta, tipografía de serie y títulos débiles. | Todas | P1 |
| 4 | El aviso de IA ocupa la primera posición de cada pantalla. | Rutina, día, dieta, receta, perfil | P2 |
| 5 | La espera de la primera generación (hasta 60 s) es una pantalla vacía con un spinner. | Generando (15) | P2 |
| 6 | Los datos van como prosa ("Series: 3 · Repeticiones: 10"; macros en una línea). | Día, dieta, receta | P2 |
| 7 | Los botones secundarios parecen deshabilitados, y "Pedir alternativa" aparece de 3 formas distintas. | Día, receta, dieta, perfil | P2 |
| 8 | Los estados vacíos y escasos no tienen diseño: la gráfica con 1 punto, "Entre 62 y 62 kg". | Progreso (25) | P2 |
| 9 | En el onboarding: contadores "0/3", unidades que solo aparecen al enfocar, chips sin check, el "7" huérfano y mucho espacio vacío. | Onboarding 1-4 | P2 |
| 10 | Accesibilidad estructural: no hay `heading()` ni `liveRegion`. | Todas | P2 |
| 11 | No hay adaptación a ventanas grandes. | Todas | P3 (tablet fuera de alcance declarado) |

## 3. Cómo se llegó a las direcciones

Se siguió el flujo de mundo visual nuevo de impeccable (`/impeccable` new-work) en modo **Operate**: es una app de uso diario, así que la claridad y las convenciones de Android mandan. El mundo visual solo aporta **tipografía, paleta, densidad y un gesto distintivo**. No aporta la navegación ni los controles, que siguen siendo Material.

**Mecanismo del producto en una frase:** la IA arma un plan semanal de entreno y comida a partir del cuerpo, el equipo y la comida local de la persona, y la persona lo sigue día a día y lo ajusta explicando por qué.

**Escena real:** una persona adulta principiante en Bogotá o Medellín, a las 6 a. m. en la sala de su casa o con la luz fluorescente de un gimnasio de barrio. Mira el teléfono entre series con una mano, y por la noche lee una receta en la cocina. **Por eso el modo claro es el principal y el oscuro se trata como de primera clase.**

**Lo típico de la categoría, descartado:** fondo negro OLED con naranja o verde neón "energético" (es lo que recomienda ui-ux-pro-max para fitness, y precisamente por eso es lo que haría cualquiera), y su opuesto, el bienestar pastel con beige y lavanda.

**Siete candidatas del mundo de la audiencia**, ordenadas por resonancia. Cubren más de tres familias de material:

1. **Origen:** el empaque de café de origen colombiano (ficha técnica, sello de lote, verde de cafetal). Familia: empaque.
2. **Perfil de etapa:** el perfil altimétrico de la Vuelta y del ciclismo que se ve en la TV colombiana. Familia: gráfica deportiva.
3. **Línea:** la señalética de transporte y ciclovía (líneas de color, estaciones, "usted está aquí"). Familia: señalética cívica.
4. **Plaza de mercado:** los letreros pintados a mano de Paloquemao y el color del producto. Familia: rótulo popular.
5. **Programa de partido:** la alineación y el marcador. Familia: impreso deportivo.
6. **Tabla nutricional:** el panel regulado del empaque. Familia: impreso regulatorio.
7. **Horario escolar:** la cuadrícula del cuaderno. Familia: papelería.

**La tirada de impeccable** (`seed:47113a5602c126fb`) asignó la **candidata 3, Línea**. Mi favorita, la 1 (Origen), entra como **alternativa B**. De los seis retadores que repartió el catálogo, así quedaron:

| Retador | Veredicto | Lo que A absorbe (disciplina, no estilo) |
|---|---|---|
| Feed vertical a pantalla completa | **Competitivo**: la audiencia lo reconoce, pero no hay videos (fuera de alcance) y no sirve para la semana | **Modo sesión: un ejercicio a la vez.** |
| Billetera de tiquetes de avión | **Competitivo**: los estados "sellados" hacen claro el producto | **Nada desaparece, se marca**: hecho, propuesta y desactualizado quedan visibles como estados. |
| Espécimen tipográfico variable | Descartado | **La escala como jerarquía**: una cifra grande por pantalla y etiquetas pequeñas. |
| Web japonesa de alta densidad | Descartado | **Sistema estricto de líneas finas**: los datos en tiras regladas compactas, sin aire inflado. |
| Catálogo de discos de los 80 | Descartado | **Nada se etiqueta dos veces**: se acaban "Objetivo" duplicado y el aviso repetido arriba. |
| Página de referencia con riel central | Descartado | **Vocabulario de estado gráfico**: pendiente con contorno, hecho relleno y pasado tachado o atenuado. |

## 4. Propuesta A: Línea (recomendada)

### 4.1 Concepto

**El plan es una ruta.** La semana es una línea con 7 estaciones, hoy es "estás aquí" y cada sesión es un tramo que se recorre. Se toma la gramática de la señalética de transporte (líneas de color que identifican, paneles oscuros con texto blanco, puntos de estación y cifras grandes), porque es el sistema visual más probado para que alguien que va con prisa sepa dónde está y qué sigue. Es exactamente la tarea diaria de AllFitness.

Se traduce a la gramática de la señalética, **no** a la marca de ningún sistema real (ni TransMilenio ni ningún metro): no se usan sus logos, nombres ni colores exactos.

### 4.2 Personalidad

Precisa, tranquila y segura. Habla poco y muestra mucho. Premium por la exactitud (alineación, cifras tabulares, líneas finas consistentes), no por la decoración. Nunca es para atletas: no hay neón, ni gritos, ni "¡DALE!".

### 4.3 Paleta (HEX, contraste verificado con la fórmula WCAG 2.x)

**Estrategia: Committed.** Hay un neutro verde grisáceo frío para la base, un **panel señal** oscuro como superficie protagonista y **tres colores de línea que hacen un trabajo**: identifican la sección (Rutina, Dieta, Progreso), igual que una línea de metro.

| Rol | Claro | Oscuro | Uso | Contraste |
|---|---|---|---|---|
| Andén (background) | `#F3F5F4` | `#0B1210` | Fondo de pantalla | — |
| Superficie (surface y tarjetas) | `#FFFFFF` | `#121A17` | Tarjetas y hojas | — |
| Tinta (onSurface) | `#101816` | `#E6ECE9` | Texto principal | 18.0 / 15.8 |
| Tinta secundaria (onSurfaceVariant) | `#4A5551` | `#A9B5B0` | Texto de apoyo | 7.8 / 8.4 |
| Hilo (outlineVariant) | `#D5DCD9` | `#2A3531` | Separadores de 1 dp | decorativo |
| Contorno (outline) | `#76817C` | `#7F8B86` | Bordes de campos | 4.0 (≥3 UI) |
| **Señal** (panel Hoy) | `#0F1F1A` | `#1D2F28` + hilo de 1 dp `#2A3531` | Panel "Hoy", modo sesión. En oscuro casi no contrasta con la superficie (1.3:1), así que se separa con el hilo y el amarillo. | blanco 17.1 |
| **Estás aquí** (amarillo señal) | `#F2C230` | `#F5CD4C` | Solo el marcador de hoy sobre Señal | 10.2 / 10.5 |
| **Línea Rutina** (primary) | `#0A7A50` | `#4CC38A` | Rutina, CTA principal, foco | 5.4 / 8.0 |
| **Línea Dieta** | gráfico `#D9661F`, texto `#A84A0E` | `#F08A45` | Dieta y macros (carbohidratos) | 3.6 gráfico / 5.8 texto / 7.1 |
| **Línea Progreso** | `#2C5BD6` | `#8FAEFF` | Progreso y gráfica de peso | 5.9 / 8.2 |
| Error | `#C62828` | `#FFB4AB` | Errores y acción destructiva | 5.6 |

**Por qué estos colores:**
- **El verde se queda pero se endurece.** Es la única herencia del launcher (`#1B6B4F`) y ancla lo saludable. Pasa de "verde app" a "verde línea" (`#0A7A50`), más saturado y con 5.4:1 sobre blanco.
- **Naranja para Dieta:** se asocia con el apetito y la comida cocinada, y no compite con el rojo de error. Los macros siguen la convención que el usuario de apps de nutrición ya conoce (ui-ux-pro-max: carbohidratos en naranja), adaptada así: proteína en Línea Progreso (azul), carbohidratos en Línea Dieta (naranja) y grasas en ocre: `#B07F00` en claro (3.6:1, gráfico) y `#E5B83A` en oscuro (9.5:1). Los macros siempre llevan su etiqueta de texto; el color nunca es la única señal.
- **Botón tonal (secondaryContainer):** claro `#DCEBE3` con tinta `#0F2A1F` (12.4:1); oscuro `#23352D` con `#D3E8DC` (10.1:1).
- **Azul para Progreso:** es frío y analítico, el color de "datos". Distingue la gráfica de la acción.
- **Amarillo solo para "Estás aquí":** es el color de alerta suave de la señalética. Usarlo una sola vez por pantalla le da un poder que nadie más tiene.
- **Neutros fríos con un 2 % de verde:** evitan el lavanda de M3 y el crema de la plantilla "premium". El blanco puro de las tarjetas sobre un andén ligeramente gris da separación sin sombras.

**Mapeo a Material 3:** se genera el esquema completo con Material Theme Builder a partir de `#0A7A50` y se fijan a mano los neutros y `inverseSurface` = Señal. Los tres colores de línea y el amarillo van en `AllFitnessColors` (un `CompositionLocal` de `core/ui/theme`), con variantes clara y oscura.

### 4.4 Tipografía

| Voz | Fuente | Por qué |
|---|---|---|
| Rótulo (display, headline, title large, cifras) | **Archivo**, variable con ejes `wght` 100–900 y `wdth` 62–125, licencia OFL | Grotesca de origen editorial pensada para datos. Su ancho semicondensado (`wdth` 87.5) da el tono de la señalética sin usar un "tipo de autopista" literal. Tiene cifras tabulares, que son críticas para series, reps, kg y kcal en columna. |
| Lectura (body, label, controles) | **Atkinson Hyperlegible Next**, variable `wght` 200–800, licencia OFL | Diseñada por el Braille Institute para baja visión: distingue 0/O e 1/l/I, algo que importa cuando se lee "10" o "12" a un metro entre series. Encaja con el principio de "cercano y alcanzable" y con el escalado de fuente que ya se verificó al 130 %. |

**Empaquetadas en `res/font`** (sin descargas): funcionan sin conexión desde el primer arranque (FR-038). Peso estimado entre 0.5 y 0.8 MB en total. **Fuera de las fuentes vetadas** por impeccable para evitar el aspecto de IA por defecto (Inter, DM Sans, Space Grotesk y similares). Roboto queda como fallback del sistema.

**Escala** (sp, con interlineado). Todo va en `sp` para respetar el tamaño de fuente del sistema:

| Rol M3 | Fuente y peso | Tamaño / interlineado | Uso |
|---|---|---|---|
| displaySmall | Archivo 700, wdth 87.5, tnum | 40 / 44 | Cifra protagonista ("1850", "3/6") |
| headlineLarge | Archivo 700, wdth 87.5 | 32 / 38 | Título de pantalla ("Tu rutina") |
| headlineMedium | Archivo 700, wdth 87.5 | 28 / 34 | Tipo de día en el panel Hoy |
| headlineSmall | Archivo 650 | 24 / 30 | Nombre de receta y de ejercicio en sesión |
| titleLarge | Archivo 600 | 20 / 26 | Títulos de sección y de tarjeta |
| titleMedium | Atkinson 700 | 16 / 22 | Nombre de ejercicio en lista y nombre de plato |
| titleSmall | Atkinson 700 | 14 / 20 | Subtítulos |
| bodyLarge | Atkinson 400 | 16 / 24 | Texto de lectura (pasos de receta) |
| bodyMedium | Atkinson 400 | 14 / 20 | Texto de apoyo |
| bodySmall | Atkinson 400 | 12 / 16 | Notas |
| labelLarge | Atkinson 700 | 14 / 20 | Botones |
| labelMedium | Atkinson 700, +0.4 tracking | 12 / 16 | Etiquetas de dato ("SERIES") |
| labelSmall | Atkinson 700, mayúsculas, +0.8 tracking | 11 / 14 | Rótulo de estación ("HOY · VIERNES 30") |

**Reglas:** máximo 3 tamaños por tarjeta. Las cifras siempre en Archivo tabular. Etiqueta pequeña arriba, cifra grande abajo: nunca "Etiqueta: valor" en una sola línea.

### 4.5 Espaciado

Base de **4 dp** con escala `4 · 8 · 12 · 16 · 24 · 32 · 48`. Se amplía el `Spacing` actual, que ya tiene xs, s, m, l y xl:
- Margen de pantalla: 16 dp (20 dp en ≥ 600 dp de ancho).
- Dentro de una tarjeta: 16 dp. Entre tarjetas: 12 dp. Entre secciones: 32 dp, y más espacio encima de un título que debajo (24 arriba, 8 abajo).
- Ancho máximo de contenido: 640 dp, centrado en ventanas anchas (cubre P3 #11 sin rehacer la navegación).

### 4.6 Formas y radios

Lenguaje de **cartel**: rectángulos con radio pequeño y preciso, y círculos solo para estaciones y marcadores.

| Token M3 | Radio | Uso |
|---|---|---|
| extraSmall | 4 dp | Chips de dato y campos |
| small | 8 dp | Botones, chips y campos de texto |
| medium | 12 dp | Tarjetas |
| large | 16 dp | Panel Señal y bottom sheets (solo esquinas superiores) |
| extraLarge | 24 dp | Diálogos |
| Círculo | — | Estaciones de la línea y FAB (si se usara) |

Los botones dejan de ser píldora. Un rectángulo de 8 dp es más "cartel" y menos "plantilla M3".

### 4.7 Elevación y superficies

- **Sin sombras** en contenido. La separación viene del contraste entre el andén `#F3F5F4` y la tarjeta `#FFFFFF`, y de un **hilo de 1 dp** `outlineVariant`.
- El **panel Señal** (oscuro) es la única superficie "elevada": ocupa la parte superior de Rutina y es el fondo del modo sesión. Es el ancla visual de la app.
- La barra inferior va en `surface` con un hilo superior de 1 dp, sin tinte tonal.
- Bottom sheet y diálogos en `surfaceContainerHigh` (neutro verde frío), nunca lavanda.

### 4.8 Componentes clave (todos en `core/ui`)

1. **SignPanel (panel Hoy):** fondo Señal y radio large. Arriba el rótulo amarillo "HOY · VIERNES 30 OCT" (labelSmall). En el centro el tipo de día en headlineMedium blanco. Debajo, una **tira de datos** con "6 ejercicios" y el estado ("Sin empezar", "3 de 6", "Terminada"). Abajo, el CTA a lo ancho "Ver sesión de hoy" en Línea Rutina. En día de descanso, el panel dice "Descanso" sin CTA y con un texto breve.
2. **RouteLine (línea de la semana):** filas compactas de 56 dp unidas por una línea vertical de 3 dp en Línea Rutina. La estación es un círculo de 12 dp: con contorno si es día de entreno pendiente, **relleno con check** si `finished`, un punto hueco de 8 dp atenuado si es descanso, y para hoy un círculo de 16 dp con halo amarillo. La fila completa es tocable (48 dp o más).
3. **DataStrip (tira de datos):** fila reglada de 2 a 4 celdas iguales, separadas por hilos verticales. Cada celda tiene la etiqueta labelMedium arriba y la cifra en Archivo tabular. Sustituye la prosa "Series: 3 · Repeticiones: 10 · Descanso: 60 s · Peso 8 kg" y se reutiliza en macros (Proteína, Carbohidratos, Grasas) y en el resumen de Progreso.
4. **ExerciseCard (lista):** el nombre en titleMedium, el grupo muscular como chip de 4 dp en Línea Rutina, la DataStrip y las instrucciones en bodyMedium. La imagen, si llega, va a 64 dp a la derecha. **Estado hecho:** la tarjeta pasa a fondo andén, el nombre se atenúa y aparece un check en la estación lateral. **Día que no es hoy:** sin checkbox, con una estación neutra (ver 4.10).
5. **SessionBar (barra de sesión, solo hoy):** barra fija inferior sobre Señal. Muestra el progreso "3 de 6" como **tramo de línea que se llena** y el botón "Terminar sesión de hoy" (FR-014: siempre activo). Al terminar, se reemplaza por "Sesión terminada" con check.
6. **AiNote (aviso de IA compacto):** el texto actual completo (FR-037) con un ícono de info de 16 dp, en bodySmall y tinta secundaria, **justo debajo del título de cada pantalla**, sin caja de color y separado por un hilo de 1 dp. Siempre a la vista sin desplazar. Ver 8.6.
7. **Botones:** primario relleno en Línea Rutina (o Línea Dieta dentro de Dieta). Secundario "tonal" en `secondaryContainer` verde frío con tinta oscura, para que **ya no parezca deshabilitado**. Terciario: texto en color de línea. Destructivo: contorno y texto en error. "Pedir alternativa" es **siempre** un botón tonal con el ícono de desvío (`alt_route`), en el mismo lugar de cada tarjeta.
8. **Chips de formulario:** `FilterChip` con `leadingIcon` de check al seleccionarse, fondo Línea Rutina al 12 % y borde de 1 dp. Selección única con `selectableGroup()`. Chips numéricos (días por semana, comidas) como **segmentos iguales en una sola fila** (`SingleChoiceSegmentedButtonRow`), lo que acaba con el "7" huérfano.
9. **Campos:** `OutlinedTextField` de radio small. La unidad va siempre visible a la derecha en `trailingIcon` ("cm", "kg", "años"), no en `suffix`, que solo aparece al enfocar. Sin contador en los campos numéricos. El rango va como texto de apoyo ("Entre 16 y 100 años").
10. **Estados** (cargando, vacío, error, sin conexión): un mismo componente con la estación como ilustración mínima. Cargando es un tramo de línea que avanza (indeterminado). Vacío es una estación hueca con su mensaje y acción. Error es una estación en color error con "Reintentar".

### 4.9 Navegación

- Se usa `NavigationSuiteScaffold` con 4 destinos (FR-035). En teléfono es la barra inferior (igual que hoy) y en ventanas anchas pasa a rail. Íconos y etiquetas siempre visibles.
- El **indicador activo toma el color de la línea de la sección**: verde en Rutina, naranja en Dieta, azul en Progreso y tinta en Perfil. El color responde a "dónde estoy", como en un mapa de metro.
- **Rutina abre en Hoy:** primero SignPanel y después RouteLine de la semana. Es un cambio de composición, no de flujo: tocar una estación sigue abriendo `RoutineDayRoute`.
- **Dieta abre en el día de hoy** (ya lo hace), con un selector de días en `SecondaryScrollableTabRow` que se desplaza a hoy y deja ver que hay más días.
- Atrás del sistema y atrás predictivo: se conservan, y las transiciones se definen como reversibles (ver 6).

### 4.10 Iconografía

- **Material Symbols Rounded**, peso 400, grado 0, tamaño óptico 24. Se exportan como `VectorDrawable` al `res/drawable` existente (mismo patrón `painterResource(R.drawable.ic_*)` y sin dependencia nueva).
- **Contorno en reposo y relleno al seleccionar** (barra inferior, chips). El cambio de relleno es la microseñal de estado.
- Íconos nuevos previstos: `ic_today`, `ic_alt_route` (alternativa = desvío), `ic_check_circle`, `ic_info`, `ic_cloud_off`, `ic_timer` (descanso), `ic_fitness_center`, `ic_restaurant`, `ic_show_chart`, `ic_person`, `ic_scale` (peso), `ic_local_fire_department` (kcal).
- Todo ícono con significado lleva `contentDescription`. Los decorativos que van junto a texto llevan `null`.

### 4.11 Pantallas: cómo se ve cada una

| Pantalla | Composición |
|---|---|
| Login y Registro | Arriba, un bloque Señal de un 35 % de alto con el logotipo tipográfico "AllFitness" en Archivo 800 y una línea verde con 3 estaciones (abstracción de "tu plan"). El formulario va sobre andén. CTA rectangular y `imePadding` ya presente. Sin la ayuda "Mínimo 8 caracteres" en el login (solo en el registro). |
| Onboarding 1-4 | La barra de progreso pasa a **4 estaciones unidas por una línea** ("Paso 2 de 4" accesible). Título en headlineLarge. Campos con unidad visible y segmentos numéricos. CTA fijo abajo. |
| Generando (60 s) | Pantalla Señal completa. Una línea que **dibuja 3 tramos** ("Leyendo tu perfil", "Armando tu rutina", "Armando tu dieta") con un texto que cambia por etapa. Las etapas son orientativas y se anuncian como progreso indeterminado (FR-010); no prometen tiempo. Debajo, un resumen de lo elegido (objetivo, días, equipo). |
| Rutina (semana) | SignPanel arriba, RouteLine de la semana, AiNote bajo el título y "Regenerar rutina" como botón tonal al final. |
| Rutina (día) | Título con fecha. Hoy: lista de ExerciseCard + SessionBar fija. Otro día: lista de solo lectura sin checkboxes, con un rótulo "Solo puedes marcar ejercicios el día de hoy" como etiqueta de estado (no como párrafo). |
| Alternativa (sheet) | `surfaceContainerHigh`, título en titleLarge, radios en lista de 56 dp, aviso de molestia (FR-019) como tarjeta de color error suave, propuesta (FR-022a) con "Usar esta" en primario y "Mantener la actual" en tonal. |
| Dieta | Tabs de día. Un **panel resumen** con kcal del día en displaySmall y la DataStrip de macros con una **barra apilada** de 3 segmentos que muestra la proporción recibida (solo valores del servidor). Las comidas en lista con hora o etiqueta (Desayuno…) a la izquierda en Línea Dieta y kcal tabular alineado a la derecha en la línea base del nombre. |
| Receta | Nombre en headlineSmall. DataStrip de tiempo, kcal y porción. Ingredientes como lista con viñeta de estación y cantidad tabular alineada. Pasos numerados con números grandes en Archivo y bodyLarge con 24 sp de interlineado (lectura en la cocina). AiNote bajo el título; "Pedir alternativa" al pie. |
| Progreso | Arriba, "Esta semana" con **una línea de N estaciones** (programadas) rellenas hasta las completadas y la racha como cifra. Gráfica de peso en Línea Progreso. **Con menos de 2 registros**, estado vacío propio ("Registra otro peso para ver tu tendencia") en lugar de un punto suelto. Rango "Entre X y Y kg" solo si X ≠ Y. Historial como tarjeta con filas regladas. |
| Perfil | Encabezado con inicial en un círculo Señal, nombre y correo. Secciones como tarjetas con DataStrip etiquetada y "Editar" como botón tonal pequeño con ícono. Cerrar sesión en tonal y eliminar cuenta en destructivo. |
| Eliminar cuenta | El campo "ELIMINAR" y el botón destructivo en color error; deshabilitado con el error al 38 %, que deja claro qué tipo de acción es. |

## 5. Propuesta B: Origen (alternativa)

### 5.1 Concepto

**El plan es un producto de origen con su ficha técnica.** Se toma la gramática del empaque de café de especialidad colombiano: un campo de color profundo de cafetal que domina, fichas con datos alineados como "altura, proceso, variedad" (aquí "series, reps, descanso") y un sello de estado. Es local y premium por asociación cultural, no por lujo importado.

### 5.2 Personalidad

Cálida, orgullosa de lo propio y artesanal pero exacta. Más emocional que A.

### 5.3 Paleta (contraste verificado)

**Estrategia: Committed sobre un campo verde.** Cada pantalla abre con una franja ancha de Hoja.

| Rol | Claro | Oscuro | Contraste |
|---|---|---|---|
| Hoja (campo de marca) | `#163A2C` | `#0E1612` | blanco 12.5 |
| Primario | `#1F5A43` | `#7FD1AE` | 8.1 / 9.5 |
| Panela (acento cálido) | texto `#8A5A16`, sobre Hoja `#E0A64A` | `#E8B260` | 5.9 / 5.8 / 8.9 |
| Cereza (racha y momentos) | `#A3352B` | `#F2A497` | 6.8 |
| Fondo | `#F2F4F1` | `#0E1612` | — |
| Tinta | `#14201B` | `#E4EAE6` | 15.2 / 15.1 |

**Por qué:** el verde de cafetal es colombiano sin caer en lo turístico. La panela (amarillo tostado) aporta calidez sin ser "naranja fitness". La cereza se reserva para los logros, con el riesgo de que se confunda con error si se usa mal.

### 5.4 Tipografía

- **Bricolage Grotesque** (OFL, ejes `opsz` y `wdth`) para rótulos: tiene trampas de tinta y carácter de impreso, como una etiqueta.
- **Roboto** (sistema) para lectura: la máxima familiaridad nativa y nada que empaquetar.

### 5.5 Sistema (resumen)

- **Espaciado:** misma base de 4 dp.
- **Formas:** suaves, 16 dp en tarjetas y 20 dp en el campo, más un "sello" circular de estado.
- **Elevación:** tonal y sin hilos.
- **Componentes:** "Ficha" (tarjeta con los datos en dos columnas tipo etiqueta) y "Sello" (hecho, propuesta, sin conexión) que se estampa con una animación de escala.
- **Navegación:** la misma, con el indicador en Panela.
- **Iconos:** Material Symbols Rounded relleno.
- **Movimiento:** más físico, con springs más vivos y el sello que "cae".

### 5.6 Por qué no es la recomendada

1. **Riesgo de plantilla "artesanal premium":** el verde profundo con un acento tostado está a un paso del crema con serif que impeccable marca como el cliché de las interfaces generadas por IA.
2. **Un solo color de campo** no puede codificar las secciones. A usa el color para orientarse y B solo para ambientar.
3. **El gesto distintivo (la ficha)** es una tarjeta mejor vestida, mientras que el de A (la línea con "estás aquí") **resuelve el problema P1 n.º 1** por sí mismo.

## 6. Propuesta de animaciones (Compose nativo; la gráfica anima con Vico)

**Gramática:** el movimiento sigue la línea. Lo que avanza en el plan avanza en pantalla (horizontal y hacia abajo), y lo que vuelve, retrocede. Las animaciones son cortas, con el easing de M3 (`CubicBezierEasing(0.2f, 0f, 0f, 1f)` enfatizado) y springs con `dampingRatio` de 0.8 a 1.0 (sin rebote visible). Se centralizan en `core/ui/theme/Motion.kt` como tokens: `durationShort` 150 ms, `durationMedium` 300 ms, `durationLong` 450 ms y `springGentle`.

| Caso | API de Compose | Detalle | Con movimiento reducido |
|---|---|---|---|
| Entre pestañas | `NavHost` con `enterTransition` y `exitTransition` | Fundido cruzado: sale con `fadeOut(90 ms)` y entra con `fadeIn(210 ms, delay 90)` + `scaleIn(0.96)` | Solo fundido de 150 ms |
| Semana → día, dieta → receta | `slideInHorizontally` + `fadeIn`, y las inversas en `popEnter` y `popExit` | Eje compartido X, desplazamiento del 10 % y 300 ms. **Compatible con atrás predictivo** (Navigation 2.10 hace la transición controlable con el gesto) | Fundido de 150 ms |
| Estación → título del día | `SharedTransitionLayout` + `Modifier.sharedBounds` | El nombre del día "viaja" del RouteLine al título. Es opcional: se implementa solo si la API es estable en el BOM 2026.09; si no, se omite sin afectar al resto | Desactivado |
| Aparecer y desaparecer (aviso sin conexión, errores, propuesta de alternativa) | `AnimatedVisibility` con `expandVertically` + `fadeIn`, y `shrinkVertically` + `fadeOut` | 250 ms | Instantáneo |
| Estados cargando / contenido / error | `AnimatedContent` en `StateContent` (`core/ui`) | Fundido de 200 ms; un único lugar para toda la app | Instantáneo |
| Pulsar botones y tarjetas | `interactionSource.collectIsPressedAsState()` + `animateFloatAsState` → `graphicsLayer { scaleX/Y }` | Escala 0.97 con springGentle; el ripple de M3 se conserva | Solo ripple |
| Marcar ejercicio | `animateColorAsState` (tarjeta → andén) + check que se dibuja con `animateFloatAsState` sobre `drawWithContent` + **haptic** `HapticFeedbackType.ToggleOn` | 200 ms | Cambio inmediato y haptic |
| Progreso de la sesión | `animateFloatAsState` del tramo lleno en SessionBar | 450 ms enfatizado | Inmediato |
| Terminar la sesión | La línea se completa, la estación de hoy se rellena con un único pulso de escala 1 → 1.15 → 1 y aparece un snackbar | Un solo momento y una sola vez | Sin pulso |
| Expandir (instrucciones del ejercicio, secciones del perfil) | `Modifier.animateContentSize(springGentle)` + rotación del chevron | — | Instantáneo |
| Listas | `LazyColumn` con `Modifier.animateItem()` | Reordenar o reemplazar (alternativa aceptada) sin saltos | Instantáneo |
| Entrada inicial de una lista | `LaunchedEffect` con `Animatable` por ítem, con retardo de índice × 30 ms (**solo los primeros 6 y solo la primera carga**) y fundido más 8 dp hacia arriba | Nunca en recomposiciones ni al volver | Desactivado |
| Generando el plan | `rememberInfiniteTransition` para el tramo que avanza + `AnimatedContent` del texto de etapa (sube y entra) | Bucle suave de 1.2 s | Barra indeterminada estática y texto que cambia |
| Sheet de alternativa | El `ModalBottomSheet` de M3 tal cual (ya anima) | — | — |

**Movimiento reducido:** se lee `Settings.Global.ANIMATOR_DURATION_SCALE`. Con 0 ("Quitar animaciones" en accesibilidad), un `LocalReducedMotion` en el tema hace que todos los tokens de `Motion.kt` usen su variante reducida. Hay que verificar en el emulador cómo trata Compose ese ajuste en el BOM actual.

**Límites:** nada se anima sin un motivo de estado. No hay parallax, ni animaciones en bucle salvo la espera de la IA, ni rebotes. Ninguna animación bloquea la interacción ni retrasa contenido que ya está listo.

## 7. Recomendación

**Propuesta A, Línea.** Razones:

1. **Resuelve los P1 con el propio lenguaje:** "estás aquí" contesta "¿qué toca hoy?" (principio 1 de PRODUCT.md), y el tramo que se llena da forma a la sesión.
2. **El color trabaja en vez de decorar:** tres líneas identifican tres secciones. Es la única propuesta en la que la paleta orienta.
3. **Profesional, moderna, clara y consistente por construcción:** la señalética es el sistema visual más probado para la claridad bajo prisa. El aspecto premium sale de la precisión (cifras tabulares, hilos de 1 dp, alineación), que es replicable en cada pantalla nueva.
4. **Lejos de los dos clichés:** ni el negro neón de fitness ni el crema artesanal.
5. **Riesgo honesto:** si se ejecuta a medias, puede leerse frío o "de transporte". Lo mitigan el amarillo único, la tipografía hiperlegible (que es cálida) y el texto cercano que ya existe.

## 8. Sistema de diseño propuesto (versión A)

### 8.1 Archivos de tema (`core/ui/theme/`)

- `Color.kt`: `LightColors` y `DarkColors` con **todos** los roles M3, más `AllFitnessColors` (lineRoutine, lineDiet, lineProgress, here, signal, onSignal, macroProtein, macroCarbs, macroFat), en claro y oscuro.
- `Type.kt` (nuevo): `FontFamily` de Archivo y Atkinson Hyperlegible Next (variables, con `FontVariation.Settings`) y la `Typography` de 4.4.
- `Shape.kt` (nuevo): `Shapes` de 4.6.
- `Motion.kt` (nuevo): los tokens de 6 y `LocalReducedMotion`.
- `Theme.kt`: compone todo y amplía `Spacing` con 12, 48 y el ancho máximo de contenido.

### 8.2 Componentes (`core/ui/`)

Nuevos: `SignPanel`, `RouteLine` y `RouteStation`, `DataStrip`, `SessionBar`, `AiNote`, `StationProgress` (onboarding y progreso semanal) y `LineIndicator` (carga). Rediseñados: `StateContent`, `LoadingState`, `ErrorState`, `EmptyState`, `OfflineBanner`, `AppTopBar` y `FormFields` (chips, segmentos, campos con unidad).

### 8.3 Reglas de uso

1. El color de línea solo identifica la sección o la acción principal de esa sección. Nunca es decoración.
2. El amarillo "Estás aquí" aparece **como máximo una vez por pantalla**.
3. Cada pantalla tiene una sola cifra en displaySmall.
4. Los datos van en DataStrip, nunca en prosa.
5. Hay un solo componente por acción repetida: "Pedir alternativa" siempre es el mismo botón tonal con `alt_route`.
6. Superficies: andén → tarjeta blanca → Señal. No hay otros niveles.

### 8.4 Accesibilidad (además de lo existente)

`semantics { heading() }` en los títulos de pantalla y sección. `liveRegion = Polite` en la espera de la IA y en el resultado. Checkbox con `enabled = false` (o sin checkbox) en días que no son hoy. `selectableGroup()` en los grupos de opción única. Contraste de texto ≥ 4.5 y de gráficos ≥ 3 en los dos temas (tabla 4.3). Áreas táctiles ≥ 48 dp. Comprobar al 130 % y al 200 % de fuente.

### 8.5 Sin conexión

`OfflineBanner` como franja fina en la parte superior con `ic_cloud_off` y el texto existente, en tinta secundaria sobre `surfaceContainerHigh`, que entra con `AnimatedVisibility`. Las acciones que requieren red (FR-039) se muestran deshabilitadas con su motivo en el texto de apoyo, no ocultas.

### 8.6 Aviso de IA (FR-037)

Decisión: `AiNote` va justo debajo del título, siempre a la vista sin desplazar y con el texto completo. Lo que cambia es el peso visual (sin caja de color, con una línea fina y tinta secundaria), no la presencia. En Rutina semana va entre el título y el SignPanel.

## 9. Criterios de aceptación UI/UX

**Funcionales (no negociables):**
- [ ] Todos los flujos de FR-001 a FR-043 siguen funcionando igual que en el mock. Se recorren con la lista de comprobación de la spec en el emulador.
- [ ] No cambia ningún archivo de `*Repository.kt`, `*Data.kt` (DTOs), ViewModels, `core/network`, `core/database` ni `core/session`, salvo los imports de UI.
- [ ] Las únicas dependencias nuevas son las de la sección 0.1, declaradas en `libs.versions.toml` y registradas en el plan.
- [ ] Todo texto nuevo está en `strings.xml`. No hay hex ni `dp` sueltos en las pantallas (solo en `core/ui/theme`).
- [ ] Build limpio y sin warnings nuevos de lint.

**Visuales:**
- [ ] No queda ningún rol de color sin definir: 0 superficies lavanda o púrpura en claro y en oscuro (capturas de todas las pantallas).
- [ ] Abrir Rutina muestra hoy (tipo de día, número de ejercicios, estado y CTA) sin desplazar, en un teléfono de 6.1".
- [ ] En el día de hoy, el progreso "N de M" y "Terminar sesión de hoy" se ven siempre sin desplazar.
- [ ] Ningún dato numérico de ejercicio, receta o macros aparece como prosa "Etiqueta: valor".
- [ ] "Pedir alternativa" se ve igual en ejercicio, receta, rutina y dieta.
- [ ] El amarillo aparece como máximo una vez por pantalla y el color de línea coincide con la pestaña activa.
- [ ] Los estados cargando, vacío, error y sin conexión usan el mismo sistema en todas las pantallas.

**Accesibilidad:**
- [ ] Contraste ≥ 4.5 en texto y ≥ 3 en gráficos y bordes, en los dos temas.
- [ ] Sin recortes ni solapes al 130 % y al 200 % de fuente.
- [ ] TalkBack: los títulos navegables por encabezado, la espera de la IA anunciada y los checkboxes de días que no son hoy anunciados como no disponibles.
- [ ] Con "Quitar animaciones" activado, nada anima salvo los indicadores de carga.

**Movimiento:**
- [ ] Las transiciones son reversibles con el atrás predictivo.
- [ ] No hay jank visible al desplazar la semana, la dieta ni el día (inspección en el emulador; en dispositivo real si hay uno disponible).
- [ ] La entrada escalonada solo ocurre en la primera carga.

**Puntuación objetivo (mismas herramientas):**
- [ ] `/impeccable critique` ≥ 30/40 (hoy 22).
- [ ] `/impeccable audit` ≥ 17/20 (hoy 13).
- [ ] Revisión con `ponytail-review` en cada etapa, sin hallazgos de correctitud abiertos.

## 10. Plan de rediseño por etapas

Cada etapa compila, se recorre en el emulador, se capturan las pantallas que toca (claro, oscuro y fuente al 130 %) y se revisa con `ponytail-review` antes de pasar a la siguiente. El plan detallado tarea por tarea se escribirá con `superpowers:writing-plans` cuando se apruebe este documento.

| Etapa | Contenido | Archivos principales | Salida verificable |
|---|---|---|---|
| **0. Base** | Dependencias de la sección 0.1, fuentes en `res/font`, tokens de color (esquema completo + `AllFitnessColors`), tipografía, formas y movimiento, y splash de marca. Sin tocar pantallas. | `gradle/libs.versions.toml`, `app/build.gradle.kts`, `core/ui/theme/*`, `res/font/*`, `res/values/themes.xml`, `MainActivity.kt` (solo `installSplashScreen()`) | Todas las pantallas ya pierden el lavanda y cambian de tipografía. Capturas antes y después. |
| **1. Componentes** | `DataStrip`, `AiNote`, botones, chips y segmentos, campos con unidad, estados (`StateContent` animado), `OfflineBanner`, `AppTopBar` con `heading()`, íconos nuevos. | `core/ui/Components.kt`, `core/ui/FormFields.kt`, `res/drawable/*`, `strings.xml` | Las pantallas existentes usan los nuevos componentes sin cambiar su composición. |
| **2. Hoy + sesión** (prioridad elegida) | `SignPanel`, `RouteLine`, `SessionBar`, ExerciseCard con estado hecho y checkbox correcto, transiciones semana → día. | `routines/RoutineWeekScreen.kt`, `routines/RoutineDayScreen.kt`, `routines/ExerciseItem.kt`, `navigation/AppNavigation.kt` | Cumple los criterios de "Hoy" y sesión de la sección 9. |
| **3. Dieta y receta** | Resumen con barra de macros, lista de comidas alineada, tabs de día, receta legible en la cocina. | `nutrition/DietScreens.kt` | Ningún macro en prosa. La receta se lee a un brazo de distancia. |
| **4. Progreso y perfil** | Línea semanal de sesiones, gráfica en Línea Progreso, estado vacío de la gráfica, perfil con DataStrip, acciones destructivas claras. | `progress/ProgressScreen.kt`, `onboarding/ProfileScreens.kt`, `onboarding/ProfileSections.kt` | Sin "Entre 62 y 62 kg" y sin la gráfica de un punto. |
| **5. Entrada y onboarding** | Login y registro con bloque Señal, onboarding con estaciones, pantalla Generando por etapas, sheet de alternativa. | `auth/AuthScreens.kt`, `onboarding/OnboardingScreen.kt`, `onboarding/ProfileForm.kt`, `aifeedback/Alternatives.kt` | Los 60 s ya no son una pantalla muerta. |
| **6. Movimiento y pulido** | Transiciones de navegación completas, entrada de listas, haptics, movimiento reducido, ancho máximo en ventanas anchas. | `navigation/AppNavigation.kt`, `core/ui/theme/Motion.kt` | Criterios de movimiento cumplidos. |
| **7. Cierre** | `/impeccable audit` + `/impeccable critique` + revisión final de impeccable + `DESIGN.md` generado desde lo construido + recorrido de la lista de comprobación de la spec. | `DESIGN.md`, `.impeccable/design.json` | Puntuaciones objetivo de la sección 9. |

## 11. Archivos y componentes que probablemente cambian

- **Tema:** `core/ui/theme/Color.kt`, `Theme.kt`, y los nuevos `Type.kt`, `Shape.kt` y `Motion.kt`.
- **Componentes:** `core/ui/Components.kt`, `core/ui/FormFields.kt` y, posiblemente, componentes nuevos en `core/ui/` (por ejemplo `Route.kt` y `DataStrip.kt`).
- **Pantallas:** `routines/RoutineWeekScreen.kt`, `routines/RoutineDayScreen.kt`, `routines/ExerciseItem.kt`, `nutrition/DietScreens.kt`, `progress/ProgressScreen.kt`, `onboarding/OnboardingScreen.kt`, `onboarding/ProfileForm.kt`, `onboarding/ProfileScreens.kt`, `onboarding/ProfileSections.kt`, `auth/AuthScreens.kt` y `aifeedback/Alternatives.kt`.
- **Navegación:** `navigation/AppNavigation.kt` (transiciones e indicador por sección).
- **Recursos:** `res/font/` (nuevo), `res/drawable/ic_*.xml`, `res/values/strings.xml` (solo añadir), `res/values/themes.xml` (color de la ventana y del splash) y, opcionalmente, `ic_launcher_foreground.xml` con el nuevo verde.
- **No se tocan:** `*Repository.kt`, `*Data.kt`, `*ViewModel` (salvo que una pantalla necesite exponer un valor ya existente), `core/network`, `core/database`, `core/session`, `app/src/mock/` y specs.

## 12. Decisiones que tomé yo (revísalas)

1. **No mostrar duración estimada** en el panel Hoy, porque calcularla sería lógica de negocio (FR-017). Si el backend la envía en el futuro, tiene un lugar en la DataStrip.
2. **Las fuentes empaquetadas** (Archivo y Atkinson Hyperlegible Next, licencia OFL) en lugar de Google Fonts descargables, para que funcionen sin conexión desde el primer arranque.
3. **Los botones rectangulares de 8 dp** en lugar de la píldora de M3.
4. **Textos nuevos mínimos:** "Ver sesión de hoy", las etapas de "Generando", "Registra otro peso para ver tu tendencia", los estados "Sin empezar", "N de M" y "Terminada", y las unidades visibles. Los textos existentes no cambian.
5. **La tablet sigue fuera de alcance:** `NavigationSuiteScaffold` da el rail gratis y se limita el ancho del contenido, pero no se diseñan layouts de varios paneles.
6. **El `SharedTransitionLayout` es opcional** y depende de que la API sea estable en el BOM.
7. **Wordmark "AllFitness"** compuesto con Archivo 800 en Login, Registro y Generando. No es un logo nuevo (PRODUCT.md dice que no existe), solo el nombre con la tipografía de la marca. El ícono del launcher puede adoptar el nuevo verde en la etapa 7, si se aprueba.
8. **Licencias:** los `.ttf` se descargan del repositorio oficial de Google Fonts y su `OFL.txt` se incluye en `app/src/main/assets/licenses/`.
9. **El modo sesión no es un pager vertical** de un ejercicio por pantalla (idea del retador de feed). Se queda como lista con barra fija, porque principiantes necesitan ver qué viene y las imágenes de ejercicio no siempre existen. Si se quiere el pager, es una etapa aparte.

## 13. Decisiones cerradas

1. **Dirección:** A (Línea), aprobada por el usuario el 2026-10-30.
2. **Librerías:** se permiten (constitución v1.1.0). Las elegidas están en la sección 0.1.
3. **FR-037 (mi decisión, conservadora):** `AiNote` va **justo debajo del título**, siempre a la vista sin desplazar y con el texto completo. Cambia su peso visual: sin caja de color, una línea fina, el ícono de info y bodySmall en tinta secundaria. Así se cumple "visible" sin discusión. Moverlo al pie queda descartado.
4. **Textos nuevos** (12.4): se incluyen en el plan como tareas explícitas para que se puedan revisar uno a uno antes de implementarlos.
