# Guía de instalación y ejecución – Avance 3 (Sistema de Denuncias · Colegio Max Uhle)

Esta guía deja el sistema funcionando en tu PC (Windows, con la misma configuración que usaron en el Avance 2) y te
indica **qué probar y qué capturar** para el informe y la sustentación. Tiempo estimado: 20–30 min la primera vez.

**Contenido:** 1 Requisitos · 2 Preparar la carpeta · 3 MySQL · 4 Backend · 5 Frontend · 6 Verificación (P1–P13 y capturas C1–C14) ·
7 Pruebas automáticas · 8 Plan B (sin MySQL) · 9 Solución de problemas · 10 Qué se cambió respecto al zip base · 11 Repaso para la sustentación · 12 Lista final

---

## 1. Requisitos

| Software | Versión | Comprobar con | Nota |
|---|---|---|---|
| JDK | 17 o superior (en el Avance 2 usaron 21) | `java -version` | El proyecto compila con nivel Java 17 |
| Maven | 3.9 o superior | `mvn -v` | Debe mostrar el mismo JDK; revisa `JAVA_HOME` |
| MySQL Server | 8.0 o superior (en el Avance 2: servicio `MySQL80`) | Sección **3.1** (servicio, puerto y versión) | Puerto 3306; Workbench opcional para ver tablas. MariaDB/XAMPP **no** es MySQL 8 |
| Node.js | **18.19 o superior** (recomendado 20 LTS o 22) | `node -v` | Angular 18 no corre con Node 16. El equipo lo ejecutó con Node 24.19.0 (Angular CLI 18.2 acepta `>=22`) |
| npm | 9 o superior (viene con Node) | `npm -v` | |
| Google Chrome | actual | — | Para la app, DevTools (pestaña *Red*) y `npm test` |
| VS Code y/o Apache NetBeans | — | — | NetBeans para el backend, VS Code para el frontend (como en S14) |
| Postman | opcional | — | Solo para repetir la colección del Avance 2 |

## 2. Preparar la carpeta

1. Descomprime `Avance3_Denuncias_Angular_SpringBoot.zip` en una ruta **corta y sin tildes ni espacios**. En esta guía se usa como ejemplo `C:\Users\USER\Documents\Avance3_Denuncias_Angular_SpringBoot`; **cambia `USER` y la ruta por las tuyas**.
   **Ojo:** con *Extraer todo*, Windows suele crear una carpeta dentro de otra con el mismo nombre. La carpeta correcta es la que contiene **juntas** `denuncias-api` y `denuncias-web`. Compruébalo en PowerShell:
   ```powershell
   dir C:\Users\USER\Documents\Avance3_Denuncias_Angular_SpringBoot
   ```
   Si ves `denuncias-api` y `denuncias-web`, esa es la ruta. Si solo ves otra carpeta llamada igual, entra en ella: la ruta correcta lleva el nombre repetido dos veces (`...\Avance3_Denuncias_Angular_SpringBoot\Avance3_Denuncias_Angular_SpringBoot`).
2. Verás dos proyectos independientes: `denuncias-api` (backend) y `denuncias-web` (frontend). Se ejecutan en **dos terminales distintas** y ambas deben quedar abiertas.

> Si prefieres los proyectos por separado, `denuncias-api.zip` y `denuncias-web.zip` contienen exactamente lo mismo.

## 3. MySQL

### 3.1 Comprobar que tienes MySQL y que está encendido

No hace falta una versión exacta: sirve cualquier **MySQL Server 8.0 o superior** (Community) escuchando en el puerto **3306**.
Abre **PowerShell** (clic derecho en *Inicio* → *Terminal* o *Windows PowerShell*). **CMD también sirve**; donde el comando cambia, va indicado.
Las tres comprobaciones, en este orden:

