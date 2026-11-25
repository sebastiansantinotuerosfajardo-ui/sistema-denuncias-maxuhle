# Denuncias Web – Colegio Max Uhle (Avance 3: frontend)

Aplicación **Angular 18** (componentes *standalone*, *signals*, control de flujo `@if/@for`) que consume la API REST de
`denuncias-api` en `http://localhost:8080/api` (constante `API_URL` en `src/app/core/api.config.ts`).

## Requisitos
Node.js **18.19 o superior** (recomendado 20 LTS o 22) y npm 9+.

## Comandos
```
npm install        # instala dependencias (usa package-lock.json)
npm start          # ng serve → http://localhost:4200  (abrir con "localhost", no con 127.0.0.1)
npm test           # 24 pruebas Karma/Jasmine (abre Chrome). Sin ventana: npx ng test --watch=false --browsers=ChromeHeadless
npm run build      # compilación de producción en dist/
```
Para que funcione el login y los datos, el backend debe estar activo en el puerto 8080.

## Estructura (`src/app`)
| Carpeta / archivo | Responsabilidad |
|---|---|
| `core/api.config.ts` | URL base de la API |
| `core/models.ts` | Interfaces TypeScript del JSON del backend, listas de tipos/estados y etiquetas legibles |
| `core/denuncia.service.ts` | Un método por endpoint (`HttpClient` → `Observable` tipado) |
| `core/auth.service.ts` | Login HTTP Basic, sesión (`sessionStorage`), rol (200 = ADMIN, 403 = OPERADOR, 401 = inválidas) |
| `core/auth.interceptor.ts` | Agrega `Authorization: Basic …` solo a peticiones a la API; ante 401 cierra la sesión |
| `core/auth.guard.ts` | `authGuard` (exige sesión) y `adminGuard` (pantallas solo ADMIN) |
| `core/errores.ts` | Traduce códigos HTTP y el JSON de error (`{error, campos}`) a mensajes |
| `core/etiquetas.pipe.ts` | Muestra «Maltrato psicológico» en vez de `MALTRATO_PSICOLOGICO` |
| `pages/login`, `lista`, `formulario`, `detalle` | Pantallas (rutas en `app.routes.ts`) |

## Rutas
| Ruta | Pantalla | Acceso |
|---|---|---|
| `/login` | Inicio de sesión + estado de la API (`GET /api/publico/info`) | público |
| `/denuncias` | Listado y filtro por estado | ADMIN, OPERADOR |
| `/denuncias/nueva` | Registrar denuncia | ADMIN, OPERADOR |
| `/denuncias/:id` | Detalle, historial y (solo ADMIN) registrar seguimiento | ADMIN, OPERADOR |
| `/denuncias/:id/editar` | Editar descripción | solo ADMIN |

> Los *guards* y los botones ocultos son ayudas de interfaz; la autorización real la aplica el backend (403).
