---
name: AllFitness
description: Plan semanal de entreno y dieta generado por IA, leído como una línea de transporte con estaciones.
colors:
  line-routine: "#0A7A50"
  line-routine-dark: "#4CC38A"
  on-primary: "#FFFFFF"
  on-primary-dark: "#003822"
  line-diet: "#D9661F"
  line-diet-dark: "#F08A45"
  line-diet-text: "#A84A0E"
  line-diet-text-dark: "#F08A45"
  line-progress: "#2C5BD6"
  line-progress-dark: "#8FAEFF"
  tonal: "#C6E3D2"
  tonal-dark: "#2A4234"
  on-tonal: "#0F2A1F"
  on-tonal-dark: "#D3E8DC"
  signal: "#0F1F1A"
  signal-dark: "#1D2F28"
  on-signal: "#FFFFFF"
  on-signal-dark: "#E6ECE9"
  signal-outline: "#00000000"
  signal-outline-dark: "#2A3531"
  here: "#F2C230"
  here-dark: "#F5CD4C"
  macro-protein: "#2C5BD6"
  macro-protein-dark: "#5E8BEF"
  macro-carbs: "#D9661F"
  macro-carbs-dark: "#DD7434"
  macro-fat: "#C2389A"
  macro-fat-dark: "#CF4E9C"
  platform: "#F3F5F4"
  platform-dark: "#0B1210"
  surface: "#FFFFFF"
  surface-dark: "#121A17"
  ink: "#101816"
  ink-dark: "#E6ECE9"
  ink-secondary: "#4A5551"
  ink-secondary-dark: "#A9B5B0"
  outline: "#76817C"
  outline-dark: "#7F8B86"
  hairline: "#D5DCD9"
  hairline-dark: "#2A3531"
  surface-container: "#E9EFEB"
  surface-container-dark: "#1B211F"
  surface-container-high: "#E3EAE6"
  surface-container-high-dark: "#252B29"
  error: "#C62828"
  error-dark: "#FFB4AB"
  error-container: "#FFDAD6"
  error-container-dark: "#93000E"
typography:
  wordmark:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "40px"
    fontWeight: 800
    lineHeight: "44px"
    fontFeature: "tnum"
  display-small:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "40px"
    fontWeight: 700
    lineHeight: "44px"
    fontFeature: "tnum"
  headline-large:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "32px"
    fontWeight: 700
    lineHeight: "38px"
    fontFeature: "tnum"
  headline-medium:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "28px"
    fontWeight: 700
    lineHeight: "34px"
    fontFeature: "tnum"
  headline-small:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "24px"
    fontWeight: 650
    lineHeight: "30px"
    fontFeature: "tnum"
  title-large:
    fontFamily: "Archivo (wdth 87.5)"
    fontSize: "20px"
    fontWeight: 600
    lineHeight: "26px"
    fontFeature: "tnum"
  title-medium:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "16px"
    fontWeight: 700
    lineHeight: "22px"
  title-small:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "14px"
    fontWeight: 700
    lineHeight: "20px"
  body-large:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: "24px"
  body-medium:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
  body-small:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: "16px"
  label-large:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "14px"
    fontWeight: 700
    lineHeight: "20px"
  label-medium:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "12px"
    fontWeight: 700
    lineHeight: "16px"
    letterSpacing: "0.4px"
  label-small:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "11px"
    fontWeight: 700
    lineHeight: "14px"
    letterSpacing: "0.8px"
rounded:
  extra-small: "4px"
  small: "8px"
  medium: "12px"
  large: "16px"
  extra-large: "24px"
  station: "9999px"
spacing:
  hairline: "1px"
  line-width: "3px"
  xs: "4px"
  s: "8px"
  sm: "12px"
  station-size: "12px"
  m: "16px"
  station-today: "16px"
  l: "24px"
  xl: "32px"
  xxl: "48px"
  route-row-height: "56px"
  avatar: "56px"
  image-size: "64px"
  chart-point: "8px"
  chart-height: "180px"
  content-max-width: "640px"
