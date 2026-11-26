import { DatePipe, formatDate } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { DownwardSelectComponent } from './downward-select';
import { Pagination, PaginationComponent } from './pagination';

type Vista = 'pacientes' | 'agenda' | 'citas';

interface CitaRecepcion {
  id: number;
  codigo: string;
  paciente: string;
  medico: string;
  especialidad: string;
  consultorio: string;
  fechaHoraInicio: string;
  estado: string;
  observaciones: string | null;
}

@Component({
  selector: 'app-citas-recepcion',
  imports: [DatePipe, PaginationComponent, DownwardSelectComponent],
  templateUrl: './citas-recepcion.component.html',
  styles: `
    :host { display:block; }
    .filtros { display:flex; flex-wrap:wrap; gap:12px; align-items:end; margin:16px 0; }
    .filtros app-downward-select { width:min(100%, 12rem); }
    input, select, button { font:inherit; padding:8px 12px; border:1px solid #cfe1df; border-radius:8px; background:#fff; color:#164f57; }
    button { cursor:pointer; }
    button:disabled { opacity:.6; cursor:default; }
    .confirmar { background:#08776d; color:#fff; border-color:#08776d; }
    .tabla { overflow:auto; background:#fff; border:1px solid #deece9; border-radius:12px; }
    table { width:100%; border-collapse:collapse; text-align:left; }
    th, td { padding:12px; border-bottom:1px solid #deece9; }
    th { background:#f3f9f8; }
    td small { display:block; margin-top:4px; }
    .error { color:#a12626; }
  `,
})
export class CitasRecepcionComponent {
  readonly vista = input.required<Vista>();
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  private readonly citas = signal<CitaRecepcion[]>([]);

  readonly paginas = new Pagination();
  readonly cargando = signal(false);
  readonly confirmando = signal<number | null>(null);
  readonly errorCarga = signal('');
  readonly estado = signal('TODAS');
  readonly estados = [
    { value: 'TODAS', label: 'Todas' },
    { value: 'PENDIENTE', label: 'Pendientes' },
    { value: 'CONFIRMADA', label: 'Confirmadas' },
    { value: 'ATENDIDA', label: 'Atendidas' },
    { value: 'CANCELADA', label: 'Canceladas' },
    { value: 'NO_ASISTIO', label: 'No se presentó' },
  ];
  readonly fecha = signal(formatDate(new Date(), 'yyyy-MM-dd', 'en-US', '-0500'));
  readonly titulos = { pacientes: 'Pacientes con cita', agenda: 'Agenda del día', citas: 'Gestión de citas' };
  readonly visibles = computed(() => {
    const vista = this.vista();
    if (vista === 'pacientes') {
      return this.citas().filter(cita => cita.estado === 'PENDIENTE');
    }
    if (vista === 'agenda') {
      return this.citas().filter(cita => cita.estado === 'CONFIRMADA'
        && cita.fechaHoraInicio.slice(0, 10) === this.fecha())
        .sort((a, b) => a.fechaHoraInicio.localeCompare(b.fechaHoraInicio));
    }
    if (vista === 'citas' && this.estado() !== 'TODAS') {
      return this.citas().filter(cita => cita.estado === this.estado());
    }
    return this.citas();
  });

  constructor() {
    effect(() => {
      this.vista();
      this.estado.set('TODAS');
      this.cargar();
    });
  }

  cargar(): void {
    this.consulta?.unsubscribe();
    this.paginas.page = 1;
    this.fecha.set(formatDate(new Date(), 'yyyy-MM-dd', 'en-US', '-0500'));
    this.errorCarga.set('');
    this.cargando.set(true);
    this.consulta = this.http.get<CitaRecepcion[]>('/api/citas/admin/todas').pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: citas => this.citas.set(citas),
      error: error => this.errorCarga.set(this.mensaje(error, 'No se pudieron cargar las citas.')),
    });
  }

  async confirmar(cita: CitaRecepcion): Promise<void> {
    if (this.vista() !== 'pacientes' || cita.estado !== 'PENDIENTE'
        || this.confirmando() !== null || this.cargando()) return;
    this.confirmando.set(cita.id);
    const resultado = await Swal.fire({
      icon: 'question',
      title: '¿Confirmar cita?',
      text: 'Cita ' + cita.codigo + ' · ' + cita.paciente,
      showCancelButton: true,
      confirmButtonText: 'Confirmar cita',
      cancelButtonText: 'Volver',
      confirmButtonColor: '#148b7e',
    });
    if (!resultado.isConfirmed || this.destroyRef.destroyed || this.vista() !== 'pacientes') {
      this.confirmando.set(null);
      return;
    }
    this.http.patch<CitaRecepcion>(`/api/citas/${cita.id}/estado`, {
      estado: 'CONFIRMADA',
      observaciones: cita.observaciones,
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.confirmando.set(null)),
    ).subscribe({
      next: actualizada => {
        this.citas.update(citas => citas.map(item => item.id === actualizada.id ? actualizada : item));
        void Swal.fire({ icon: 'success', title: 'Cita confirmada',
          text: 'Código: ' + actualizada.codigo, confirmButtonColor: '#148b7e' });
      },
      error: error => {
        void Swal.fire({ icon: 'error', title: 'No se pudo confirmar la cita',
          text: this.mensaje(error, 'Inténtalo nuevamente.'), confirmButtonColor: '#148b7e' });
      },
    });
  }

  private mensaje(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 401) return 'Tu sesión venció. Vuelve a iniciar sesión.';
    if (error.status === 403) return 'Tu cuenta no tiene permiso para esta operación.';
    return error.error?.mensaje || fallback;
  }
}
