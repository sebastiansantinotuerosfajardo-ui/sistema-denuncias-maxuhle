# Sistema de Denuncias – Colegio Privado Max Uhle (Arequipa)

Aplicación web para registrar y dar seguimiento a denuncias de convivencia escolar, acoso y maltrato.
Proyecto académico UTP · Desarrollo Web Integrado · sección 34385 · Avance 3.

> ⚠️ **Todos los datos de demostración son ficticios.** El sistema trata con menores de edad: nunca cargues datos reales en la demo.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 18 (build `application`), Node 22 |
| Backend | Spring Boot 3.3.4, Java 17, Maven, Spring Security (HTTP Basic + BCrypt, sin sesión), JPA/Hibernate |
| Base de datos | MySQL 8 (H2 en memoria solo para pruebas) |
| Despliegue | Railway (MySQL + backend + frontend) |

## Demo desplegada

- Frontend: `https://observant-tranquility-production-5f23c66d.up.railway.app`
- Backend (API): `https://sistema-denuncias-maxuhle-production.up.railway.app/api`
- Repositorio: `https://github.com/sebastiansantinotuerosfajardo-ui/sistema-denuncias-maxuhle`

### Usuarios de demostración

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | ADMIN | la entrega el equipo por separado (no se publica en este repositorio) |
| `op.psicologia`, `op.convivencia`, `op.tutoria`, `op.secretaria` | OPERADOR | la entrega el equipo por separado |

`ADMIN` puede registrar, consultar, cambiar estados, editar, eliminar y ver estadísticas. `OPERADOR` solo registra y consulta.

## Estructura

```
.
├── denuncias-api/      Backend Spring Boot (controller, service, repository, model, dto, config)
│   └── src/main/resources/   application.properties (local) · application-prod.properties (nube) · application-h2.properties (pruebas)
├── denuncias-web/      Frontend Angular (core, features, environments, scripts/set-env.js)
├── .env.example        Variables de entorno (solo ejemplos)
└── README.md
```

## Ejecutar en local

Requisitos: JDK 17+, Maven 3.9+, Node 22, MySQL 8 en `localhost:3306`.

```bash
# Backend (usa por defecto root/root en local; sobrescribe con variables de entorno)
cd denuncias-api
mvn spring-boot:run          # http://localhost:8080
mvn test                     # pruebas con H2

# Frontend
cd denuncias-web
npm install
npm start                    # http://localhost:4200
npm test
```

En local, `DataInitializer` crea dos usuarios de prueba (`admin` y `operador`) con contraseñas de desarrollo que están en el código fuente. **Ese inicializador no corre en producción** (perfiles `prod` y `seed`).

## Variables de entorno

| Variable | Dónde | Descripción |
|---|---|---|
| `DB_URL` | backend | URL JDBC de MySQL (`jdbc:mysql://host:puerto/base?...`) |
| `DB_USER` | backend | Usuario de MySQL |
| `DB_PASSWORD` | backend | Contraseña de MySQL |
| `CORS_ALLOWED_ORIGINS` | backend | Orígenes del frontend, separados por coma; no se acepta `*` |
| `SPRING_PROFILES_ACTIVE` | backend | `prod` en la nube; `prod,seed` solo para cargar datos demo una vez |
| `SEED_ADMIN_PASSWORD`, `SEED_OPERADOR_PASSWORD` | backend (solo seed) | Contraseñas de los usuarios demo, mínimo 8 caracteres |
| `DDL_AUTO` | backend | `update` en el primer arranque; luego `validate` |
| `API_URL` | frontend (build) | URL pública del backend; `scripts/set-env.js` la escribe en `environment.prod.ts` |

La guía paso a paso de Railway está en [`DESPLIEGUE_RAILWAY.md`](DESPLIEGUE_RAILWAY.md).

## Medidas de seguridad implementadas

- Contraseñas con BCrypt; autenticación HTTP Basic sin sesión en servidor; roles ADMIN/OPERADOR por endpoint.
- Validación de entrada con `@Valid` (Bean Validation) en los DTO.
- Consultas JPQL con parámetros nombrados (sin concatenar texto de usuario).
- CORS restringido a los orígenes de `CORS_ALLOWED_ORIGINS`.
- Cabeceras del backend: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, HSTS (efectivo detrás de HTTPS).
- Credenciales de base de datos y de usuarios demo solo en variables de entorno; sin trazas de error hacia el cliente.

## Pendientes (NO implementado todavía)

Para no afirmar protecciones inexistentes, esto **no** está hecho:

- [ ] Rate limiting por IP (empezando por el login).
- [ ] Cifrado de campos sensibles (descripción y nombres) con AES-256-GCM.
- [ ] CSP y cabeceras de seguridad en el frontend; límite de tamaño de petición; TLS explícito hacia MySQL.
- [ ] GitHub Actions (CI), Secret scanning y Dependabot.
- [ ] WAF / Cloudflare (Bot Fight Mode).
- [ ] JWT (hoy se usa HTTP Basic: las credenciales viajan en cada petición, por eso HTTPS es obligatorio).

## Licencia / uso

Proyecto académico; sin licencia de uso comercial.