components:
  button-primary:
    backgroundColor: "{colors.line-routine}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    height: "48px"
  button-tonal:
    backgroundColor: "{colors.tonal}"
    textColor: "{colors.on-tonal}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    height: "48px"
  button-destructive:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.error}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    height: "48px"
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.medium}"
    padding: "{spacing.m}"
  sign-panel:
    backgroundColor: "{colors.signal}"
    textColor: "{colors.on-signal}"
    rounded: "{rounded.large}"
    padding: "{spacing.m}"
  sign-panel-label:
    textColor: "{colors.here}"
    typography: "{typography.label-small}"
  session-bar:
    backgroundColor: "{colors.signal}"
    textColor: "{colors.on-signal}"
    padding: "{spacing.m}"
  session-bar-button:
    backgroundColor: "{colors.line-routine}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.small}"
  route-row:
    textColor: "{colors.ink}"
    typography: "{typography.title-medium}"
    height: "{spacing.route-row-height}"
  data-strip-label:
    textColor: "{colors.ink-secondary}"
    typography: "{typography.label-medium}"
  data-strip-value:
    textColor: "{colors.ink}"
    typography: "{typography.title-large}"
  chip-muscle:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.line-routine}"
    typography: "{typography.label-large}"
    rounded: "{rounded.extra-small}"
  text-field:
    backgroundColor: "{colors.platform}"
    textColor: "{colors.ink}"
    typography: "{typography.body-large}"
    rounded: "{rounded.extra-small}"
  nav-bar:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.label-medium}"
  sheet:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.extra-large}"
  offline-banner:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.body-small}"
---

# Design System: AllFitness

> Registrado el 2026-10-30 desde lo construido en la rama `redesign/premium-ui` (código de `core/ui/theme`, `core/ui`, `navigation` y pantallas, más las capturas de `visual-audit/redesign/final/`). La dirección aprobada es la A, "Línea", de `docs/superpowers/specs/2026-10-30-rediseno-visual-design.md`. Donde el código y la spec no coinciden, manda el código y la diferencia se anota.
>
> Plataforma Android (Compose, Material 3): en el frontmatter, `px` equivale a `dp` en medidas y a `sp` en tipografía. Las claves sin sufijo son el tema claro y las que terminan en `-dark`, el oscuro. La fuente de verdad es `Color.kt`, `AppColors.kt`, `Type.kt`, `Shape.kt`, `Motion.kt` y `Theme.kt`.

## Overview

**Creative North Star: "La Línea"**

El plan es una ruta. La semana es una línea vertical con siete estaciones, hoy es "estás aquí" y cada sesión es un tramo que se llena. El sistema toma la gramática de la señalética de transporte (líneas de color que identifican, un panel oscuro con texto claro, puntos de estación y cifras grandes), pero no la marca de ningún sistema real. Es la forma más probada de que alguien con prisa, entre series y con una mano, sepa dónde está y qué sigue.

El aspecto premium sale de la exactitud, no de la decoración: cifras tabulares alineadas en columna, hilos de 1 dp, radios pequeños y un solo panel oscuro por pantalla. El color trabaja: tres colores de línea identifican Rutina, Dieta y Progreso, y el indicador de navegación toma el de la sección activa. La densidad es media y reglada: los datos van en tiras con etiqueta arriba y cifra abajo, nunca en prosa. La app es Material 3 en lo que es convención de Android (navegación, controles, hojas, diálogos); el mundo propio aporta la tipografía, la paleta, la densidad y el gesto de la línea con estaciones.

Claro es el tema principal y oscuro es de primera clase; los dos siguen al sistema. Se descartan dos clichés de la categoría: el negro OLED con neón "energético" y el bienestar pastel con beige y lavanda (el lavanda de M3 por defecto quedó eliminado al definir los 48 roles del esquema).

**Key Characteristics:**
- Línea vertical de 3 dp con estaciones de 12 dp (16 dp hoy) como gesto distintivo, reutilizado en semana, progreso, onboarding, entrada y estados.
- Señal: superficie oscura en los dos temas, única superficie "elevada", con el único amarillo de la pantalla.
- Tres colores de línea por sección; el texto solo usa sus tonos de texto validados.
- Archivo semicondensado con cifras tabulares para rótulos y números; Atkinson Hyperlegible Next para leer.
- Plano: sin sombras; separación por andén gris, tarjeta blanca e hilo de 1 dp.
- Movimiento corto que sigue la línea, con alternativa completa para "Quitar animaciones".

## Colors

Neutros fríos con un 2 % de verde, una Señal oscura como protagonista y tres colores de línea que identifican la sección (estrategia Committed).

### Primary
- **Verde Línea Rutina** (`line-routine`): color de Rutina y de la acción principal en toda la app (`primary` de M3, botón relleno, foco, estaciones de la semana, chip de grupo muscular). En oscuro sube de luminosidad (`line-routine-dark`). Su tinta encima es `on-primary`.

