import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, interval } from 'rxjs';
import Swal from 'sweetalert2';
import { DownwardSelectComponent } from '../downward-select';
import { Pagination, PaginationComponent } from '../pagination';

type ResultadoCita = {
  estado: 'ATENDIDA' | 'NO_ASISTIO';
  observaciones: string | null;
};

interface CitaMedica {
  id: number;
  codigo: string;
  pacienteId: number;
  paciente: string;
  consultorio: string;
  fechaHoraInicio: string;
  estado: string;
  motivo: string;
  observaciones: string | null;
}

interface PacienteConCitas {
  id: number;
  nombre: string;
  cantidad: number;
  ultimaCita: string;
}

@Component({
  selector: 'app-citas-medico',
  imports: [DatePipe, PaginationComponent, DownwardSelectComponent],
  templateUrl: './citas-medico.component.html',
  styles: `
    .toolbar { display:flex; flex-wrap:wrap; align-items:end; gap:12px; margin:16px 0; }
    .toolbar app-downward-select { width:min(100%, 13rem); }
    .detalle { min-width:10rem; }
    .tabla { overflow:auto; border:1px solid #deece9; border-radius:12px; background:#fff; }
    table { width:100%; border-collapse:collapse; text-align:left; }
    th, td { padding:12px; border-bottom:1px solid #deece9; vertical-align:top; }
    th { background:#f3f9f8; }
    td small { display:block; margin-top:4px; }
    td p { margin:0 0 8px; overflow-wrap:anywhere; white-space:pre-wrap; }
    button { font:inherit; padding:8px 12px; border:1px solid #cfe1df; border-radius:8px;
      background:#fff; color:#164f57; cursor:pointer; }
    button.atender { background:#08776d; color:#fff; border-color:#08776d; }
    button:disabled { opacity:.6; cursor:default; }
    .error { color:#a12626; }
  `,
})
export class CitasMedicoComponent implements OnInit {
  readonly vista = input.required<'mis-citas' | 'mis-pacientes'>();
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly citas = signal<CitaMedica[]>([]);
  private readonly ahora = signal(Date.now());

  readonly paginasCitas = new Pagination();
  readonly paginasPacientes = new Pagination();
  readonly cargando = signal(false);
  readonly guardando = signal<number | null>(null);
  readonly errorCarga = signal('');
  readonly filtro = signal('CONFIRMADA');
  readonly filtros = [
    { value: 'CONFIRMADA', label: 'Por atender' },
    { value: 'ATENDIDA', label: 'Atendidas' },
    { value: 'NO_ASISTIO', label: 'No se presentó' },
  ];
  readonly citasAtencion = computed(() => this.citas().filter(cita => cita.estado === this.filtro()));

  readonly pacientes = computed(() => {
    const porPaciente = new Map<number, PacienteConCitas>();
    for (const cita of this.citas()) {
      const registro = porPaciente.get(cita.pacienteId);
      if (registro) {
        registro.cantidad++;
        if (cita.fechaHoraInicio > registro.ultimaCita) registro.ultimaCita = cita.fechaHoraInicio;
      } else {
        porPaciente.set(cita.pacienteId, {
          id: cita.pacienteId, nombre: cita.paciente, cantidad: 1, ultimaCita: cita.fechaHoraInicio,
        });
      }
    }
    return [...porPaciente.values()].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es') || a.id - b.id);
  });

  ngOnInit(): void {
    this.cargar();
    interval(30_000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.ahora.set(Date.now()));
  }

  cargar(): void {
    if (this.cargando() || this.guardando() !== null) return;
    this.ahora.set(Date.now());
    this.errorCarga.set('');
    this.cargando.set(true);
    this.http.get<CitaMedica[]>('/api/citas/medico/mis-citas').pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: citas => this.citas.set(citas),
      error: error => this.errorCarga.set(this.mensaje(error, 'No se pudieron cargar las citas.')),
    });
  }

  puedeAtender(cita: CitaMedica): boolean {
    return cita.estado === 'CONFIRMADA'
      && Date.parse(cita.fechaHoraInicio + '-05:00') <= this.ahora();
  }

  async atender(cita: CitaMedica): Promise<void> {
    if (!this.puedeAtender(cita) || this.guardando() !== null || this.cargando()) return;
    this.guardando.set(cita.id);
    const resultado = await Swal.fire<ResultadoCita>({
      title: 'Registrar resultado de la cita',
      html: `
        <div style="display:grid;gap:8px;text-align:left">
          <label for="resultado-cita">Resultado</label>
          <select id="resultado-cita" class="swal2-select" style="margin:0;width:100%">
            <option value="">Selecciona un resultado</option>
            <option value="ATENDIDA">Atendida</option>
            <option value="NO_ASISTIO">No se presentó</option>
          </select>
          <label for="observacion-cita">Descripción (obligatoria si fue atendida)</label>
          <textarea id="observacion-cita" class="swal2-textarea" maxlength="1000"
                    style="margin:0;width:100%"></textarea>
        </div>`,
      didOpen: popup => {
        popup.querySelector<HTMLTextAreaElement>('#observacion-cita')!.value = cita.observaciones ?? '';
      },
      showCancelButton: true,
      confirmButtonText: 'Guardar resultado',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#148b7e',
      preConfirm: (): ResultadoCita | false => {
        const popup = Swal.getPopup();
        const estado = popup?.querySelector<HTMLSelectElement>('#resultado-cita')?.value;
        const descripcion = popup?.querySelector<HTMLTextAreaElement>('#observacion-cita')?.value.trim() ?? '';
        if (estado !== 'ATENDIDA' && estado !== 'NO_ASISTIO') {
          Swal.showValidationMessage('Selecciona el resultado de la cita.');
          return false;
        }
        if (estado === 'ATENDIDA' && !descripcion) {
          Swal.showValidationMessage('Escribe la descripción de la atención.');
          return false;
        }
        if (descripcion.length > 1000) {
          Swal.showValidationMessage('Usa un máximo de 1000 caracteres.');
          return false;
        }
        return { estado, observaciones: descripcion || null };
      },
    });
    if (!resultado.isConfirmed || !resultado.value || this.destroyRef.destroyed) {
      this.guardando.set(null);
      return;
    }

    this.http.patch<CitaMedica>(`/api/citas/medico/${cita.id}/estado`, resultado.value).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.guardando.set(null)),
    ).subscribe({
      next: actualizada => {
        this.citas.update(citas => citas.map(item => item.id === actualizada.id ? actualizada : item));
        void Swal.fire({ icon: 'success', title: actualizada.estado === 'NO_ASISTIO' ? 'Inasistencia registrada' : 'Atención registrada', confirmButtonColor: '#148b7e' });
      },
      error: error => {
        void Swal.fire({ icon: 'error', title: 'No se pudo guardar el resultado',
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
