# Gestión móvil y seguimiento de solicitudes de clientes

Aplicación Android desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, sede Puente Alto), basada en el caso *"Gestión móvil y seguimiento de solicitudes de clientes"* (005D).

Una pequeña empresa de servicios informáticos recibe hoy los requerimientos de sus clientes por WhatsApp, lo que dificulta saber qué solicitudes están pendientes, quién las atiende y en qué estado van. La app representa cómo una solicitud puede quedar **registrada, clasificada, asignada, seguida y cerrada** con trazabilidad completa. No se integra con WhatsApp: se comparte una referencia estructurada mediante el menú *Compartir* de Android.

> Proyecto académico (MVP). Usa únicamente **datos ficticios**: usuarios, clientes, solicitudes y fotos de prueba.

## Integrantes

| Nombre | Aporte principal                                                                                                                            |
|---|---------------------------------------------------------------------------------------------------------------------------------------------|
| Matias Figueroa | Capa de datos con Room, repositorio, login, menú, lista, formulario, detalle, paneles del administrador y del técnico, compartir referencia |
| Diego Nuñez | Integración de la cámara (Coil, FileProvider y captura de foto de evidencia) , informe y trello                                             |

- **Docente:** Vicente Julio Zapata Valenzuela
- **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles

## Funcionalidades implementadas

### Todos los perfiles
- Inicio de sesión con cuentas ficticias, identificación del perfil, control de acceso y cierre de sesión.
- Lista de solicitudes con código, título, categoría y estado (cada estado con su color).
- Detalle de la solicitud: estado vigente, prioridad, última actualización, datos, descripción, foto de evidencia e **historial cronológico** (fecha, hora, acción, cambio de estado, comentario y usuario).
- Comentarios en la solicitud mientras no esté cerrada o cancelada.
- **Compartir referencia**: mensaje estructurado con código, título, categoría y un enlace de seguimiento ficticio (sin descripción ni datos del cliente).
- **Notificaciones locales** con permiso del usuario: avisos de cambios de estado, asignaciones, prioridad, comentarios y solicitudes atrasadas. Cada perfil recibe además, al iniciar sesión, las novedades que otros hicieron en sus solicitudes desde su última sesión.

### Cliente
- Crear solicitudes con título, descripción, categoría, servicio relacionado y sitio o activo relacionado (opcional), con validaciones visibles por campo.
- Adjuntar una **foto de evidencia** con la cámara del dispositivo y verla al consultar la solicitud.
- Código único (`SOL-AAAA-NNNN`), fecha y hora de creación y estado inicial *Recibida*.
- Editar sus solicitudes mientras no estén cerradas o canceladas.
- Responder las solicitudes de información, **confirmar la resolución** o **solicitar la reapertura**.
- Solo ve sus propias solicitudes.

### Administrador
- Ve todas las solicitudes.
- Define la **prioridad**, **asigna o reasigna** el responsable, cambia el estado según las reglas y cancela con motivo.
- **Panel del administrador:** total de solicitudes, atrasadas (más de 7 días sin resolver), cantidad por estado, por prioridad y por responsable, y filtros por texto, estado, prioridad, cliente, responsable y solo atrasadas.

### Técnico
- Ve únicamente las solicitudes que tiene asignadas.
- **Panel del técnico:** indicadores, lista ordenada (las más urgentes primero) y su actividad reciente.
- Inicia el trabajo, registra avances, pide información al cliente y registra la solución para resolver.

## Interfaz y experiencia de usuario

- **Formularios validados con íconos y mensajes por campo:** inicio de sesión, nueva solicitud y edición, comentarios, buscador y cuadros de texto de las acciones (reapertura, respuesta, avance, solución). Cada campo muestra su ícono, se marca en rojo con un ícono de advertencia y despliega el mensaje de error debajo. Todos usan un mismo componente reutilizable (`CampoFormulario`).
- **Validaciones desacopladas de la interfaz:** las reglas viven en `domain` (`ValidadorSolicitud`, `ValidadorLogin`, `ValidadorTexto`) y los `ViewModel` publican los errores en el `UiState`.
- **Animaciones funcionales:**
  - Transiciones entre pantallas (deslizamiento con fundido).
  - Mensajes de error que aparecen y desaparecen suavemente.
  - Indicador de progreso dentro de los botones *Ingresar* y *Guardar*.
  - Chip de estado que cambia de color y de texto con una transición.
  - Tarjetas de listas e historial que se reacomodan al cambiar los datos, y tarjetas del detalle que crecen o se encogen.
  - Contadores de los paneles que "cuentan" hasta su valor.
- Cada estado de la solicitud tiene su propio color, y la interfaz respeta el tema claro y oscuro del dispositivo.

## Reglas de negocio

Estados: *Recibida → En revisión → Pendiente de información → Asignada → En proceso → Resuelta → Cerrada / Cancelada*.

- Cada perfil solo puede realizar las transiciones de estado que le corresponden (`ReglasEstado`).
- El cliente no puede cerrar una solicitud por su cuenta: cierra confirmando una resolución.
- Todo cambio de estado, prioridad o responsable queda en el historial con fecha, hora, usuario y perfil.
- Una solicitud está **atrasada** si sigue sin resolverse a los 7 días de su creación (`ReglasAtraso`).

## Arquitectura

Patrón **MVVM** con persistencia local en **Room**. Las pantallas solo muestran un `UiState` expuesto como `StateFlow` y notifican eventos al `ViewModel`; la validación y las reglas de negocio no viven en la interfaz.

```
Pantalla (Compose) -> ViewModel (UiState + StateFlow) -> Repository -> DAO -> Room (SQLite)
```

