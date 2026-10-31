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
- Navegación principal con barra inferior: Inicio, Rutina, Dieta, Progreso y Perfil.

## Capabilities and Constraints

- La fuente de la verdad funcional es `specs/001-allfitness-mvp/spec.md` y la constitución en `.specify/memory/constitution.md`.
- **Solo frontend:** el backend calcula todo (calorías, macros, rachas, progresiones) y genera el contenido; la app solo muestra y da formato. Flavors `mock` (FakeBackend) y `real`.
- **Stack existente:** Kotlin, Jetpack Compose, Material 3, Hilt, Navigation Compose, Retrofit, Room, DataStore y Coil. minSdk 26, targetSdk 37.
- **Aviso de IA obligatorio:** toda pantalla con rutina o dieta debe mostrar que el contenido es orientativo y no reemplaza a un profesional de salud.
- **Regenerar no destruye:** el contenido actual sigue visible hasta que llega el nuevo. Toda acción de IA muestra carga, bloquea duplicados y ofrece reintentar.
- Toda pantalla que carga datos maneja los estados cargando, contenido, vacío y error.
- Solo se marca como terminada la sesión del día actual.
- Toda la interfaz está en español y todo texto visible va en `strings.xml`.
- **Fuera de alcance:** pagos, login social, recuperación de contraseña, notificaciones, wearables, funciones sociales, chat libre con la IA, edición manual de planes, historial de planes, registro de comidas, lista de compras, videos de ejercicios o temporizador, medidas distintas al peso, selector de tema (la app usa un único tema oscuro), otros idiomas y versiones web o iOS.

## Brand Commitments

- El nombre "AllFitness" es fijo.
- Logo-mark del Figma (`logo-mark`): placa verde #2ECC71 redondeada con un rayo y el nombre "AllFitness" al lado (`LogoMark` en `core/ui/Brand.kt`). Es la marca en login, registro, onboarding y la espera de la IA. El ícono del launcher todavía usa el verde #1B6B4F anterior.
- Tema oscuro único (FR-035c): fondo #11111E, tarjetas #1C1C2C y un solo verde de acción #2ECC71. No hay tema claro ni selector de tema.
- Tipografía Outfit (títulos y cifras tabulares) y Manrope (lectura), empaquetadas en la app.

## Evidence on Hand

- Auditoría visual real de la app actual en el emulador: `visual-audit/2026-10-29/AUDITORIA.md`, con 31 capturas y sus dumps de UI.
- Crítica de diseño (`/impeccable critique`, 2026-10-29): 22/40 (Aceptable), con detalle en `.impeccable/critique/`. Diagnóstico: la interfaz es Material 3 por defecto y no tiene carácter propio. Problemas prioritarios: (P1) "Hoy" no está a un vistazo; (P1) entrenar no tiene forma propia: sin progreso, "Terminar sesión" al final de la lista y checkboxes inertes en días que no son hoy; (P1) no hay identidad visual (mezcla de verde, lavanda y menta, tipografía de serie); (P2) el aviso de IA ocupa el mejor espacio; (P2) la espera de 60 s es una pantalla muerta.
- Auditoría técnica (`/impeccable audit`, 2026-10-30): 13/20 (Aceptable), con detalle en `.impeccable/audit/2026-10-30__app.md`. Es nativa y conforme con Material, pero el tema está a medio definir: `Color.kt` no define los roles `surfaceContainer*`, y por eso M3 rellena con lavanda, y la tipografía es `Typography()` de serie. Otros hallazgos: (P1) checkbox inerte en días que no son hoy, sin `enabled = false`; (P2) no hay `heading()` ni `liveRegion` para TalkBack; (P2) no hay adaptación a tablet ni a apaisado.
- Rediseño "Línea" (rama `redesign/premium-ui`, 2026-10-30):
  - auditoría técnica final 17/20, Buena, con P0 0 y P1 0 (`.impeccable/audit/2026-10-30__app-final.md`);
  - crítica final 30/40, Buena, con P0 0 y P1 0 (`.impeccable/critique/2026-10-30T08-25-15Z__app-src-main-java-com-allfitness-app.md`);
  - capturas en claro, oscuro y con fuente al 130 % y al 200 % en `visual-audit/redesign/final/`;
  - ese sistema visual se retiró: `DESIGN.md` describe ahora la interfaz del Figma.