**a) ¿Existe el servicio y está encendido?**
```powershell
Get-Service MySQL*
```
Debe aparecer una línea con `Running` y un nombre como `MySQL80` (puede ser `MySQL84` u otro, no importa). En CMD: `sc query MySQL80` → debe decir `STATE : 4 RUNNING`.
- Si aparece `Stopped`: `Win + R` → `services.msc` → clic derecho en el servicio de MySQL → *Iniciar*.
- Si PowerShell no muestra nada, MySQL no está instalado como servicio: instálalo (MySQL Installer, versión 8.x) o salta al **Plan B (sección 8)**.

**b) ¿Escucha en el puerto 3306?**
```powershell
Test-NetConnection localhost -Port 3306
```
Debe mostrar `TcpTestSucceeded : True`. En CMD: `netstat -ano | findstr :3306` → una línea con `LISTENING`.

**c) ¿Qué versión es y tu contraseña funciona?**
```powershell
mysql -u root -p -e "SELECT VERSION();"
```
Te pide la contraseña (la que pusiste al instalar MySQL, la misma de Workbench) y debe mostrar `8.0.xx` (o `8.4.x`, `9.x`). Si muestra algo como `10.x-MariaDB`, es MariaDB (por ejemplo, la de XAMPP) y **no** es el MySQL del proyecto: instala MySQL 8 o usa el Plan B. Si responde `ERROR 1045 … Access denied`, esa no es la contraseña de `root`: pruébala en Workbench; si allí entras con otro usuario, ese usuario es el que indicarás luego con `DB_USER`.

**Si dice que `mysql` «no se reconoce como un comando»:** el programa existe, pero su carpeta no está en el `PATH`. Dos salidas:
1. Averigua dónde está y úsalo solo en esta ventana:
   ```powershell
   (Get-CimInstance Win32_Service -Filter "Name='MySQL80'").PathName
   ```
   Cambia `MySQL80` por el nombre que viste en el paso a). Mostrará algo como `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" …`. Agrega esa carpeta `bin` al `PATH` de la ventana y repite el paso c):
   ```powershell
   $env:Path += ";C:\Program Files\MySQL\MySQL Server 8.0\bin"
   ```
   (En CMD: `set PATH=%PATH%;C:\Program Files\MySQL\MySQL Server 8.0\bin`).
2. O usa **MySQL Workbench** (el mismo del Avance 2): abre tu conexión local, escribe la contraseña y ejecuta `SELECT VERSION();`. Si conecta y muestra 8.x, MySQL está bien.

Con el servicio en `Running`, el puerto 3306 activo y la contraseña funcionando, solo falta el apartado 3.2.

### 3.2 Contraseña y base de datos

El backend usa por defecto usuario **root** y contraseña **root**. Si tu MySQL tiene otra contraseña (lo normal), **no la escribas en ningún archivo**: defínela como variable de entorno **en la misma terminal donde arrancarás el backend**:

| Terminal | Comando |
|---|---|
| PowerShell | `$env:DB_PASSWORD = "tu_clave"` (y si tu usuario no es root: `$env:DB_USER = "tu_usuario"`) |
| CMD | `set DB_PASSWORD=tu_clave` |
| Git Bash / Linux / macOS | `export DB_PASSWORD=tu_clave` |

- No hace falta crear la base: `denuncias_maxuhle` y sus tablas (`denuncia`, `seguimiento`, `usuario`) se crean solas al primer arranque, y los usuarios `admin` y `operador` también.
- Si en tu PC quedó una base de una versión anterior y el backend falla al arrancar por el esquema, recréala:
   `mysql -u root -p -e "DROP DATABASE IF EXISTS denuncias_maxuhle;"` (borra los datos de prueba).

## 4. Backend (Spring Boot) – puerto 8080

### Opción A: terminal (la más simple), paso a paso

Usa **una ventana de PowerShell solo para el backend** (normal; no hace falta «como administrador») y no la cierres mientras uses el sistema.

