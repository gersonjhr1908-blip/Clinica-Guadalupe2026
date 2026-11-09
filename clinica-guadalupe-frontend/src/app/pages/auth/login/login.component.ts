import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css', './login-extra.css'],
})
export class LoginComponent {
  @Output() loggedIn = new EventEmitter<void>();
  @Output() registerRequested = new EventEmitter<void>();
  private readonly auth = inject(AuthService);
  form = { email: '', password: '' };
  loading = false;
  error = '';
  passwordVisible = false;
  submit(): void {
    this.error = '';
    this.loading = true;
    this.auth.login(this.form).subscribe({
      next: (response) => {
        this.auth.saveSession(response);
        this.loading = false;
        Swal.fire({
          icon: 'success',
          title: 'Inicio de sesión exitoso',
          text: 'Bienvenido a Clínica Guadalupe.',
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#148b7e',
        }).then(() => this.loggedIn.emit());
      },
      error: (error) => {
        this.loading = false;
        this.error = this.message(error, 'Correo o contraseña incorrectos');
      },
    });
  }
  private message(error: any, fallback: string): string {
    if (error.error?.campos)
      return Object.entries(error.error.campos)
        .map(([key, value]) => `${key}: ${value}`)
        .join(' · ');
    return error.error?.mensaje ?? fallback;
  }
}
