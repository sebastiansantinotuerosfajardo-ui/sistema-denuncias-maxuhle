# API de Denuncias – Colegio Max Uhle (Avance 3: backend)

API REST con **Spring Boot 3.3.4** y **Java 17** (probado con JDK 21), persistencia **MySQL 8** (Spring Data JPA),
seguridad **HTTP Basic** con roles `ADMIN` y `OPERADOR`. El Avance 3 solo agrega **CORS** (`SecurityConfig`) para que el
frontend Angular (`http://localhost:4200`) pueda consumirla.

## Requisitos
- JDK 17 o superior, Maven 3.9+, MySQL 8 en `localhost:3306`.

## Configuración
`src/main/resources/application.properties` lee la conexión de variables de entorno (valores por defecto: usuario `root`, clave `root`):

| Variable      | Para qué sirve              | Ejemplo (PowerShell)                 |
|---------------|-----------------------------|--------------------------------------|
| `DB_USER`     | Usuario de MySQL            | `$env:DB_USER = "root"`              |
| `DB_PASSWORD` | Contraseña de MySQL         | `$env:DB_PASSWORD = "tu_clave"`      |

La base `denuncias_maxuhle` y sus tablas se crean solas. **No escribas tu contraseña real dentro del archivo** (no se sube ni se entrega).

## Ejecutar
```
mvn test                                              # 14 pruebas automáticas (H2, sin MySQL)
mvn spring-boot:run                                   # API con MySQL  → http://localhost:8080
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"   # plan B sin MySQL (datos en memoria)
```
En Apache NetBeans: *File → Open Project → `denuncias-api`* y ejecutar con **Run Project (F6)**, o clic derecho sobre
`DenunciasApiApplication.java → Run File`. Para la clave de MySQL: *Project Properties → Actions → Run project → Set Properties →*
`Env.DB_PASSWORD=tu_clave`.

Comprobación: `http://localhost:8080/api/publico/info` → `{"sistema":"...","version":"0.2.0","estado":"operativo"}` (sin credenciales).

## Usuarios de prueba (se crean al primer arranque)
| Usuario  | Contraseña  | Rol      |
|----------|-------------|----------|
| admin    | admin123    | ADMIN    |
| operador | operador123 | OPERADOR |

## Endpoints y permisos
| Método | Ruta                                   | ADMIN | OPERADOR | Público |
|--------|----------------------------------------|:-----:|:--------:|:-------:|
| GET    | `/api/publico/info`                    |  ✔    |   ✔      |   ✔     |
| POST   | `/api/denuncias`                       |  ✔    |   ✔      |         |
| GET    | `/api/denuncias`, `/{id}`, `/estado/{estado}`, `/{id}/seguimientos` | ✔ | ✔ | |
| PUT    | `/api/denuncias/{id}`                  |  ✔    |          |         |
| DELETE | `/api/denuncias/{id}`                  |  ✔    |          |         |
| POST   | `/api/denuncias/{id}/seguimientos`     |  ✔    |          |         |
| GET    | `/api/denuncias/protocolo-pendiente`, `/estadisticas/por-tipo` | ✔ | | |

Códigos: `401` sin credenciales válidas · `403` rol insuficiente · `404` no existe · `409` regla de negocio · `400` validación.
La colección `postman/denuncias-api.postman_collection.json` (25 peticiones) se ejecuta en orden con el Runner de Postman.

## Si ya existía una base de datos de una versión anterior
`seguimiento` tiene la clave foránea `usuario_id`. Si el arranque falla por el esquema antiguo, recrea la base:

    mysql -u root -p -e "DROP DATABASE IF EXISTS denuncias_maxuhle;"
