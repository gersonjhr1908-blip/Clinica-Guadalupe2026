import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse { token: string; usuarioId: number; nombreCompleto: string; email: string; rol: string; }
export interface RegistroPaciente { nombres: string; apellidos: string; dni: string; email: string; telefono: string; password: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api';
  login(data: { email: string; password: string }): Observable<LoginResponse> { return this.http.post<LoginResponse>(`${this.api}/auth/login`, data); }
  register(data: RegistroPaciente): Observable<unknown> { return this.http.post(`${this.api}/auth/registro-paciente`, data); }
  token(): string { return localStorage.getItem('cg_token') ?? ''; }
  role(): string { return localStorage.getItem('cg_role') ?? ''; }
  saveSession(response: LoginResponse): void { localStorage.setItem('cg_token', response.token); localStorage.setItem('cg_name', response.nombreCompleto); localStorage.setItem('cg_role', response.rol); localStorage.setItem('cg_user_id', String(response.usuarioId)); }
  clearSession(): void { localStorage.removeItem('cg_token'); localStorage.removeItem('cg_name'); localStorage.removeItem('cg_role'); localStorage.removeItem('cg_user_id'); }
}