- Prototipo de Figma `All Fitness_prototype.fig` (2026-09-17), con 7 pantallas en tema oscuro y acento verde. El árbol de nodos decodificado, la miniatura y los scripts que lo regeneran están en `docs/design/figma/`. La traducción a la app y lo que no se construye están en `docs/superpowers/specs/2026-10-30-interfaz-figma-design.md`.
- Interfaz del Figma (rama `redesign/figma-ui`, 2026-10-30):
  - auditoría técnica 16/20, Buena, con P0 0 y P1 0 (`.impeccable/audit/2026-10-30__app-figma-final.md`);
  - crítica 31/40, Buena, con P0 0 y P1 0 (`.impeccable/critique/2026-10-30T16-00-00Z__app-src-main-java-com-allfitness-app.md`);
  - recorrido del quickstart 1–8 en `visual-audit/figma/final/flujo/` y las 12 pantallas al 100 %, 130 % y 200 % en `visual-audit/figma/final/{100,130,200}/`;
  - el sistema visual resultante está en `DESIGN.md` y `.impeccable/design.json`.
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
- La interfaz debe soportar escalado de fuente del sistema. Se verificó al 130 % y al 200 % sin recortes (`visual-audit/figma/final/`).
- La app usa un único tema oscuro (FR-035c); el contraste se verifica sobre él.

## Mejoras de la ejecución anterior

Lecciones del rediseño "Línea" (rama `redesign/premium-ui`) para las próximas ejecuciones:

- **Verificación visual sin tests:** no hay tests de UI, así que cada arreglo visual se comprobó solo con capturas. La lógica de presentación (qué botón mostrar, qué estado tiene un día, qué página sigue) va en funciones puras con test. Si una pantalla falla dos veces, se considera añadir un screenshot test.
- **Estados sin disparador en el mock:** algunos estados no se podían provocar desde el `FakeBackend`, como 0 sesiones programadas o la tarjeta de error de una sección. Antes de dar por hecho un estado hay que tener cómo verlo; si el mock no lo permite, se anota como no verificado.
- **Toques con texto exacto:** en el recorrido con adb, buscar "4" tocó "Paso 3 de 4". Los toques por texto usan coincidencia exacta.
- **Cambios de ViewModel:** se añadieron campos (`completed`, `isPast`) a `WeekDayRow` y a `DayView`, y el aviso de sesión terminada pasó de un snackbar a la barra de sesión, sin preguntar. Cualquier cambio en un ViewModel se consulta antes, aunque sea derivado.
- **Conformidad con la spec en cada tarea:** el documentador encontró desviaciones al final. Cada tarea se revisa contra su sección de la spec antes del commit.
- **Barras del sistema:** la pantalla Generando dejaba franjas claras en la barra de estado y en la de navegación. Cada captura revisa ambas barras.
- **Contraste calculado:** los pares de color se calculan con script y se fijan en `ContrastTest`; no se estiman a ojo.
- **Instalaciones aisladas:** un `npm install` sin `package.json` propio escribió en `C:\Users\USER\package.json` y hubo que revertirlo. Toda instalación auxiliar va en una carpeta con su propio `package.json` o con `pip --target`.
- **Pendientes heredados:**
  - siguen abiertos:
    - `OfflineHint` queda fuera de la vista cuando la lista ya está desplazada;
    - el DatePicker y los textos de accesibilidad de la hoja ("Close sheet", "Drag handle") salen en el idioma del dispositivo;
    - la vista previa del gesto atrás muestra la pantalla sin barra;
    - en el onboarding, "Objetivo" se repite como título y como etiqueta, y el paso 3 es muy denso;
    - el scroll se pierde tras la muerte del proceso (`contentKey`);
  - corregidos en el plan de Figma: la inicial del avatar con pares sustitutos, la espera al regenerar el plan completo y los literales de duración y de `sp` fuera de los tokens.
