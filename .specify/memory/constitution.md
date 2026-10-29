<!--
Sync Impact Report
- Version change: (plantilla sin ratificar) → 1.0.0
- Principios definidos (antes eran placeholders):
  - [PRINCIPLE_1_NAME] → I. Alcance Solo Frontend (NO NEGOCIABLE)
  - [PRINCIPLE_2_NAME] → II. Contrato de Datos Definido por el Frontend
  - [PRINCIPLE_3_NAME] → III. Contenido de IA Seguro y Resiliente en la UX
  - [PRINCIPLE_4_NAME] → IV. Errores Centralizados y Amigables
  - [PRINCIPLE_5_NAME] → V. Cero Código Sombra y Fuente de la Verdad
- Secciones añadidas:
  - Naturaleza del Proyecto (preámbulo)
  - [SECTION_2_NAME] → Stack Tecnológico y Estructura de Código
  - [SECTION_3_NAME] → Calidad, Pruebas y Flujo de Trabajo
  - Governance completada
- Secciones eliminadas: ninguna
- Templates: no modificados (fuera del alcance de este comando; los leen en tiempo de ejecución)
  - .specify/templates/plan-template.md: su "Constitution Check" debe validar los principios I–V
  - .specify/templates/tasks-template.md: las tareas de tests son opcionales (coherente con la
    sección de Calidad)
- Reconstrucciones de texto truncado en la entrada (revisar):
  - "kotlinx.serialization.nía" → "kotlinx.serialization; Coroutines + Flow para asincronía"
  - "puede fallatenido a la IA" → "puede fallar: toda acción que pide contenido a la IA"
  - "`onboar`" → "`onboarding/`, `routines/`" (se añadió `routines/` por el módulo Rutinas)
  - "Composables sin estado siempre)" → "sin estado (stateless) siempre que sea posible"
  - "porcento derivados" → "porcentajes o totales derivados trivialmente"
  - "prevalece sobre cualquie" → "prevalece sobre cualquier otra práctica o guía del proyecto"
- TODOs diferidos: ninguno
-->
# All Fitness (Frontend Android) Constitution

All Fitness es una app Android de fitness que usa inteligencia artificial para generar rutinas de
ejercicio y planes de nutrición/recetas personalizados según las características, gustos y
preferencias de cada usuario. El usuario puede ver y seguir su rutina, ver su dieta, pedir una
alternativa si algo no le gusta (indicando cómo lo preferiría) y consultar su progreso.

- **Módulos principales:** Rutinas, Nutrición/Dieta y Progreso.
- **Módulos de soporte:** Login/Registro, Onboarding/Perfil y Feedback a la IA.

## Core Principles

### I. Alcance Solo Frontend (NO NEGOCIABLE)

- Este repositorio es SOLO FRONTEND. El equipo construye únicamente la app Android: pantallas,
  navegación, estado de UI, consumo de API y caché local.
- El backend, la base de datos del servidor, la lógica de autenticación, los modelos/prompts de IA
  y la generación de rutinas y recetas son responsabilidad del equipo de backend (3 ingenieros).
  El agente MUST NOT implementar, simular como lógica real ni diseñar nada de eso.
- La app MUST NOT llamar directamente a proveedores de IA ni guardar claves de API. Solo envía
  peticiones al backend y muestra lo que recibe.
- Calorías, macros, progresiones de carga y demás cálculos de negocio los calcula el backend. El
  frontend solo da formato y muestra (fechas, unidades, porcentajes o totales derivados
  trivialmente).

**Rationale:** La división de responsabilidades entre equipos evita trabajo duplicado, lógica de
negocio inconsistente y exposición de secretos en el cliente.

### II. Contrato de Datos Definido por el Frontend

- El frontend propone los DTOs y endpoints que necesita cada pantalla. Cada feature MUST
  documentar su contrato en `specs/[feature]/contracts/` (endpoint, método, request/response JSON
  de ejemplo y códigos de error) para compartirlo con backend.
- Mocks primero: mientras el endpoint real no exista, cada Repository MUST tener una
  implementación Fake que devuelve JSON de ejemplo conforme al contrato. El cambio entre Fake y
  real se hace con una sola configuración (build flavor `mock` / `real`), nunca editando pantallas.
- Los DTOs de red MUST NOT llegar a los Composables. El Repository los mapea a modelos de dominio
  simples, de modo que un cambio de backend solo toque el mapeo.
- Si el backend entrega algo distinto a lo acordado, no se "parcha" en la UI: se actualiza primero
  el contrato de la feature y luego el código.
- Convenciones del JSON: camelCase en campos, fechas en ISO-8601 (UTC), unidades métricas (kg, cm,
  kcal, gramos).

**Rationale:** Un contrato explícito permite avanzar en paralelo con backend y aísla la UI de
cambios en la API.

### III. Contenido de IA Seguro y Resiliente en la UX

- Toda pantalla que muestre una rutina o dieta generada por IA MUST incluir un aviso visible de
  que es orientativa y no reemplaza la recomendación de un profesional de salud.
- La generación con IA es lenta y puede fallar: toda acción que pide contenido a la IA (generar,
  regenerar, pedir alternativa) MUST mostrar un estado de carga claro, impedir envíos duplicados
  mientras espera y ofrecer reintentar si falla.
- Regenerar no destruye: pedir otra rutina o receta MUST NOT borrar la actual en pantalla hasta
  que la nueva llegue correctamente.
- Offline: la rutina, la dieta y el último progreso cargados MUST poder consultarse sin conexión
  desde la caché. Las acciones que requieren red (generar con IA, editar perfil, login) se
  deshabilitan sin conexión con un mensaje claro. No se implementa cola de sincronización de
  escrituras offline salvo que una spec lo pida.