1. Entra a la carpeta del backend, la que contiene `pom.xml` (ajusta la ruta a la tuya, ver apartado 2):
   ```powershell
   cd C:\Users\USER\Documents\Avance3_Denuncias_Angular_SpringBoot\denuncias-api
   dir
   ```
   `dir` debe mostrar `pom.xml` y la carpeta `src`. Si dice que no encuentra la ruta o no ves `pom.xml`, revisa la carpeta repetida del apartado 2.
2. Guarda la clave de MySQL para **esta** ventana (es la misma que escribiste en el paso 3.1 con `mysql -u root -p`; si tu clave fuera `root`, sáltate este paso). Te la pregunta y la escribes:
   ```powershell
   $env:DB_PASSWORD = Read-Host "Clave de MySQL"
   ```
   Sirve con cualquier símbolo. Vale solo para esta ventana: si la cierras, repite este paso.
3. Arranca el backend:
   ```powershell
   mvn spring-boot:run
   ```
   La primera vez Maven descarga dependencias (varios minutos). Verás muchas líneas, incluidas consultas SQL: es normal. Terminó bien cuando aparecen `Tomcat started on port 8080` y `Started DenunciasApiApplication in … seconds`.
4. **No cierres ni escribas en esta ventana**: ahí vive el servidor. Para el frontend abrirás **otra** ventana (apartado 5). Para detener el backend: `Ctrl + C`.

Si falla: `Access denied` → la clave del paso 2 no es la de MySQL (repítelo); `Port 8080 was already in use` → hay otro backend abierto (por ejemplo el del Avance 2 o NetBeans): ciérralo; otros casos, apartado 9.

### Opción B: Apache NetBeans
1. *File → Open Project…* → selecciona la carpeta `denuncias-api` (se reconoce como proyecto Maven).
2. Para la clave de MySQL: clic derecho en el proyecto → *Properties → Actions → Run project → Set Properties* → agrega `Env.DB_PASSWORD=tu_clave`.
3. Ejecuta con **Run Project (F6)**. Si no arranca, clic derecho sobre `src/main/java/com/maxuhle/denuncias/DenunciasApiApplication.java` → *Run File*.

#### Si NetBeans pide elegir la clase principal o no la encuentra (*main class*)

**Qué significa.** La *clase principal* es la que contiene el método `main` y arranca Spring Boot. En este proyecto hay una sola: `com.maxuhle.denuncias.DenunciasApiApplication` (archivo `src/main/java/com/maxuhle/denuncias/DenunciasApiApplication.java`). Si NetBeans no sabe cuál es, al pulsar *Run* puede mostrar un cuadro para elegirla o fallar con mensajes como «Could not find or load main class» o «The parameters 'mainClass' … are missing or invalid». Con PowerShell (`mvn spring-boot:run`, Opción A) **no hace falta nada de esto**: el plugin de Spring Boot localiza la clase por sí solo.

**Solución (una línea en `pom.xml`):**
1. Abre `denuncias-api/pom.xml` (en NetBeans: *Project Files → pom.xml*).
2. Dentro del bloque `<properties>`, agrega la línea `<start-class>`. Debe quedar así:
   ```xml
   <properties>
       <java.version>17</java.version>
       <start-class>com.maxuhle.denuncias.DenunciasApiApplication</start-class>
   </properties>
   ```
3. Guarda con **Ctrl + S** y vuelve a ejecutar (**Run**).

`start-class` es una propiedad de Maven y Spring Boot (no de NetBeans) que declara cuál es la clase principal; se aplica porque este `pom.xml` hereda de `spring-boot-starter-parent`. Escribe el nombre completo, con el paquete y respetando mayúsculas: un nombre equivocado impide arrancar. **El `pom.xml` entregado no trae esta línea** (es el del Avance 2, sin cambios); agrégala solo si NetBeans la necesita.

