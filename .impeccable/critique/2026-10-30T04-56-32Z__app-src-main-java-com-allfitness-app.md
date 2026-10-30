---
target: app completa (mockDebug)
total_score: 22
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 3
target_identity: "file:C:\\Users\\USER\\Desktop\\Allfitness\\app\\src\\main\\java\\com\\allfitness\\app"
timestamp: 2026-10-30T04-56-32Z
slug: app-src-main-java-com-allfitness-app
---
Method: design review only (detector not applicable, native platform)
Evidencia: 31 capturas emulador `visual-audit/2026-10-29/` + código routines/, nutrition/, strings.xml.

## Heurísticas (22/40, Aceptable)
| # | Heurística | Nota | Problema |
|---|---|---|---|
| 1 | Visibilidad del estado | 2 | Espera de 60 s = spinner suelto; marcar ejercicio no cambia la tarjeta; sin "3 de 6" |
| 2 | Mundo real | 3 | "Entre 62 y 62 kg", "Objetivo" duplicado, "Series: 3" |
| 3 | Control y libertad | 3 | OK |
| 4 | Consistencia | 2 | "Pedir alternativa" de 3 formas; secundarios parecen deshabilitados; verde/lavanda/menta |
| 5 | Prevención de errores | 2 | Checkboxes de días no-hoy se ven activos pero no responden |
| 6 | Reconocer vs recordar | 2 | Unidades solo al enfocar; "Dom" oculto; descanso = entreno visualmente |
| 7 | Flexibilidad | 1 | Rutina no lleva a hoy; terminar sesión al final de la lista |
| 8 | Estética | 2 | Aviso IA en el mejor espacio; tarjetas idénticas; mitades vacías |
| 9 | Recuperación de errores | 3 | Mensajes específicos con reintentar y caché |
| 10 | Ayuda | 2 | Poca ayuda contextual |

## Especificidad
Intercambiable: M3 base con primario verde (lavanda, Roboto, Card idénticas). Solo el contenido (comida local) tiene carácter; datos en texto corrido.

## Funciona
- Regenerar no destruye; errores concretos con salida (error_ai_unavailable, offline_banner_with_date).
- Sheet de alternativa pide motivo (posicionamiento).
- Contenido local y concreto.

## Problemas prioritarios
1. [P1] "Hoy" no está a un vistazo: 7 tarjetas iguales, hoy es la 5.ª en menta; dieta del día en otra pestaña. Fix: tarjeta héroe Hoy + semana compacta + línea de dieta del día. /impeccable layout
2. [P1] Entrenar no tiene forma: marcar no cambia la tarjeta, sin progreso, "Terminar sesión" al final; checkbox no-hoy inerte (onCheckedChange=null sin enabled=false, routines/ExerciseItem.kt:42). Fix: barra inferior fija con progreso+terminar, tarjeta hecha atenuada, cifras grandes. /impeccable harden + layout
3. [P1] Sin identidad: M3 por defecto, mezcla verde/lavanda/menta, títulos peso regular. Fix: esquema derivado del verde, escala tipográfica propia, cifras tabulares. /impeccable colorize + typeset
4. [P2] Aviso IA ocupa el top de cada pantalla. Fix: línea compacta al pie o ícono info junto al título. /impeccable distill
5. [P2] Espera de primera generación es pantalla muerta (valle emocional). Fix: etapas visibles + resumen de lo elegido + revelación del plan. /impeccable onboard

## Personas
- Jordan: "0/3" bajo Edad; unidades ocultas; chip seleccionado sin check; "Objetivo" duplicado.
- Casey: bajar hasta el final para terminar; columna de checkbox de ~200 px; sin feedback al marcar.
- Marcela (principiante en casa, Bogotá): receta = muro de texto; Progreso con 1 punto y "Entre 62 y 62 kg".

## Carga cognitiva
Moderada (3/8 fallan: jerarquía, foco único, revelación progresiva). Sheet de 5 radios; selector de 7 días con scroll oculto.

## Recorrido emocional
Pico (plan listo) tras spinner mudo; final (sesión terminada) sin reconocimiento.

## Menores
Ayuda "Mínimo 8 caracteres" en login; enlaces legales desalineados y CTA oculto por teclado; kcal desalineadas; gráfica con 1 punto debería ser estado vacío; eliminar cuenta deshabilitado gris neutro; punto M3 al final de la barra del onboarding.

## Preguntas
- ¿Pestaña "Hoy" en vez de "Rutina"?
- ¿Modo "en sesión" para el día?
- ¿Fitness para principiantes que no intimida ni parece un formulario?
