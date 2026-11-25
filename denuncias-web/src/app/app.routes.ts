import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login.component';
import { ListaComponent } from './pages/lista.component';
import { FormularioComponent } from './pages/formulario.component';
import { DetalleComponent } from './pages/detalle.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión · Denuncias Max Uhle' },
  { path: 'denuncias', component: ListaComponent, canActivate: [authGuard], title: 'Denuncias · Max Uhle' },
  { path: 'denuncias/nueva', component: FormularioComponent, canActivate: [authGuard], title: 'Nueva denuncia · Max Uhle' },
  { path: 'denuncias/:id/editar', component: FormularioComponent, canActivate: [authGuard, adminGuard], title: 'Editar denuncia · Max Uhle' },
  { path: 'denuncias/:id', component: DetalleComponent, canActivate: [authGuard], title: 'Detalle de denuncia · Max Uhle' },
  { path: '', pathMatch: 'full', redirectTo: 'denuncias' },
  { path: '**', redirectTo: 'denuncias' },
];