**Si NetBeans sigue pidiendo la clase** (o prefieres no editar el `pom.xml`), sirve cualquiera de estas alternativas:

| Alternativa | Pasos en NetBeans |
|---|---|
| Elegir la clase en las propiedades del proyecto | Clic derecho en el proyecto → *Properties → Run → Main Class → Browse…* → `com.maxuhle.denuncias.DenunciasApiApplication` → *Select Main Class* → *OK* |
| Que *Run* use el objetivo de Maven | Clic derecho en el proyecto → *Properties → Actions → Run project* → en *Execute Goals* escribe `spring-boot:run` → *OK*. Deja en *Set Properties* la línea `Env.DB_PASSWORD=tu_clave` del paso 2 |
| Ejecutar solo ese archivo | Clic derecho en `DenunciasApiApplication.java` → *Run File* (Shift + F6) |

### Comprobar que el backend responde
- Navegador: <http://localhost:8080/api/publico/info> → debe mostrar `{"sistema":"API de Denuncias - Colegio Max Uhle (Arequipa)","version":"0.2.0","estado":"operativo"}` (no pide credenciales).
- Navegador: <http://localhost:8080/api/denuncias> → el navegador pide usuario y contraseña (HTTP Basic): `admin` / `admin123` → muestra `[]` o la lista en JSON.

## 5. Frontend (Angular) – puerto 4200

Abre una **segunda ventana de PowerShell** (la del backend debe seguir abierta y sin tocar) y escribe:
```powershell
cd C:\Users\USER\Documents\Avance3_Denuncias_Angular_SpringBoot\denuncias-web
dir                  # debe mostrar package.json y la carpeta src
npm install          # solo la primera vez (1–3 min)
npm start            # equivale a: ng serve
```
- Si pregunta por *analytics* o *autocompletado* de Angular, responde **N**.
- Espera `Local: http://localhost:4200/` y abre **<http://localhost:4200>** en Chrome. Usa `localhost`, **no** `127.0.0.1`: el CORS del backend solo admite el origen `http://localhost:4200`.
- Si `ng` no se reconoce, no pasa nada: `npm start` y `npx ng …` usan el Angular local del proyecto.

| Usuario | Contraseña | Qué puede hacer |
|---|---|---|
| `admin` | `admin123` | Todo: registrar, ver, **editar, eliminar, registrar seguimientos** |
| `operador` | `operador123` | Registrar y consultar (sin editar/eliminar/seguimiento) |

Qué debes ver al abrir la app: la pantalla de login con **«● API operativa · versión 0.2.0»** (la app consultó `GET /api/publico/info`). Si dice «Sin conexión con la API», el backend no está activo o no es el puerto 8080.

---

## 6. Verificación de integración (pruebas P1–P13) y capturas

Con **MySQL, backend (8080) y frontend (4200) activos**, ejecuta en orden. Solo lo que pase y esté **capturado** puede declararse
«integrado y probado de extremo a extremo» en la matriz del informe. Para cada captura usa recortes nítidos (Win + Shift + S).
Abre DevTools (`F12` → pestaña **Red**; si tu Chrome está en inglés se llama **Network**) cuando se indique. Si DevTools muestra el aviso azul de idioma, pulsa *Don't show again*.

**Preparación (2 min):** en Chrome, `F12` → pestaña **Red** (*Network*), filtro **Fetch/XHR**, marca **Disable cache** (*Inhabilitar caché*) y activa la columna **Método** (clic derecho en el encabezado de la tabla → *Method*) para ver GET, POST, PUT y DELETE. En las versiones recientes de Chrome, una petición con `Authorization` aparece como «GET + Preflight»: el `OPTIONS` previo va unido a su fila. Abre **MySQL Workbench** con las consultas de más abajo (empieza con `USE denuncias_maxuhle;`). Captura con `Win + Shift + S` recortando al área útil (aplicación, pestaña Red y Workbench), sin pestañas personales ni barra de tareas. Si una evidencia necesita navegador y Workbench, haz dos capturas y pégalas en el mismo marco del informe.