### Secondary
- **Naranja Línea Dieta** (`line-diet`): solo gráfico (indicador de pestaña de día, viñeta de ingrediente, segmento de carbohidratos, indicador de navegación en Dieta). Da 3,6:1 sobre blanco, insuficiente para texto.
- **Naranja Tostado de texto** (`line-diet-text`): la versión de Dieta para texto e íconos pequeños (pestaña de día activa, tipo de comida, números de paso, ícono y etiqueta de la pestaña). En oscuro coincide con `line-diet-dark`.
- **Azul Línea Progreso** (`line-progress`): Progreso, línea y puntos de la gráfica de peso (Vico), estaciones de la semana en Progreso.
- **Verde Tonal** (`tonal`, con tinta `on-tonal`): `secondaryContainer`, fondo del botón secundario. Se oscureció respecto a la spec para separarse de las hojas.

### Tertiary
- **Amarillo Estás Aquí** (`here`): solo el rótulo de estación de la Señal ("HOY · VIERNES 30 OCT"). Aparece como máximo una vez por pantalla y siempre sobre la Señal.
- **Trío de macros** (`macro-protein`, `macro-carbs`, `macro-fat`): azul, naranja y magenta, validados en pares con daltonismo. En claro coinciden con las líneas (proteína = Progreso, carbohidratos = Dieta); en oscuro tienen tonos propios dentro de la banda de luminosidad de datos. Siempre acompañan una etiqueta de texto.

### Neutral
- **Señal** (`signal`, tinta `on-signal`): verde casi negro en claro y verde profundo en oscuro. Es `inverseSurface` (y por eso el fondo del snackbar). En oscuro casi no contrasta con la superficie y lleva un hilo `signal-outline-dark`; en claro el hilo es transparente.
- **Andén** (`platform`): fondo de pantalla, barra superior, fila de pestañas y la tarjeta de ejercicio ya hecho.
- **Tarjeta** (`surface`): blanco puro en claro; tarjetas y campos.
- **Tinta** (`ink`) y **Tinta secundaria** (`ink-secondary`): texto principal y de apoyo, etiquetas de dato, unidades, aviso de IA, días pasados y de descanso.
- **Contorno** (`outline`): bordes de controles, estación pasada y de descanso, estado vacío.
- **Hilo** (`hairline`): `outlineVariant`, separadores de 1 dp, borde de tarjeta y reglas de la gráfica.
- **Contenedor** (`surface-container`): barra o rail de navegación. **Contenedor alto** (`surface-container-high`): hojas, aviso sin conexión y diálogos.
- **Error** (`error`, `error-container`): errores, acción destructiva (contorno y texto), estación de error, aviso de molestia en la hoja de alternativa.

El esquema M3 completo (48 roles, incluidos `surfaceContainerLowest…Highest`, `surfaceBright`, `surfaceDim` y los `*Fixed`) está definido a mano en `Color.kt`; los roles no listados aquí no tienen uso directo en pantallas y existen para que M3 no rellene con su paleta lavanda.

### Contraste fijado por tests

`ContrastTest.kt` exige estos mínimos en los dos temas; los valores son los calculados (WCAG 2.x) con los hex del frontmatter, claro / oscuro.

| Par | Mínimo | Claro / oscuro |
|---|---|---|
| Tinta sobre tarjeta | 4,5 | 18,0 / 14,8 |
| Tinta secundaria sobre andén | 4,5 | 7,1 / 9,0 |
| Línea Rutina (primary) sobre tarjeta | 4,5 | 5,4 / 8,0 |
| onPrimary sobre primary | 4,5 | 5,4 / 6,0 |
| Línea Rutina sobre andén (chip de grupo muscular en tarjeta hecha) | 4,5 | 4,9 / 8,6 |
| Dieta texto sobre tarjeta | 4,5 | 5,8 / 7,1 |
| Dieta gráfico sobre tarjeta | 3 | 3,6 / 7,1 |
| Línea Progreso sobre tarjeta | 4,5 | 5,9 / 8,2 |
| Macros sobre tarjeta (proteína, carbohidratos, grasas) | 3 | 5,9 · 3,6 · 4,9 / 5,4 · 5,6 · 4,4 |
| Tinta de Señal sobre Señal | 4,5 | 17,1 / 11,8 |
| `line-routine-dark` sobre Señal (en los dos temas) | 3 | 7,7 / 6,4 |
| Amarillo sobre Señal | 4,5 | 10,2 / 9,2 |
| Botón tonal (tinta sobre fondo) | 4,5 | 11,2 / 8,5 |
| Tonal frente a hoja (`surface-container-high`) | ΔE OKLab ≥ 4,5 | — |
| Ítem de navegación activo sobre la barra (las 4 secciones) | 4,5 | — |
| Ícono activo sobre el indicador al 16 % | 3 | — |
| Error sobre tarjeta | 4,5 | 5,6 / 10,4 |
| Contorno sobre tarjeta | 3 | 4,0 / 5,0 |
| Snackbar: texto y acción sobre la Señal | 4,5 | 17,1 · 7,7 / 11,8 · 6,4 |

