# Feature Specification: All Fitness MVP (App Android)

**Feature Branch**: `001-allfitness-mvp` (sin rama creada; se trabaja en `master`)

**Created**: 2026-10-28

**Status**: Draft

**Input**: User description: "MVP de All Fitness: registro e inicio de sesión, onboarding, rutina semanal generada por IA, dieta y recetas, alternativas y feedback a la IA, progreso, perfil de usuario y comportamiento transversal (estados, sin conexión, errores). Fuente completa: `instruccionSpecify.txt`."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registro, inicio de sesión y onboarding (Priority: P1)

Una persona nueva crea su cuenta con nombre, correo y contraseña, completa un onboarding corto con sus datos físicos, objetivo y preferencias de entrenamiento y alimentación, y recibe su primera rutina y su primera dieta. Un usuario que ya tiene cuenta inicia sesión y entra directo a la app; la sesión se mantiene al reabrirla.

**Why this priority**: Sin cuenta y sin perfil no existe ningún plan personalizado; es la puerta de entrada a todo lo demás.

**Independent Test**: Con datos de ejemplo, registrar una cuenta nueva, completar los 4 pasos del onboarding y llegar a la pantalla principal con rutina y dieta generadas; cerrar y reabrir la app y comprobar que sigue la sesión.

**Acceptance Scenarios**:

1. **Given** una persona sin cuenta, **When** se registra con nombre, correo válido y contraseña de 8+ caracteres, **Then** la cuenta se crea y la app la lleva al primer paso del onboarding.
2. **Given** el formulario de registro, **When** el correo ya está registrado, **Then** la app muestra un mensaje claro en español (ej. "Este correo ya tiene una cuenta") y no avanza.
3. **Given** el formulario de login, **When** las credenciales son incorrectas, **Then** la app muestra "Correo o contraseña incorrectos" sin detalles técnicos.
4. **Given** un campo con formato inválido (correo mal escrito, contraseña de menos de 8 caracteres, campo vacío), **When** el usuario intenta enviar, **Then** la app marca el campo con el error y no envía nada.
5. **Given** un paso del onboarding con un valor fuera de rango (ej. estatura 90 cm), **When** el usuario intenta avanzar, **Then** la app indica el rango permitido y no avanza.
6. **Given** un usuario que abandonó el onboarding en el paso 3, **When** vuelve a entrar a la app, **Then** retoma el onboarding en el paso 3 con lo que ya había llenado.
7. **Given** el último paso del onboarding completo, **When** el usuario confirma, **Then** la app envía el perfil, muestra un estado de carga mientras se generan la rutina y la dieta, y al terminar muestra la pantalla principal.
8. **Given** un usuario con sesión iniciada, **When** cierra y reabre la app, **Then** entra directo a la pantalla principal sin volver a iniciar sesión.
9. **Given** un usuario con sesión iniciada, **When** el servidor indica que la sesión ya no es válida, **Then** la app borra la sesión y lo devuelve al Login con un mensaje de que debe iniciar sesión de nuevo.

---

### User Story 2 - Ver y seguir la rutina semanal (Priority: P1)

El usuario abre la app, ve su rutina de la semana con el día de hoy destacado, entra al día, revisa cada ejercicio (series, repeticiones, descanso, peso sugerido, instrucciones), lo marca como completado a medida que entrena y al final marca la sesión como terminada.

**Why this priority**: Seguir la rutina es el uso diario principal de la app y la base del módulo de Progreso.

**Independent Test**: Con una rutina de ejemplo, abrir la pestaña Rutina, entrar al día de hoy, marcar ejercicios y terminar la sesión; comprobar que días pasados y futuros no permiten terminar sesión.

**Acceptance Scenarios**:

