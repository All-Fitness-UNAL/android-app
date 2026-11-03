---
version: 1
slug: "app-src-main-java-com-allfitness-app"
primary_target: "app/src/main/java/com/allfitness/app"
related_targets: []
---

# Surface brief: AllFitness app (Android)

Scope: toda la app Android (Compose). Mode: Operate. Audiencia: adultos principiantes/intermedios en Colombia/LatAm; uso diario, corto, con una mano. Tarea: saber qué toca hoy, entrenar marcando ejercicios, leer recetas, registrar peso. Restricciones: FR-001..FR-043, constitución v1.1.0, PRODUCT.md. Documento completo: docs/superpowers/specs/2026-10-30-interfaz-figma-design.md (sustituye a la dirección "Línea" de 2026-10-30-rediseno-visual-design.md).

## Direction contract

THESIS: La app es el prototipo de Figma `All Fitness_prototype.fig` traducido a Material 3: tema oscuro único con acento verde, lo de hoy primero. Rechaza las funciones y datos del Figma que el producto no tiene (kcal consumidas, temporizador, notificaciones, unidades, fotos inventadas, favoritos).

OWN-WORLD: Fondo #11111E, tarjetas #1C1C2C con hilo #2A2A3E, hojas #25253A, campos #121B2A. Un solo verde #2ECC71 para acción y hecho, con brillo solo en el botón principal. Ámbar #FFB300 solo para la racha; macros #22A85E / #5B8DEF / #E8607A solo como gráfico. Outfit 700/800 tabular para títulos y cifras; Manrope para leer. Radios 6/8/12/16/20 y píldoras para botones y chips.

STORY: La persona abre la app y en Inicio ve sus anillos de hoy y de la semana, el entreno de hoy y la dieta de hoy; entra a la sesión, recorre un ejercicio por página y la termina con un solo primario. Cree que el plan es suyo y alcanzable.

FIRST VIEWPORT: Inicio: avatar con inicial y "Hola, {nombre}", AiNote completo, tarjeta de 4 anillos (Hoy, Semana, Racha, Kcal plan), "Entreno de hoy" con su CTA o el descanso, y "Tu dieta de hoy" con la barra de macros; barra inferior con el destino activo en verde.

FORM: Figma (7 frames, `docs/design/figma/miniatura.png`, valores en `arbol-nodos.txt`).

Signature interaction: los anillos y barras se llenan en verde (450 ms, enfatizado); el modo sesión avanza página a página.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