### Named Rules
**The Here Once Rule.** El amarillo `here` aparece como máximo una vez por pantalla y solo sobre la Señal. La estación de hoy en la línea lleva halo del color de la línea, no amarillo.

**The Text Tone Rule.** El texto nunca lleva un color de serie gráfico. Se usa `lineTextFor(section)`: en Dieta es `line-diet-text`; en Rutina y Progreso el color de línea ya pasa 4,5:1. Las marcas de leyenda (punto de 8 dp en `DataStrip`) llevan el color de la serie y la etiqueta sigue en tinta secundaria.

**The Dark Signal Rule.** La Señal es oscura en los dos temas, así que todo lo que va encima en color de línea usa `DarkAppColors.lineRoutine` también en tema claro (`#0A7A50` sobre la Señal da 3,2:1).

**The Section Line Rule.** El color de línea solo identifica la sección o su acción principal. Perfil no tiene línea y usa la tinta.

## Typography

**Display Font:** Archivo variable, ancho 87,5 (semicondensado), pesos 600–800, cifras tabulares (`tnum`), empaquetada en `res/font`.
**Body Font:** Atkinson Hyperlegible Next variable, pesos 400 y 700, empaquetada en `res/font`.

**Character:** Archivo da la voz de rótulo de estación, precisa y algo condensada, con cifras que se alinean en columna; Atkinson, diseñada para baja visión, distingue 0/O e 1/l/I cuando se lee "10" o "12" a un brazo de distancia y le pone calidez a lo que se lee.

### Hierarchy
- **Wordmark** (Archivo 800, 40/44): "AllFitness" en `BrandHeader` (login y registro) y en la espera de IA a pantalla completa. Es `displaySmall` con peso ExtraBold, no un logo.
- **Display** (Archivo 700, 40/44; `displayLarge` 57 y `displayMedium` 45 definidos sin uso): la cifra protagonista de la pantalla (kcal del día, kilos actuales).
- **Headline** (Archivo 700, 32/38 · 28/34; Archivo 650, 24/30): `headlineLarge` es el título de pestaña en `AppTopBar` sin atrás; `headlineMedium` el tipo de día en la Señal; `headlineSmall` el mensaje de la espera de IA y los números de paso de receta.
- **Title** (Archivo 600, 20/26; Atkinson 700, 16/22 y 14/20): `titleLarge` es el título de sección (`SectionTitle`), de pantalla de detalle y el valor de `DataStrip`; `titleMedium` nombre de ejercicio, plato y día de la semana; `titleSmall` rótulo de grupo de formulario.
- **Body** (Atkinson 400, 16/24 · 14/20 · 12/16): `bodyLarge` para leer (pasos de receta, ingredientes, mensajes de estado), `bodyMedium` apoyo e instrucciones, `bodySmall` aviso de IA, unidades y sin conexión.
- **Label** (Atkinson 700; 14/20 · 12/16 con +0,4 · 11/14 con +0,8): `labelLarge` en botones; `labelMedium` etiqueta de dato ("SERIES", "PROTEÍNA"; mayúsculas desde `strings.xml`) y tipo de comida; `labelSmall`, en mayúsculas con `uppercase()`, solo para el rótulo de estación de la Señal.

### Named Rules
**The Tabular Figure Rule.** Toda cifra (series, reps, kg, kcal, "N de M") va en Archivo tabular. Fuera de los roles de Archivo se usa `numberStyle(size)`, que conserva el tamaño del rol y cambia a Archivo.

**The Label Above Rule.** Etiqueta pequeña arriba y cifra grande abajo. Nunca "Etiqueta: valor" en una línea.

**The One Hero Number Rule.** Una sola cifra en `displaySmall` por pantalla.

## Layout

Rejilla de 4 dp con escala `xs 4 · s 8 · sm 12 · m 16 · l 24 · xl 32 · xxl 48`, más medidas propias del mundo: `hairline` 1, `lineWidth` 3, estación 12 y 16, fila de ruta 56, imagen 64, avatar 56, gráfica 180 de alto y punto 8.

