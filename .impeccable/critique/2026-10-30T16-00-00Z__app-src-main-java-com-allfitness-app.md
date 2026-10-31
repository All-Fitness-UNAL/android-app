---
target: app completa (mockDebug, interfaz Figma)
total_score: 31
max_score: 40
na_heuristics:
p0_count: 0
p1_count: 0
target_identity: "file:C:\\Users\\USER\\Desktop\\Allfitness\\app\\src\\main\\java\\com\\allfitness\\app"
timestamp: 2026-10-30T16-00-00Z
slug: app-src-main-java-com-allfitness-app
---
Method: design review only (detector not applicable, native platform). Revisión en el hilo, sin subagentes.
Evidencia: `visual-audit/figma/final/flujo/` (quickstart 1–8, 50 capturas), `visual-audit/figma/final/{100,130,200}/` y los 7 frames de `docs/design/figma/miniatura.png` + `arbol-nodos.txt`.
Antes: 22/40 (MVP), 30/40 (Línea).

## Heurísticas (31/40, Bueno)
| # | Heurística | Nota | Hallazgo |
|---|---|---|---|
| 1 | Visibilidad del estado | 3 | Anillos Hoy/Semana/Racha/Kcal en Inicio, "Ejercicio N de M" con barra, etapas de IA; regenerar bloquea la hoja con su mensaje |
| 2 | Mundo real | 3 | Copia en español, unidades visibles; "Sesiones" + "4 semanas de racha" sin decir qué cuenta la racha |
| 3 | Control y libertad | 3 | Mantener la actual, descartar cambios, confirmar reemplazo del plan, Anterior/Saltar |
| 4 | Consistencia | 3 | Un verde, píldoras, tarjetas de 16 dp en todas las pestañas; hoja y DatePicker con textos del sistema en inglés |
| 5 | Prevención de errores | 3 | Rangos visibles, día pasado bloqueado con motivo, fecha repetida pregunta, ELIMINAR escrito |
| 6 | Reconocer vs recordar | 3 | "Entreno de hoy" y "Tu dieta de hoy" en el primer viewport de Inicio |
| 7 | Flexibilidad | 3 | CTA de entreno directo a la sesión, pestañas de días, alternativa desde ejercicio y receta |
| 8 | Estética | 4 | Fiel al Figma: fondo #11111E, tarjetas #1C1C2C con hilo, Outfit/Manrope, un brillo verde por pantalla |
| 9 | Recuperación de errores | 3 | Mensajes concretos + Reintentar; guardar perfil con fallo conserva lo editado; el fallo genérico del perfil sigue vago |
| 10 | Ayuda | 3 | Ayuda en el punto de necesidad (rangos, aviso de dolor, aviso sin conexión bajo el control) |

## Especificidad
Ya no es Material de serie ni "Línea": es el prototipo de Figma traducido a M3 (oscuro, acento verde, logo-mark con rayo, anillos). El riesgo es el inverso: el oscuro con verde es un patrón de categoría, y lo que lo distingue es la exactitud de cifras (Outfit tabular) y la honestidad de datos (sin fotos ni kcal consumidas inventadas).

## Funciona
- Inicio responde "qué toca hoy" en un vistazo: anillos, entreno y dieta del día.
- Modo sesión de un ejercicio por página con un solo primario; día pasado bloqueado con el motivo en el pie.
- Hoja de alternativa: aviso de dolor al elegir el motivo, propuesta completa antes de decidir, píldora deshabilitada visible sin conexión.

## Problemas prioritarios
1. [P2] Tira de días de Dieta: abre desplazada (Lun fuera, Mar partido). /impeccable layout
2. [P2] Textos de M3 en inglés con el teléfono en inglés (hoja, DatePicker). /impeccable harden
3. [P2] Paso 3 del onboarding sigue denso (nivel, 7 días, duración, lugar, 8 equipos, lesiones). /impeccable distill
4. [P3] Día de descanso en Inicio: tarjeta grande con solo texto; el Figma pone ahí el entreno. Aceptable, es el dato real. /impeccable polish

## Personas
- Jordan (primera vez): onboarding claro en 4 pasos con "Paso N de 4"; paso 3 denso.
- Casey (una mano, entrenando): Anterior/Saltar abajo, 56 dp; alternativa a un toque.
- Marcela (cocinando): receta tipográfica legible, 2×2 macros; "Pedir alternativa" fijo abajo.

## Diferencias con el Figma
Ver `.superpowers/sdd/2026-10-30-interfaz-figma/task-12-report.md` (ruling-candidate vs pendiente).