1. **Given** un usuario con rutina activa, **When** abre la pestaña Rutina, **Then** ve los 7 días de la semana, los días de entrenamiento con su contenido, los demás como "Descanso" y el día de hoy destacado.
2. **Given** un día de entrenamiento, **When** el usuario lo abre, **Then** ve la lista de ejercicios con nombre, grupo muscular, series, repeticiones o duración, descanso, peso sugerido (si aplica) e instrucciones breves; y la imagen cuando existe.
3. **Given** el día de hoy, **When** el usuario marca un ejercicio como completado, **Then** el ejercicio queda marcado y el cambio se envía al servidor; si el envío falla, la marca se revierte y se muestra un mensaje con opción de reintentar.
4. **Given** el día de hoy, **When** el usuario marca la sesión como terminada, **Then** la sesión queda registrada como terminada y cuenta en su Progreso.
5. **Given** un día pasado o futuro, **When** el usuario lo abre, **Then** puede ver los ejercicios pero no puede marcarlos ni terminar la sesión.
6. **Given** cualquier pantalla de rutina, **When** se muestra, **Then** incluye el aviso de que la rutina es orientativa y no reemplaza a un profesional de salud.

---

### User Story 3 - Ver la dieta y sus recetas (Priority: P2)

El usuario abre la pestaña Dieta, ve las comidas de cada día con los totales de calorías y macros, y abre una receta para ver ingredientes, cantidades, pasos y tiempo de preparación.

**Why this priority**: Es el segundo módulo principal del producto, pero el usuario puede entrenar sin él.

**Independent Test**: Con una dieta de ejemplo, recorrer los 7 días, verificar los totales mostrados y abrir al menos una receta de cada tipo de comida.

**Acceptance Scenarios**:

1. **Given** un usuario con dieta activa, **When** abre la pestaña Dieta, **Then** ve el día de hoy con sus comidas (según el número de comidas elegido) y los totales del día de calorías, proteína, carbohidratos y grasas, y puede cambiar a otro día de la semana.
2. **Given** una comida, **When** el usuario la abre, **Then** ve nombre, ingredientes con cantidades en g o ml, pasos de preparación, tiempo aproximado, y calorías y macros por porción.
3. **Given** cualquier pantalla de dieta, **When** se muestra, **Then** incluye el aviso de que la dieta es orientativa y no reemplaza a un profesional de salud.
4. **Given** valores nutricionales recibidos del servidor, **When** se muestran, **Then** coinciden exactamente con los recibidos (la app no los recalcula).

---

### User Story 4 - Pedir alternativas y regenerar planes (Priority: P2)

Si al usuario no le gusta un ejercicio, una receta, la rutina o la dieta completa, pide una alternativa indicando el motivo y, si quiere, cómo lo preferiría. Mientras la IA genera, sigue viendo el contenido actual; cuando llega la alternativa, reemplaza al anterior.

**Why this priority**: Es el diferenciador del producto (planes que se adaptan al gusto del usuario), pero depende de que existan rutina y dieta.

**Independent Test**: Con datos de ejemplo, pedir una alternativa para un ejercicio con cada motivo, simular una respuesta exitosa, una lenta (cercana a 60 s) y una fallida, y verificar que el contenido actual nunca desaparece antes de tiempo.

**Acceptance Scenarios**:

1. **Given** un ejercicio, **When** el usuario pide una alternativa, **Then** la app le ofrece los motivos "No me gusta", "No tengo el equipo", "Me causa molestia o dolor", "Muy difícil", "Muy fácil" y un campo opcional "¿Cómo lo preferirías?" de máximo 300 caracteres.
2. **Given** una receta, **When** el usuario pide una alternativa, **Then** la app le ofrece los motivos "No me gusta", "No tengo los ingredientes", "Es muy elaborada", "No encaja con mi dieta o alergias" y el mismo campo opcional.
3. **Given** la rutina o la dieta completa, **When** el usuario pide regenerarla, **Then** la app solo pide el texto libre opcional y una confirmación de que el plan actual será reemplazado.
4. **Given** el motivo "Me causa molestia o dolor", **When** el usuario lo selecciona, **Then** la app muestra además una recomendación de consultar a un profesional de salud.
5. **Given** una solicitud enviada, **When** la IA está generando, **Then** el contenido actual sigue visible, se muestra un estado de carga y la acción queda deshabilitada (no se pueden enviar solicitudes duplicadas).
6. **Given** una solicitud en curso, **When** la alternativa llega correctamente (en hasta 60 s), **Then** reemplaza al contenido anterior y la app lo confirma.
7. **Given** una solicitud en curso, **When** falla o supera el tiempo de espera, **Then** el contenido actual se conserva intacto y se muestra un mensaje amigable con opción de reintentar.