- Las listas de pantalla usan `contentPadding` de 16 dp; dentro de tarjeta, 16 dp; entre tarjetas, 12 dp.
- El contenido del `NavHost` se limita a 640 dp (`contentMaxWidth`) y se centra en ventanas anchas. No hay layouts de varios paneles.
- Una pestaña abre con `AppTopBar` (título `headlineLarge` sobre andén), `AiNote` justo debajo y luego el contenido. En Rutina el orden es título, aviso, Señal de hoy y "Tu semana".
- `DataStrip` reparte celdas iguales en una fila; si alguna no cabe entera (fuente al 130–200 %), pasa a filas parejas (2 + 2, no 3 + 1) en lugar de cortar palabras. La lista de comidas pone el tipo encima del plato cuando la escala de fuente es ≥ 1,5.
- Columnas de cifras (cantidades de ingredientes, números de paso, tipo de comida) se alinean midiendo el texto más ancho; nunca con anchos fijos.

### Navegación y adaptación
- `NavigationSuiteScaffold` con 4 destinos (Rutina, Dieta, Progreso, Perfil): barra inferior en teléfono y rail en ventanas anchas (verificado en tableta simulada). El teléfono en apaisado conserva la barra (altura compacta, comportamiento de la librería). En pantallas de detalle la suite se oculta (`NavigationSuiteType.None`).
- El contenedor es `surface-container` sin hilo superior. El indicador del ítem activo es el color de línea de la sección al 16 %; ícono y etiqueta activos en `lineTextFor(section)`. Perfil usa la tinta. Íconos y etiquetas siempre visibles.
- Dieta abre en el día de hoy con `SecondaryScrollableTabRow` de pestañas de 72 dp (más estrechas que las 90 por defecto, para que asomen días cortados), indicador en `line-diet` y texto activo en `line-diet-text`.

### Movimiento
Tokens en `Motion.kt`: `SHORT` 150 ms, `MEDIUM` 300 ms, `LONG` 450 ms, easing `Emphasized` = cubic-bezier(0.2, 0, 0, 1), y `gentle()` = spring con amortiguación 0,9 y rigidez media-baja (sin rebote).

| Caso | Con movimiento | Con movimiento reducido |
|---|---|---|
| Entre pestañas | sale fundido 90 ms; entra fundido 210 ms con retardo 90 y escala desde 0,96 | fundido 150 ms |
| Lista → detalle (semana → día, dieta → receta) y vuelta | desplazamiento horizontal del 10 % + fundido, 300 ms enfatizado, inverso al volver | fundido 150 ms |
| Cargando / vacío / error / contenido (`StateContent`) | fundido lineal 150 ms solo al cambiar de tipo de estado | sin transición |
| Aviso sin conexión | `expandVertically` + fundido | instantáneo |
| Pulsar botón tonal o fila de ruta | escala 0,97 con `gentle()`; en `RouteRow` solo se encoge el texto, la línea sigue continua | solo ripple |
| Marcar ejercicio | color de tarjeta a andén + háptico ToggleOn/ToggleOff | cambio inmediato y háptico |
| Tramo de la `SessionBar` | 450 ms enfatizado | `snap()` |
| Terminar la sesión | pulso único de la estación 1 → 1,15 → 1 en 300 ms, solo al pasar a terminada | sin pulso |
| Entrada de lista | primeros 6 ítems, escalón 30 ms, fundido + 8 dp hacia arriba, una vez por pantalla (330 ms en total) | desactivada |
| Carga (`LineIndicator`) | tramo de un tercio del ancho que recorre la línea, 1,2 s en bucle | `LinearProgressIndicator` indeterminado de M3 |

**The Reduced Motion Rule.** Movimiento reducido = `ANIMATOR_DURATION_SCALE` igual a 0 ("Quitar animaciones"); se relee al volver a la app y se expone como `AppTheme.reducedMotion`. Todo componente animado tiene rama reducida; solo los indicadores de carga siguen moviéndose. Nada anima sin un cambio de estado, no hay parallax, ni rebote, ni bucles salvo la espera.

## Elevation & Depth

Sistema plano. Ninguna superficie de contenido lleva sombra. La profundidad tiene tres niveles fijos: andén gris (`platform`) → tarjeta blanca (`surface`) con hilo de 1 dp (`hairline`) → Señal (`signal`). La Señal es la única superficie "elevada" y lo es por tono, no por sombra; en oscuro se separa con su hilo `signal-outline-dark`. Hojas y diálogos usan `surface-container-high` (tonal M3, no sombra), y la barra de navegación `surface-container`.

**The No Shadow Rule.** No se añaden sombras ni elevación tonal a tarjetas. Si una superficie necesita separarse, se usa el hilo de 1 dp o el siguiente nivel de la escala andén → tarjeta → Señal; no hay otros niveles.

## Shapes

