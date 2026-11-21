# Backend – Microservicios Spring Boot

Dos microservicios independientes, cada uno con **su propia base de datos**, que la app Android consume con **Retrofit**.

| Microservicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `usuarios-service` | 8081 | `gestion_usuarios` | Inicio de sesión, usuarios (perfiles) y fichas de clientes |
| `solicitudes-service` | 8082 | `gestion_solicitudes` | Solicitudes, historial, reglas de estado, prioridad, asignación, estadísticas |

```
App Android ──Retrofit──▶ usuarios-service (8081) ──▶ MySQL gestion_usuarios
     │
     └──────Retrofit──▶ solicitudes-service (8082) ──▶ MySQL gestion_solicitudes
                               │
                               └──RestClient──▶ usuarios-service (valida quién hace la petición y su perfil)
```

- Java 17 o superior, Spring Boot 3.5, Spring Data JPA, Bean Validation, springdoc (Swagger).
- Las contraseñas se guardan cifradas con BCrypt.
- Cada petición al microservicio de solicitudes lleva la cabecera `X-Usuario-Id`. El servicio consulta a `usuarios-service` el perfil de ese usuario y aplica las mismas reglas de acceso y de cambio de estado que tenía la app.
- Los errores siempre responden JSON `{ "estado": 409, "mensaje": "...", "timestamp": ... }`; la app muestra el `mensaje` tal cual.

## Cómo ejecutar

### 1. Base de datos
**Opción A – MySQL (XAMPP / MySQL Workbench).** Encender MySQL en el puerto 3306. Las bases se crean solas. Por defecto se usa el usuario `root` sin contraseña; si tu MySQL tiene clave, define las variables de entorno `DB_USER` y `DB_PASSWORD` (o edita `application.properties`).

**Opción B – Sin MySQL (H2 en archivo).** Ejecutar cada microservicio con el perfil `h2` (ver abajo). Los datos quedan en la carpeta `data/` de cada microservicio y se pueden ver en `http://localhost:PUERTO/h2-console`.

### 2. Levantar los microservicios
Abrir la carpeta `backend` en IntelliJ IDEA (se reconoce como proyecto Maven) y ejecutar, **en este orden**:

1. `UsuariosServiceApplication` (puerto 8081)
2. `SolicitudesServiceApplication` (puerto 8082)

O desde una terminal, dentro de `backend`:

```bash
mvn -pl usuarios-service spring-boot:run
mvn -pl solicitudes-service spring-boot:run        # en otra terminal

# con H2 en vez de MySQL
mvn -pl usuarios-service spring-boot:run -Dspring-boot.run.profiles=h2
mvn -pl solicitudes-service spring-boot:run -Dspring-boot.run.profiles=h2
```

En el primer arranque se cargan los datos de demostración (solo si las tablas están vacías).

> Importante: `usuarios-service` debe arrancar primero sobre una base vacía, porque las solicitudes de ejemplo usan los ids de usuario 1 a 5. Si se borra una de las bases, borrar también la otra.

### 3. Probar
- Swagger: http://localhost:8081/swagger-ui.html y http://localhost:8082/swagger-ui.html
- Pruebas unitarias de reglas de negocio: `mvn test`

### 4. Conectar la app
La app apunta a `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/` (el computador visto desde el **emulador**). Para un **teléfono físico** conectado a la misma red Wi-Fi, cambiar esas URLs en `app/build.gradle.kts` por la IP del computador (por ejemplo `http://192.168.1.20:8081/`) y permitir los puertos en el firewall de Windows.

## Cuentas de demostración (contraseña `1234`)

| id | Perfil | Correo |
|---|---|---|
| 1 | Administrador | `admin@demo.cl` |
| 2 | Técnico | `tecnico1@demo.cl` |
| 3 | Técnico | `tecnico2@demo.cl` |
| 4 | Cliente (cliente 1) | `cliente1@demo.cl` |
| 5 | Cliente (cliente 2) | `cliente2@demo.cl` |

## Endpoints

### usuarios-service (8081)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Inicio de sesión `{correo, password}` → usuario (401 si no coincide) |
| GET | `/api/usuarios?perfil=TECNICO` | Lista de usuarios (filtro opcional por perfil) |
| GET | `/api/usuarios/{id}` | Un usuario |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Editar usuario (contraseña vacía = mantener) |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |
| GET / POST | `/api/clientes` | Listar / crear clientes |
| GET / PUT / DELETE | `/api/clientes/{id}` | Ver / editar / eliminar cliente |

### solicitudes-service (8082) — todas con cabecera `X-Usuario-Id`

| Método | Ruta | Quién | Descripción |
|---|---|---|---|
| GET | `/api/solicitudes` | Todos | Solicitudes visibles para el perfil. Filtros: `estado`, `prioridad`, `clienteId`, `responsableId`, `texto` |
| GET | `/api/solicitudes/{id}` | Con acceso | Detalle |
| POST | `/api/solicitudes` | Cliente | Crear (genera el código `SOL-AAAA-NNNN` y el estado *Recibida*) |
| PUT | `/api/solicitudes/{id}` | Cliente / Admin | Editar (no si está cerrada o cancelada) |
| DELETE | `/api/solicitudes/{id}` | Admin | Eliminar con su historial |
| PATCH | `/api/solicitudes/{id}/estado` | Según reglas | `{estado, comentario, accion}` |
| PATCH | `/api/solicitudes/{id}/prioridad` | Admin | `{prioridad}` |
| PATCH | `/api/solicitudes/{id}/responsable` | Admin | `{tecnicoId}` (valida contra usuarios-service que sea técnico) |
| POST | `/api/solicitudes/{id}/avances` | Técnico | `{texto}` (solo en proceso) |
| POST | `/api/solicitudes/{id}/comentarios` | Con acceso | `{texto}` |
| GET | `/api/solicitudes/{id}/historial` | Con acceso | Historial cronológico |
| GET | `/api/historial` | Todos | Historial de todas sus solicitudes visibles |
| GET | `/api/solicitudes/estadisticas` | Admin | Total, atrasadas, por estado, prioridad y responsable |

### Ejemplo con curl

```bash
curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
     -d '{"correo":"cliente1@demo.cl","password":"1234"}'

curl http://localhost:8082/api/solicitudes -H "X-Usuario-Id: 4"
```
