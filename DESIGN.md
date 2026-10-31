---
name: AllFitness
description: Plan semanal de entreno y dieta generado por IA, en el tema oscuro con acento verde del prototipo de Figma.
colors:
  primary: "#2ECC71"
  on-primary: "#11111E"
  primary-container: "#142728"
  on-primary-container: "#2ECC71"
  secondary-container: "#3A3B52"
  on-secondary-container: "#FFFFFF"
  background: "#11111E"
  surface: "#1C1C2C"
  surface-container-low: "#161626"
  surface-container-high: "#25253A"
  surface-container-highest: "#2A2A3E"
  surface-input: "#121B2A"
  on-surface: "#FFFFFF"
  on-surface-variant: "#9FA2B4"
  outline: "#6B6F86"
  outline-variant: "#2A2A3E"
  muted: "#5F6175"
  error: "#FF5F57"
  error-container: "#2E1A25"
  accent: "#FFB300"
  accent-container: "#2E241A"
  macro-protein: "#22A85E"
  macro-carbs: "#5B8DEF"
  macro-fat: "#E8607A"
  inverse-surface: "#E6E7F0"
  inverse-primary: "#11703C"
typography:
  display-small:
    fontFamily: "Outfit"
    fontSize: "32px"
    fontWeight: 800
    lineHeight: "40px"
    fontFeature: "tnum"
  headline-medium:
    fontFamily: "Outfit"
    fontSize: "28px"
    fontWeight: 800
    lineHeight: "35px"
    fontFeature: "tnum"
  headline-small:
    fontFamily: "Outfit"
    fontSize: "24px"
    fontWeight: 800
    lineHeight: "30px"
    fontFeature: "tnum"
  title-large:
    fontFamily: "Outfit"
    fontSize: "20px"
    fontWeight: 800
    lineHeight: "26px"
    fontFeature: "tnum"
  title-medium:
    fontFamily: "Outfit"
    fontSize: "16px"
    fontWeight: 700
    lineHeight: "20px"
    fontFeature: "tnum"
  title-small:
    fontFamily: "Outfit"
    fontSize: "15px"
    fontWeight: 700
    lineHeight: "19px"
    fontFeature: "tnum"
  body-large:
    fontFamily: "Manrope"
    fontSize: "15px"
    fontWeight: 500
    lineHeight: "21px"
  body-medium:
    fontFamily: "Manrope"
    fontSize: "14px"
    fontWeight: 500
    lineHeight: "19px"
  body-small:
    fontFamily: "Manrope"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: "16px"
  label-large:
    fontFamily: "Manrope"
    fontSize: "14px"
    fontWeight: 700
    lineHeight: "19px"
  label-medium:
    fontFamily: "Manrope"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: "16px"
  label-small:
    fontFamily: "Manrope"
    fontSize: "10px"
    fontWeight: 700
    lineHeight: "14px"
rounded:
  extra-small: "6px"
  small: "8px"
  medium: "12px"
  large: "16px"
  extra-large: "20px"
  pill: "9999px"
spacing:
  hairline: "1px"
  xs: "4px"
  s: "8px"
  sm: "12px"
  m: "16px"
  l: "24px"
  screen-edge: "24px"
  xl: "32px"
  xxl: "48px"
  button-height: "56px"
  list-row: "56px"
  progress-height: "6px"
  ring: "60px"
  ring-large: "72px"
  icon-tile: "36px"
  thumb-meal: "64px"
  thumb-exercise: "76px"
  chart-height: "180px"
  content-max-width: "640px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.pill}"
    height: "{spacing.button-height}"
  button-secondary:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "{spacing.button-height}"
  button-tonal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.large}"
    padding: "{spacing.m}"
  card-highlighted:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.extra-large}"
  text-field:
    backgroundColor: "{colors.surface-input}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    rounded: "{rounded.medium}"
  chip:
    backgroundColor: "{colors.surface-input}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
  chip-selected:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.primary}"
    rounded: "{rounded.pill}"
  nav-bar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-small}"
  sheet:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.extra-large}"
  offline-banner:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.body-small}"
---

# Design System: AllFitness

Fuente: el código que se entrega (`app/src/main/java/com/allfitness/app/core/ui/theme/` y `core/ui/`), no las intenciones. Autoridad de valores: `docs/superpowers/specs/2026-10-30-interfaz-figma-design.md` y el árbol de nodos `docs/design/figma/arbol-nodos.txt`.