Lenguaje de cartel: rectángulos de radio pequeño y preciso; los círculos quedan para estaciones, marcadores de serie y puntos de gráfica.

- 4 dp (`extra-small`): chip de grupo muscular, campos de texto (por defecto de M3).
- 8 dp (`small`): botones (primario, tonal, destructivo, botón de la barra de sesión), segmentos, imagen de ejercicio y área táctil del checkbox. Los botones no son píldora.
- 12 dp (`medium`): tarjetas (`AppCard`).
- 16 dp (`large`): Señal (`SignPanel`, `BrandHeader`).
- 24 dp (`extra-large`): diálogos y esquinas superiores de las hojas (por defecto de M3).
- Círculo: estaciones, viñeta de ingrediente, marca de leyenda y punto de la gráfica.
- Línea: trazo de 3 dp (`lineWidth`) sin remates, que se detiene justo en el borde de cada estación.

## Components

### Buttons
- **Shape:** rectángulo de 8 dp, altura mínima de 48 dp.
- **Primary:** relleno en `line-routine` con `on-primary` y `labelLarge` ("Ver sesión de hoy", enviar formularios, guardar). Dentro de la Señal hereda el mismo verde del tema; en la `SessionBar` el contenedor es el `primary` del tema y, deshabilitado, usa la tinta de Señal al 12 % / 38 % porque los colores por defecto son para fondo claro.
- **Tonal (`TonalActionButton`):** acción secundaria en `tonal` con `on-tonal`, opcionalmente con ícono de 18 dp. Encoge a 0,97 al pulsar. Es el único estilo secundario: "Regenerar", "Editar", "Mantener la actual", "Cerrar sesión" y acciones de estado vacío o error.
- **Alternativa (`AlternativeButton`):** "Pedir alternativa" siempre es el mismo botón tonal con el ícono de desvío `alt_route`, en ejercicio, receta, rutina y dieta.
- **Destructive:** contorno de 1 dp y texto en `error` ("Eliminar cuenta").
- **Text:** `TextButton` de M3 solo en diálogos (incluidas las opciones de regenerar plan) y enlaces legales.

### Chips
- **Grupo muscular:** contorno de 1 dp y texto en `line-routine`, radio 4 dp, fondo igual al de la tarjeta (también sobre la tarjeta hecha, en andén).
- **Opción (`SingleChoiceChips`, `MultiChoiceChips`):** `FilterChip` de M3 con check delante al seleccionarse; la opción única va en `selectableGroup()`.
- **Conteos cortos (`SegmentedChoice`):** días por semana y comidas como segmentos iguales en una fila, con radio de 8 dp en los extremos y sin check.

### Cards / Containers
- **Corner Style:** 12 dp.
- **Background:** `surface`; la tarjeta de ejercicio hecho pasa a `platform`.
- **Shadow Strategy:** ninguna (ver Elevation & Depth).
- **Border:** hilo de 1 dp en `hairline`.
- **Internal Padding:** 16 dp. Las listas dentro de tarjeta (comidas, historial de peso) son filas regladas separadas por hilos, con 56 dp de alto mínimo si se pueden tocar.

### Inputs / Fields
- **Style:** `OutlinedTextField` de M3 a lo ancho, con etiqueta flotante.
- **Unidad:** siempre visible a la derecha como `trailingIcon` con 12 dp de margen ("cm", "kg", "años"), nunca como `suffix`.
- **Apoyo:** el rango va como texto de apoyo ("Entre 16 y 100 años"); el contador "0/N" solo en texto libre, nunca en campos numéricos. Lo pegado también se recorta al máximo.
- **Error:** el mensaje de error de `strings.xml` reemplaza al apoyo; los grupos de chips y segmentos muestran el error en `bodySmall` color `error`.

### Navigation
- Ver Layout. Barra o rail de M3 en `surface-container`, indicador del color de la sección al 16 %, ícono y etiqueta activos en `lineTextFor`, inactivos en tinta secundaria. Íconos Material Symbols Rounded (peso 400) como vector drawables.
- `AppTopBar`: título `headlineLarge` en pestañas y `titleLarge` con flecha atrás en detalle, sobre andén, marcado como `heading()`.

### Station vocabulary (`StationState`, `RouteStation`)
La estación es la unidad del mundo. Se dibuja en Canvas con trazo de 2 dp:

