import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Exige sesión iniciada; si no la hay, redirige al login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticado ? true : inject(Router).createUrlTree(['/login']);
};

/**
 * Evita mostrar pantallas exclusivas de ADMIN (p. ej. editar) a un OPERADOR.
 * Es solo una ayuda de interfaz: la autorización real la aplica el backend (403).
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.esAdmin ? true : inject(Router).createUrlTree(['/denuncias']);
};
