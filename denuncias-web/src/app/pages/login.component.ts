import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../core/auth.service';
import { DenunciaService } from '../core/denuncia.service';
import { mensajeError } from '../core/errores';

/** Estado del backend según GET /api/publico/info (endpoint público, sin credenciales). */
type EstadoApi = 'verificando' | 'ok' | 'caida';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="card login">
      <h2>Iniciar sesión</h2>

      <p class="estado-api" role="status" aria-live="polite">
        @switch (api()) {
          @case ('verificando') { Verificando conexión con la API… }
          @case ('ok') { <span class="punto ok-bg"></span> API operativa @if (version()) { · versión {{ version() }} } }
          @case ('caida') {
            <span class="punto error-bg"></span> Sin conexión con la API (¿Spring Boot activo en :8080?)
            <button type="button" class="secundario pequeno" (click)="verificarApi()">Reintentar</button>
          }
        }
      </p>

      @if (motivo === 'sesion') {
        <p class="aviso" role="alert">Su sesión ya no es válida. Ingrese nuevamente.</p>
      }

      <form (ngSubmit)="entrar()">
        <label>Usuario <input name="u" [(ngModel)]="usuario" required autocomplete="username" autofocus></label>
        <label>Contraseña <input name="p" type="password" [(ngModel)]="clave" required autocomplete="current-password"></label>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <button type="submit" [disabled]="cargando() || !usuario || !clave">{{ cargando() ? 'Validando…' : 'Ingresar' }}</button>
      </form>
    </div>`,
})
export class LoginComponent implements OnInit {
  private auth = inject(AuthService);
  private svc = inject(DenunciaService);
  private router = inject(Router);
  motivo = inject(ActivatedRoute).snapshot.queryParamMap.get('motivo');
  usuario = ''; clave = '';
  error = signal(''); cargando = signal(false);
  api = signal<EstadoApi>('verificando');
  version = signal('');

  ngOnInit() { this.verificarApi(); }

  verificarApi() {
    this.api.set('verificando');
    this.svc.info().subscribe({
      next: info => {
        this.version.set(info['version'] ?? '');
        this.api.set(info['estado'] === 'operativo' ? 'ok' : 'caida');
      },
      error: () => this.api.set('caida'),
    });
  }

  entrar() {
    this.error.set(''); this.cargando.set(true);
    this.auth.login(this.usuario, this.clave).subscribe({
      next: () => this.router.navigate(['/denuncias']),
      error: (e: HttpErrorResponse) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
}
