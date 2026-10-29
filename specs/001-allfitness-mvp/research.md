# Research: All Fitness MVP

El stack lo fija la constitución, así que este documento resuelve cómo usarlo para cumplir la spec. No quedan `NEEDS CLARIFICATION`.

## R1. Llamadas largas a la IA (hasta 60 s)

- **Decision**: Petición HTTP síncrona. Los endpoints de IA (generar rutina o dieta, pedir alternativa) usan un cliente con timeout de lectura de 90 s; el resto, 15 s.
- **Rationale**: Cubre los 60 s de la spec con margen y es lo más simple para ambos equipos. La vista previa de alternativas evita perder cambios: el backend solo guarda una alternativa de ejercicio o receta cuando llega el "aceptar". Si la app se cierra durante una regeneración completa, el backend puede terminarla; al volver, la app muestra el plan que haya en el servidor.
- **Alternatives considered**: Trabajo asíncrono con `jobId` y consultas periódicas (más robusto ante cortes, pero duplica endpoints y estados en ambos lados; se deja para cuando la IA supere ~60 s). WebSockets o SSE (desproporcionado para un MVP).

## R2. Fechas en el contrato

- **Decision**: Los instantes (`createdAt`, `updatedAt`) van en ISO-8601 UTC (`2026-10-28T14:00:00Z`). Los días del calendario del usuario (registro de peso, sesión del día, inicio de semana) van como fecha ISO-8601 sin hora (`2026-10-28`), calculada con la fecha local del dispositivo.
- **Rationale**: La constitución pide ISO-8601 UTC, pero "hoy" para el usuario es un día local. Convertir un día a UTC lo correría de fecha en la noche colombiana (UTC−5). Una fecha sin hora no tiene zona horaria, así que respeta la convención sin ese error.
- **Alternatives considered**: Mandar todo como instante UTC (rompe "hoy" después de las 19:00 en Colombia). Mandar la zona horaria del usuario y que el backend calcule el día (más acoplamiento, y la spec dice "según la fecha del dispositivo").

## R3. Flavors `mock` / `real` y Fakes

- **Decision**: `productFlavors { mock; real }` en una dimensión `backend`. Cada flavor tiene un módulo Hilt que enlaza las interfaces `*Repository` a su implementación. Los Fakes leen JSON de `src/mock/assets/mock/` (copiados de los ejemplos de `contracts/`), simulan 1 s de latencia (6 s para la IA) y guardan en memoria solo lo necesario para recorrer los flujos.
- **Rationale**: Es lo que pide la constitución ("una sola configuración, nunca editando pantallas"). Los assets con el mismo JSON del contrato también prueban que el mapeo de los DTO funciona.
- **Alternatives considered**: Un servidor mock local (MockWebServer o json-server): más fiel, pero añade dependencias y un proceso que cada miembro del equipo tendría que levantar.
- **Escenarios de error en el flavor mock** (para la verificación manual): ver `quickstart.md`. Se activan con valores de entrada fijos (correo `existe@allfitness.co`, contraseña `Incorrecta1`, comentarios `#lento`, `#falla`, `#roto`, `#sindieta`, peso `299.9`) que solo interpreta el código del source set `mock`. Las pantallas no tienen ningún control oculto para esto.

## R4. Caché offline

- **Decision**: Una tabla Room `snapshots(key TEXT PK, json TEXT, savedAt INTEGER)`. Se guarda el modelo de dominio serializado bajo las claves `ROUTINE`, `DIET`, `WORKOUT_WEEK`, `WEIGHTS`, `SUMMARY` y `PROFILE`. El Repository emite primero la instantánea (si existe) y luego, con conexión, la del servidor.
- **Rationale**: La caché solo es de lectura y se reemplaza entera en cada carga, así que no hace falta un esquema relacional. Esto cumple "Room para la caché" con una sola entidad y un DAO de 3 métodos, y SC-003 (< 2 s) se logra leyendo una sola fila.
- **Alternatives considered**: Tablas normalizadas por entidad (más migraciones y mapeos sin ningún beneficio para datos de solo lectura). Archivos JSON sin Room (contradice la constitución).

## R5. Detección de conexión

- **Decision**: `ConnectivityObserver` en `core/network`, basado en `ConnectivityManager.registerDefaultNetworkCallback`, expone `StateFlow<Boolean>` (red con `NET_CAPABILITY_VALIDATED`). Los ViewModels lo combinan con su estado para deshabilitar acciones y mostrar `OfflineBanner`.
- **Rationale**: Es API de la plataforma (minSdk 26 lo soporta) y no necesita librerías. Además, cualquier `IOException` de una petición se mapea a `AppError.NoConnection`, que cubre los casos en que la red está "conectada" pero no funciona.
- **Alternatives considered**: Hacer ping al backend (más tráfico y más código). Solo reaccionar a errores (no permite deshabilitar los botones antes de que el usuario toque).

## R6. Sesión y 401

- **Decision**: El token va en DataStore (`SessionStore`). `AuthInterceptor` añade `Authorization: Bearer`. Si un endpoint que **no** es `/auth/*` responde 401, se llama a `SessionManager.expire()`: borra el token, la caché y el borrador, y emite `SessionState.Expired`. El NavHost raíz observa ese estado y navega al Login con el mensaje "Tu sesión expiró".
- **Rationale**: Hay un solo lugar para el 401 (constitución IV). Excluir `/auth/login` evita que unas credenciales incorrectas parezcan una sesión vencida.
- **Alternatives considered**: Refresh tokens (no están en la spec; los aportaría backend). Manejar el 401 en cada ViewModel (duplicado).
- **Riesgo aceptado**: El token queda en DataStore sin cifrar dentro del almacenamiento privado de la app, como dicta la constitución. Para que el token y los datos de salud no salgan en las copias de seguridad en la nube, se desactivan los backups (`allowBackup=false` y `dataExtractionRules` excluyendo todo).

