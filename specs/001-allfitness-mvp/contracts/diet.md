# Contrato: Dieta

## GET /diet

Devuelve la dieta activa. Al igual que la rutina, el backend puede actualizarla e incrementar `version` (FR-011a).

**200 OK**
```json
{
  "id": "d_321",
  "version": 1,
  "updatedAt": "2026-10-28T05:00:00Z",
  "days": [
    {
      "dayOfWeek": "MONDAY",
      "totals": { "kcal": 2150, "proteinG": 140.0, "carbsG": 230.5, "fatG": 70.0 },
      "meals": [
        {
          "id": "m_1",
          "type": "BREAKFAST",
          "recipe": {
            "name": "Arepa con huevo y aguacate",
            "ingredients": [
              { "name": "Arepa de maíz", "amount": 80, "unit": "G" },
              { "name": "Huevo", "amount": 100, "unit": "G" },
              { "name": "Aguacate", "amount": 50, "unit": "G" },
              { "name": "Café sin azúcar", "amount": 200, "unit": "ML" }
            ],
            "steps": [
              "Asa la arepa 4 minutos por lado.",
              "Prepara los huevos revueltos sin aceite.",
              "Sirve con el aguacate en tajadas."
            ],
            "prepMinutes": 15,
            "perServing": { "kcal": 520, "proteinG": 22.0, "carbsG": 45.0, "fatG": 27.0 }
          }
        }
      ]
    }
  ]
}
```
- `days` MUST tener exactamente 7 elementos, de `MONDAY` a `SUNDAY`, y cada día tantas comidas como `mealsPerDay` (el ejemplo está recortado).
- `type`: `BREAKFAST`, `LUNCH`, `DINNER` o `SNACK`.
- `totals` y `perServing` los calcula el backend; la app no suma nada (FR-017).

**Errores**: 401, 404 `NO_ACTIVE_PLAN` (estado vacío con "Generar dieta").

## POST /diet/generate (IA, hasta 90 s)

Genera una dieta nueva y reemplaza la activa en cuanto termina.

**Request**
```json
{ "comment": "Menos tiempo de preparación entre semana" }
```
- `comment` es opcional (máx. 300 caracteres). Puede ser `{}`.

**200 OK**: la dieta nueva (mismo cuerpo que `GET /diet`).

**Errores**: 400, 401, 502/503 `AI_UNAVAILABLE`, 504 `AI_TIMEOUT`. Si hay error, la dieta activa MUST quedar intacta.

## Alternativa de una receta (IA, hasta 90 s)

### POST /diet/meals/{mealId}/alternative

Devuelve una **propuesta**; la dieta no cambia hasta aceptarla.

**Request**
```json
{ "reason": "NO_INGREDIENTS", "comment": "No tengo aguacate" }
```
- `reason`: `DISLIKE`, `NO_INGREDIENTS`, `TOO_ELABORATE` o `DIET_MISMATCH` (obligatorio). `comment` es opcional (máx. 300 caracteres).

**200 OK**
```json
{
  "proposalId": "p_654",
  "recipe": {
    "name": "Arepa con huevo y queso campesino",
    "ingredients": [
      { "name": "Arepa de maíz", "amount": 80, "unit": "G" },
      { "name": "Huevo", "amount": 100, "unit": "G" },
      { "name": "Queso campesino", "amount": 40, "unit": "G" }
    ],
    "steps": ["Asa la arepa 4 minutos por lado.", "Prepara los huevos revueltos.", "Sirve con el queso."],
    "prepMinutes": 12,
    "perServing": { "kcal": 500, "proteinG": 28.0, "carbsG": 42.0, "fatG": 23.0 }
  }
}
```

**Errores**: 400, 401, 404 `NOT_FOUND`, 502/503 `AI_UNAVAILABLE`, 504 `AI_TIMEOUT`.

### POST /diet/meals/{mealId}/alternative/{proposalId}/accept

Reemplaza la receta de esa comida y recalcula en el backend los `totals` del día.

**Request**: vacío.

**200 OK**: la dieta completa actualizada (mismo cuerpo que `GET /diet`).

**Errores**: 401, 409 `PROPOSAL_EXPIRED`.