---

### User Story 5 - Consultar sin conexión (Priority: P2)

El usuario entrena en un lugar sin señal: puede abrir la app y consultar la rutina, la dieta y el último progreso que ya había cargado, sabiendo que pueden no estar actualizados.

**Why this priority**: Los gimnasios suelen tener mala señal; sin esto la app no sirve durante el entrenamiento.

**Independent Test**: Cargar rutina, dieta y progreso con conexión; activar modo avión, reabrir la app y verificar que se consultan y que las acciones que requieren red están deshabilitadas con mensaje.

**Acceptance Scenarios**:

1. **Given** datos ya cargados y sin conexión, **When** el usuario abre Rutina, Dieta o Progreso, **Then** ve el último contenido cargado y un indicador de que puede no estar actualizado.
2. **Given** sin conexión, **When** el usuario intenta pedir una alternativa, editar su perfil, marcar un ejercicio o sesión, registrar peso, iniciar sesión o registrarse, **Then** la acción está deshabilitada y un mensaje claro explica que requiere conexión.
3. **Given** sin conexión y sin datos cargados previamente, **When** el usuario abre una pestaña, **Then** ve un estado vacío que explica que necesita conexión para cargar su plan.
4. **Given** sin conexión, **When** se recupera la conexión, **Then** las acciones se habilitan de nuevo y el indicador desaparece al actualizar el contenido.

---

### User Story 6 - Registrar y ver progreso (Priority: P3)

El usuario registra su peso corporal y ve cómo evoluciona en una lista y una gráfica, junto con un resumen de sus sesiones de la semana y su racha.

**Why this priority**: Motiva la constancia, pero el valor principal (rutina y dieta) existe sin él.

**Independent Test**: Con datos de ejemplo, registrar pesos en varias fechas, intentar una fecha futura, repetir una fecha existente y revisar lista, gráfica y resumen.

**Acceptance Scenarios**:

1. **Given** la pestaña Progreso, **When** el usuario registra un peso válido (30 a 300 kg) sin cambiar la fecha, **Then** se guarda con la fecha de hoy y aparece en la lista y la gráfica.
2. **Given** el selector de fecha, **When** el usuario intenta elegir una fecha futura, **Then** no puede seleccionarla.
3. **Given** ya existe un registro para una fecha, **When** el usuario registra otro peso en esa fecha, **Then** la app avisa que se actualizará el existente y, al confirmar, queda un solo registro con el nuevo valor.
4. **Given** el resumen de entrenamiento, **When** se muestra, **Then** indica sesiones completadas frente a programadas en la semana actual y la racha de semanas cumplidas, tal como las entrega el servidor.
5. **Given** un usuario sin registros de peso ni sesiones, **When** abre Progreso, **Then** ve un estado vacío que lo invita a registrar su primer peso o completar su primera sesión.

---

### User Story 7 - Gestionar perfil y cuenta (Priority: P3)

El usuario revisa y modifica sus datos y preferencias desde la pestaña Perfil, decide si regenera sus planes con los datos nuevos, consulta el aviso de salud, cierra sesión o elimina su cuenta.

**Why this priority**: Necesario para mantener los planes al día y cumplir con la eliminación de cuenta, pero no bloquea el uso diario.

**Independent Test**: Con datos de ejemplo, editar cada sección del perfil, guardar, elegir cada opción de regeneración, salir con cambios sin guardar, cerrar sesión y eliminar la cuenta.

**Acceptance Scenarios**:

