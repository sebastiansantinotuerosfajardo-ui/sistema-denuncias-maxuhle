import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { DenunciaService } from '../core/denuncia.service';
import { AuthService } from '../core/auth.service';
import { Denuncia, ESTADOS, ETIQUETA_ESTADO, Seguimiento } from '../core/models';
import { EtiquetaEstadoPipe, EtiquetaTipoPipe } from '../core/etiquetas.pipe';
import { mensajeError } from '../core/errores';

@Component({
  selector: 'app-detalle',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, EtiquetaTipoPipe, EtiquetaEstadoPipe],
  template: `
    <a routerLink="/denuncias">← Volver al listado</a>
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

    @if (d(); as den) {
      <div class="card">
        <h2>Denuncia #{{ den.id }} <span [class]="'chip estado-' + den.estado">{{ den.estado | etiquetaEstado }}</span></h2>
        <p>
          <b>Tipo:</b> {{ den.tipoDenuncia | etiquetaTipo }} ·
          <b>Protocolo activado:</b> {{ den.protocoloActivado ? 'Sí' : 'No' }} ·
          <b>Registrada:</b> {{ den.fechaRegistro | date:'dd/MM/yyyy HH:mm' }}
        </p>
        <p><b>Afectado:</b> {{ den.nombreAfectado }} · <b>Denunciante:</b> {{ den.nombreDenunciante || '—' }}</p>
        <p class="descripcion">{{ den.descripcion }}</p>
      </div>

      <h3>Historial de seguimiento</h3>
      <div class="tabla-wrap">
        <table>
          <caption class="sr-only">Historial de seguimiento de la denuncia {{ den.id }}</caption>
          <thead><tr><th>Fecha</th><th>Responsable</th><th>Cambio</th><th>Observación</th></tr></thead>
          <tbody>
            @for (s of seguimientos(); track s.id) {
              <tr>
                <td>{{ s.fecha | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ s.responsable }}</td>
                <td>{{ s.estadoAnterior ? (s.estadoAnterior | etiquetaEstado) : '—' }} → {{ s.estadoNuevo | etiquetaEstado }}</td>
                <td>{{ s.observacion || '—' }}</td>
              </tr>
            } @empty { <tr><td colspan="4" class="vacio">Sin movimientos registrados.</td></tr> }
          </tbody>
        </table>
      </div>

      @if (auth.esAdmin) {
        <div class="card">
          <h3>Registrar seguimiento (solo ADMIN)</h3>
          @if (den.estado === 'CERRADA') {
            <p class="aviso">Esta denuncia está cerrada: el backend rechazará cualquier cambio de estado (HTTP 409).</p>
          }
          <form (ngSubmit)="registrar()">
            <label>Nuevo estado
              <select name="e" [(ngModel)]="nuevoEstado">
                @for (e of opciones(); track e) { <option [value]="e">{{ e | etiquetaEstado }}</option> }
              </select>
            </label>
            <label>Observación (obligatoria para cerrar)
              <textarea name="o" rows="3" maxlength="1000" [(ngModel)]="observacion"></textarea>
            </label>
            <p class="hint">Reglas del backend: una denuncia cerrada no admite cambios; un protocolo activado no puede volver a «Recibida»; para cerrar se exige una observación.</p>
            <button type="submit" [disabled]="registrando()">{{ registrando() ? 'Registrando…' : 'Registrar' }}</button>
          </form>
          @if (ok()) { <p class="ok" role="status">{{ ok() }}</p> }
          @if (errorSeguimiento()) { <p class="error" role="alert">{{ errorSeguimiento() }}</p> }
        </div>
      }
    }`,
})
export class DetalleComponent implements OnInit {
  private svc = inject(DenunciaService);
  private route = inject(ActivatedRoute);
  auth = inject(AuthService);
  id = 0;
  d = signal<Denuncia | null>(null);
  seguimientos = signal<Seguimiento[]>([]);
  /** Estados a los que se puede pasar: todos menos el actual. */
  opciones = computed(() => ESTADOS.filter(e => e !== this.d()?.estado));
  nuevoEstado = 'EN_ATENCION';
  observacion = '';
  error = signal(''); errorSeguimiento = signal(''); ok = signal(''); registrando = signal(false);

  ngOnInit() { this.id = +this.route.snapshot.paramMap.get('id')!; this.cargar(); }

  cargar() {
    this.error.set('');
    const fallo = (e: HttpErrorResponse) => this.error.set(mensajeError(e));
    this.svc.obtener(this.id).subscribe({
      next: d => { this.d.set(d); this.nuevoEstado = ['EN_ATENCION', 'DERIVADA', 'CERRADA'].find(e => e !== d.estado)!; },
      error: fallo,
    });
    this.svc.seguimientos(this.id).subscribe({ next: s => this.seguimientos.set(s), error: fallo });
  }

  registrar() {
    this.errorSeguimiento.set(''); this.ok.set(''); this.registrando.set(true);
    const anterior = this.d()?.estado ?? '';
    this.svc.registrarSeguimiento(this.id, { estadoNuevo: this.nuevoEstado, observacion: this.observacion }).subscribe({
      next: s => {
        this.ok.set(`Seguimiento registrado: ${ETIQUETA_ESTADO[anterior] ?? anterior} → ${ETIQUETA_ESTADO[s.estadoNuevo] ?? s.estadoNuevo}`);
        this.observacion = ''; this.registrando.set(false); this.cargar();
      },
      // p. ej. 409 por regla de negocio: se muestra el mensaje que envía el backend
      error: (e: HttpErrorResponse) => { this.errorSeguimiento.set(mensajeError(e)); this.registrando.set(false); },
    });
  }
}
