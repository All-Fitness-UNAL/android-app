# Contrato: Perfil

El peso **no** viaja en el perfil. Se guarda con `PUT /progress/weights/{date}` ([progress.md](progress.md)), y `GET /profile` solo lo devuelve como lectura (FR-026a). Así, guardar otra sección del perfil no crea registros de peso por accidente.

## GET /profile

**200 OK**
```json
{
  "age": 28,
  "sex": "FEMALE",
  "heightCm": 165,
  "currentWeightKg": 62.5,
  "goal": "GAIN_MUSCLE",
  "level": "BEGINNER",
  "daysPerWeek": 3,
  "sessionMinutes": 45,
  "location": "GYM",
  "equipment": ["DUMBBELLS", "BARBELL", "BENCH", "GYM_MACHINES"],
  "injuries": "Molestia leve en rodilla derecha",
  "dietType": "OMNIVORE",
  "allergies": ["LACTOSE", "OTHER"],
  "otherAllergy": "Kiwi",
  "dislikedFoods": "Hígado, berenjena",
  "mealsPerDay": 4
}
```
- `currentWeightKg` es el registro de peso más reciente; vale `null` si no hay ninguno.
- `injuries`, `otherAllergy` y `dislikedFoods` son opcionales.

**Errores**: 401 `UNAUTHORIZED`, 404 `NOT_FOUND` (aún no hay perfil: la app manda al onboarding).

## PUT /profile

Crea o reemplaza el perfil completo. Se usa al terminar el onboarding y al guardar cualquier sección en el Perfil (la app envía el perfil completo con la sección modificada). Es idempotente. **No** regenera los planes (FR-030); el primer `PUT` marca `onboardingCompleted = true`.

**Request**: mismo cuerpo que el `GET`, sin `currentWeightKg`.

**200 OK**: el perfil guardado (igual que el `GET`).

**Validación en backend** (igual que la app): `age` 16–100, `heightCm` 100–250, `daysPerWeek` 1–7, `sessionMinutes` 15–120, `mealsPerDay` 3–5, `equipment` con al menos 1 elemento, `otherAllergy` obligatorio si `allergies` incluye `OTHER` (máx. 100 caracteres), textos libres de máx. 300 caracteres.

**Errores**: 400 `VALIDATION_ERROR` (`fields`), 401 `UNAUTHORIZED`.

## Orden al terminar el onboarding (R8)

1. `PUT /profile`
2. `PUT /progress/weights/{hoy}` con el peso del onboarding
3. En paralelo: `POST /routine/generate` y `POST /diet/generate` (sin `comment`)