1. **Given** la pestaña Perfil, **When** se abre, **Then** muestra nombre, correo y un resumen de datos físicos, objetivo, preferencias de entrenamiento y de alimentación.
2. **Given** una sección en edición, **When** el usuario guarda valores válidos, **Then** los cambios se envían y la app pregunta si quiere regenerar la rutina, la dieta, ambas o ninguna.
3. **Given** una sección en edición, **When** un valor está fuera de rango, **Then** se aplican las mismas validaciones del onboarding y no se guarda.
4. **Given** un guardado que falla, **When** se muestra el error, **Then** los datos editados siguen en pantalla y se ofrece reintentar.
5. **Given** cambios sin guardar, **When** el usuario intenta salir de la sección, **Then** la app pide confirmar antes de descartarlos.
6. **Given** el Perfil, **When** el usuario elige cerrar sesión y confirma, **Then** se borran la sesión y los datos guardados en el dispositivo y vuelve al Login.
7. **Given** el Perfil, **When** el usuario elige eliminar su cuenta, **Then** debe escribir "ELIMINAR" para confirmar; al completarse en el servidor, la app borra los datos del dispositivo y vuelve al Login. Si falla, la cuenta y la sesión siguen intactas y se muestra un mensaje.
8. **Given** el Perfil, **When** el usuario mira su correo, **Then** se muestra solo como lectura (no se puede cambiar).

---

### Edge Cases

- **Generación inicial falla al terminar el onboarding**: el perfil queda guardado; la app muestra un error con reintentar. Si solo una de las dos (rutina o dieta) falla, el usuario entra a la app y la pestaña afectada muestra un estado vacío con la acción "Generar".
- **El usuario cierra la app mientras la IA genera**: al volver, el contenido anterior sigue intacto (si existía) y el usuario puede volver a pedir la alternativa.
- **Pérdida de conexión durante una solicitud a la IA**: se trata como fallo; el contenido actual se conserva y se ofrece reintentar al recuperar conexión.
- **Cambio de día con la app abierta (medianoche)**: al volver a la pantalla, el día destacado y el día que permite terminar sesión se actualizan a la nueva fecha.
- **Rutina con 7 días de entrenamiento**: no hay días de descanso y la semana se muestra completa sin errores.
- **Respuesta del servidor con campos faltantes** (ej. ejercicio sin imagen o sin peso sugerido): el campo se omite en pantalla; la app no se cierra.
- **Respuesta del servidor ilegible o incompleta en datos esenciales**: se muestra el estado de error con reintentar, nunca un error técnico.
- **Texto libre de más de 300 caracteres** (incluido pegado): la app no permite superar el límite y muestra el contador.
- **Doble toque en "Guardar", "Terminar sesión" o "Pedir alternativa"**: solo se envía una solicitud.
- **Sesión invalidada mientras el usuario edita su perfil**: se pierde la edición no guardada y la app vuelve al Login con un mensaje.
- **Otro usuario inicia sesión en el mismo dispositivo**: no ve datos del usuario anterior (se borraron al cerrar sesión).
- **Sesión del día marcada sin completar todos los ejercicios**: se permite; el registro lo interpreta el servidor.

## Requirements *(mandatory)*

### Functional Requirements

**Registro, inicio de sesión y sesión**

- **FR-001**: La app MUST permitir registrarse con nombre, correo y contraseña, e iniciar sesión con correo y contraseña.
- **FR-002**: La app MUST validar antes de enviar: campos obligatorios no vacíos, correo con formato válido y contraseña de mínimo 8 caracteres. La creación de la cuenta y la verificación de credenciales las hace el servidor.
- **FR-003**: La app MUST mostrar mensajes claros en español cuando el correo ya está registrado o las credenciales son incorrectas, sin detalles técnicos.
- **FR-004**: La app MUST mantener la sesión al cerrar y reabrir la app hasta que el usuario cierre sesión o el servidor la invalide; si el servidor la invalida, MUST borrar la sesión y volver al Login.
- **FR-005**: Todas las pantallas excepto Login y Registro MUST requerir sesión activa.

**Onboarding**

