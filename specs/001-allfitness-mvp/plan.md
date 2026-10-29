# Implementation Plan: All Fitness MVP (App Android)

**Branch**: `001-allfitness-mvp` (sin rama git; se trabaja en `master`) | **Date**: 2026-10-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-allfitness-mvp/spec.md`

## Summary

App Android (solo frontend) del MVP de All Fitness: registro con consentimiento de datos, onboarding de 4 pasos, rutina y dieta semanales generadas por IA en el backend, alternativas con vista previa, progreso de peso y entrenamiento, perfil y cuenta, y consulta sin conexión. Enfoque técnico: Kotlin + Jetpack Compose, MVVM simple (Screen → ViewModel → Repository), Retrofit contra un contrato REST que define este plan (`contracts/`), caché offline en Room como instantáneas JSON por pantalla, y build flavors `mock`/`real` para avanzar sin backend. Las llamadas a la IA son síncronas con tiempo de espera de 90 s.

## Technical Context

**Language/Version**: Kotlin 2.x (última estable al crear el proyecto), JDK 17

**Primary Dependencies**: Jetpack Compose (BOM) + Material 3, Navigation Compose (rutas tipadas), Hilt (+ hilt-navigation-compose), Retrofit + converter de kotlinx.serialization, OkHttp, kotlinx.serialization, Coroutines/Flow, Room, DataStore Preferences, Coil (coil-compose). Versiones fijadas en `gradle/libs.versions.toml`.

**Storage**: Room (instantáneas de rutina, dieta, progreso y perfil para uso sin conexión); DataStore (token de sesión y borrador del onboarding). Los datos de verdad viven en el backend.

**Testing**: Ninguna prueba automatizada (la spec no las pide; constitución). Verificación manual según `quickstart.md`.

**Target Platform**: Android minSdk 26, targetSdk última estable; teléfonos en vertical.

**Project Type**: mobile-app (un único módulo Gradle `app`).

**Performance Goals**: Contenido guardado visible en < 2 s al abrir (SC-003); interfaz fluida a 60 fps en listas de ejercicios y recetas.

**Constraints**: Sin conexión: solo lectura desde la caché, sin escrituras en cola; espera de la IA ≥ 60 s (se usan 90 s); sin lógica de negocio en el cliente; toda la interfaz en español desde `strings.xml`.

**Scale/Scope**: ~16 pantallas, 7 historias de usuario, 6 archivos de contrato; un usuario por dispositivo a la vez.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Cómo lo cumple el plan | Estado |
|-----------|------------------------|--------|
| I. Alcance solo frontend | La app no llama a proveedores de IA ni guarda claves. Calorías, macros, totales diarios, racha y sesiones programadas llegan calculados del backend. La gráfica de peso solo dibuja los valores recibidos. Los Fakes devuelven JSON predefinido y solo guardan en memoria lo mínimo (marcas, pesos) para poder recorrer los flujos; no generan planes. | ✅ |
| II. Contrato definido por el frontend | `contracts/` documenta endpoints, JSON de ejemplo y errores por módulo. Cada Repository tiene implementación Fake (flavor `mock`) y real (flavor `real`). Los DTO se mapean a modelos de dominio en el Repository. camelCase, unidades métricas, instantes en ISO-8601 UTC; los días del calendario como fecha ISO-8601 local `YYYY-MM-DD` (ver research R2). | ✅ |
| III. Contenido de IA seguro y resiliente | Componente `AiDisclaimer` único en `core/ui`. Las acciones de IA muestran carga, se deshabilitan mientras esperan y ofrecen reintentar. El contenido actual no se toca hasta recibir éxito (y en ejercicio/receta hasta "Usar esta"). Caché offline de rutina, dieta y progreso. Un 401 limpia la sesión y vuelve al Login. Estados cargando/contenido/vacío/error en todas las pantallas. | ✅ |
| IV. Errores centralizados | `core/network` mapea sin conexión, timeout, HTTP y códigos de error del contrato a `AppError`; las pantallas solo eligen el texto en `strings.xml`. Campos opcionales con valores por defecto en el mapeo; los esenciales faltantes → `AppError.InvalidResponse`. | ✅ |
| V. Cero código sombra | Solo lo de la spec. Sin interceptor de logging, analítica ni ajustes extra. Lo que requiere servidor (generación, cálculos, validación de la fecha de hoy, versiones de los textos legales) queda en el contrato como requisito para backend. | ✅ |
| Stack | Todo está en la lista de la constitución. Solo se añaden integraciones oficiales de esas mismas librerías: `hilt-navigation-compose`, `room-ktx`, `lifecycle-runtime-compose`, `activity-compose` y el converter oficial de Retrofit para kotlinx.serialization. | ✅ |
| Calidad | Sin tareas de tests. Verificación manual (`quickstart.md` + sección de la spec). Build y lint sin warnings nuevos. | ✅ |

**Re-check post-diseño (Phase 1)**: ✅ Sin violaciones. El data model, los contratos y el quickstart no añaden capas (sin UseCases), ni librerías, ni lógica de servidor.

## Project Structure

### Documentation (this feature)

```text
specs/001-allfitness-mvp/
├── plan.md              # Este archivo
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1: contrato para backend
│   ├── README.md        # Convenciones, autenticación, errores
│   ├── auth.md
│   ├── profile.md
│   ├── routine.md
│   ├── diet.md
│   └── progress.md
├── checklists/requirements.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts                 # flavors mock / real, BASE_URL por flavor
└── src/
    ├── main/
    │   ├── AndroidManifest.xml      # allowBackup=false
    │   ├── res/values/strings.xml   # todos los textos en español
    │   └── java/com/allfitness/app/
    │       ├── AllFitnessApp.kt     # @HiltAndroidApp
    │       ├── MainActivity.kt
    │       ├── navigation/          # NavHost raíz (auth → onboarding → main), barra inferior
    │       ├── core/
    │       │   ├── network/         # Retrofit, AppError, safeApiCall, AuthInterceptor, ConnectivityObserver
    │       │   ├── database/        # Room: SnapshotEntity + SnapshotDao
    │       │   ├── session/         # SessionStore (DataStore), estado de sesión, limpieza total
    │       │   ├── time/            # "hoy" y semana actual (lunes a domingo)
    │       │   └── ui/              # theme/, AiDisclaimer, LoadingState, ErrorState, EmptyState, OfflineBanner
    │       ├── auth/                # Login, Registro (+ consentimiento), AuthRepository
    │       ├── onboarding/          # Onboarding de 4 pasos + Perfil (comparten secciones y validación)
    │       ├── routines/            # Semana, día, detalle de ejercicio, marcas, RoutineRepository
    │       ├── nutrition/           # Semana de dieta, receta, DietRepository
    │       ├── aifeedback/          # Hoja "Pedir alternativa", motivos, vista previa, AlternativeRepository
    │       └── progress/            # Registro de peso, historial + gráfica (Canvas), resumen, ProgressRepository
    ├── mock/
    │   ├── assets/mock/*.json       # JSON de ejemplo conforme a contracts/
    │   └── java/com/allfitness/app/di/   # Fakes + módulo Hilt que los enlaza
    └── real/
        └── java/com/allfitness/app/di/   # módulo Hilt que enlaza los Repository reales
```

**Structure Decision**: Un único módulo `app` con paquetes por feature, como dicta la constitución. El Perfil vive en `onboarding/` (módulo "Onboarding/Perfil" de la constitución) porque reutiliza las mismas secciones de formulario y validaciones. Los Fakes y su configuración de Hilt existen solo en el source set `mock`; los Repository reales viven en `main` y se enlazan en `real`.

## Complexity Tracking

Sin violaciones de la constitución; no aplica.
