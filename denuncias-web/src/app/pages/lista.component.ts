import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { DenunciaService } from '../core/denuncia.service';
import { AuthService } from '../core/auth.service';
import { Denuncia, ESTADOS } from '../core/models';
import { EtiquetaEstadoPipe, EtiquetaTipoPipe } from '../core/etiquetas.pipe';
import { mensajeError } from '../core/errores';

@Component({
  selector: 'app-lista',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, EtiquetaTipoPipe, EtiquetaEstadoPipe],
  template: `
    <div class="barra">
      <h2>Listado de denuncias</h2>
      <a routerLink="/denuncias/nueva" class="btn">+ Nueva denuncia</a>
    </div>

    <div class="filtros">
      <label>Filtrar por estado
        <select [ngModel]="filtro" (ngModelChange)="filtrar($event)">
          <option value="">Todos</option>
          @for (e of estados; track e) { <option [value]="e">{{ e | etiquetaEstado }}</option> }
        </select>
      </label>
      <span class="contador" aria-live="polite">{{ cargando() ? 'Cargando…' : denuncias().length + ' denuncia(s)' }}</span>
    </div>

    @if (error()) {
      <p class="error" role="alert">{{ error() }} <button type="button" class="secundario pequeno" (click)="cargar()">Reintentar</button></p>
    }
    @if (mensaje()) { <p class="ok" role="status">{{ mensaje() }}</p> }

    <div class="tabla-wrap">
      <table>
        <caption class="sr-only">Denuncias registradas</caption>
        <thead><tr><th>ID</th><th>Tipo</th><th>Afectado</th><th>Estado</th><th>Protocolo</th><th>Registro</th><th>Acciones</th></tr></thead>
        <tbody>
          @for (d of denuncias(); track d.id) {
            <tr>
              <td>{{ d.id }}</td>
              <td>{{ d.tipoDenuncia | etiquetaTipo }}</td>
              <td>{{ d.nombreAfectado }}</td>
              <td><span [class]="'chip estado-' + d.estado">{{ d.estado | etiquetaEstado }}</span></td>
              <td>{{ d.protocoloActivado ? 'Sí' : 'No' }}</td>
              <td>{{ d.fechaRegistro | date:'dd/MM/yyyy HH:mm' }}</td>
              <td class="acciones">
                <a [routerLink]="['/denuncias', d.id]" [attr.aria-label]="'Ver denuncia ' + d.id">Ver</a>
                @if (auth.esAdmin) {
                  <a [routerLink]="['/denuncias', d.id, 'editar']" [attr.aria-label]="'Editar denuncia ' + d.id">Editar</a>
                  <button type="button" class="peligro" [attr.aria-label]="'Eliminar denuncia ' + d.id" (click)="eliminar(d)">Eliminar</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="vacio">{{ cargando() ? 'Cargando…' : 'Sin denuncias para mostrar.' }}</td></tr>
          }
        </tbody>
      </table>
    </div>`,
})
export class ListaComponent implements OnInit, OnDestroy {
  private svc = inject(DenunciaService);
  auth = inject(AuthService);
  estados = ESTADOS;
  filtro = '';
  denuncias = signal<Denuncia[]>([]);
  error = signal('');
  cargando = signal(false);
  /** Mensaje que deja la pantalla anterior (p. ej. «Denuncia #5 registrada») vía navigation state. */
  mensaje = signal<string>(inject(Router).getCurrentNavigation()?.extras.state?.['mensaje'] ?? '');
  private peticion?: Subscription;

  ngOnInit() { this.cargar(); }
  ngOnDestroy() { this.peticion?.unsubscribe(); }

  filtrar(estado: string) { this.filtro = estado; this.mensaje.set(''); this.cargar(); }

  cargar() {
    this.error.set('');
    this.cargando.set(true);
    this.peticion?.unsubscribe(); // evita que una respuesta lenta pise a una más reciente
    const obs = this.filtro ? this.svc.porEstado(this.filtro) : this.svc.listar();
    this.peticion = obs.subscribe({
      next: d => { this.denuncias.set([...d].sort((a, b) => b.id - a.id)); this.cargando.set(false); }, // más recientes primero
      error: (e: HttpErrorResponse) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  eliminar(d: Denuncia) {
    if (!confirm(`¿Eliminar la denuncia #${d.id} y su historial de seguimiento? Esta acción no se puede deshacer.`)) return;
    this.svc.eliminar(d.id).subscribe({
      next: () => { this.cargar(); this.mensaje.set(`Denuncia #${d.id} eliminada`); },
      error: (e: HttpErrorResponse) => this.error.set(mensajeError(e)),
    });
  }
}
