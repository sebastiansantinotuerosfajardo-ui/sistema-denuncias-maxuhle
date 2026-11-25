# Despliegue en Railway (MySQL + backend + frontend)

⚠️ = paso manual en el panel web. Tiempo estimado: 25–35 min.

## 0. GitHub (desde tu PC, en la carpeta del proyecto)

```bash
# 1. Crea el repo VACÍO en https://github.com/new (sin README ni .gitignore). Luego:
git remote add origin https://github.com/TU-USUARIO/TU-REPO.git
git push -u origin main
```

## 1. Proyecto y MySQL ⚠️

1. railway.com → **New Project → Deploy from GitHub repo** → elige el repo (autoriza la app de GitHub si lo pide).
   Si crea un servicio automático desde la raíz, bórralo: crearemos los dos servicios a mano.
2. **+ New → Database → Add MySQL**. Anota que el servicio se llama exactamente `MySQL` (las referencias distinguen mayúsculas).

## 2. Backend ⚠️

1. **+ New → GitHub Repo** → el mismo repo. Nómbralo `backend`.
2. **Settings → Source → Root Directory** = `denuncias-api`.
3. **Variables** (usa *Raw Editor*):

```
SPRING_PROFILES_ACTIVE=prod,seed
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima
DB_USER=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
CORS_ALLOWED_ORIGINS=https://REEMPLAZAR-FRONTEND.up.railway.app
SEED_ADMIN_PASSWORD=<elige una contraseña de 8+ caracteres>
SEED_OPERADOR_PASSWORD=<otra contraseña de 8+ caracteres>
```

   Se usa el host interno (red privada de Railway). `useSSL=false` aplica solo a ese tramo interno; el TLS explícito a MySQL queda como pendiente (README).
4. **Settings → Networking → Generate Domain** (HTTPS automático). Copia la URL.
5. Railway detecta Maven por `pom.xml` y usa JDK 21 por defecto; el proyecto compila a Java 17 y funciona. Si el build falla por versión, añade `RAILPACK_JDK_VERSION=17`.

## 3. Frontend ⚠️

1. **+ New → GitHub Repo** → mismo repo. Nómbralo `frontend`.
2. **Settings → Root Directory** = `denuncias-web`.
3. **Variables**: `API_URL=https://URL-DEL-BACKEND.up.railway.app` (sin `/api`; el script lo añade).
   `.nvmrc` fija Node 22. Railway sirve el build de Angular como SPA automáticamente (no pongas comando de inicio propio).
4. **Generate Domain**. Vuelve al backend y pon esa URL en `CORS_ALLOWED_ORIGINS` (sin `/` final) → se redepliega.

## 4. Cargar los datos demo (una sola vez)

Con `SPRING_PROFILES_ACTIVE=prod,seed` el primer arranque crea las tablas, 5 usuarios, 120 denuncias y unos 400 seguimientos. En **Deploy Logs** debe aparecer `[seed] Datos de demostración creados`.

Después, **desactívalo** ⚠️: cambia `SPRING_PROFILES_ACTIVE=prod` y añade `DDL_AUTO=validate`. (Aunque queden activos, el seeder no duplica: no hace nada si ya existen denuncias.)

Para re-sembrar desde cero: borra los datos (`DELETE FROM seguimiento; DELETE FROM denuncia; DELETE FROM usuario;`) y vuelve a poner `prod,seed`.

## 5. Verificación

```bash
API=https://URL-DEL-BACKEND.up.railway.app
# público (sin credenciales)
curl -s -o /dev/null -w "%{http_code}\n" $API/api/denuncias            # 401
# login/listado con el admin
curl -s -u admin:TU_SEED_ADMIN_PASSWORD $API/api/denuncias | head -c 400
curl -s -u admin:TU_SEED_ADMIN_PASSWORD $API/api/denuncias/estadisticas/por-tipo
curl -sI $API/api/denuncias | grep -i -E "x-content-type|x-frame|referrer|strict"
```

En el navegador: abre el frontend, inicia sesión como `admin` y revisa listado, estadísticas y seguimientos. Si ves error de CORS, `CORS_ALLOWED_ORIGINS` no coincide exactamente con la URL del frontend.

Consultas SQL (Railway → MySQL → Data/Connect):

```sql
SELECT tipo_denuncia, COUNT(*) FROM denuncia GROUP BY tipo_denuncia;
SELECT estado, COUNT(*) FROM denuncia GROUP BY estado;
SELECT COUNT(*) FROM seguimiento;
SELECT d.id, COUNT(s.id) n FROM denuncia d JOIN seguimiento s ON s.denuncia_id = d.id GROUP BY d.id ORDER BY n DESC LIMIT 5;
```

## Checklist de entrega

- [ ] Repo en GitHub (público o con acceso al docente) y `git log` con commits claros
- [ ] URL frontend y backend funcionando por HTTPS
- [ ] `SPRING_PROFILES_ACTIVE=prod` y `DDL_AUTO=validate` tras la carga
- [ ] Usuarios demo probados (admin y un operador)
- [ ] README: reemplazadas las URLs `REEMPLAZAR-…`

## Texto para el informe: «Despliegue y seguridad»

El sistema se desplegó en Railway con tres servicios: MySQL, el backend Spring Boot y el frontend Angular, publicados por HTTPS y conectados mediante variables de entorno (sin credenciales en el repositorio). El backend aplica contraseñas con BCrypt, autorización por roles, validación de entrada, consultas JPQL parametrizadas, CORS restringido a un origen y cabeceras de seguridad (nosniff, X-Frame-Options, Referrer-Policy y HSTS). La base se pobló con 120 denuncias y unos 400 seguimientos ficticios. Quedan como trabajo pendiente: limitación de intentos por IP, cifrado de campos sensibles (AES-256-GCM), CSP en el frontend, integración continua con análisis de secretos y dependencias, WAF (Cloudflare) y migración de HTTP Basic a JWT.