| Estado | Forma | Uso |
|---|---|---|
| `PENDING` | anillo de 12 dp en color de línea | entreno por hacer (hoy o futuro) |
| `PAST` | anillo de 12 dp en `outline` | entreno que ya pasó sin terminarse: se apaga, no se tacha |
| `DONE` | círculo lleno de 12 dp con check de 8 dp en color de tarjeta | entreno terminado |
| `REST` | anillo fino de 8 dp en `outline` | descanso |
| `TODAY` | anillo de 16 dp con halo del color de línea al 24 % (24 dp en total) | hoy, sin terminar |
| `TODAY_DONE` | círculo lleno de 16 dp con halo al 24 % y check de 12 dp | hoy, terminado |

El texto de la fila de `PAST` y `REST` va en tinta secundaria. El estado se expone a TalkBack como `stateDescription` ("Hoy", "Terminada"), porque visualmente solo lo dice la estación. Una estación hueca en `outline` es la ilustración del estado vacío y una llena en `error`, la del estado de error.

### Signature components
- **`RouteRow` (línea de la semana):** fila tocable completa de 56 dp mínimo: carril de 32 dp con la estación sobre una línea vertical continua de 3 dp, día en `titleMedium` y tipo en `bodyMedium`. La línea se corta en la primera y la última estación.
- **`SignPanel` (Señal de hoy):** superficie Señal de 16 dp de radio y 16 dp de relleno: rótulo de estación en amarillo `labelSmall` mayúsculas (con `contentDescription` en minúsculas para que TalkBack no deletree "OCT"), tipo de día `headlineMedium` como `heading()`, estado en `bodyMedium` y CTA primario a lo ancho. En descanso no lleva CTA.
- **`SessionBar` (sesión de hoy):** barra fija inferior sobre la Señal: tramo de 3 dp que se llena en `line-routine-dark` sobre la tinta de Señal al 24 %, "N de M" en `titleLarge` y "Terminar sesión de hoy" siempre a la vista (FR-014). Al terminar se reemplaza por la estación `TODAY_DONE` con pulso único y el mensaje, anunciado con `liveRegion`.
- **`StationProgress`:** fila de N estaciones unidas por tramos de 3 dp; las alcanzadas van llenas y los tramos pendientes al 24 %. Se usa en el onboarding (pasos), en "Esta semana" de Progreso (sesiones) y en `BrandHeader` (3 de 3, sobre la Señal en `line-routine-dark`).
- **`BrandHeader`:** Señal redondeada dentro del margen en login y registro, con el wordmark y una línea de 3 estaciones llenas.
- **`DataStrip`:** fila reglada de 2 a 4 celdas separadas por hilos verticales: etiqueta `labelMedium` en tinta secundaria arriba (con marca de color opcional para leyendas), valor `titleLarge` tabular y unidad `bodySmall` en la misma línea base. Sustituye toda prosa de datos (series, reps, descanso, peso, macros, tiempo, resumen de progreso).
- **`AiNote`:** el aviso completo de IA (FR-037) justo debajo del título: ícono de info de 16 dp, `bodySmall` en tinta secundaria, sin caja y cerrado por un hilo a todo el ancho.
- **`AiGeneratingState`:** espera de la IA sobre la Señal: mensaje `headlineSmall` con `liveRegion`, `LineIndicator` en `line-routine-dark` y etapas orientativas con estación hueca (ninguna se marca hecha, porque no hay progreso real). Con `fullBleed` (primer plan) llega bajo las barras del sistema con íconos claros, añade el wordmark y un resumen etiqueta/valor de lo elegido.
- **Estados (`StateContent`):** un único componente para cargando (`LineIndicator` a media anchura), vacío (estación hueca + mensaje + acción tonal), error (estación de error + "Reintentar") y contenido con `OfflineBanner` encima cuando los datos son guardados.
- **`OfflineBanner`:** franja a todo el ancho en `surface-container-high` con ícono de nube tachada de 16 dp y la fecha del guardado en `bodySmall` tinta secundaria.
- **Tarjeta de ejercicio:** checkbox (solo hoy) o estación de solo lectura en otros días, nombre `titleMedium`, chip de grupo muscular, `DataStrip`, imagen de 64 dp a la derecha si llega, instrucciones `bodyMedium` y `AlternativeButton`. Hecho: fondo andén, nombre en tinta secundaria.
- **Resumen de dieta:** kcal en `displaySmall` con "KCAL" `labelMedium` en la línea base, barra apilada de macros (proporción recibida del servidor) y `DataStrip` con leyenda. Las comidas van en una sola tarjeta de filas regladas: tipo en `line-diet-text`, plato `titleMedium`, kcal tabular a la derecha en la misma línea base.
- **Gráfica de peso (Vico):** 180 dp de alto, línea y puntos de 8 dp en `line-progress`, ejes y guías en `hairline`. Con menos de 2 registros se muestra un estado propio con una estación pendiente en lugar de un punto suelto.
- **Hoja de alternativa:** `ModalBottomSheet` en `surface-container-high`, motivos como filas de radio de 56 dp, aviso de molestia en `error-container`, "Usar esta" primario y "Mantener la actual" tonal.

