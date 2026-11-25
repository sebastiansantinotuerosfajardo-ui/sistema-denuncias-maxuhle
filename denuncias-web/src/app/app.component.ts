import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header>
      <strong>Denuncias escolares · Colegio Max Uhle</strong>
      @if (auth.sesion(); as s) {
        <span class="sesion">
          <span class="usuario">{{ s.usuario }} <span class="rol">{{ s.rol }}</span></span>
          <button class="secundario" (click)="salir()">Salir</button>
        </span>
      }
    </header>
    <main><router-outlet /></main>`,
})
export class AppComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  salir() { this.auth.logout(); this.router.navigate(['/login']); }
}