## Overview

AllFitness traduce a Material 3 el prototipo de Figma `All Fitness_prototype.fig` (7 pantallas, tema oscuro, acento verde). Hay un solo tema, oscuro (FR-035c): no sigue al sistema y no hay selector. La interfaz es Operate: la persona abre la app para saber qué entrenar y qué comer hoy, marca ejercicios con una mano y lee recetas mientras cocina.

El carácter sale de tres cosas: un fondo azul noche casi negro con tarjetas un paso más claras y un hilo de 1 dp, un único verde (#2ECC71) que marca lo que se puede hacer o lo que está hecho, y las cifras en Outfit ExtraBold con números tabulares. Lo demás es Material 3 tematizado: barra inferior, hojas, diálogos, campos y chips.

Del Figma no se construye lo que no existe en el producto (§3 de la spec): kcal consumidas, agua, temporizador, registro por serie, notificaciones, unidades, fotos de comida o de ejercicio inventadas, favoritos. Donde el Figma pone una foto, la app pone una placa con ícono o, en ejercicios, la imagen que envía el backend.

## Colors

Un solo esquema (`Color.kt`), con los 48 roles definidos con el constructor completo para que M3 no rellene ninguno con su paleta de referencia (de ahí venía el lavanda del MVP).

### Primary
- **Verde** `primary` #2ECC71: botón principal, arco de anillos, barra de progreso, línea de la gráfica de peso, ítem activo de la barra inferior, selección y foco. Es el único acento de acción.
- `on-primary` #11111E: texto sobre el verde (8,9:1).
- `primary-container` #142728: el verde al 12 % sobre el fondo, precalculado como hex opaco. Fondo de chip seleccionado, `AiBadge` y etiqueta de grupo muscular.

### Secondary
- `secondary-container` #3A3B52 con `on-secondary-container` blanco: botones tonales (`TonalActionButton`, "Mantener la actual", "Regenerar").

### Neutral
- `background` #11111E: fondo de pantalla.
- `surface` #1C1C2C: `AppCard`, barra inferior.
- `surface-container-high` #25253A: hojas, botón secundario, placas de ícono, pista de anillos y barras.
- `surface-input` #121B2A (`AppColors`): campos y chips sin seleccionar.
- `on-surface` #FFFFFF y `on-surface-variant` #9FA2B4: texto principal y secundario (≥ 4,5:1 sobre las cuatro superficies).
- `outline` #6B6F86: borde de campos, chips y píldoras deshabilitadas (≥ 3:1). `outline-variant` #2A2A3E: hilos de tarjeta y separadores.
- `muted` #5F6175: solo decorativo (viñetas, puntos). Nunca texto.

### Colores sin rol M3 (`AppColors`)
- **Ámbar** `accent` #FFB300 sobre `accent-container` #2E241A: solo la racha.
- **Macros** proteína #22A85E, carbohidratos #5B8DEF, grasas #E8607A: solo gráficos (barra apilada, punto de leyenda). El texto de un macro nunca va en su color.
- `error` #FF5F57: mensajes de error, aviso sin conexión bajo un control y la acción destructiva.

### Contraste fijado por tests (`ContrastTest`)
- Texto `on-surface` y `on-surface-variant` ≥ 4,5:1 sobre `background`, `surface`, `surface-container-high` y `surface-input`.
- `primary` como texto ≥ 4,5:1 sobre `background` y `surface`; `on-primary` sobre `primary`; `on-secondary-container` sobre `secondary-container`; `on-primary-container` sobre `primary-container`; `accent` sobre `accent-container`; `error` sobre `surface`; snackbar (`inverse*`).
- Gráficos ≥ 3:1: macros y `primary` sobre `surface` y `surface-container-high`; `outline` sobre `surface`, `surface-input` y `surface-container-high`.
- La pista de anillos y barras (`surface-container-high` sobre `surface`, 1,12:1) es decorativa: el valor lo lleva el arco contra ella.

### Named Rules
- **Un verde.** No hay color por sección. El verde dice "esto se hace" o "esto está hecho".
- **El ámbar es la racha.** No se usa para nada más.
- **Sin hex fuera del tema.** Ninguna pantalla escribe `Color(0x…)`.

## Typography

Dos familias variables empaquetadas en `res/font`, con el eje de peso fijado por instancia (`Type.kt`):
- **Outfit** (700, 800) para títulos y cifras, siempre con `tnum`: series, kg y kcal se alinean en columna.
- **Manrope** (400–700) para lectura, etiquetas y controles.

### Hierarchy
| Rol | Familia | Tamaño / línea | Uso |
|---|---|---|---|
| displaySmall | Outfit 800 | 32/40 | Cifra protagonista (kcal del día, peso) |
| headlineMedium | Outfit 800 | 28/35 | Título de pantalla ("Tu dieta", "Hola, …") |
| headlineSmall | Outfit 800 | 24/30 | Nombre del ejercicio en sesión, título de paso |
| titleLarge | Outfit 800 | 20/26 | Títulos de sección, logo "AllFitness" |
| titleMedium | Outfit 700 | 16/20 | Botón principal, nombre de comida, cifras de tarjeta |
| titleSmall | Outfit 700 | 15/19 | Etiquetas de grupo de formulario, cifras pequeñas |
| bodyLarge / bodyMedium | Manrope 500 | 15/21, 14/19 | Lectura |
| bodySmall | Manrope 400 | 12/16 | Aviso de IA, ayudas, aviso sin conexión |
| labelLarge / labelMedium | Manrope 700 / 600 | 14/19, 12/16 | Botones secundarios, chips, rótulos |
| labelSmall | Manrope 700 | 10/14 | Rótulos en mayúsculas (`uppercase()` al usarlo), barra inferior |

### Named Rules
- **Todo en `sp` desde el tema.** No hay `N.sp` fuera de `core/ui/theme`; la única excepción con nombre es `NavLabelMinSize` (6 sp), el suelo del auto-tamaño de la etiqueta de la barra inferior al 200 %.
- **Cifras con `numberStyle`.** Outfit tabular para cualquier número que se compara.

## Layout

- Margen de pantalla 24 dp (`Spacing.screenEdge`), escala 4/8/12/16/24/32/48.
- Tarjetas a lo ancho con 16 dp de relleno y 12–16 dp entre ellas.
- Ancho máximo de lectura 640 dp centrado en pantallas grandes.
- Alto mínimo, nunca fijo: con la fuente al 130 % y al 200 % las tarjetas y los botones crecen. Las tiras de datos (`DataStrip`, `stripColumns`) miden la etiqueta más larga y pasan de 4 a 2 o 1 columnas antes de cortar.

### Navegación y adaptación
- `NavigationSuiteScaffold`: barra inferior en compacto y rail en ancho, ambas en `surface`, sin píldora indicadora: el ícono y la etiqueta activos van en verde. Hilo `outline-variant` sobre la barra.
- Cinco destinos: Inicio, Rutina, Dieta, Progreso y Perfil. Íconos Lucide como vector drawables (`lc_*`), trazo 2.
- Los detalles (día de rutina, receta, sección de perfil, eliminar cuenta) ocultan la barra y llevan flecha atrás; el atrás del sistema siempre funciona.

### Movimiento (`Motion.kt`)
- Duraciones 150 / 300 / 450 ms y easing enfatizado `cubic-bezier(0.2, 0, 0, 1)`.
- Entre pestañas, fade through: entra en 210 ms tras 90 ms (`FADE_THROUGH_IN/OUT`) con escala desde 0,96. Hacia un detalle, desplazamiento lateral del 10 % con fundido (300 ms).
- Indicador de carga: un tramo que recorre el ancho en 1200 ms (`LOADER_CYCLE`).
- `pressScale` encoge a 0,97 al pulsar con un resorte suave; anillos y barras se llenan en 450 ms con el easing enfatizado.
- Con `ANIMATOR_DURATION_SCALE == 0` solo quedan fundidos de 150 ms y la carga.

## Elevation & Depth

Profundidad tonal, sin sombras: `background` → `surface` (tarjetas) → `surface-container-high` (hojas, placas). La única sombra es el brillo verde del botón principal habilitado (`primaryGlow`, 12 dp, color `primary`), una vez por pantalla.

## Shapes

`AppShapes`: 6 dp etiquetas, 8 dp controles, 12 dp campos y miniaturas, 16 dp tarjetas, 20 dp tarjeta destacada y hojas. Botones y chips son píldoras (`PillShape`).

## Components

### Buttons (`Buttons.kt`)
- **PrimaryButton:** píldora verde de 56 dp mínimo, `titleMedium`, brillo verde, ícono opcional detrás. Una por pantalla.
- **SecondaryButton:** la misma píldora en `surface-container-high` con texto blanco ("Anterior", "Saltar").
- **Deshabilitado:** contenedor `surface-container-high`, texto `on-surface-variant` (legible, 5,9:1) y un hilo `outline`, para que la píldora no desaparezca dentro de una hoja del mismo color.
- **TonalActionButton:** píldora `secondary-container` con ícono delante, para acciones secundarias que no deben leerse como deshabilitadas.
- **AlternativeButton:** "Pedir alternativa" con el ícono de desvío, en ejercicio y receta.
- **Destructivo:** "Eliminar mi cuenta", píldora en `error`; deshabilitado sigue rojo al 12 % / 38 %.

### Cards (`Components.kt`, `Cards.kt`)
- **AppCard:** `surface`, radio 16, hilo `outline-variant`; destacada: radio 20 e hilo verde.
- **StatCard:** rótulo, cifra Outfit y unidad (Perfil).
- **ChoiceCard:** opción grande con placa de ícono (objetivo del onboarding), seleccionada con hilo y placa verdes.
- **PreferenceRow:** fila de 56 dp con ícono, rótulo y valor (Perfil).
- **IconTile:** placa `surface-container-high` de 36 dp con ícono verde; también miniatura de comida (64 dp) y de ejercicio (76 dp) cuando no hay imagen.
- **MealCard:** placa por tipo de comida, tipo en verde, nombre, kcal y minutos de preparación.

### Progreso (`Progress.kt`)
- **ProgressRing:** anillo de trazo 6 dp, pista `surface-container-high`, arco verde desde las 12 (60 y 72 dp).
- **LinearProgress:** píldora de 6 dp ("Ejercicio N de M").
- **SegmentedProgress:** segmentos iguales con hueco de 6 dp (pasos del onboarding, nivel).

### Datos
- **DataStrip:** rótulo `labelMedium` en mayúsculas arriba, cifra Outfit abajo, columnas según el ancho.
- **MacroBar:** barra apilada de 8 dp con hueco de 2 dp entre macros; la `DataStrip` de debajo es su leyenda.

### Inputs (`FormFields.kt`)
- **FormTextField:** rótulo `titleSmall` arriba, campo `surface-input` de radio 12 con borde `outline` (verde con foco, rojo con error), unidad a la derecha, rango o contador como apoyo.
- **SingleChoiceChips / MultiChoiceChips:** píldoras de 33 dp visuales (48 táctiles), seleccionadas en `primary-container` con texto y borde verdes.
- **SegmentedChoice:** conteos cortos (días por semana, comidas) como segmentos iguales.

### Marca (`Brand.kt`)
- **LogoMark:** placa verde redondeada con el rayo (`lc_zap`) y "AllFitness" en `titleLarge` (28 dp; 48 dp en la entrada).
- **Avatar:** inicial del nombre en un círculo con anillo verde (no hay foto de perfil).
- **AiBadge:** "Plan de IA" con destellos, en `primary-container`.

### Estados y avisos
- **StateContent:** cargando, contenido, vacío y error con reintentar, con fundido entre estados.
- **AiGeneratingState:** espera de la IA a pantalla completa con logo, etapas y un anillo por etapa.
- **AiNote:** el aviso de IA completo, `bodySmall` secundario con ícono de información, bajo la cabecera y separado por un hilo.
- **OfflineBanner:** franja `surface-container-high` con la fecha de los datos guardados.
- **OfflineHint:** "Necesitas conexión…" en `error`, siempre debajo del control deshabilitado por falta de red.
- **Hojas:** `ModalBottomSheet` en `surface-container-high` para pedir alternativa y regenerar.

## Do's and Don'ts

- **Do** sacar todo color, medida, tipo y duración de `core/ui/theme`.
- **Do** dejar un solo `PrimaryButton` por pantalla y poner el aviso sin conexión bajo el control que deshabilita.
- **Do** mostrar el dato real o nada: placa con ícono en lugar de foto, kcal del plan en lugar de kcal consumidas.
- **Don't** usar el ámbar o los colores de macro para texto o acciones.
- **Don't** fijar altos de tarjetas o botones: al 200 % crecen.
- **Don't** reintroducir colores por sección, panel oscuro "Señal" ni líneas con estaciones: eran el rediseño "Línea" y se retiraron.
