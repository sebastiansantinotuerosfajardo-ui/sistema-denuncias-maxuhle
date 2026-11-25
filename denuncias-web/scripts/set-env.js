// Genera src/environments/environment.prod.ts a partir de la variable API_URL.
// Se ejecuta solo (script "prebuild" de npm) antes de "npm run build".
const fs = require('fs');
const path = require('path');

const raw = (process.env.API_URL || '').trim();
if (!raw) {
  console.log('[set-env] API_URL no definida: se conserva src/environments/environment.prod.ts');
  process.exit(0);
}
let url = raw.replace(/\/+$/, '');
if (!/^https:\/\//i.test(url)) {
  console.error('[set-env] API_URL debe empezar con https:// (recibido: ' + url + ')');
  process.exit(1);
}
if (!/\/api$/.test(url)) url += '/api';

const destino = path.join(__dirname, '..', 'src', 'environments', 'environment.prod.ts');
const contenido =
  '// Archivo generado por scripts/set-env.js a partir de API_URL.\n' +
  'export const environment = {\n' +
  '  production: true,\n' +
  "  apiUrl: '" + url + "',\n" +
  '};\n';
fs.writeFileSync(destino, contenido);
console.log('[set-env] apiUrl =', url);
