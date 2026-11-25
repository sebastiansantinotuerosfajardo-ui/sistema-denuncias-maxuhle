import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, CanActivateFn, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { adminGuard, authGuard } from './auth.guard';

describe('guards de ruta', () => {
  let auth: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
  });

  const ejecutar = (guard: CanActivateFn) =>
    TestBed.runInInjectionContext(() => guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  it('authGuard deja pasar a un usuario con sesión', () => {
    auth.sesion.set({ usuario: 'operador', rol: 'OPERADOR', token: 'x' });
    expect(ejecutar(authGuard)).toBeTrue();
  });

  it('authGuard redirige a /login si no hay sesión', () => {
    expect((ejecutar(authGuard) as UrlTree).toString()).toBe('/login');
  });

  it('adminGuard deja pasar a un ADMIN', () => {
    auth.sesion.set({ usuario: 'admin', rol: 'ADMIN', token: 'x' });
    expect(ejecutar(adminGuard)).toBeTrue();
  });

  it('adminGuard redirige a /denuncias si el usuario es OPERADOR', () => {
    auth.sesion.set({ usuario: 'operador', rol: 'OPERADOR', token: 'x' });
    expect((ejecutar(adminGuard) as UrlTree).toString()).toBe('/denuncias');
  });
});