**Orden recomendado de los datos:** P1–P5 no modifican nada. En **P6** creas una denuncia de prueba (anota su número `#N`; sirve `admin` u `operador`) y la usas en P8, P9 y P10; en **P11** eliminas **solo** esa, nunca las del Avance 2. **P8 a P11 requieren `admin`**: con `operador` no aparecen Editar, Eliminar ni el cuadro de seguimiento, así que pulsa *Salir* y entra como `admin` antes de P8. Haz en Workbench las consultas de cada prueba **antes de eliminar la denuncia** (P11), porque después ya no existirá.

| N.º | Prueba (pasos) | Resultado esperado | Captura |
|---|---|---|---|
| P1 | Abrir `http://localhost:4200/login` **sin sesión** (si hay una abierta, pulsa *Salir*) con `F12 → Red` visible y recargar con `F5` | Indicador «API operativa · versión 0.2.0». En *Red*: `info` con estado 200 y método **GET** sin «+ Preflight» (el GET público no lleva `Authorization`, que siempre exige una comprobación previa) | **C1** *(evidencia mínima común del profesor)* |
| P2 | Login `admin` / `admin123` | Entra a «Listado de denuncias»; la cabecera muestra `admin ADMIN`. En *Red*: `por-tipo` 200 (opcional: clic en la fila → *Headers* para ver `Authorization: Basic …`) | **C2** |
| P3 | Salir → login `admin` / `incorrecta` | Mensaje «Credenciales inválidas (401)»; sigue en `/login` | **C3** |
| P4 | Login `operador` / `operador123` | Cabecera `operador OPERADOR`; **no** aparecen Editar/Eliminar. En *Red*: `por-tipo` 403 (así se deduce el rol). En el detalle de una denuncia tampoco aparece el cuadro de seguimiento | **C4** |
| P5 | Listado (`/denuncias`); luego elegir un estado en *Filtrar por estado* (p. ej. «En atención») | Tabla con datos reales. Mostrar junto a Workbench `SELECT estado, COUNT(*) AS total FROM denuncia GROUP BY estado;`, cuyos totales deben coincidir con los de cada filtro. En *Red*: `GET /api/denuncias/estado/EN_ATENCION` (consulta JPQL 1) y solo quedan las filas con ese estado. Un estado sin denuncias (p. ej. «Derivada») muestra «Sin denuncias para mostrar» | **C5** |
| P6 | Nueva denuncia (`admin` u `operador`): tipo *Maltrato psicológico*, afectado y descripción → Registrar | Vuelve al listado con «Denuncia #N registrada (estado inicial: Protocolo activado)». En *Red*: `POST` 201. En MySQL: nueva fila con `protocolo_activado = 1` y `estado = PROTOCOLO_ACTIVADO`, más su primer seguimiento | **C6** |
| P7 | Nueva denuncia con campos vacíos → Registrar | Mensajes «obligatorio» en rojo; **no** se envía la petición (pestaña *Red* sin POST) | **C7** |
| P8 | `admin`: Editar una denuncia (solo se puede cambiar la descripción) → Guardar | «Denuncia #N actualizada»; en *Red*: `PUT` 200; en MySQL cambia solo `descripcion` | **C8** |
| P9 | `admin`: Ver → en «Registrar seguimiento (solo ADMIN)» dejar «En atención» (ya viene elegido) + escribir una observación → Registrar | Mensaje «Seguimiento registrado: …» y nueva fila en el historial (responsable `admin`). En *Red*: `POST seguimientos` 201 (si el panel es pequeño y la fila `POST` queda oculta, agranda F12 o sube la lista antes de capturar). En MySQL: nueva fila en `seguimiento` | **C9** |
| P10 | `admin`: en la misma denuncia (ahora «En atención») elegir «Cerrada» (el cuadro propone «Derivada») **sin** observación → Registrar | Mensaje rojo «Para cerrar una denuncia debe registrar una observación (409)»; en *Red*: `POST seguimientos` 409. El estado y el historial no cambian (verificar en MySQL) | **C10** |
| P11 | `admin`: Eliminar la denuncia de prueba (confirmar el aviso del navegador) | «Denuncia #N eliminada»; desaparece. En *Red*: `DELETE` 204. En MySQL ya no está la fila ni sus seguimientos (`COUNT(*)` = 0 en ambas tablas) | **C11** |
| P12 | `F12 → Red`: filtro **All**, texto `api/denuncias` en *Filter* y **Disable cache** marcado (para que Chrome envíe el preflight en cada llamada); recargar el listado con `F5` | Dos filas `denuncias`: `OPTIONS` (tipo *preflight*) con 200 y, en su respuesta, `Access-Control-Allow-Origin: http://localhost:4200` con `Allow-Methods` y `Allow-Headers`; y el `GET` con 200 y la misma cabecera `Access-Control-Allow-Origin` | **C12** |
| P13 | Con la sesión cerrada (*Salir*), detener el backend (`Ctrl + C` en su terminal) y recargar `/login` con `F5`. (Opcional: intentar entrar con `admin` / `admin123`) | «Sin conexión con la API» con botón *Reintentar*; en *Red*, `info` en rojo (`net::ERR_CONNECTION_REFUSED`). La API solo se comprueba al cargar la página, por eso el botón aparece tras recargar. Si se intenta entrar, no entra y muestra un mensaje de falta de conexión. Al volver a encender el backend, *Reintentar* restablece «API operativa» | **C13** |

