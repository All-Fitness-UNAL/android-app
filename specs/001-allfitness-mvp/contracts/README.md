# Contrato API: All Fitness MVP

Propuesto por el equipo frontend para que lo implemente backend. Ningún endpoint existe todavía; la app trabaja con Fakes que devuelven estos mismos JSON (flavor `mock`).

| Archivo | Endpoints | Pantallas |
|---------|-----------|-----------|
| [auth.md](auth.md) | registro, login, logout, `/me` | Login, Registro, arranque, Eliminar cuenta |
| [profile.md](profile.md) | `/profile` | Onboarding, Perfil |
| [routine.md](routine.md) | rutina, registros de entrenamiento, alternativas de ejercicio | Rutina, Día, Ejercicio |
| [diet.md](diet.md) | dieta, alternativas de receta | Dieta, Receta |
| [progress.md](progress.md) | pesos, resumen | Progreso, Onboarding, Perfil |

## Convenciones

- **Base URL**: `{BASE_URL}/api/v1`. El valor de `BASE_URL` lo define backend; cada flavor lo configura aparte.
- **Formato**: JSON UTF-8 con campos en camelCase. Los enums van en MAYÚSCULAS (`LOSE_FAT`).
- **Unidades**: kg, cm, kcal, gramos (`G`) y mililitros (`ML`). Pesos con máximo 1 decimal.
- **Instantes**: ISO-8601 en UTC (`"2026-10-28T14:00:00Z"`).
- **Días del calendario**: fecha ISO-8601 sin hora (`"2026-10-28"`), calculada con la fecha local del dispositivo. El backend MUST aceptar como "hoy" cualquier fecha entre su fecha UTC −1 día y +1 día (por las zonas horarias).
- **Semana**: de lunes a domingo; `weekStart` siempre es un lunes.
- **Autenticación**: `Authorization: Bearer <token>` en todo, salvo `POST /auth/register` y `POST /auth/login`. El token no expira durante el MVP salvo que lo invalide el servidor (logout, eliminación de cuenta o revocación).
- **Campos opcionales**: pueden venir omitidos o como `null`. La app no se cierra si falta uno (constitución IV).

## Errores

Todas las respuestas de error usan este cuerpo:

```json
{ "code": "VALIDATION_ERROR", "message": "heightCm must be between 100 and 250", "fields": ["heightCm"] }
```

La app nunca muestra `message`: solo usa `code` (y `fields`, para marcar campos).

| HTTP | `code` | Cuándo |
|------|--------|--------|
| 400 | `VALIDATION_ERROR` | Cuerpo inválido o fuera de rango |
| 400 | `FUTURE_DATE` | Fecha posterior a "hoy" |
| 400 | `INVALID_DATE` | Sesión o ejercicio en una fecha que no es "hoy" |
| 401 | `INVALID_CREDENTIALS` | Solo en `POST /auth/login` |
| 401 | `UNAUTHORIZED` | Token ausente, inválido o revocado (la app cierra la sesión) |
| 404 | `NO_ACTIVE_PLAN` | El usuario no tiene rutina o dieta activa |
| 404 | `NOT_FOUND` | Recurso inexistente (ejercicio, comida) |
| 409 | `EMAIL_TAKEN` | Registro con un correo existente |
| 409 | `PROPOSAL_EXPIRED` | Se intenta aceptar una propuesta que ya no existe o no corresponde al plan actual |
| 502/503 | `AI_UNAVAILABLE` | Falló el proveedor de IA |
| 504 | `AI_TIMEOUT` | La IA no respondió a tiempo |
| 500 | `INTERNAL_ERROR` | Cualquier otro error |

## Tiempos

- Endpoints normales: la app espera hasta **15 s**.
- Endpoints de IA (`*/generate`, `*/alternative`): la app espera hasta **90 s**. Backend MUST responder (con éxito o con `AI_TIMEOUT`) antes de 85 s.