- **FR-006**: La app MUST exigir completar el onboarding antes de acceder al resto de la app, en 4 pasos: datos físicos, objetivo, entrenamiento y alimentación.
- **FR-007**: El onboarding MUST recoger: edad (16–100 años), sexo (masculino o femenino), estatura (100–250 cm), peso actual (30–300 kg); objetivo (perder grasa, ganar masa muscular, mejorar condición física general); nivel (principiante, intermedio, avanzado), días disponibles por semana (1–7), duración por sesión (15–120 minutos), lugar (casa o gimnasio), equipo disponible (selección múltiple) y lesiones o limitaciones (texto libre opcional, máx. 300 caracteres); tipo de dieta (omnívora, vegetariana, vegana), alergias o intolerancias (selección múltiple más "otra" en texto), alimentos que no le gustan (texto libre opcional, máx. 300 caracteres) y comidas al día (3–5).
- **FR-008**: Cada paso MUST validar sus campos y no permitir avanzar con valores fuera de rango, indicando el rango permitido.
- **FR-009**: Si el usuario abandona el onboarding, la app MUST retomarlo en el paso donde quedó con los datos ya llenados en ese dispositivo.
- **FR-010**: Al terminar, la app MUST enviar el perfil y solicitar la primera rutina y la primera dieta, mostrando un estado de carga hasta que lleguen o fallen.

**Rutina**

- **FR-011**: La app MUST mostrar la rutina activa como un plan de 7 días con los días de entrenamiento y los demás como descanso, destacando el día de hoy.
- **FR-012**: Cada ejercicio MUST mostrar nombre, grupo muscular, series, repeticiones o duración, descanso entre series, peso sugerido (si existe) e instrucciones breves; e imagen si el servidor la envía.
- **FR-013**: La app MUST permitir marcar ejercicios como completados y la sesión como terminada solo en el día actual según la fecha del dispositivo, y enviar estos cambios al servidor.
- **FR-014**: La app MUST permitir terminar la sesión del día aunque no todos los ejercicios estén marcados.

**Dieta**

- **FR-015**: La app MUST mostrar la dieta activa como un plan de 7 días; cada día con sus comidas y los totales de calorías, proteína, carbohidratos y grasas recibidos del servidor.
- **FR-016**: Cada receta MUST mostrar nombre, ingredientes con cantidades (g o ml), pasos de preparación, tiempo aproximado, y calorías y macros por porción.
- **FR-017**: La app MUST NOT calcular calorías ni macros; solo muestra los valores recibidos.

**Alternativas y feedback a la IA**

- **FR-018**: La app MUST permitir pedir una alternativa para un ejercicio, una receta, la rutina completa o la dieta completa, con los motivos definidos en la Historia 4 y un texto libre opcional de máximo 300 caracteres.
- **FR-019**: Al elegir "Me causa molestia o dolor", la app MUST mostrar una recomendación de consultar a un profesional de salud.
- **FR-020**: Mientras una solicitud a la IA está en curso, la app MUST mantener visible el contenido actual, mostrar un estado de carga y deshabilitar la acción para impedir duplicados.
- **FR-021**: La app MUST esperar la respuesta de la IA al menos 60 segundos antes de considerarla fallida.
- **FR-022**: El contenido anterior MUST reemplazarse solo cuando la alternativa llega correctamente; si falla, MUST conservarse intacto y ofrecerse reintentar.
- **FR-023**: Regenerar la rutina o la dieta completa MUST pedir confirmación, ya que reemplaza al plan activo.

**Progreso**

- **FR-024**: La app MUST permitir registrar el peso corporal (30–300 kg) con fecha, por defecto hoy, sin permitir fechas futuras.
- **FR-025**: Si ya existe un registro para la fecha elegida, la app MUST avisar y, al confirmar, actualizarlo (un solo registro por día).
- **FR-026**: La app MUST mostrar el historial de peso como lista (más reciente primero) y como gráfica de evolución en el tiempo.
- **FR-027**: La app MUST mostrar las sesiones completadas frente a las programadas en la semana actual y la racha de semanas cumplidas, tal como las entrega el servidor.

**Perfil y cuenta**