Consultas útiles en MySQL Workbench:
```sql
USE denuncias_maxuhle;
SELECT * FROM denuncia ORDER BY id DESC;
SELECT estado, COUNT(*) AS total FROM denuncia GROUP BY estado;   -- P5: debe coincidir con los totales de cada filtro
SELECT s.id, s.denuncia_id, u.username AS responsable, s.estado_anterior, s.estado_nuevo, s.observacion, s.fecha
  FROM seguimiento s JOIN usuario u ON u.id = s.usuario_id ORDER BY s.id DESC;
SELECT id, username, rol, LEFT(password, 4) AS prefijo_bcrypt FROM usuario;   -- la clave NO está en texto plano
-- P11 (cambia N por el número de tu denuncia de prueba; ambas deben dar 0 después de eliminarla)
SELECT COUNT(*) AS filas FROM denuncia WHERE id = N;
SELECT COUNT(*) AS filas FROM seguimiento WHERE denuncia_id = N;
```

**Demo de sustentación (máx. 3 min):** P1 (login con API operativa) → P2 → P6 (protocolo automático) → P9 → P10 (regla 409) → mostrar en *Red* una petición con `Authorization: Basic` y la fila en MySQL. Cierra explicando qué **no** está integrado (JWT, reportes en pantalla).

## 7. Pruebas automáticas (aisladas; **no** prueban la integración)

| Proyecto | Comando | Resultado esperado | Captura |
|---|---|---|---|
| Backend | `cd denuncias-api` → `mvn test` | `Tests run: 14, Failures: 0, Errors: 0` y `BUILD SUCCESS` (usa H2; no necesita MySQL) | opcional (ya está en el Avance 2) |
| Frontend | `cd denuncias-web` → `npm test` | Abre Chrome (Karma) y termina con `TOTAL: 24 SUCCESS` (HTTP simulado con `HttpTestingController`). Sin ventana: `npx ng test --watch=false --browsers=ChromeHeadless` | **C14** |

## 8. Plan B: si MySQL falla el día de la sustentación

```powershell
cd denuncias-api
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```
Usa una base H2 en memoria (los datos se pierden al detener el servidor) y el frontend funciona igual. **Declara en la sustentación que corriste con H2** y muestra las evidencias de MySQL del Avance 2.

