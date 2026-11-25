# Avance 3 – Sistema de Denuncias · Colegio Privado Max Uhle (Arequipa)

Curso **Desarrollo Web Integrado** (UTP) · Sección 34385 · Docente: Ing. Barnett Contreras, Oscar Alberto

Solución Full Stack: **Angular 18** (frontend) + **Spring Boot 3.3.4 / Java 17** (API REST) + **MySQL 8**.

```
Avance3_Denuncias_Angular_SpringBoot/
├── LEEME.md                          ← este archivo
├── INSTALACION_Y_EJECUCION.md        ← guía paso a paso (empezar por aquí)
├── denuncias-api/                    ← backend Spring Boot (Avance 2 + CORS para Angular)
└── denuncias-web/                    ← frontend Angular (login, listado, formulario, detalle con seguimientos)
```

## Arranque rápido (necesita MySQL, JDK 17+, Maven y Node.js 18.19+)

```bash
# Terminal 1 – backend  (http://localhost:8080)
cd denuncias-api
mvn spring-boot:run                    # si tu MySQL tiene clave: define antes la variable DB_PASSWORD

# Terminal 2 – frontend (http://localhost:4200)
cd denuncias-web
npm install
npm start
```

Abrir **http://localhost:4200** (no `127.0.0.1`: CORS solo admite ese origen).

| Usuario  | Contraseña  | Rol      |
|----------|-------------|----------|
| admin    | admin123    | ADMIN    |
| operador | operador123 | OPERADOR |

Sin MySQL (plan B): `mvn spring-boot:run "-Dspring-boot.run.profiles=h2"` (base en memoria; los datos se pierden al detener).

## Pruebas automáticas

| Proyecto | Comando | Qué ejecuta |
|----------|---------|-------------|
| `denuncias-api` | `mvn test` | 14 pruebas JUnit/MockMvc (usan H2; no necesitan MySQL) |
| `denuncias-web` | `npm test` | 24 pruebas Karma/Jasmine del frontend (HTTP simulado; abre Chrome) |

> Las pruebas automáticas acreditan cada proyecto **por separado**. La integración Angular ↔ Spring Boot ↔ MySQL se
> demuestra ejecutando ambos servidores y siguiendo la sección «Verificación» de `INSTALACION_Y_EJECUCION.md`.

## Estado real de la integración (criterio de honestidad técnica)

- **Implementado en backend:** CRUD, consultas JPQL, transacciones, reglas de negocio, HTTP Basic con roles, CORS.
- **Implementado en frontend:** login, listado con filtro, formulario (crear/editar), detalle e historial, seguimiento (ADMIN), manejo de errores.
- **Integrado y probado de extremo a extremo:** solo lo que el equipo ejecute y capture (pruebas P1–P13 de la guía).
- **Pendiente:** JWT (no existe `/api/auth/login`), pantalla de reportes, despliegue.