## R7. Borrador del onboarding

- **Decision**: El borrador del onboarding y el paso actual se guardan como JSON en DataStore tras cada "Siguiente". Al terminar con éxito, o al cerrar sesión, se borra.
- **Rationale**: FR-009 (retomar donde quedó, solo en ese dispositivo). DataStore ya está en el stack y es un solo valor.
- **Alternatives considered**: Room (excesivo para un solo objeto). Guardar el borrador en el backend (no lo pide la spec).

## R8. Orquestación al terminar el onboarding

- **Decision**: La app hace, en orden: `PUT /profile` → `PUT /progress/weights/{hoy}` → en paralelo `POST /routine/generate` y `POST /diet/generate`. Si falla el perfil o el peso, no se continúa y se ofrece reintentar desde ese paso. Si falla una de las generaciones, el usuario entra a la app y esa pestaña muestra el estado vacío con "Generar".
- **Rationale**: Todos los PUT son idempotentes, así que reintentar es seguro. Generar en paralelo reduce la espera, y la falla parcial cumple el caso límite de la spec.
- **Alternatives considered**: Un solo endpoint "completar onboarding" (oculta la falla parcial y obliga a reintentar todo).

## R9. Gráfica de peso

- **Decision**: `WeightChart` en `progress/`, dibujado con Compose `Canvas` (línea y puntos, eje de fechas con primera y última fecha, eje de peso con mínimo y máximo). Cuando hay un solo punto, se muestra solo el punto. Una `contentDescription` resume la tendencia ("De 80 kg a 77,5 kg entre 1 sep y 28 oct").
- **Rationale**: No hay librería de gráficas en el stack aprobado, y una línea simple son unas ~80 líneas de código.
- **Alternatives considered**: Vico o MPAndroidChart (librería nueva sin justificación suficiente para una sola gráfica).

## R10. "Hoy" y la semana

- **Decision**: `core/time/Today` expone `LocalDate.now()` y la semana actual de lunes a domingo (`java.time`, disponible en minSdk 26). Los ViewModels recalculan al volver a primer plano (`ON_RESUME`), lo que cubre el cambio de día a medianoche.
- **Rationale**: La spec usa la fecha del dispositivo, y en Colombia la semana empieza el lunes.
- **Alternatives considered**: Pedir la fecha al servidor (contradice la spec).

## R11. Marcar ejercicios

- **Decision**: Actualización optimista: la marca cambia al instante, se envía `PUT` y, si falla, se revierte y se muestra un snackbar con "Reintentar" (spec, US2 escenario 3). Mientras hay una petición en curso para ese ejercicio, se ignoran más toques.
- **Rationale**: Durante el entrenamiento, esperar a la red en cada toque se siente lento. La reversión cumple la spec y el bloqueo por ejercicio evita duplicados.
- **Alternatives considered**: Esperar la respuesta antes de mostrar la marca (más lento, sin otra ventaja).

## R12. Mapeo de errores

- **Decision**: Hay una sola función `safeApiCall` en `core/network`, que devuelve `Result<T, AppError>`:

| Origen | AppError |
|--------|----------|
| `IOException` (sin red) | `NoConnection` |
| `SocketTimeoutException` | `Timeout` |
| 400 `VALIDATION_ERROR` / `FUTURE_DATE` / `INVALID_DATE` | `Validation(code)` |
| 401 en `/auth/login` | `InvalidCredentials` |
| 401 en otro endpoint | `SessionExpired` (+ `SessionManager.expire()`) |
| 404 `NO_ACTIVE_PLAN` | `NoActivePlan` (la pantalla lo muestra como estado vacío) |
| 404 otro | `NotFound` |
| 409 `EMAIL_TAKEN` | `EmailTaken` |
| 409 `PROPOSAL_EXPIRED` | `ProposalExpired` |
| 502/503/504 `AI_UNAVAILABLE` / `AI_TIMEOUT` | `AiUnavailable` |
| 5xx otro | `Server` |
| JSON ilegible o campo esencial faltante | `InvalidResponse` |

- **Rationale**: Constitución IV. El `message` del backend nunca se muestra; solo se usa el `code`.

## R13. Cerrar sesión y eliminar cuenta

- **Decision**: `SessionManager.clearAll()`: borra el token y el borrador en DataStore, vacía Room (`clearAllTables`) y limpia la caché de imágenes de Coil. Al cerrar sesión, primero intenta `POST /auth/logout` (si falla, se ignora) y después borra todo. Al eliminar la cuenta, solo borra todo si `DELETE /me` responde 204.
- **Rationale**: FR-033 y FR-034, y que el siguiente usuario del mismo dispositivo no vea los datos del anterior (caso límite).

## R14. Textos legales

- **Decision**: Las URLs de términos y de la política de datos, y su versión (`LEGAL_VERSION`), son constantes de `BuildConfig` por flavor. Los enlaces abren el navegador del sistema. El registro envía `acceptedLegalVersion`.
- **Rationale**: La app solo enlaza y registra la aceptación (supuesto de la spec). No se crea una pantalla WebView.
- **Alternatives considered**: Un endpoint `GET /legal` (útil si los textos cambian seguido; se puede añadir sin tocar las pantallas).

## R15. Identificadores del proyecto

- **Decision**: `applicationId` y paquete raíz `com.allfitness.app`. El flavor `mock` añade el sufijo `.mock`, para poder instalar ambos a la vez.
- **Rationale**: Es un nombre neutro; el equipo puede cambiarlo antes de publicar.
