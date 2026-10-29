# Contrato: Progreso

## GET /progress/weights

Historial completo de peso, del más reciente al más antiguo.

**200 OK**
```json
[
  { "date": "2026-10-28", "weightKg": 77.5 },
  { "date": "2026-10-14", "weightKg": 78.2 },
  { "date": "2026-09-01", "weightKg": 80.0 }
]
```
Si no hay registros, devuelve una lista vacía (`[]`).

## PUT /progress/weights/{date}

Crea o actualiza el único registro de esa fecha (FR-025). Se usa desde Progreso, al terminar el onboarding y al editar el peso en el Perfil (con la fecha de hoy).

**Request**
```json
{ "weightKg": 77.5 }
```

**200 OK**
```json
{ "date": "2026-10-28", "weightKg": 77.5 }
```

**Errores**: 400 `VALIDATION_ERROR` (`weightKg` fuera de 30–300 o con más de 1 decimal), 400 `FUTURE_DATE`, 401.

## GET /progress/summary?weekStart=2026-10-27

Resumen de la semana que empieza en `weekStart` (un lunes). Lo calcula todo el backend.

**200 OK**
```json
{
  "weekStart": "2026-10-27",
  "completedSessions": 2,
  "scheduledSessions": 3,
  "weekStreak": 4
}
```
- `scheduledSessions`: días `TRAINING` de la rutina activa en esa semana. Si la rutina cambió a mitad de semana, se cuentan los días de la rutina vigente desde el día del cambio (FR-023a).
- `weekStreak`: semanas consecutivas, hasta la anterior, en las que `completedSessions >= scheduledSessions`, más 1 si la semana actual ya se cumplió.
- Si no hay sesiones, devuelve ceros (la app muestra el estado vacío del resumen).

**Errores**: 400 `VALIDATION_ERROR` (`weekStart` no es lunes), 401.
