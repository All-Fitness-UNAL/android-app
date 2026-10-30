---
target: app completa (mockDebug, rediseño Línea)
total_score: 30
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 0
target_identity: "file:C:\\Users\\USER\\Desktop\\Allfitness\\app\\src\\main\\java\\com\\allfitness\\app"
timestamp: 2026-10-30T08-25-15Z
slug: app-src-main-java-com-allfitness-app
---
Method: design review only (detector not applicable, native platform)
Evidencia: 64 capturas emulador `visual-audit/redesign/final/` (flujo + claro/oscuro/130/200), T11 (tableta simulada, transiciones) y código.

## Heurísticas (30/40, Bueno; antes 22/40)
| # | Heurística | Nota | Hallazgo |
|---|---|---|---|
| 1 | Visibilidad del estado | 3 | Barra de sesión, etapas de IA, aviso sin conexión; regenerar plan dice "una alternativa" |
| 2 | Mundo real | 3 | Ruta/estaciones, unidades visibles; "Objetivo" duplicado |
| 3 | Control y libertad | 3 | Mantener la actual, descartar cambios, confirmar regenerar, atrás predictivo |
| 4 | Consistencia | 3 | Secundario tonal único, color por sección; DatePicker en inglés si el teléfono lo está |
| 5 | Prevención de errores | 3 | Rangos visibles, fecha futura deshabilitada, día pasado bloqueado con motivo, ELIMINAR |
| 6 | Reconocer vs recordar | 3 | Hoy a un vistazo, estados de estación, tiras etiquetadas |
| 7 | Flexibilidad | 3 | "Ver sesión de hoy" en el primer viewport, terminar siempre visible |
| 8 | Estética | 3 | Una cifra protagonista por pantalla; aviso IA arriba (4 líneas al 200 %) |
| 9 | Recuperación de errores | 3 | Mensajes concretos con reintentar; fallo de perfil genérico |
| 10 | Ayuda | 3 | Ayuda en el punto de necesidad |

## Especificidad
Ya no es intercambiable: línea con estaciones, Señal con "HOY" amarillo, color por sección, cifras en Archivo. Formularios/hojas/diálogos M3 tematizados (Operate).

## Funciona
- Entrenar tiene forma: hecho atenuado, tramo que se llena, terminar fijo, estación rellena.
- Espera de IA con Señal y etapas; regenerar deja el contenido visible.
- Progreso: cifra protagonista, gráfica legible, historial alineado.

## Problemas prioritarios
1. [P2] Espera del primer plan sin cubrir barras del sistema (franjas claras en el pico emocional). /impeccable polish
2. [P2] Barra de pestañas aparece de golpe tras un detalle y falta en la vista previa del atrás predictivo. /impeccable polish
3. [P2] Paso 3 del onboarding: 4 decisiones, 8 chips de equipo, 7 días. /impeccable distill
4. [P3] Copia: regenerar plan dice "una alternativa"; "Objetivo" duplicado. /impeccable clarify

## Personas
- Jordan: paso 3 denso; "Objetivo" duplicado; DatePicker en inglés.
- Casey: barra de sesión fija OK; apaisado pierde ~20 % de alto; botón grande por tarjeta alarga la lista.
- Marcela: receta legible; "RACHA 4 semanas" vs "1/3 sesiones" sin explicar.

## Carga cognitiva
Moderada-baja (2/8: opciones mínimas en paso 3 y hoja de 5 motivos).

## Recorrido emocional
Pico con preámbulo; final reconocido (estación rellena + mensaje en la barra de sesión). Valle restante: paso 3.

## Menores
CTA de hoy tras terminar sigue "Ver sesión de hoy"; gráfica ~95 dp al 200 %; enlace del login en dos líneas al 200 %.

## Preguntas
- ¿La Señal de hoy pasa a "resumen" cuando la sesión está terminada?
- ¿El aviso de IA al pie del contenido en vez de encabezarlo?
