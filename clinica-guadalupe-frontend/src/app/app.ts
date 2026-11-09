import { Component } from '@angular/core';
import { InicioComponent } from './pages/inicio/inicio.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegistroPacienteComponent } from './pages/auth/registro-paciente/registro-paciente.component';
import { PerfilComponent } from './pages/paciente/perfil/perfil.component';
import { AdministradorComponent } from './pages/administrador/administrador.component';
import { AuthService } from './core/services/auth.service';
import { PortalRoleComponent } from './pages/portal/portal-role.component';
import { DoctorPortalComponent } from './pages/portal/doctor-portal.component';

type View = 'inicio' | 'login' | 'registro' | 'dashboard' | 'perfil';
@Component({ selector: 'app-root', standalone: true, imports: [InicioComponent, LoginComponent, RegistroPacienteComponent, PerfilComponent, AdministradorComponent, PortalRoleComponent, DoctorPortalComponent], templateUrl: './app.html', styleUrls: ['./app.css', './app-extra.css'] })
export class App {
  view: View = 'inicio'; message = '';
  constructor(private readonly auth: AuthService) {}
  get token(): string { return this.auth.token(); }
  get userName(): string { return localStorage.getItem('cg_name') ?? ''; }
  get userRole(): string { return this.auth.role(); }
  go(view: View): void { this.message = ''; this.view = view; }
  onLogin(): void { this.view = 'dashboard'; }
  onRegister(email: string): void { this.message = `Cuenta creada para ${email}. Ahora inicia sesión.`; this.view = 'login'; }
  logout(): void { this.auth.clearSession(); this.view = 'inicio'; }
  sessionExpired(): void { this.auth.clearSession(); this.view = 'login'; this.message = 'Tu sesión expiró. Inicia sesión nuevamente.'; }
}
