import { Component, EventEmitter, Output, inject, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

interface DashboardSummary {
  totalUsuarios: number;
  totalPacientes: number;
  totalMedicos: number;
  totalRecepcionistas: number;
  especialidadesActivas: number;
  consultoriosActivos: number;
  totalCitas: number;
  citasHoy: number;
  citasPendientes: number;
  citasConfirmadas: number;
}
interface Especialidad {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
}
interface Usuario {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string;
  email: string;
  telefono: string;
  rol: string;
  activo: boolean;
}
interface Medico {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string;
  email: string;
  telefono: string;
  especialidadId: number;
  especialidad: string;
  cmp: string;
  precioConsulta: number;
  duracionConsultaMinutos: number;
  biografia?: string;
  activo: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard-admin.component.html',
  styleUrls: [
    './dashboard.component.css',
    './dashboard-extra.css',
    './dashboard-modal.css',
    './dashboard-actions.css',
    './dashboard-medical.css',
    './dashboard-roles.css',
  ],
})
export class DashboardComponent {
  @Output() openProfile = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @HostBinding('class.role-recepcionista') get isReceptionistLayout(): boolean { return this.role === 'RECEPCIONISTA'; }
  @HostBinding('class.role-paciente') get isPatientLayout(): boolean { return this.role === 'PACIENTE'; }
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  summary: DashboardSummary | null = null;
  activeSection = 'inicio';
  especialidades: Especialidad[] = [];
  usuarios: Usuario[] = [];
  medicos: Medico[] = [];
  nuevaEspecialidad = { nombre: '', descripcion: '' };
  showUserForm = false;
  showMedicoForm = false;
  editingUserId: number | null = null;
  editingMedicoId: number | null = null;
  userSearch = '';
  medicoSearch = '';
  userRoleFilter = 'TODOS';
  nuevoUsuario = {
    nombres: '',
    apellidos: '',
    dni: '',
    email: '',
    telefono: '',
    password: '',
    rol: 'RECEPCIONISTA',
  };
  nuevoMedico = {
    nombres: '',
    apellidos: '',
    dni: '',
    email: '',
    telefono: '',
    password: '',
    especialidadId: null as number | null,
    cmp: '',
    precioConsulta: null as number | null,
    duracionConsultaMinutos: null as number | null,
    biografia: '',
  };
  ngOnInit(): void {
    if (this.role === 'ADMINISTRADOR') {
      this.http
        .get<DashboardSummary>('/api/dashboard/resumen', { headers: this.headers })
        .subscribe({ next: (value) => (this.summary = value) });
    }
  }
  private get headers() {
    return { Authorization: `Bearer ${this.auth.token()}` };
  }
  open(section: string): void {
    this.activeSection = section;
    if (section === 'especialidades') this.loadEspecialidades();
    if (['usuarios', 'recepcionistas', 'pacientes'].includes(section)) this.loadUsuarios();
    if (section === 'medicos') {
      this.loadMedicos();
      this.loadEspecialidades();
    }
  }
  loadEspecialidades(): void {
    this.http
      .get<Especialidad[]>('/api/especialidades/todas', { headers: this.headers })
      .subscribe({ next: (value) => (this.especialidades = value) });
  }
  loadUsuarios(): void {
    this.http
      .get<Usuario[]>('/api/usuarios', { headers: this.headers })
      .subscribe({ next: (value) => (this.usuarios = value) });
  }
  loadMedicos(): void {
    this.http
      .get<Medico[]>('/api/medicos/admin/todos', { headers: this.headers })
      .subscribe({ next: (value) => (this.medicos = value) });
  }
  addUsuario(): void {
    const wasEditing = !!this.editingUserId;
    const request = this.editingUserId
      ? this.http.put<Usuario>(
          `/api/usuarios/${this.editingUserId}`,
          { ...this.nuevoUsuario, rol: this.nuevoUsuario.rol },
          { headers: this.headers },
        )
      : this.http.post<Usuario>('/api/usuarios', this.nuevoUsuario, { headers: this.headers });
    request.subscribe({
      next: (value) => {
        const index = this.usuarios.findIndex((item) => item.id === value.id);
        if (index >= 0) this.usuarios[index] = value;
        else this.usuarios = [value, ...this.usuarios];
        this.closeUserModal();
        Swal.fire({
          icon: 'success',
          title: wasEditing ? 'Usuario actualizado' : 'Usuario registrado',
          text: 'La operación se realizó correctamente.',
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#148b7e',
        });
      },
      error: (error) =>
        Swal.fire({
          icon: 'error',
          title: 'No se pudo guardar',
          text: this.errorMessage(error),
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#c85b5b',
        }),
    });
  }
  addMedico(): void {
    if (!this.nuevoMedico.especialidadId) {
      Swal.fire({
        icon: 'warning',
        title: 'Falta la especialidad',
        text: 'Selecciona una especialidad para continuar.',
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#148b7e',
      });
      return;
    }
    const wasEditing = !!this.editingMedicoId;
    const request = this.editingMedicoId
      ? this.http.put<Medico>(
          `/api/medicos/${this.editingMedicoId}`,
          {
            especialidadId: this.nuevoMedico.especialidadId,
            cmp: this.nuevoMedico.cmp,
            precioConsulta: this.nuevoMedico.precioConsulta,
            duracionConsultaMinutos: this.nuevoMedico.duracionConsultaMinutos,
            biografia: this.nuevoMedico.biografia,
          },
          { headers: this.headers },
        )
      : this.http.post<Medico>('/api/medicos', this.nuevoMedico, { headers: this.headers });
    request.subscribe({
      next: (value) => {
        const index = this.medicos.findIndex((item) => item.id === value.id);
        if (index >= 0) this.medicos[index] = value;
        else this.medicos = [value, ...this.medicos];
        this.showMedicoForm = false;
        this.editingMedicoId = null;
        Swal.fire({
          icon: 'success',
          title: wasEditing ? 'Médico actualizado' : 'Médico registrado',
          text: 'La operación se realizó correctamente.',
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#148b7e',
        });
      },
      error: (error) =>
        Swal.fire({
          icon: 'error',
          title: 'No se pudo guardar',
          text: this.errorMessage(error),
          confirmButtonText: 'Aceptar',
          confirmButtonColor: '#c85b5b',
        }),
    });
  }
  private errorMessage(error: any): string {
    const body =
      typeof error?.error === 'string'
        ? (() => {
            try {
              return JSON.parse(error.error);
            } catch {
              return null;
            }
          })()
        : error?.error;
    return (
      body?.mensaje ||
      (body?.campos
        ? Object.values(body.campos).join(' · ')
        : 'Verifica los datos y que no estén duplicados.')
    );
  }
  toggleUsuario(item: Usuario): void {
    const action = item.activo ? 'Desactivar cuenta' : 'Habilitar cuenta';
    Swal.fire({
      icon: 'warning',
      title: `¿${action}?`,
      text: item.activo
        ? 'El usuario no podrá iniciar sesión.'
        : 'El usuario podrá volver a iniciar sesión.',
      showCancelButton: true,
      confirmButtonText: 'Confirmar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: item.activo ? '#c85b5b' : '#168c7f',
    }).then((result) => {
      if (result.isConfirmed)
        this.http
          .patch<Usuario>(
            `/api/usuarios/${item.id}/estado`,
            { activo: !item.activo },
            { headers: this.headers },
          )
          .subscribe({
            next: (value) => {
              item.activo = value.activo;
              Swal.fire({
                icon: 'success',
                title: value.activo ? 'Cuenta habilitada' : 'Cuenta desactivada',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#168c7f',
              });
            },
          });
    });
  }
  toggleMedico(item: Medico): void {
    const action = item.activo ? 'Desactivar cuenta' : 'Habilitar cuenta';
    Swal.fire({
      icon: 'warning',
      title: `¿${action}?`,
      text: item.activo
        ? 'El médico no podrá iniciar sesión.'
        : 'El médico podrá volver a iniciar sesión.',
      showCancelButton: true,
      confirmButtonText: 'Confirmar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: item.activo ? '#c85b5b' : '#168c7f',
    }).then((result) => {
      if (result.isConfirmed)
        this.http
          .patch<Medico>(
            `/api/medicos/${item.id}/estado`,
            { activo: !item.activo },
            { headers: this.headers },
          )
          .subscribe({
            next: (value) => {
              item.activo = value.activo;
              Swal.fire({
                icon: 'success',
                title: value.activo ? 'Cuenta habilitada' : 'Cuenta desactivada',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#168c7f',
              });
            },
          });
    });
  }
  editUsuario(item: Usuario): void {
    this.nuevoUsuario = {
      nombres: item.nombres,
      apellidos: item.apellidos,
      dni: item.dni,
      email: item.email,
      telefono: item.telefono,
      password: '',
      rol: item.rol,
    };
    this.editingUserId = item.id;
    this.showUserForm = true;
  }
  removeUsuario(item: Usuario): void {
    Swal.fire({
      icon: 'error',
      title: '¿Eliminar definitivamente?',
      text: 'Esta acción eliminará el registro de la base de datos.',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#c85b5b',
    }).then((result) => {
      if (result.isConfirmed)
        this.http.delete(`/api/usuarios/${item.id}`, { headers: this.headers }).subscribe({
          next: () => {
            this.usuarios = this.usuarios.filter((value) => value.id !== item.id);
            Swal.fire({
              icon: 'success',
              title: 'Registro eliminado',
              confirmButtonText: 'Aceptar',
              confirmButtonColor: '#168c7f',
            });
          },
          error: (error) =>
            Swal.fire({
              icon: 'error',
              title: 'No se pudo eliminar',
              text: this.errorMessage(error),
              confirmButtonText: 'Aceptar',
              confirmButtonColor: '#c85b5b',
            }),
        });
    });
  }
  closeUserModal(): void {
    this.showUserForm = false;
    this.editingUserId = null;
    this.nuevoUsuario = {
      nombres: '',
      apellidos: '',
      dni: '',
      email: '',
      telefono: '',
      password: '',
      rol: 'RECEPCIONISTA',
    };
  }
  openUserModal(section: string): void {
    this.editingUserId = null;
    this.nuevoUsuario.rol = section === 'pacientes' ? 'PACIENTE' : 'RECEPCIONISTA';
    this.showUserForm = true;
  }
  resetMedicoForm(): void {
    this.editingMedicoId = null;
    this.nuevoMedico = {
      nombres: '',
      apellidos: '',
      dni: '',
      email: '',
      telefono: '',
      password: '',
      especialidadId: null,
      cmp: '',
      precioConsulta: null,
      duracionConsultaMinutos: null,
      biografia: '',
    };
  }
  editMedico(item: Medico): void {
    this.editingMedicoId = item.id;
    this.nuevoMedico.nombres = item.nombres;
    this.nuevoMedico.apellidos = item.apellidos;
    this.nuevoMedico.dni = item.dni;
    this.nuevoMedico.email = item.email;
    this.nuevoMedico.telefono = item.telefono;
    this.nuevoMedico.especialidadId = item.especialidadId;
    this.nuevoMedico.cmp = item.cmp;
    this.nuevoMedico.precioConsulta = item.precioConsulta;
    this.nuevoMedico.duracionConsultaMinutos = item.duracionConsultaMinutos;
    this.nuevoMedico.biografia = item.biografia ?? '';
    this.showMedicoForm = true;
  }
  removeMedico(item: Medico): void {
    Swal.fire({
      icon: 'error',
      title: '¿Eliminar definitivamente?',
      text: 'Esta acción eliminará al médico y su usuario de la base de datos.',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#c85b5b',
    }).then((result) => {
      if (result.isConfirmed)
        this.http.delete(`/api/medicos/${item.id}`, { headers: this.headers }).subscribe({
          next: () => {
            this.medicos = this.medicos.filter((value) => value.id !== item.id);
            Swal.fire({
              icon: 'success',
              title: 'Médico eliminado',
              confirmButtonText: 'Aceptar',
              confirmButtonColor: '#168c7f',
            });
          },
          error: (error) =>
            Swal.fire({
              icon: 'error',
              title: 'No se pudo eliminar',
              text: this.errorMessage(error),
              confirmButtonText: 'Aceptar',
              confirmButtonColor: '#c85b5b',
            }),
        });
    });
  }
  addEspecialidad(): void {
    if (!this.nuevaEspecialidad.nombre.trim()) return;
    this.http
      .post<Especialidad>('/api/especialidades', this.nuevaEspecialidad, { headers: this.headers })
      .subscribe({
        next: (value) => {
          this.especialidades = [value, ...this.especialidades];
          this.nuevaEspecialidad = { nombre: '', descripcion: '' };
          Swal.fire({
            icon: 'success',
            title: 'Especialidad agregada',
            text: 'Ya estará disponible al registrar un médico.',
            confirmButtonText: 'Aceptar',
            confirmButtonColor: '#148b7e',
          });
        },
      });
  }
  get role(): string {
    return localStorage.getItem('cg_role') ?? 'PACIENTE';
  }
  get name(): string {
    return localStorage.getItem('cg_name') ?? 'Usuario';
  }
  get roleLabel(): string {
    return this.role.replace('_', ' ');
  }
  get filteredUsuarios(): Usuario[] {
    const query = this.normalize(this.userSearch);
    const selectedRole = this.normalize(this.userRoleFilter);
    return this.usuarios.filter((item) => {
      const itemRole = this.normalize(String(item.rol));
      const fullText = this.normalize(`${item.nombres} ${item.apellidos} ${item.email}`);
      return (
        (selectedRole === 'TODOS' || itemRole === selectedRole) &&
        (!query || fullText.includes(query))
      );
    });
  }
  get filteredMedicos(): Medico[] {
    const query = this.normalize(this.medicoSearch);
    return this.medicos.filter(
      (item) =>
        !query ||
        this.normalize(
          `${item.nombres} ${item.apellidos} ${item.email} ${item.especialidad}`,
        ).includes(query),
    );
  }
  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toUpperCase();
  }
  onRoleFilterChange(): void {
    this.usuarios = [...this.usuarios];
  }
}