- Sesión: todas las pantallas excepto Login/Registro requieren sesión activa. Ante un 401 del
  backend, la app MUST limpiar la sesión y volver al Login.
- Toda pantalla que carga datos MUST manejar los estados: cargando, contenido, vacío y error.

**Rationale:** Es contenido de salud generado por un sistema lento y falible; el usuario nunca
debe quedar sin información, confundido o con datos perdidos.

### IV. Errores Centralizados y Amigables

- Ningún stack trace, mensaje de excepción ni código HTTP MUST llegar al usuario. Todo error se
  traduce a un mensaje amigable en español (ej.: "No pudimos generar tu rutina. Inténtalo de
  nuevo").
- Los errores de red y HTTP (sin conexión, timeout, 400, 401, 404, 409, 5xx) MUST mapearse en un
  solo lugar de `core/network` a un tipo de error de la app; las pantallas solo deciden qué
  mensaje mostrar.
- Campos faltantes o inesperados en la respuesta del backend MUST NOT cerrar la app; se manejan
  como error o con valores por defecto definidos en el mapeo.

**Rationale:** Un único punto de mapeo mantiene mensajes coherentes y evita crashes por datos.

### V. Cero Código Sombra y Fuente de la Verdad

- El agente construye estrictamente lo documentado en spec.md. MUST NOT añadir pantallas, opciones
  ni features "por si acaso" (pagos, redes sociales, notificaciones, wearables, modo oscuro
  personalizado, etc.) salvo que una spec lo pida.
- Si una feature parece requerir lógica de servidor, IA o base de datos remota, el agente MUST
  detenerse y documentarlo como requisito para backend en el contrato, sin implementarlo en la app.
- Si una instrucción del usuario contradice esta constitución, o se detecta una falla lógica o un
  contrato de datos incompleto, el agente MUST detenerse, advertir del problema y solicitar
  actualizar spec.md o el contrato antes de tocar código.

**Rationale:** En SDD la spec es la fuente de la verdad; el código fuera de ella es deuda no
revisada.

## Stack Tecnológico y Estructura de Código

**Stack (reglas de implementación):**

- **Lenguaje:** Kotlin en todo el proyecto. No usar Java.
- **UI:** Jetpack Compose con Material 3. No usar XML layouts ni Fragments.
- **Navegación:** Navigation Compose.
- **Arquitectura:** MVVM simple: Screen (Composable) → ViewModel (StateFlow de estado de UI) →
  Repository → fuente de datos (API remota, caché local o mock). Sin Clean Architecture ni capas
  de UseCase, salvo que una lógica se repita en 2+ ViewModels.
- **Red:** Retrofit + kotlinx.serialization; Coroutines + Flow para asincronía (no RxJava).
- **Persistencia local:** Room para la caché offline de rutina, dieta y progreso; DataStore para
  preferencias y token de sesión.
- **Inyección de dependencias:** Hilt.
- **Imágenes:** Coil.
- **Plataforma:** minSdk 26 (Android 8.0), targetSdk en la última versión estable. Un único
  módulo Gradle `app`.
- **Dependencias nuevas:** MUST NOT añadirse librerías fuera de esta lista sin justificarlo en el
  plan.md de la feature.

**Estructura y estilo:**

- **Paquetes por feature:** `auth/`, `onboarding/`, `routines/`, `nutrition/`, `progress/`,
  `aifeedback/`, más `core/` (red, base de datos, tema, componentes comunes) y `navigation/`.
  Cada feature contiene sus pantallas, ViewModel, Repository y modelos.
- **Componentes reutilizables:** los elementos visuales repetidos (botones, tarjetas, estados de
  carga/error, aviso de IA) viven en `core/ui` y se reutilizan; no se duplican.
- **Diseño:** colores, tipografía y espaciados salen del tema de Material 3 (`core/ui/theme`).
  No usar colores ni tamaños hardcodeados en las pantallas.
- **Textos:** toda la interfaz está en español y todo texto visible va en `strings.xml` (nada
  hardcodeado), para permitir traducción futura.
- **Estilo:** Composables sin estado (stateless) siempre que sea posible; el estado vive en el
  ViewModel. Funciones pequeñas y legibles.
- **Nomenclatura:** camelCase para funciones/variables; PascalCase para clases, Composables y
  tipos. Las pantallas terminan en `Screen`, los ViewModels en `ViewModel` y los estados en
  `UiState`.
- **Accesibilidad:** contentDescription en imágenes e íconos con significado, áreas táctiles de
  mínimo 48dp y contraste suficiente.

## Calidad, Pruebas y Flujo de Trabajo

- **Validación manual como estándar:** por ser un MVP, la verificación principal es manual. Cada
  feature MUST terminar con una lista de comprobación en su spec (flujos felices, estados de
  error, sin conexión) que se recorre en emulador o dispositivo antes de darla por terminada.
- **Pruebas automatizadas opcionales:** solo se escriben si la spec de la feature lo pide
  explícitamente. No se generan tareas de tests por defecto.
- **Build limpio:** el proyecto MUST compilar sin errores y sin warnings nuevos de lint antes de
  cerrar una feature.

## Governance

- Esta constitución prevalece sobre cualquier otra práctica o guía del proyecto. Cada plan.md
  MUST verificar el cumplimiento de estos principios (Constitution Check) y justificar cualquier
  desviación.
- Las enmiendas se hacen solo mediante `/speckit-constitution`, con versionado semántico: MAJOR
  por eliminar o redefinir principios, MINOR por añadir principios o secciones, PATCH por
  aclaraciones.
- Los cambios de stack o de alcance entre frontend y backend requieren acuerdo del equipo completo
  antes de enmendar.

**Version**: 1.0.0 | **Ratified**: 2026-10-28 | **Last Amended**: 2026-10-28
