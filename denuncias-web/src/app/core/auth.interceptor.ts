import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from './api.config';
import { AuthService } from './auth.service';

/**
 * Agrega Authorization: Basic ... solo a las peticiones hacia nuestra API (nunca a otros dominios).
 * Si el backend responde 401 con una sesión activa, la sesión deja de ser válida: se cierra y se vuelve al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(API_URL)) return next(req);

  const auth = inject(AuthService);
  const token = auth.token;
  // Sin sesión (p. ej. /publico/info) o con Authorization propio (login): se deja pasar tal cual.
  if (!token || req.headers.has('Authorization')) return next(req);

  const router = inject(Router);
  return next(req.clone({ setHeaders: { Authorization: `Basic ${token}` } })).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { motivo: 'sesion' } });
      }
      return throwError(() => e);
    }));
};