## 9. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| Login muestra «Sin conexión con la API… ¿CORS?» | Backend apagado, otro puerto, o abriste la app con `127.0.0.1` | Arranca el backend; abre `http://localhost:4200`; revisa que `/api/publico/info` responda |
| Backend: `Access denied for user 'root'@'localhost'` | `DB_PASSWORD` no coincide con tu MySQL | Define `DB_PASSWORD` (y `DB_USER`) **en la misma terminal** antes de `mvn spring-boot:run` |
| Backend: `Communications link failure` / `Connection refused` | MySQL detenido | Inicia el servicio `MySQL80` |
| Backend: `Port 8080 was already in use` | Otra instancia o programa en 8080 | Ejecuta `netstat -ano` y busca la línea con `:8080` para ver el PID; ciérralo con `taskkill /PID <pid> /F`; o cierra la terminal anterior |
| `mvn` no se reconoce / `release version 17 not supported` | Maven fuera del PATH o JDK < 17 | Revisa `JAVA_HOME`, `mvn -v` y que el JDK sea 17+ |
| NetBeans marca errores en getters/setters | Procesamiento de anotaciones (Lombok) | Ejecuta con Maven (`mvn spring-boot:run`) o limpia y recompila (*Clean and Build*); la compilación Maven es la que vale |
| NetBeans pide elegir la clase principal o falla con «Could not find or load main class» | NetBeans no sabe cuál es la clase con `main` (`DenunciasApiApplication`) | Agrega `<start-class>com.maxuhle.denuncias.DenunciasApiApplication</start-class>` dentro de `<properties>` del `pom.xml`, guarda (Ctrl + S) y ejecuta de nuevo; alternativas en el apartado 4, Opción B |
| `npm install` falla con `EBADENGINE` o errores de sintaxis | Node demasiado antiguo | Instala Node 20 LTS o 22 y repite |
| `npm ci` falla | (Corregido en este zip: el lock original no coincidía con `package.json`) | Usa `npm install`; si persiste, borra `node_modules` y repite |
| `npm test` no encuentra Chrome | Chrome no instalado o fuera de ruta | Instala Chrome o define `CHROME_BIN`; o usa `npx ng test --watch=false --browsers=ChromeHeadless` |
| Login correcto pero la sesión «se pierde» | La sesión vive en `sessionStorage` (por pestaña) | Es el diseño: al cerrar la pestaña se borra; inicia sesión de nuevo |
| Tras reiniciar el backend los datos «desaparecen» | Estás en el perfil `h2` | Arranca sin `-Dspring-boot.run.profiles=h2` para usar MySQL |
| En PowerShell: `Unknown lifecycle phase ".run.profiles=h2"` | Falta entrecomillar `-D…` | Usa comillas: `mvn spring-boot:run "-Dspring-boot.run.profiles=h2"` |

## 10. Qué se cambió respecto al zip base del compañero (para poder explicarlo)