```
app/src/main/java/com/example/gestionmovilsolicitudes/
├── data/
│   ├── dao/            consultas Room (SolicitudDao, HistorialDao, ...)
│   ├── entity/         tablas: usuarios, clientes, solicitudes, historial, evidencias
│   ├── repository/     SolicitudRepository (reglas de acceso y transacciones)
│   ├── AppDatabase.kt
│   └── SeedData.kt     datos ficticios de demostración
├── domain/             reglas puras: ValidadorSolicitud, ValidadorLogin, ValidadorTexto, ReglasEstado,
│                       ReglasAtraso, ReglasNotificacion, MensajeSeguimiento
├── model/              enums: Categoria, EstadoSolicitud, Prioridad, Perfil
├── notificaciones/     Notificador (canal, permiso y avisos) y UltimaConexion
├── session/            SessionManager (usuario conectado)
├── ui/
│   ├── login/  menu/  solicitud/  admin/  tecnico/
│   ├── componentes/    CampoFormulario, ErrorAnimado, ContadorAnimado (reutilizables)
│   ├── navigation/     AppNavigation (rutas y transiciones)
│   └── theme/
├── GestionApp.kt       Application: base de datos, repositorio y datos de prueba
└── MainActivity.kt
```

## Tecnologías

| Componente | Versión |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Interfaz | Jetpack Compose y Material Design 3 (BOM 2026.02.01) |
| Navegación | Navigation Compose 2.10.2 |
| Estado | ViewModel 2.11.0, StateFlow y corrutinas 1.10.2 |
| Persistencia | Room 2.8.5 (con KSP) |
| Imágenes | Coil 3.1.0 |
| Build | Android Gradle Plugin 9.4.1 y Gradle 9.6.0 |
| Plataforma | Android, `minSdk` 24 y `targetSdk` 37 |

**Recursos nativos del dispositivo:**

| Recurso | Uso en la app |
|---|---|
| Cámara | Captura de la foto de evidencia de la solicitud (`TakePicture` con `FileProvider`, almacenada en la carpeta interna de la app) |
| Notificaciones locales | Avisos de cambios en las solicitudes. Desde Android 13 se solicita el permiso `POST_NOTIFICATIONS` en tiempo de ejecución, con una explicación y un botón en el menú; si el usuario lo rechaza, la app sigue funcionando |
| Menú *Compartir* | Envío de la referencia estructurada de una solicitud |

## Cómo ejecutar el proyecto

1. Instalar una versión reciente de **Android Studio** compatible con Android Gradle Plugin 9.4.1 (incluye el JDK necesario).
2. Clonar el repositorio:
   ```bash
   git clone https://github.com/matiasfigueroami/GestionMovilSolicitudes.git
   ```
3. Abrir la carpeta en Android Studio y esperar a que termine la sincronización de Gradle (**Sync Now**, requiere internet la primera vez).
4. Crear o elegir un emulador (por ejemplo, Pixel 7 con una imagen reciente de Android) o conectar un teléfono con depuración USB.
5. Presionar **Run ▶**. Al primer arranque la app crea la base de datos y carga los datos de demostración.

> Si se necesita partir de cero, desinstalar la app del emulador para que se vuelva a crear la base de datos.

### Cuentas de demostración

Datos ficticios definidos en `data/SeedData.kt`. Contraseña de todas las cuentas: `1234`.

| Perfil | Correo |
|---|---|
| Administrador | `admin@demo.cl` |
| Técnico | `tecnico1@demo.cl` y `tecnico2@demo.cl` |
| Cliente | `cliente1@demo.cl` y `cliente2@demo.cl` |

### Flujo sugerido para la demostración
1. **Preparación de las notificaciones:** entrar una vez con cada perfil y, en el menú, tocar **Activar notificaciones** y aceptar el permiso. La primera sesión de cada usuario solo registra su hora; los avisos llegan por lo que otros hagan después.
2. **Cliente** (`cliente1@demo.cl`): crear una solicitud con foto de evidencia.
3. **Administrador**: definir la prioridad y asignarla a un técnico.
4. **Técnico**: iniciar el trabajo, registrar un avance y registrar la solución.
5. **Cliente**: confirmar la resolución. La solicitud queda *Cerrada* y el historial muestra todo el recorrido.

Al cambiar de perfil, cada usuario ve en la barra de notificaciones las novedades de sus solicitudes.

### Notas sobre el emulador
- Las notificaciones requieren un emulador o teléfono con **Android 13 o superior** para que aparezca el diálogo de permiso.
- La cámara del emulador muestra una escena virtual: es lo esperado.
- Las horas se muestran según la zona horaria del dispositivo. Si el emulador está en GMT, configurarlo en *Santiago (Chile)* para ver la hora local.

## Pruebas

Pruebas unitarias (JVM) con JUnit, 24 en total, que cubren las reglas del dominio sin depender de Android:

| Archivo | Qué comprueba |
|---|---|
| `ValidadorSolicitudTest` | Campos obligatorios, largo de título y descripción, servicio según categoría y sitio opcional |
| `ValidadorLoginTest` | Validación por campo del inicio de sesión (correo, contraseña) y de los textos libres |
| `MensajeSeguimientoTest` | Contenido del mensaje para compartir, sin la descripción de la solicitud |
| `ReglasNotificacionTest` | A quién se avisa en cada evento y el resumen de novedades |

```bash
./gradlew testDebugUnitTest
```

## Próximos pasos
- Evidencias adicionales adjuntadas por el técnico.
- Comentarios internos que el cliente no pueda ver.
- Exportación de información ficticia y medición simulada de tiempos (complementos del caso).