- **FR-028**: La pestaña Perfil MUST mostrar nombre, correo (solo lectura) y un resumen de datos y preferencias.
- **FR-029**: La app MUST permitir editar, por secciones, los mismos datos del onboarding con las mismas validaciones.
- **FR-030**: Tras guardar el perfil, la app MUST preguntar si regenerar la rutina, la dieta, ambas o ninguna; MUST NOT regenerar automáticamente.
- **FR-031**: Si el guardado falla, la app MUST conservar los datos editados en pantalla y ofrecer reintentar.
- **FR-032**: Al salir de una sección con cambios sin guardar, la app MUST pedir confirmación antes de descartarlos.
- **FR-033**: La app MUST permitir cerrar sesión con confirmación; al hacerlo MUST borrar la sesión y todos los datos del usuario guardados en el dispositivo.
- **FR-034**: La app MUST permitir eliminar la cuenta escribiendo "ELIMINAR" como confirmación; solo cuando el servidor confirma la eliminación, MUST borrar los datos del dispositivo y volver al Login.

**Comportamiento transversal**

- **FR-035**: La navegación principal MUST ser una barra inferior con Rutina, Dieta, Progreso y Perfil.
- **FR-036**: Toda pantalla que carga datos MUST manejar los estados cargando, contenido, vacío y error (con reintentar).
- **FR-037**: Toda pantalla que muestre contenido generado por IA (rutina, ejercicio, dieta, receta) y el Perfil MUST mostrar el aviso de que es orientativo y no reemplaza a un profesional de salud.
- **FR-038**: Sin conexión, la app MUST permitir consultar la última rutina, dieta y progreso cargados, con un indicador de que pueden no estar actualizados.
- **FR-039**: Sin conexión, la app MUST deshabilitar con un mensaje claro: login, registro, generar o pedir alternativas, editar perfil, marcar ejercicios o sesiones, registrar peso y eliminar cuenta.
- **FR-040**: La app MUST NOT mostrar al usuario errores técnicos (códigos, trazas, mensajes de excepción); todo error se muestra como mensaje amigable en español.
- **FR-041**: Datos faltantes u opcionales en las respuestas del servidor MUST NOT cerrar la app; los campos opcionales se omiten y los esenciales faltantes llevan al estado de error.
- **FR-042**: Toda la interfaz MUST estar en español.
- **FR-043**: La app MUST poder funcionar completa con datos de ejemplo que cumplan el contrato acordado con backend, mientras el servidor real no esté disponible.

### Key Entities *(include if feature involves data)*

- **Usuario**: cuenta de la persona; nombre, correo. Tiene un Perfil, una Rutina activa, una Dieta activa y su Progreso.
- **Perfil**: datos físicos (edad, sexo, estatura, peso), objetivo, preferencias de entrenamiento (nivel, días, duración, lugar, equipo, lesiones) y de alimentación (tipo de dieta, alergias, alimentos rechazados, comidas al día).
- **Rutina**: plan semanal activo generado por IA; contiene 7 Días de rutina.
- **Día de rutina**: día de la semana, tipo (entrenamiento o descanso) y su lista de Ejercicios.
- **Ejercicio**: nombre, grupo muscular, series, repeticiones o duración, descanso, peso sugerido opcional, instrucciones, imagen opcional.
- **Registro de entrenamiento**: para una fecha concreta, qué ejercicios se completaron y si la sesión se terminó.
- **Dieta**: plan semanal activo generado por IA; contiene 7 Días de dieta con sus totales de calorías y macros.
- **Comida / Receta**: tipo de comida (desayuno, almuerzo, cena, snack), nombre, ingredientes con cantidades, pasos, tiempo aproximado, calorías y macros por porción.
- **Solicitud de alternativa**: objetivo (ejercicio, receta, rutina o dieta), motivo, texto libre opcional y estado (en curso, completada, fallida).
- **Registro de peso**: fecha y peso en kg; único por fecha.
- **Resumen de entrenamiento**: sesiones completadas y programadas en la semana actual y racha de semanas cumplidas (calculadas por el servidor).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Una persona nueva completa el registro y los 4 pasos del onboarding en menos de 5 minutos (sin contar el tiempo de generación de la IA).
- **SC-002**: Desde que abre la app con sesión iniciada, el usuario ve los ejercicios del día de hoy en 2 toques o menos.
- **SC-003**: Al abrir la app, la rutina o dieta ya cargadas aparecen en menos de 2 segundos, con o sin conexión.
- **SC-004**: En el 100% de las solicitudes de alternativa que fallan o tardan, el contenido anterior sigue visible e intacto.
- **SC-005**: El 100% de las pantallas con contenido generado por IA muestran el aviso de salud.
- **SC-006**: En el recorrido de verificación de errores (sin conexión, credenciales incorrectas, servidor caído, respuesta incompleta), el usuario no ve ningún mensaje técnico y la app no se cierra en ningún caso.
- **SC-007**: El usuario registra su peso del día en menos de 30 segundos.
- **SC-008**: Al menos 9 de cada 10 personas de prueba completan el onboarding y llegan a su rutina sin ayuda en su primer intento.

