# Contrato: Autenticación y cuenta

## POST /auth/register

Crea la cuenta y devuelve la sesión. Sin token.

**Request**
```json
{
  "name": "Laura Gómez",
  "email": "laura@correo.com",
  "password": "Secreta123",
  "acceptedLegalVersion": "2026-10"
}
```
- `acceptedLegalVersion` es obligatorio: es la versión de los términos y de la política de datos que el usuario aceptó (FR-002a). Backend MUST guardarlo junto con la fecha de aceptación.

**201 Created**
```json
{
  "token": "eyJhbGciOi...",
  "user": { "id": "u_123", "name": "Laura Gómez", "email": "laura@correo.com" },
  "onboardingCompleted": false
}
```

**Errores**: 400 `VALIDATION_ERROR` (`fields`: `name`, `email`, `password`, `acceptedLegalVersion`), 409 `EMAIL_TAKEN`.

## POST /auth/login

**Request**
```json
{ "email": "laura@correo.com", "password": "Secreta123" }
```

**200 OK**: mismo cuerpo que el 201 del registro (`onboardingCompleted` dice si la app debe ir al onboarding).

**Errores**: 400 `VALIDATION_ERROR`, 401 `INVALID_CREDENTIALS` (la app **no** lo trata como sesión expirada).

## POST /auth/logout

Invalida el token actual. Si falla, la app no se detiene: cierra la sesión localmente de todas formas.

**204 No Content**

## GET /me

Se llama al abrir la app con una sesión guardada, para confirmar que el token sigue válido y saber si el onboarding está completo.

**200 OK**
```json
{
  "user": { "id": "u_123", "name": "Laura Gómez", "email": "laura@correo.com" },
  "onboardingCompleted": true
}
```

**Errores**: 401 `UNAUTHORIZED`.

## DELETE /me

Elimina la cuenta y todos sus datos (perfil, planes, registros), e invalida el token. La app solo borra los datos locales cuando recibe 204 (FR-034).

**204 No Content**

**Errores**: 401 `UNAUTHORIZED`, 500 `INTERNAL_ERROR`.
