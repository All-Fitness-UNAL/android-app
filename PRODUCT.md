# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Adultos principiantes o de nivel intermedio en Colombia y Latinoamérica que quieren empezar a entrenar o retomar el hábito. Entrenan en casa o en un gimnasio básico, a menudo con poco equipo. Necesitan un plan claro de qué entrenar y qué comer sin contratar a un entrenador ni a un nutricionista. La app se usa a diario: al empezar el día o la sesión miran "qué toca hoy", marcan ejercicios mientras entrenan y consultan recetas al cocinar.

## Product Purpose

AllFitness genera con IA una rutina de ejercicio semanal y un plan de alimentación con recetas, personalizados según los datos físicos, el objetivo, el nivel, el equipo, el tipo de dieta, las alergias y los gustos de cada persona. El usuario sigue su rutina, consulta su dieta, pide alternativas cuando algo no le sirve y registra su progreso (peso y sesiones completadas). El éxito es que la persona completa el onboarding sin ayuda, llega a su primer plan y lo sigue semana a semana.

## Positioning

Un plan integral que se adapta a la persona: rutina y dieta se generan juntas a partir de su cuerpo, su equipo disponible y comida cotidiana de la región, y cuando algo no le sirve pide una alternativa explicando por qué ("no tengo el equipo", "me causa molestia", "no tengo los ingredientes"). La relación con el plan es una conversación guiada por motivos, no un catálogo fijo.

## Operating Context

- Uso en el móvil, en sesiones cortas y frecuentes: consultar el día, marcar ejercicios mientras se entrena y leer una receta mientras se cocina.
- La generación con IA tarda hasta 60 segundos y puede fallar. La espera y el reintento son parte normal del uso.
- Hay uso sin conexión: rutina, dieta y último progreso se consultan desde caché, con indicador de que pueden no estar actualizados.
- Navegación principal con barra inferior: Rutina, Dieta, Progreso y Perfil.

## Capabilities and Constraints

- La fuente de la verdad funcional es `specs/001-allfitness-mvp/spec.md` y la constitución en `.specify/memory/constitution.md`.
- **Solo frontend:** el backend calcula todo (calorías, macros, rachas, progresiones) y genera el contenido; la app solo muestra y da formato. Flavors `mock` (FakeBackend) y `real`.
- **Stack existente:** Kotlin, Jetpack Compose, Material 3, Hilt, Navigation Compose, Retrofit, Room, DataStore y Coil. minSdk 26, targetSdk 37.
- **Aviso de IA obligatorio:** toda pantalla con rutina o dieta debe mostrar que el contenido es orientativo y no reemplaza a un profesional de salud.
- **Regenerar no destruye:** el contenido actual sigue visible hasta que llega el nuevo. Toda acción de IA muestra carga, bloquea duplicados y ofrece reintentar.
- Toda pantalla que carga datos maneja los estados cargando, contenido, vacío y error.
- Solo se marca como terminada la sesión del día actual.
- Toda la interfaz está en español y todo texto visible va en `strings.xml`.
- **Fuera de alcance:** pagos, login social, recuperación de contraseña, notificaciones, wearables, funciones sociales, chat libre con la IA, edición manual de planes, historial de planes, registro de comidas, lista de compras, videos de ejercicios o temporizador, medidas distintas al peso, modo oscuro personalizado (se sigue el tema del sistema), otros idiomas y versiones web o iOS.

## Brand Commitments

- El nombre "AllFitness" es fijo.
- Todo lo visual es reemplazable en el rediseño: el verde #1B6B4F del launcher, la tipografía y los componentes. No existe logo.

## Evidence on Hand

- Auditoría visual real de la app actual en el emulador: `visual-audit/2026-10-29/AUDITORIA.md`, con 31 capturas y sus dumps de UI.
- Crítica de diseño (`/impeccable critique`, 2026-10-29): 22/40 (Aceptable), con detalle en `.impeccable/critique/`. Diagnóstico: la interfaz es Material 3 por defecto y no tiene carácter propio. Problemas prioritarios: (P1) "Hoy" no está a un vistazo; (P1) entrenar no tiene forma propia: sin progreso, "Terminar sesión" al final de la lista y checkboxes inertes en días que no son hoy; (P1) no hay identidad visual (mezcla de verde, lavanda y menta, tipografía de serie); (P2) el aviso de IA ocupa el mejor espacio; (P2) la espera de 60 s es una pantalla muerta.
- Auditoría técnica (`/impeccable audit`, 2026-10-30): 13/20 (Aceptable), con detalle en `.impeccable/audit/2026-10-30__app.md`. Es nativa y conforme con Material, pero el tema está a medio definir: `Color.kt` no define los roles `surfaceContainer*`, y por eso M3 rellena con lavanda, y la tipografía es `Typography()` de serie. Otros hallazgos: (P1) checkbox inerte en días que no son hoy, sin `enabled = false`; (P2) no hay `heading()` ni `liveRegion` para TalkBack; (P2) no hay adaptación a tablet ni a apaisado.
- Los datos de ejemplo del `FakeBackend` (`app/src/mock/`) muestran el contenido real esperado: ejercicios con grupo muscular, series, repeticiones y peso sugerido, y comidas colombianas con kcal y macros.
- No hay fotografías de ejercicios ni de comida, ni testimonios, métricas de usuarios o marca registrada. Cuando el backend envía imágenes de ejercicio, la app las muestra; no hay que fabricarlas.

## Product Principles

1. **Lo de hoy primero:** la persona abre la app para saber qué entrenar y qué comer hoy, y eso debe estar a un vistazo.
2. **El plan es suyo y se puede ajustar:** pedir una alternativa con un motivo es una acción de primera clase, no un recurso escondido.
3. **Honestidad sobre la IA:** el contenido es orientativo y la generación es lenta y falible. La interfaz lo comunica con claridad, sin alarmar y sin perder nunca lo que ya hay.
4. **Cercano y alcanzable:** está pensado para principiantes y nivel intermedio con poco equipo y comida local. Nada debe hacerles sentir que la app es para atletas.
5. **Funciona sin red:** consultar el plan nunca depende de la conexión.

## Accessibility & Inclusion

- Áreas táctiles de mínimo 48dp, `contentDescription` en imágenes e íconos con significado, y contraste suficiente (requisito de la constitución).
- La interfaz debe soportar escalado de fuente del sistema. En la auditoría se verificó al 130 % sin recortes.
- Debe respetar el modo claro/oscuro del sistema.
