import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { Denuncia, DenunciaRequest, Seguimiento, SeguimientoRequest } from './models';

/** Servicio que encapsula todo el consumo REST de /api/denuncias y /api/publico. */
@Injectable({ providedIn: 'root' })
export class DenunciaService {
  private http = inject(HttpClient);
  private url = `${API_URL}/denuncias`;

  info(): Observable<Record<string, string>> { return this.http.get<Record<string, string>>(`${API_URL}/publico/info`); }
  listar(): Observable<Denuncia[]> { return this.http.get<Denuncia[]>(this.url); }
  porEstado(estado: string): Observable<Denuncia[]> { return this.http.get<Denuncia[]>(`${this.url}/estado/${estado}`); }
  obtener(id: number): Observable<Denuncia> { return this.http.get<Denuncia>(`${this.url}/${id}`); }
  crear(d: DenunciaRequest): Observable<Denuncia> { return this.http.post<Denuncia>(this.url, d); }
  actualizar(id: number, descripcion: string): Observable<Denuncia> { return this.http.put<Denuncia>(`${this.url}/${id}`, { descripcion }); }
  eliminar(id: number): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
  seguimientos(id: number): Observable<Seguimiento[]> { return this.http.get<Seguimiento[]>(`${this.url}/${id}/seguimientos`); }
  registrarSeguimiento(id: number, s: SeguimientoRequest): Observable<Seguimiento> { return this.http.post<Seguimiento>(`${this.url}/${id}/seguimientos`, s); }
}
