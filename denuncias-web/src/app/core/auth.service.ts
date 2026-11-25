import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { API_URL } from './api.config';
import { Rol } from './models';

export interface Sesion { usuario: string; rol: Rol; token: string; }

/** Base64 de los bytes UTF-8 (btoa a secas falla con caracteres fuera de Latin-1 y Spring Security decodifica en UTF-8). */
export function base64Utf8(texto: string): string {
  let binario = '';
  new TextEncoder().encode(texto).forEach(b => (binario += String.fromCharCode(b)));
  return btoa(binario);
}

/**
 * El backend (Avance 2) usa HTTP Basic y NO tiene /api/auth/login ni JWT.
 * El "login" envía las credenciales a un endpoint exclusivo de ADMIN y deduce el resultado por el código HTTP:
 *   200 → credenciales válidas y rol ADMIN
 *   403 → credenciales válidas, pero el rol es OPERADOR (autenticado sin permiso)
 *   401 → credenciales inválidas
 *   otro (0 = sin conexión o CORS, 5xx…) → se informa el error; nunca se asume un rol.
 * Limitación conocida (ver informe): el rol se infiere; con JWT o GET /api/auth/me vendría explícito.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private readonly CLAVE = 'sesion-denuncias';
  sesion = signal<Sesion | null>(this.leer());

  private leer(): Sesion | null {
    try { return JSON.parse(sessionStorage.getItem(this.CLAVE) ?? 'null'); } catch { return null; }
  }

  get token(): string | null { return this.sesion()?.token ?? null; }
  get esAdmin(): boolean { return this.sesion()?.rol === 'ADMIN'; }
  get autenticado(): boolean { return this.sesion() !== null; }

  login(usuario: string, clave: string): Observable<Sesion> {
    const token = base64Utf8(`${usuario}:${clave}`);
    const headers = new HttpHeaders({ Authorization: `Basic ${token}` });
    return this.http.get(`${API_URL}/denuncias/estadisticas/por-tipo`, { headers }).pipe(
      map((): Rol => 'ADMIN'),
      catchError((e: HttpErrorResponse) => e.status === 403 ? of<Rol>('OPERADOR') : throwError(() => e)),
      map(rol => {
        const s: Sesion = { usuario, rol, token };
        sessionStorage.setItem(this.CLAVE, JSON.stringify(s));
        this.sesion.set(s);
        return s;
      }));
  }

  logout(): void {
    sessionStorage.removeItem(this.CLAVE);
    this.sesion.set(null);
  }
}
