import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { API_URL } from './api.config';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let cliente: HttpClient;
  let ctrl: HttpTestingController;
  let auth: AuthService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    cliente = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });
  afterEach(() => ctrl.verify());

  const iniciarSesion = () => auth.sesion.set({ usuario: 'admin', rol: 'ADMIN', token: 'dG9rZW4=' });

  it('agrega Authorization: Basic a las peticiones hacia la API cuando hay sesión', () => {
    iniciarSesion();
    cliente.get(`${API_URL}/denuncias`).subscribe();
    const req = ctrl.expectOne(`${API_URL}/denuncias`);
    expect(req.request.headers.get('Authorization')).toBe('Basic dG9rZW4=');
    req.flush([]);
  });

  it('no agrega nada si no hay sesión', () => {
    cliente.get(`${API_URL}/publico/info`).subscribe();
    const req = ctrl.expectOne(`${API_URL}/publico/info`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('no envía la credencial a otros dominios', () => {
    iniciarSesion();
    cliente.get('https://ejemplo.com/datos').subscribe();
    const req = ctrl.expectOne('https://ejemplo.com/datos');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('respeta un Authorization que ya viene en la petición (caso del login)', () => {
    iniciarSesion();
    cliente.get(`${API_URL}/denuncias`, { headers: { Authorization: 'Basic otro' } }).subscribe();
    const req = ctrl.expectOne(`${API_URL}/denuncias`);
    expect(req.request.headers.get('Authorization')).toBe('Basic otro');
    req.flush([]);
  });

  it('401 con sesión activa: cierra la sesión y vuelve al login', () => {
    iniciarSesion();
    const navegar = spyOn(router, 'navigate').and.resolveTo(true);
    cliente.get(`${API_URL}/denuncias`).subscribe({ error: () => undefined });
    ctrl.expectOne(`${API_URL}/denuncias`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(auth.autenticado).toBeFalse();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { motivo: 'sesion' } });
  });

  it('403 no cierra la sesión (es falta de permiso, no de autenticación)', () => {
    iniciarSesion();
    const navegar = spyOn(router, 'navigate').and.resolveTo(true);
    cliente.get(`${API_URL}/denuncias/estadisticas/por-tipo`).subscribe({ error: () => undefined });
    ctrl.expectOne(`${API_URL}/denuncias/estadisticas/por-tipo`).flush(null, { status: 403, statusText: 'Forbidden' });
    expect(auth.autenticado).toBeTrue();
    expect(navegar).not.toHaveBeenCalled();
  });
});
