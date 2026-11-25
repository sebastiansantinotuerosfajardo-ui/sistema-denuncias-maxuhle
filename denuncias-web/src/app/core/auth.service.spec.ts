import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { API_URL } from './api.config';
import { AuthService, Sesion, base64Utf8 } from './auth.service';

/** Pruebas unitarias del login (HTTP Basic). El backend se simula con HttpTestingController: NO es una prueba de integración. */
describe('AuthService', () => {
  const SONDA = `${API_URL}/denuncias/estadisticas/por-tipo`; // endpoint exclusivo de ADMIN
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('200 en el endpoint de ADMIN → rol ADMIN y sesión guardada', () => {
    let sesion: Sesion | undefined;
    auth.login('admin', 'admin123').subscribe(s => (sesion = s));
    const req = http.expectOne(SONDA);
    expect(req.request.headers.get('Authorization')).toBe('Basic ' + btoa('admin:admin123'));
    req.flush([]);
    expect(sesion?.rol).toBe('ADMIN');
    expect(auth.esAdmin).toBeTrue();
    expect(auth.autenticado).toBeTrue();
    expect(sessionStorage.getItem('sesion-denuncias')).toContain('"rol":"ADMIN"');
  });

  it('403 (autenticado pero sin permiso) → rol OPERADOR', () => {
    let sesion: Sesion | undefined;
    auth.login('operador', 'operador123').subscribe(s => (sesion = s));
    http.expectOne(SONDA).flush({ error: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
    expect(sesion?.rol).toBe('OPERADOR');
    expect(auth.esAdmin).toBeFalse();
    expect(auth.autenticado).toBeTrue();
  });

  it('401 → credenciales inválidas: se propaga el error y no hay sesión', () => {
    let error: HttpErrorResponse | undefined;
    auth.login('admin', 'mala').subscribe({ error: e => (error = e) });
    http.expectOne(SONDA).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(error?.status).toBe(401);
    expect(auth.autenticado).toBeFalse();
    expect(sessionStorage.getItem('sesion-denuncias')).toBeNull();
  });

  it('status 0 (API caída o CORS) → se propaga el error y NO se asume ningún rol', () => {
    let error: HttpErrorResponse | undefined;
    auth.login('admin', 'admin123').subscribe({ error: e => (error = e) });
    http.expectOne(SONDA).error(new ProgressEvent('error'));
    expect(error?.status).toBe(0);
    expect(auth.autenticado).toBeFalse();
  });

  it('500 → se propaga el error y no se crea sesión', () => {
    let error: HttpErrorResponse | undefined;
    auth.login('admin', 'admin123').subscribe({ error: e => (error = e) });
    http.expectOne(SONDA).flush({ error: 'fallo' }, { status: 500, statusText: 'Server Error' });
    expect(error?.status).toBe(500);
    expect(auth.autenticado).toBeFalse();
  });

  it('las credenciales se codifican en UTF-8 (Spring Security decodifica así)', () => {
    expect(base64Utf8('a:ñ')).toBe('YTrDsQ=='); // bytes 61 3A C3 B1
    expect(base64Utf8('admin:admin123')).toBe(btoa('admin:admin123'));
  });

  it('logout borra la sesión', () => {
    auth.sesion.set({ usuario: 'admin', rol: 'ADMIN', token: 'x' });
    sessionStorage.setItem('sesion-denuncias', '{}');
    auth.logout();
    expect(auth.autenticado).toBeFalse();
    expect(sessionStorage.getItem('sesion-denuncias')).toBeNull();
  });
});
