# Quickstart: Verificación del MVP de All Fitness

Esta guía sirve para compilar la app y recorrer la verificación manual de la spec ([spec.md](spec.md), sección "Manual Verification"). Los formatos de datos están en [contracts/](contracts/README.md) y [data-model.md](data-model.md).

## Requisitos previos

- Android Studio (última estable) con JDK 17.
- Dos emuladores: uno con API 26 (minSdk) y otro con la API objetivo (targetSdk). Ambos con la fecha y hora automáticas y la zona horaria `America/Bogota`.
- Para el flavor `real`: la URL del backend en `local.properties` (`allfitness.baseUrl=https://...`). El flavor `mock` no necesita backend.

## Compilar e instalar

```bash
./gradlew assembleMockDebug      # app con datos de ejemplo (id com.allfitness.app.mock)
./gradlew installMockDebug
./gradlew lintMockDebug          # MUST terminar sin errores ni warnings nuevos
./gradlew assembleRealDebug      # mismo código contra el backend real
```

Cambiar de `mock` a `real` es solo elegir otra variante de build; ninguna pantalla cambia (constitución II).

## Escenarios especiales del flavor `mock`

Estos valores solo existen en `src/mock` y permiten provocar cada estado sin backend:

| Para provocar | Haz esto |
|---------------|----------|
| Correo ya registrado (409) | Regístrate con `existe@allfitness.co` |
| Credenciales incorrectas (401) | Inicia sesión con la contraseña `Incorrecta1` |
| IA lenta (~55 s, éxito) | Escribe `#lento` en el comentario de una alternativa o regeneración |
| IA falla (`AI_UNAVAILABLE`) | Escribe `#falla` en el comentario |
| Falla parcial al terminar el onboarding | Escribe `#sindieta` en "alimentos que no te gustan": la rutina se genera y la dieta falla |
| Sesión expirada (401) | Registra un peso de `299.9` kg: el Fake responde `UNAUTHORIZED` |
| Respuesta incompleta | Pide una alternativa de receta con `#roto`: llega un JSON sin `recipe` |
| Usuario sin datos de progreso | Registra una cuenta nueva |

Cualquier otro correo y contraseña válidos inician sesión. El Fake guarda en memoria las marcas, los pesos y el perfil hasta que se cierra el proceso.

## Recorrido de validación

Marca cada punto en la sección "Manual Verification" de la spec al completarlo.

1. **Registro y onboarding (US1)**: registro sin marcar la casilla de aceptación (no deja continuar) → registro válido → cerrar la app en el paso 3 → reabrir (retoma en el paso 3) → valores fuera de rango (estatura 90, edad 15) → terminar. Debe verse la carga de la IA y luego la pestaña Rutina. Repetir con `#sindieta`.
2. **Sesión**: cerrar y reabrir la app (entra directo). Provocar el 401 (vuelve al Login con "Tu sesión expiró").
3. **Rutina (US2)**: la semana con hoy destacado y los días de descanso; marcar y desmarcar ejercicios; terminar la sesión con ejercicios sin marcar; abrir un día pasado y uno futuro (todo bloqueado). Aviso de IA visible.
4. **Dieta (US3)**: los 7 días; los totales coinciden con `src/mock/assets/mock/diet.json`; abrir una receta de cada tipo de comida. Aviso de IA visible.
5. **Alternativas (US4)**: un ejercicio con cada motivo (con `PAIN` debe salir la recomendación de un profesional); probar "Usar esta", "Mantener la actual" y salir sin decidir; una receta con `#roto`; regenerar la rutina completa con `#lento` (el contenido actual sigue visible y el botón deshabilitado) y con `#falla` (el contenido intacto y "Reintentar"); un doble toque solo envía una petición.
6. **Sin conexión (US5)**: con los datos cargados, activar el modo avión y reabrir. Rutina, Dieta y Progreso se ven con el indicador de "puede no estar actualizado"; las acciones de red quedan deshabilitadas con su mensaje. Probar también con una instalación limpia en modo avión (estado vacío).
7. **Progreso (US6)**: el peso del onboarding aparece como primer registro; registrar un peso hoy; intentar una fecha futura (bloqueada); repetir una fecha (avisa y actualiza); el peso actual del Perfil coincide con el último registro; la gráfica con 1 y con varios puntos.
8. **Perfil (US7)**: editar cada sección; las 4 opciones de regeneración; salir con cambios sin guardar (pide confirmación); guardar con `#falla` en el campo de lesiones (el guardado falla y los datos editados se conservan); cerrar sesión (otra cuenta no ve los datos anteriores); eliminar la cuenta escribiendo `ELIMINAR`.
9. **Transversal**: cambiar la fecha del emulador a las 23:59 y esperar a las 00:00 con la app abierta y en segundo plano (al volver se actualiza "hoy"); revisar que no se vea ningún texto en inglés ni ningún error técnico; repetir los pasos 1, 3 y 6 en el emulador con API 26.

## Resultado esperado

Los 9 puntos pasan en ambos emuladores, `lintMockDebug` no añade warnings y `assembleRealDebug` compila. Con el backend disponible, se repiten los pasos 1, 3, 5 y 7 con `installRealDebug`, ignorando los valores especiales del mock.