## Do's and Don'ts

### Do:
- **Do** tomar todo color de `MaterialTheme.colorScheme` o `AppTheme.colors`; los hex solo existen en `core/ui/theme`.
- **Do** usar `lineTextFor(section)` para texto e íconos pequeños en color de sección, y `lineFor(section)` solo para trazos, indicadores y marcas.
- **Do** usar `DarkAppColors.lineRoutine` para cualquier línea, tramo o estación sobre la Señal, en los dos temas.
- **Do** poner todo texto visible en `strings.xml`, en español; las etiquetas de dato en mayúsculas se escriben así en el recurso.
- **Do** marcar con `semantics { heading() }` los títulos de pantalla, de sección (`SectionTitle`) y el tipo de día de la Señal.
- **Do** anunciar con `liveRegion = Polite` las esperas de IA, los mensajes de carga y la sesión terminada (solo al terminarla, no al abrir una ya terminada).
- **Do** dar 48 dp o más a todo objetivo táctil y hacer tocable la fila completa (`RouteRow`, `MealRow`, radios de la hoja: 56 dp).
- **Do** comprobar cada pantalla al 130 % y al 200 % de fuente: las tiras pasan a varias filas y las columnas se miden, nunca se recortan.
- **Do** dar a cada animación su rama de movimiento reducido leyendo `AppTheme.reducedMotion`.
- **Do** poner `contentDescription = null` en íconos decorativos junto a texto y texto descriptivo en los que tienen significado propio.
- **Do** mostrar los datos en `DataStrip` y las cifras en Archivo tabular.
- **Do** usar el tonal para toda acción secundaria y `AlternativeButton` para "Pedir alternativa".

### Don't:
- **Don't** escribir texto en `line-diet` ni en el color de una serie de datos: no llega a 4,5:1.
- **Don't** usar el amarillo `here` fuera del rótulo de la Señal ni más de una vez por pantalla; tampoco en la estación de hoy.
- **Don't** usar `labelSmall` en mayúsculas como sobretítulo de sección o de tarjeta: es el rótulo de estación de la Señal y lleva un dato (día y fecha), no un adorno.
- **Don't** pintar el verde claro `#0A7A50` sobre la Señal (3,2:1).
- **Don't** añadir sombras, degradados, blur ni niveles de superficie fuera de andén → tarjeta → Señal.
- **Don't** usar botones píldora ni radios mayores de 16 dp en contenido; los círculos son solo para estaciones y marcadores.
- **Don't** escribir datos como "Etiqueta: valor" en una sola línea.
- **Don't** dejar roles de color de M3 sin definir: reaparece el lavanda.
- **Don't** mostrar checkbox en días que no son hoy: van con estación de solo lectura y el motivo como etiqueta de estado.
- **Don't** marcar como hechas las etapas de la espera de IA ni prometer tiempos.
- **Don't** usar texto visible fuera de `strings.xml` ni hex o `dp` sueltos en pantallas.

## Desviaciones registradas

Código frente a la spec (§4 y §14 de `docs/superpowers/specs/2026-10-30-rediseno-visual-design.md`); manda el código.

- Las de la tabla §14 se confirman en el código: tonal `#C6E3D2` / `#2A4234`, grasas en magenta con trío propio en oscuro, halo de hoy en color de línea, tramo de sesión en `line-routine-dark`, sesión terminada en la barra, días no-hoy sin checkbox, barra en `surface-container`, rail solo en ventanas anchas, marca de entrada redondeada, puntos de 8 dp.
- Además del §14: los chips de opción usan el `FilterChip` de M3 sin el relleno de Línea Rutina al 12 % ni el borde de 1 dp de la spec (§4.8.8); el margen de pantalla es 16 dp también en ventanas ≥ 600 dp (la spec pedía 20); la espera de IA usa `LineIndicator` sin `AnimatedContent` del texto de etapa (§6); `StateContent` funde en 150 ms, no 200 (§6); Archivo lleva `tnum` también en headline y `titleLarge`, no solo en display.
- Algunas razones de contraste de la tabla 4.3 de la spec no coinciden con el cálculo sobre los hex vigentes (tinta en oscuro 14,8, no 15,8; tinta secundaria 7,1 / 9,0 sobre andén; amarillo en oscuro 9,2). Los tests fijan mínimos, no valores, y todos se cumplen.
