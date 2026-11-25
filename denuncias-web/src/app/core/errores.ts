import { HttpErrorResponse } from '@angular/common/http';

/** Traduce el JSON de error del backend ({error, campos}) o el código HTTP a un mensaje legible. */
export function mensajeError(e: HttpErrorResponse): string {
  if (e.status === 0) return 'No hay conexión con la API (¿Spring Boot activo en :8080? ¿CORS?)';
  if (e.status === 401) return 'Credenciales inválidas (401)';
  if (e.status === 403) return 'No tiene permiso para esta operación (403)';
  const campos = e.error?.campos ? ' — ' + Object.values(e.error.campos).join('; ') : '';
  return `${e.error?.error ?? 'Error'} (${e.status})${campos}`;
}