## Assumptions

- **Plan semanal que se repite**: la rutina y la dieta son una plantilla de 7 días que se repite cada semana hasta que el usuario la regenera; el registro de ejercicios completados es por fecha, así que cada semana empieza sin marcas.
- **Alternativa de un elemento**: la alternativa de un ejercicio o receta reemplaza ese elemento en el plan activo (para todas las semanas), no solo en la fecha actual.
- **Onboarding parcial**: el avance del onboarding no terminado se guarda solo en el dispositivo; si el usuario entra desde otro dispositivo, empieza de nuevo.
- **Sexo**: se pide como masculino o femenino porque el servidor lo usa para los cálculos nutricionales.
- **Opciones predefinidas**: el equipo disponible (peso corporal, mancuernas, barra y discos, kettlebell, bandas elásticas, banco, barra de dominadas, máquinas de gimnasio) y las alergias (gluten, lactosa, frutos secos, maní, mariscos, huevo, soya) son listas cerradas más "otra"; el equipo final lo puede ajustar el equipo de producto.
- **Tiempo de generación**: la generación con IA tarda hasta 60 segundos; valor pendiente de confirmar con backend.
- **Fecha de referencia**: "hoy" se toma de la fecha del dispositivo.
- **Sin escrituras offline**: las acciones sin conexión se bloquean; no se guardan para enviarse después (decisión de la constitución).
- **Contrato con backend**: el contrato (endpoints, ejemplos de petición/respuesta y errores) se documenta pantalla por pantalla en `specs/001-allfitness-mvp/contracts/` durante la fase de plan; backend lo implementa en paralelo.
- **Servidor responsable**: autenticación, almacenamiento, generación con IA, calorías, macros, rachas y progresiones son responsabilidad del servidor.
- **Fuera de alcance**: pagos y suscripciones; login social, recuperación y cambio de contraseña; cambio de correo; notificaciones push, recordatorios y su ajuste en el Perfil; wearables, Google Fit y Health Connect; funciones sociales; chat libre con la IA; edición manual de rutina o dieta; historial de planes anteriores; registro de lo comido, conteo de calorías y escáner de alimentos; lista de compras; videos de ejercicios, temporizador de descanso y registro de peso por serie; medidas corporales distintas al peso; modo oscuro personalizado y otros idiomas; panel de administrador y versiones web/iOS.

## Manual Verification *(requerido por la constitución)*

Recorrido en emulador o dispositivo antes de dar la feature por terminada:

- [ ] Flujos felices: registro → onboarding → rutina y dieta generadas; login con cuenta existente; sesión persistente al reabrir.
- [ ] Rutina: ver la semana, marcar ejercicios y terminar la sesión de hoy; días pasados y futuros bloqueados.
- [ ] Dieta: ver los 7 días y abrir recetas; valores iguales a los de los datos de ejemplo.
- [ ] Alternativas: ejercicio, receta, rutina y dieta completas con respuesta exitosa, lenta (~60 s) y fallida.
- [ ] Progreso: registrar peso, fecha futura bloqueada, fecha repetida actualiza, estado vacío.
- [ ] Perfil: editar cada sección, las 4 opciones de regeneración, salir con cambios sin guardar, cerrar sesión, eliminar cuenta.
- [ ] Errores: credenciales incorrectas, correo duplicado, servidor caído, respuesta incompleta, sesión invalidada; ningún mensaje técnico.
- [ ] Sin conexión: consultar rutina, dieta y progreso cargados; acciones de red deshabilitadas con mensaje; primera apertura sin datos.
- [ ] Aviso de salud visible en todas las pantallas con contenido de IA y en el Perfil.