| Archivo(s) | Cambio y motivo |
|---|---|
| `denuncias-web/package.json`, `package-lock.json` | El lock no coincidía con `package.json` y `npm ci` fallaba (conflicto de `chokidar`). Se fijó `chokidar ^3.6.0` y se regeneró el lock |
| `core/auth.service.ts` | El login hace **una** petición a un endpoint exclusivo de ADMIN y deduce el resultado por el código HTTP: 200 → ADMIN, 403 → OPERADOR, 401 → inválidas; **cualquier otro error se informa** (antes cualquier fallo se interpretaba como OPERADOR). Base64 en UTF-8 |
| `core/auth.interceptor.ts` | Solo agrega `Authorization` a peticiones hacia la API (no a otros dominios); ante 401 con sesión activa cierra la sesión y vuelve al login |
| `core/auth.guard.ts`, `app.routes.ts` | Nuevo `adminGuard` para `/denuncias/:id/editar`; títulos de pestaña por ruta |
| `pages/login.component.ts` | Muestra el estado de la API consultando el GET público `/api/publico/info` (evidencia mínima común) y avisa si la sesión caducó |
| `core/models.ts`, `core/etiquetas.pipe.ts` | Tipo `CONVIVENCIA` (como en las pruebas y Postman del Avance 2) en lugar de `OTRO`; etiquetas legibles |
| `pages/lista`, `formulario`, `detalle` | Estados de carga/vacío/error, validaciones y contador, avisos de reglas de negocio, mensajes de éxito, el estado actual no se ofrece como destino, orden más reciente primero |
| `styles.css`, `index.html` | Estilos de estado, foco visible, tablas adaptables; `lang="es"` y título |
| `*.spec.ts` (4 archivos) | 24 pruebas unitarias nuevas |
| `denuncias-api` | **Sin cambios de código Java** (solo `README`, `.gitignore` y comentarios de `application.properties`). El backend es el del Avance 2 + el CORS que ya agregó el compañero |

## 11. Repaso para la sustentación (preguntas individuales del docente)

| Tema | Respuesta corta en este proyecto | Dónde verlo |
|---|---|---|
| Componente vs servicio (Angular) | El componente controla una pantalla (plantilla, estado, eventos) y **no** conoce HTTP; el servicio concentra el acceso a la API | `pages/*.component.ts` vs `core/denuncia.service.ts` |
| `HttpClient` y URL base | `provideHttpClient(withInterceptors([authInterceptor]))` en `app.config.ts`; URL base `http://localhost:8080/api` en `api.config.ts` | `app.config.ts`, `api.config.ts` |
| Método HTTP ↔ endpoint ↔ JSON | Ej.: `POST /api/denuncias` con `{tipoDenuncia, descripcion, nombreDenunciante, nombreAfectado}` → `201` y la denuncia con `estado` y `protocoloActivado` decididos por el servidor | `DenunciaController`, `denuncia.service.ts` |
| Controller / Service / Repository | Controller: HTTP y `@Valid`; Service: reglas y `@Transactional`; Repository: acceso a datos con JPA/JPQL | `denuncias-api/src/main/java/...` |
| Entidad JPA ↔ tabla | `@Entity Denuncia` ↔ tabla `denuncia`; `Seguimiento` N:1 `Denuncia` y N:1 `Usuario` | `model/`, Workbench |
| CORS | El navegador bloquea llamadas entre orígenes distintos (4200 → 8080); el backend responde el preflight `OPTIONS` y permite el origen `http://localhost:4200` | `SecurityConfig.corsConfigurationSource()` |
| Autenticación | **HTTP Basic**, no JWT: Angular envía `Authorization: Basic base64(usuario:clave)` en cada petición; la API es *stateless*. El rol se deduce por 200/403 | `auth.service.ts`, `auth.interceptor.ts` |
| Códigos HTTP | 401 credenciales inválidas · 403 rol insuficiente · 404 no existe · 409 regla de negocio · 400 validación | `GlobalExceptionHandler`, `errores.ts` |
| Qué NO está integrado | JWT, pantalla de reportes (JPQL de estadísticas y protocolos pendientes), despliegue | Informe, rubros 4 y 11 |

## 12. Lista final antes de entregar (8:00 pm) y sustentar (8:15 pm)

- [ ] Backend y frontend corren; P1–P13 ejecutadas; capturas C1–C14 guardadas.
- [ ] Informe: capturas pegadas en sus marcos, columna **Obtenido** completada (solo lo que pasó) y matriz del rubro 4 actualizada.
- [ ] En Word: clic en el índice → *Actualizar campo* (F9) → exportar a PDF.
- [ ] Zip entregable sin `node_modules`, `target` ni contraseñas.
- [ ] Cada integrante puede explicar la tabla del apartado 11.
