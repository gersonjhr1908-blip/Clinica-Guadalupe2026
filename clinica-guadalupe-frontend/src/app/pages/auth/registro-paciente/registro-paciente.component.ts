import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, RegistroPaciente } from '../../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-registro-paciente',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registro-paciente.component.html',
  styleUrls: ['./registro-paciente.component.css', './registro-extra.css'],
})
export class RegistroPacienteComponent {
  @Output() registered = new EventEmitter<string>();
  @Output() loginRequested = new EventEmitter<void>();
  private readonly auth = inject(AuthService);
  form: RegistroPaciente = {
    nombres: '',
    apellidos: '',
    dni: '',
    email: '',
    telefono: '',
    password: '',
  };
  loading = false;
  error = '';
  passwordVisible = false;
  submit(): void {
    this.error = '';
    if (!/^\d{8}$/.test(this.form.dni)) {
      this.error = 'El DNI debe tener exactamente 8 números.';
      return;
    }
    if (!/^\d{9}$/.test(this.form.telefono)) {
      this.error = 'El teléfono debe tener exactamente 9 números.';
      return;
    }
    if (this.form.password.length < 8) {
      this.error = 'La contraseña debe tener mínimo 8 caracteres.';
      return;
    }
    this.loading = true;
    this.auth.register(this.form).subscribe({
      next: () => {
        this.loading = false;
        Swal.fire({
          icon: 'success',
          title: 'Cuenta creada exitosamente',
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#148b7e',
        }).then(() => this.registered.emit(this.form.email));
      },
      error: (error) => {
        this.loading = false;
        this.error = this.message(
          error,
          'No se pudo crear la cuenta. Verifica que el correo y DNI no estén registrados.',
        );
      },
    });
  }

  private message(error: any, fallback: string): string {
    let body = error?.error;
    if (typeof body === 'string') {
      try {
        body = JSON.parse(body);
      } catch {
        return body || fallback;
      }
    }
    if (body?.campos)
      return Object.entries(body.campos)
        .map(([key, value]) => `${key}: ${value}`)
        .join(' · ');
    if (body?.mensaje) return body.mensaje;
    if (error?.status === 409) return 'El correo o DNI ya está registrado.';
    return fallback;
  }
}
