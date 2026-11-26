import { Component, DestroyRef, EventEmitter, Output, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, finalize, forkJoin } from 'rxjs';

import { Pagination, PaginationComponent } from '../pagination';
import { DownwardSelectComponent } from '../downward-select';

interface Especialidad {
  id: number;
  nombre: string;
}

interface Medico {
  id: number;
  nombres: string;
  apellidos: string;
  especialidadId: number;
}

interface HorarioDisponible {
  consultorio: string;
  horaInicio: string;
}

interface Cita {
  id: number;
  codigo: string;
  medico: string;
  especialidad: string;
  consultorio: string;
  fechaHoraInicio: string;
  estado: string;
}

import { CitasRecepcionComponent } from '../citas-recepcion.component';

@Component({
  selector: 'app-portal-role',
  standalone: true,
  imports: [DatePipe, FormsModule, PaginationComponent, DownwardSelectComponent, CitasRecepcionComponent],
  templateUrl: './portal-role.component.html',
  styleUrls: ['./portal-role.component.css'],
})
export class PortalRoleComponent {
  @Output() openProfile = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private consultaHorarios?: Subscription;
  private catalogoCargado = false;

  readonly patient = localStorage.getItem('cg_role') === 'PACIENTE';
  readonly name = localStorage.getItem('cg_name') ?? 'Usuario';

  section = 'inicio';
  readonly appointmentPages = new Pagination();
  reserva = { especialidadId: null as number | null, medicoId: null as number | null, fecha: '', hora: '', motivo: '' };
  readonly especialidades = signal<Especialidad[]>([]);
  readonly medicos = signal<Medico[]>([]);
  readonly horarios = signal<HorarioDisponible[]>([]);
  readonly citas = signal<Cita[]>([]);
  readonly cargandoCatalogo = signal(false);
  readonly cargandoHorarios = signal(false);
  readonly cargandoCitas = signal(false);
  readonly guardando = signal(false);
  readonly errorCatalogo = signal('');
  readonly errorHorarios = signal('');
  readonly errorReserva = signal('');
  readonly errorCitas = signal('');
  readonly codigoReservado = signal('');
  readonly especialidadesAbiertas = signal(false);

  setSection(section: string): void {
    this.section = section;
    this.appointmentPages.page = 1;
    if (!this.patient) return;
    if (section === 'agendar' && !this.catalogoCargado) this.cargarCatalogo();
    if (section === 'mis-citas' || section === 'historial') this.cargarCitas();
  }

  cargarCatalogo(): void {
    if (this.cargandoCatalogo()) return;
    this.errorCatalogo.set('');
    this.cargandoCatalogo.set(true);
    forkJoin({
      especialidades: this.http.get<Especialidad[]>('/api/especialidades'),
      medicos: this.http.get<Medico[]>('/api/medicos'),
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargandoCatalogo.set(false)),
    ).subscribe({
      next: ({ especialidades, medicos }) => {
        this.especialidades.set(especialidades);
        this.medicos.set(medicos);
        this.catalogoCargado = true;
      },
      error: error => this.errorCatalogo.set(this.mensaje(error, 'No se pudieron cargar las especialidades y los médicos.')),
    });
  }

  medicosFiltrados(): Medico[] {
    return this.medicos().filter(medico => medico.especialidadId === this.reserva.especialidadId);
  }

  especialidadSeleccionada(): string {
    return this.especialidades().find(especialidad => especialidad.id === this.reserva.especialidadId)?.nombre
      ?? 'Selecciona una especialidad';
  }

  cerrarEspecialidadesAlSalir(event: FocusEvent): void {
    const campo = event.currentTarget as HTMLElement;
    if (!campo.contains(event.relatedTarget as Node | null)) this.especialidadesAbiertas.set(false);
  }

  seleccionarEspecialidad(id: number): void {
    this.especialidadesAbiertas.set(false);
    if (id === this.reserva.especialidadId) return;
    this.reserva.especialidadId = id;
    this.reserva.medicoId = null;
    this.cargarHorarios();
  }

  cargarHorarios(): void {
    this.consultaHorarios?.unsubscribe();
    this.reserva.hora = '';
    this.horarios.set([]);
    this.errorHorarios.set('');
    this.errorReserva.set('');
    const { medicoId, fecha } = this.reserva;
    if (!medicoId || !fecha) return;

    this.cargandoHorarios.set(true);
    this.consultaHorarios = this.http.get<HorarioDisponible[]>(`/api/disponibilidad/medicos/${medicoId}`, {
      params: { fecha },
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargandoHorarios.set(false)),
    ).subscribe({
      next: horarios => this.horarios.set(horarios),
      error: error => this.errorHorarios.set(this.mensaje(error, 'No se pudieron consultar los horarios disponibles.')),
    });
  }

  opcionesHorario(): { value: string; label: string }[] {
    return this.horarios().map(turno => ({
      value: turno.horaInicio,
      label: turno.horaInicio.slice(0, 5) + ' · ' + turno.consultorio,
    }));
  }

  reservar(): void {
    const { medicoId, fecha, hora, motivo } = this.reserva;
    if (this.guardando() || !medicoId || !fecha || !this.horarios().some(turno => turno.horaInicio === hora) || !motivo.trim()) return;

    this.guardando.set(true);
    this.errorReserva.set('');
    this.http.post<Cita>('/api/citas', { medicoId, fecha, hora, motivo: motivo.trim() }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.guardando.set(false)),
    ).subscribe({
      next: cita => {
        this.reserva = { especialidadId: null, medicoId: null, fecha: '', hora: '', motivo: '' };
        this.horarios.set([]);
        this.codigoReservado.set(cita.codigo);
        this.setSection('mis-citas');
      },
      error: error => {
        if (error.status === 409) this.cargarHorarios();
        this.errorReserva.set(this.mensaje(error, 'No se pudo agendar la cita.'));
      },
    });
  }

  cargarCitas(): void {
    if (this.cargandoCitas()) return;
    this.errorCitas.set('');
    this.cargandoCitas.set(true);
    this.http.get<Cita[]>('/api/citas/mis-citas').pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargandoCitas.set(false)),
    ).subscribe({
      next: citas => this.citas.set(citas),
      error: error => this.errorCitas.set(this.mensaje(error, 'No se pudieron cargar tus citas.')),
    });
  }

  citasVisibles(): Cita[] {
    return this.citas().filter(cita => {
      const vigente = ['PENDIENTE', 'CONFIRMADA'].includes(cita.estado)
        && new Date(cita.fechaHoraInicio).getTime() >= Date.now();
      return this.section === 'historial' ? !vigente : vigente;
    });
  }

  private mensaje(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 401) return 'Tu sesión venció. Vuelve a iniciar sesión.';
    if (error.status === 403) return 'Tu cuenta no tiene permiso para realizar esta operación.';
    if (error.status === 0 || error.status === 502 || error.status === 504) {
      return 'No se pudo conectar con el servidor. Inténtalo de nuevo.';
    }
    const body = error.error;
    if (typeof body?.mensaje === 'string' && body.mensaje) return body.mensaje;
    if (error.status === 400 && body?.campos) {
      const mensajes = Object.values(body.campos).filter(mensaje => typeof mensaje === 'string');
      if (mensajes.length) return mensajes.join('. ');
    }
    return error.status ? fallback + ' (HTTP ' + error.status + ')' : fallback;
  }
}
