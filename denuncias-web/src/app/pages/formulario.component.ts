import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { DenunciaService } from '../core/denuncia.service';
import { DenunciaRequest, ETIQUETA_ESTADO, TIPOS, TIPOS_CON_PROTOCOLO } from '../core/models';
import { EtiquetaTipoPipe } from '../core/etiquetas.pipe';
import { mensajeError } from '../core/errores';

/** Crear (POST) cuando no hay :id en la ruta; editar descripción (PUT, solo ADMIN) cuando sí lo hay. */
@Component({
  selector: 'app-formulario',
  standalone: true,
  imports: [FormsModule, RouterLink, EtiquetaTipoPipe],
  template: `
    <div class="card">
      <h2>{{ id ? 'Editar denuncia #' + id : 'Nueva denuncia' }}</h2>
      @if (id) {
        <p class="hint">Solo se puede modificar la descripción; el resto de los datos forma parte del registro original.</p>
      }
      <form (ngSubmit)="guardar(f)" #f="ngForm">
        <label>Tipo de denuncia
          <select name="tipo" [(ngModel)]="m.tipoDenuncia" [disabled]="!!id" required>
            @for (t of tipos; track t) { <option [value]="t">{{ t | etiquetaTipo }}</option> }
          </select>
        </label>
        @if (!id && activaProtocolo) {
          <p class="aviso">Este tipo activa automáticamente el protocolo: la denuncia se registrará en estado «Protocolo activado».</p>
        }

        <label>Nombre del afectado
          <input name="af" #af="ngModel" [(ngModel)]="m.nombreAfectado" [disabled]="!!id" required>
        </label>
        @if (af.invalid && (af.touched || f.submitted)) { <small class="campo-error">El nombre del afectado es obligatorio.</small> }

        <label>Nombre del denunciante (opcional)
          <input name="de" [(ngModel)]="m.nombreDenunciante" [disabled]="!!id">
        </label>

        <label>Descripción
          <textarea name="desc" #desc="ngModel" rows="5" maxlength="2000" [(ngModel)]="m.descripcion" required></textarea>
        </label>
        <small class="contador-chars">{{ m.descripcion.length }} / 2000</small>
        @if (desc.invalid && (desc.touched || f.submitted)) { <small class="campo-error">La descripción es obligatoria.</small> }

        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <div class="botonera">
          <button type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : (id ? 'Guardar cambios' : 'Registrar') }}</button>
          <a routerLink="/denuncias" class="btn secundario">Cancelar</a>
        </div>
      </form>
    </div>`,
})
export class FormularioComponent implements OnInit {
  private svc = inject(DenunciaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  tipos = TIPOS;
  id: number | null = null;
  m: DenunciaRequest = { tipoDenuncia: 'MALTRATO_PSICOLOGICO', descripcion: '', nombreDenunciante: '', nombreAfectado: '' };
  error = signal(''); guardando = signal(false);

  get activaProtocolo(): boolean { return TIPOS_CON_PROTOCOLO.includes(this.m.tipoDenuncia); }

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.id = +p;
      this.svc.obtener(this.id).subscribe({
        next: d => this.m = { tipoDenuncia: d.tipoDenuncia, descripcion: d.descripcion, nombreAfectado: d.nombreAfectado, nombreDenunciante: d.nombreDenunciante ?? '' },
        error: (e: HttpErrorResponse) => this.error.set(mensajeError(e)),
      });
    }
  }

  guardar(f: NgForm) {
    this.error.set('');
    // El backend también valida (@NotBlank → 400); aquí se evita el viaje de red por un error evidente.
    if (f.invalid || !this.m.descripcion.trim() || (!this.id && !this.m.nombreAfectado.trim())) {
      this.error.set('Complete los campos obligatorios.');
      return;
    }
    this.guardando.set(true);
    const idEdicion = this.id;
    const op = idEdicion ? this.svc.actualizar(idEdicion, this.m.descripcion) : this.svc.crear(this.m);
    op.subscribe({
      next: d => {
        const mensaje = idEdicion
          ? `Denuncia #${idEdicion} actualizada`
          : `Denuncia #${d.id} registrada (estado inicial: ${ETIQUETA_ESTADO[d.estado] ?? d.estado})`;
        this.router.navigate(['/denuncias'], { state: { mensaje } });
      },
      error: (e: HttpErrorResponse) => { this.error.set(mensajeError(e)); this.guardando.set(false); },
    });
  }
}
