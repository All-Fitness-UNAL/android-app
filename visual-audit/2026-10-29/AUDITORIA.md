# Auditoría visual: AllFitness (mockDebug)

- **Fecha:** 2026-10-29
- **Dispositivo:** emulador `emulator-5554`, sdk_gphone64_x86_64, Android 17, 1080×2424, 420 dpi
- **Build:** `:app:installMockDebug`, applicationId `com.allfitness.app.mock`, actividad `com.allfitness.app.MainActivity`
- **Usuario de prueba (mock):** Laura Gomez / laura@allfitness.co / Segura123

Cada `NN-nombre.png` tiene al lado un `.ui.txt` con los textos y los bounds del árbol de UI (uiautomator).

## Problemas globales (se repiten en varias pantallas)

1. **La paleta no es coherente.** La marca es verde, pero las superficies usan el lavanda/gris por defecto de Material 3. Pasa en las tarjetas, la barra de navegación inferior, el bottom sheet y el diálogo (16, 18, 20, 22, 25, 26, 27). El "Hoy" usa un menta saturado que no pertenece a ninguna de las dos familias (16).
2. **No hay identidad visual.** No hay logo, imágenes, ilustraciones ni iconografía, salvo la de la barra inferior. El login y el registro son formularios sueltos y más de la mitad inferior de la pantalla queda vacía (01, 03).
3. **La tipografía es la de serie.** Se usan Roboto y la escala M3 por defecto. Los títulos de pantalla ("Tu rutina", "Tu dieta") tienen peso regular y fino, así que la jerarquía es débil. Los datos se muestran como "Etiqueta: valor" en texto corrido (18, 27).
4. **Las listas son monótonas.** Todas las tarjetas tienen el mismo color, tamaño y peso. Un día de descanso y un día de entrenamiento solo se distinguen por el texto (16).
5. **El aviso de IA se repite arriba en cada pantalla** (rutina, día, dieta, receta, perfil) y ocupa el espacio más valioso.
6. **Los botones secundarios parecen deshabilitados.** "Regenerar rutina/dieta", "Cerrar sesión" y "Volver" son outline gris con texto gris (17, 22, 28). "Pedir alternativa" es un text button sangrado en el ejercicio (18) y un outline a ancho completo en la receta (23).
7. **Hay contenido cortado bajo la barra inferior o el borde.** Se ve "Regenerar rutina" a medias (16, 31), el campo "Lesiones" cortado (10) y el chip "Dom" reducido a una astilla sin indicio de scroll (22).

## Por pantalla

| # | Pantalla | Observaciones |
|---|----------|---------------|
| 01-02 | Login | Formulario sin marca. La ayuda "Mínimo 8 caracteres" en el login sobra. El estado de error es correcto (borde y texto rojos). |
| 03-05 | Registro | Los dos enlaces legales en columnas desalineadas: uno ocupa 1 línea y el otro 2. Con el teclado abierto, "Crear cuenta" queda oculto (04). |
| 06-07 | Onboarding 1 | Los contadores "0/3" y "0/5" en campos numéricos son ruido. Las unidades (años, cm, kg) solo aparecen al enfocar el campo. El chip seleccionado solo tiene un fondo tenue, sin check. La barra de progreso termina en un "punto" de M3 que parece un artefacto. |
| 08 | Onboarding 2 | "Objetivo" aparece dos veces (título y etiqueta). Unas 3/4 partes de la pantalla quedan vacías. |
| 10-11 | Onboarding 3 | Los chips 1–7 dejan el "7" huérfano en una segunda fila. Los chips numéricos tienen un margen izquierdo distinto al de los demás chips. El último campo queda cortado sobre los botones. |
| 13 | Onboarding 4 | Mismo patrón que el paso 3. Los chips 3/4/5 son pequeños y quedan aislados. |
| 15 | Generando | Spinner y texto centrados sobre la pantalla vacía, sin nada de marca para la espera de "hasta un minuto". |
| 16-17 | Rutina semana | Lista plana de 7 tarjetas iguales. "Hoy" en menta fuera de paleta. No hay resumen (duración, nº de ejercicios). |
| 18-21 | Rutina día | Una columna izquierda de unos 200 px solo para el checkbox desperdicia ancho. No hay imagen del ejercicio. Al marcar un ejercicio solo cambia el checkbox; la tarjeta no da feedback de "hecho" (19). En días que no son hoy, los checkboxes parecen activos pese al aviso "Solo puedes marcar… hoy" (34). |
| 20 | Pedir alternativa (sheet) | Es correcto pero genérico: fondo lavanda y radios por defecto. |
| 22 | Dieta semana | Los macros van en texto plano, sin visualización. Las kcal quedan desalineadas verticalmente respecto al nombre del plato. El selector de días no deja ver que hace scroll. |
| 23 | Receta | Muro de texto con el mismo espaciado entre secciones. Ingredientes sin viñetas y sin imagen. |
| 25-26 | Progreso | La gráfica es un único punto dentro de una tarjeta enorme. "Entre 62 y 62 kg" suena redundante. La fila del historial queda fuera del estilo de las tarjetas. El resumen semanal es solo texto, sin anillo ni barra. |
| 27-29 | Perfil / editar | No hay avatar. "Editar" es un enlace de texto. Cada tarjeta repite una pila de valores sin etiquetas visuales. La edición (29) reutiliza el formulario del onboarding. |
| 30 | Eliminar cuenta | El botón deshabilitado es gris neutro y no anticipa visualmente que la acción es destructiva. |

## Variantes del sistema

- **Modo oscuro (31-32):** funciona sin roturas. Las superficies son gris con matiz púrpura y "Hoy" va en verde oscuro, lo que resulta más coherente que el modo claro.
- **Fuente al 130 % (33-34):** no hay recortes ni solapamientos en las pantallas probadas.

## No inspeccionado

DatePicker de "Registrar peso" y su guardado, el resultado de enviar una alternativa, "Regenerar", "Terminar sesión de hoy", cerrar sesión y sesión expirada, los enlaces legales (externos), el resto de secciones de edición del perfil, y los estados sin conexión, de error de red y vacíos.
