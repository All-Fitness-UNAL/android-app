---
version: 1
slug: "app-src-main-java-com-allfitness-app"
primary_target: "app/src/main/java/com/allfitness/app"
related_targets: []
---

# Surface brief: AllFitness app (Android)

Scope: toda la app Android (Compose). Mode: Operate. Audiencia: adultos principiantes/intermedios en Colombia/LatAm; uso diario, corto, con una mano. Tarea: saber qué toca hoy, entrenar marcando ejercicios, leer recetas, registrar peso. Restricciones: FR-001..FR-043, constitución v1.1.0, PRODUCT.md. Documento completo: docs/superpowers/specs/2026-10-30-rediseno-visual-design.md.

## Direction contract

THESIS: El plan es una ruta: la semana es una línea con estaciones y hoy es "estás aquí". Rechaza la lista plana de tarjetas iguales de M3 y el negro neón de fitness.

OWN-WORLD: Andén #F3F5F4, tarjetas blancas con hilos de 1 dp, panel Señal #0F1F1A, amarillo #F2C230 solo para "estás aquí", líneas de sección: verde #0A7A50 (Rutina), naranja #D9661F (Dieta), azul #2C5BD6 (Progreso). Archivo semicondensada para rótulos y cifras tabulares; Atkinson Hyperlegible Next para la lectura. Radios de 4/8/12/16, sin sombras.

STORY: La persona abre la app y ve de inmediato qué toca hoy y en qué punto de la semana está, recorre la sesión viendo cuánto lleva y la cierra con un gesto claro. Cree que el plan es suyo y alcanzable.

FIRST VIEWPORT: Rutina: título "Tu rutina", AiNote compacto, SignPanel oscuro (rótulo amarillo HOY · fecha, tipo de día en headline, tira "N ejercicios · estado", CTA "Ver sesión de hoy") y debajo la RouteLine de 7 estaciones; barra de navegación con el indicador en el color de la línea.

FORM: candidata 3 de 7 (Línea, señalética de transporte y ciclovía). seed:47113a5602c126fb

Signature interaction: el tramo de la SessionBar se llena al marcar ejercicios; al terminar, la estación de hoy se rellena con un único pulso. Gramática de movimiento: lo que avanza se desplaza hacia adelante.

Raises: modo sesión de un ejercicio a la vez (feed vertical) → se mantiene como lista con barra fija; nada desaparece, se marca (billetera de tiquetes); una cifra protagonista por pantalla (espécimen); tiras de datos regladas (alta densidad); nada se etiqueta dos veces (catálogo); estados pendiente con contorno / hecho relleno / pasado atenuado (riel central).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
