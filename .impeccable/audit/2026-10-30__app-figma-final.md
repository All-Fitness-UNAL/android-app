---
target: app completa (mockDebug, interfaz Figma, rama redesign/figma-ui)
total_score: 16
max_score: 20
p0_count: 0
p1_count: 0
timestamp: 2026-10-30
---
Método: auditoría nativa desde el código (Compose) más recorrido en el emulador (API 37, 1080×2424).
Evidencia: `visual-audit/figma/final/flujo/` (quickstart 1–8) y `visual-audit/figma/final/{100,130,200}/` (12 pantallas por escala).
Antes: 13/20 (MVP), 17/20 (Línea, tema claro + oscuro).

## Audit Health Score

| # | Dimensión | Nota | Hallazgo clave |
|---|---|---|---|
| 1 | Accesibilidad | 3 | Textos de componentes M3 en el idioma del sistema: "Close sheet", "Drag handle" y el DatePicker en inglés con el teléfono en inglés |
| 2 | Rendimiento | 3 | Listas largas en LazyColumn; varias pantallas cortas en Column + verticalScroll (13), sin coste visible |
| 3 | Apariencia y tema | 4 | 48 roles definidos, 0 hex fuera de `core/ui/theme`, sin `tween(N)` ni `N.sp` sueltos; contraste fijado en `ContrastTest` |
| 4 | Conformidad con la plataforma | 3 | Material 3 tematizado; la tira de días de Dieta es una ScrollableTabRow que corta "Mar" en el borde al abrir |
| 5 | Adaptación | 3 | NavigationSuiteScaffold (barra/rail) y ancho máximo 640 dp; apaisado y tableta no se recorrieron en esta pasada |
| **Total** | | **16/20** | **Bueno** |

## Veredicto de conformidad
Pasa: se lee como app Android nativa. Barra inferior M3 con indicador, atrás del sistema y predictivo, hojas M3, diálogos M3, snackbars, edge-to-edge con barras transparentes y la IME respetada (`imePadding`). El tema oscuro único es de primera clase (FR-035c). No hay controles de web ni de iOS.

## Resumen
- P0 0 · P1 0 · P2 3 · P3 3.
- Principales: idioma de los textos de M3 (P2), tira de días cortada (P2), disabled outline nuevo en todas las píldoras (P3, decisión).

## Hallazgos

- **[P2] Textos de accesibilidad de M3 en inglés.** Ubicación: `ModalBottomSheet` (Alternatives.kt), `DatePicker` (ProgressScreen). Categoría: Accesibilidad. Impacto: TalkBack lee "Close sheet"/"Drag handle" y el selector de fecha sale en inglés si el sistema lo está; choca con FR-042. Recomendación: fijar la configuración regional de la app a `es` (per-app locale `LocaleManager`/`AppCompatDelegate.setApplicationLocales` + `localeConfig`). Pendiente heredado. `/impeccable harden`.
- **[P2] Tira de días de Dieta cortada al abrir.** Ubicación: `DietScreens.kt` (ScrollableTabRow). Categoría: Conformidad. Impacto: "Lun" queda fuera y "Mar" aparece partido en el borde izquierdo con hoy al final de la semana; el lunes solo se ve desplazando. Recomendación: 7 pestañas fijas de ancho igual cuando caben (≥ 48 dp cada una) y desplazable solo si no. `/impeccable layout`.
- **[P2] `OfflineHint` del Perfil arriba de los cuatro "Editar".** Ubicación: `ProfileScreens.kt:184`. Es un aviso para cuatro controles a la vez, no bajo uno; se dejó así a propósito, pero con la lista desplazada no se ve (pendiente heredado "OfflineHint fuera de la vista"). `/impeccable harden`.
- **[P3] Contorno en píldoras deshabilitadas.** `Buttons.kt` PillButton: ahora todas las píldoras deshabilitadas llevan un hilo `outline` (3,02:1 sobre la hoja). Se ve también en "Anterior" del modo sesión. Es intencional (la píldora desaparecía en la hoja). `/impeccable polish` si se prefiere solo dentro de hojas.
- **[P3] Tamaños prestados de `Spacing`.** `Spacing.sm` (12 dp) como tamaño de íconos de 12 dp y del anillo de etapa; `Spacing.xxl`/`weekDot` como tamaños. Funciona; un token de tamaño de ícono pequeño lo haría explícito. `/impeccable polish`.
- **[P3] La pista de los anillos es decorativa (1,12:1 sobre AppCard).** Fijado en `ContrastTest` solo el arco contra la pista (7,1:1). Aceptado: el valor lo lleva el arco.

## Patrones
- Todo token sale de `core/ui/theme`: el grep de literales (`tween\([0-9]|[0-9]+\.sp`) fuera del tema queda vacío; solo quedan dos `dp` privados con nombre en `core/ui` (ChipHeight, SegmentGap) y un `0.dp`.
- Los textos que no salen de `strings.xml` (componentes M3) son la única fuga de idioma.

## Lo que funciona
- `ContrastTest` fija texto ≥ 4,5:1 sobre las 4 superficies, contornos ≥ 3:1, macros y gráficos verdes ≥ 3:1 sobre AppCard y `surfaceContainerHigh`.
- Al 130 % y al 200 % no hay recortes en las 12 pantallas: alto mínimo, nunca fijo; la etiqueta "Progreso" se reduce con `TextAutoSize` en vez de cortarse.
- Movimiento reducido: `ANIMATOR_DURATION_SCALE == 0` deja fundidos de 150 ms y nada más.
- Sin conexión: banner con fecha de los datos, controles de red deshabilitados con el aviso debajo (R7).

## Acciones recomendadas
1. [P2] `/impeccable harden`: idioma de la app fijado a español para los textos de M3.
2. [P2] `/impeccable layout`: tira de días de Dieta fija cuando cabe.
3. [P3] `/impeccable polish`: tokens de tamaño de ícono.
