# Contrato: Rutina y entrenamiento

## GET /routine

Devuelve la rutina activa. El backend puede actualizarla en cualquier momento (progresión de cargas, FR-011a); cuando lo hace, incrementa `version`. Con conexión, la app la pide cada vez que se abre la pestaña.

**200 OK**
```json
{
  "id": "r_789",
  "version": 3,
  "updatedAt": "2026-10-27T05:00:00Z",
  "days": [
    {
      "dayOfWeek": "MONDAY",
      "type": "TRAINING",
      "title": "Tren superior",
      "exercises": [
        {
          "id": "ex_1",
          "name": "Press de banca con mancuernas",
          "muscleGroup": "Pecho",
          "sets": 4,
          "reps": 10,
          "durationSeconds": null,
          "restSeconds": 90,
          "suggestedWeightKg": 12.5,
          "instructions": "Baja las mancuernas hasta la altura del pecho y empuja sin bloquear los codos.",
          "imageUrl": "https://cdn.allfitness.co/ex/press-banca-mancuernas.webp"
        },
        {
          "id": "ex_2",
          "name": "Plancha",
          "muscleGroup": "Core",
          "sets": 3,
          "reps": null,
          "durationSeconds": 40,
          "restSeconds": 60,
          "suggestedWeightKg": null,
          "instructions": "Mantén el cuerpo recto, apoyado en antebrazos y puntas de los pies.",
          "imageUrl": null
        }
      ]
    },
    { "dayOfWeek": "TUESDAY", "type": "REST", "title": null, "exercises": [] }
  ]
}
```
- `days` MUST tener exactamente 7 elementos, de `MONDAY` a `SUNDAY` (el ejemplo está recortado).
- Cada ejercicio trae `reps` **o** `durationSeconds`.
- Los ejercicios que no cambian entre versiones MUST conservar su `id`, porque las marcas de hoy se asocian por `id`.

**Errores**: 401, 404 `NO_ACTIVE_PLAN` (la app muestra el estado vacío con "Generar rutina").

## POST /routine/generate (IA, hasta 90 s)

Genera una rutina nueva a partir del perfil y reemplaza la activa en cuanto termina (US4 escenario 7). La usan el onboarding, la regeneración desde el Perfil y "Regenerar rutina completa".

**Request**
```json
{ "comment": "Prefiero más ejercicios de pierna" }
```
- `comment` es opcional (máx. 300 caracteres). Puede ser `{}`.

**200 OK**: la rutina nueva (mismo cuerpo que `GET /routine`).

**Errores**: 400 `VALIDATION_ERROR`, 401, 502/503 `AI_UNAVAILABLE`, 504 `AI_TIMEOUT`. Si hay error, la rutina activa MUST quedar intacta.

## Registros de entrenamiento

### GET /workout-logs?from=2026-10-27&to=2026-11-02

Devuelve los registros de la semana (de lunes a domingo). Los días sin actividad no aparecen.

**200 OK**
```json
[
  { "date": "2026-10-27", "completedExerciseIds": ["ex_1", "ex_2"], "sessionFinished": true },
  { "date": "2026-10-28", "completedExerciseIds": ["ex_5"], "sessionFinished": false }
]
```

### PUT /workout-logs/{date}/exercises/{exerciseId}

Marca o desmarca un ejercicio del día. Es idempotente.

**Request**
```json
{ "completed": true }
```

**200 OK**: el registro del día (un elemento como los del `GET`).

**Errores**: 400 `INVALID_DATE` (la fecha no es "hoy" o la sesión ya está terminada), 404 `NOT_FOUND` (el ejercicio no pertenece al día en la rutina actual), 401.

### POST /workout-logs/{date}/finish

Termina la sesión del día, aunque no estén todos los ejercicios marcados (FR-014). Si ya estaba terminada, responde 200 con el mismo registro.

**200 OK**: el registro del día con `"sessionFinished": true`.

**Errores**: 400 `INVALID_DATE` (la fecha no es "hoy" o el día es de descanso), 401.

## Alternativa de un ejercicio (IA, hasta 90 s)

### POST /routine/exercises/{exerciseId}/alternative

Devuelve una **propuesta**. La rutina activa no cambia hasta que se acepta (FR-022a).

**Request**
```json
{ "reason": "NO_EQUIPMENT", "comment": "Solo tengo bandas elásticas" }
```
- `reason`: `DISLIKE`, `NO_EQUIPMENT`, `PAIN`, `TOO_HARD` o `TOO_EASY` (obligatorio). `comment` es opcional (máx. 300 caracteres).

**200 OK**
```json
{
  "proposalId": "p_456",
  "exercise": {
    "id": "ex_9",
    "name": "Press de pecho con banda elástica",
    "muscleGroup": "Pecho",
    "sets": 4,
    "reps": 12,
    "durationSeconds": null,
    "restSeconds": 60,
    "suggestedWeightKg": null,
    "instructions": "Ancla la banda detrás de ti a la altura del pecho y empuja hacia adelante.",
    "imageUrl": null
  }
}
```

**Errores**: 400, 401, 404 `NOT_FOUND`, 502/503 `AI_UNAVAILABLE`, 504 `AI_TIMEOUT`.

### POST /routine/exercises/{exerciseId}/alternative/{proposalId}/accept

Reemplaza el ejercicio por el propuesto, en todas las semanas del plan. Para descartar una propuesta no hay que llamar a nada: el backend la expira sola (se recomienda a las 24 h).

**Request**: vacío.

**200 OK**: la rutina completa actualizada (mismo cuerpo que `GET /routine`, con `version` incrementada).

**Errores**: 401, 409 `PROPOSAL_EXPIRED`.
