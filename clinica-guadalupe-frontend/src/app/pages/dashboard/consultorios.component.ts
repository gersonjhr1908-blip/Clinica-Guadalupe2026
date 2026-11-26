import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import Swal from 'sweetalert2';
import { Pagination, PaginationComponent } from '../pagination';

interface Consultorio {
  id: number;
  codigo: string;
  nombre: string;
  piso: number;
  descripcion: string | null;
  activo: boolean;
}

const API = '/api/consultorios';

@Component({
  selector: 'app-consultorios',
  imports: [FormsModule, PaginationComponent],
  templateUrl: './consultorios.component.html',
  styleUrls: ['./dashboard-extra.css', './dashboard-actions.css'],
  styles: `
    .form-card { max-width:640px; }
    .form-actions { display:flex; flex-wrap:wrap; gap:10px; }
    .secondary { padding:10px 14px; border:1px solid #cfe1df; border-radius:8px; color:#164f57; background:#fff; cursor:pointer; }
    button:disabled { opacity:.6; cursor:default; }
    .descripcion { min-width:150px; overflow-wrap:anywhere; white-space:pre-wrap; }
    .error { color:#a12626; }
  `,
})
export class ConsultoriosComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroy = inject(DestroyRef);
  private readonly consultorios = signal<Consultorio[]>([]);
  readonly filas = computed(() => [...this.consultorios()].sort((a, b) =>
    a.codigo.localeCompare(b.codigo, 'es', { numeric: true })));
  readonly paginas = new Pagination();
  readonly cargando = signal(false);
  readonly ocupado = signal(false);
  readonly errorCarga = signal('');
  formularioAbierto = false;
  editandoId: number | null = null;
  datos = { codigo: '', nombre: '', piso: 1 as number | null, descripcion: '' };

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    if (this.cargando() || this.ocupado()) return;
    this.errorCarga.set('');
    this.cargando.set(true);
    this.http.get<Consultorio[]>(API + '/admin/todos').pipe(
      takeUntilDestroyed(this.destroy),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: consultorios => this.consultorios.set(consultorios),
      error: error => this.errorCarga.set(this.mensaje(error, 'No se pudieron cargar los consultorios.')),
    });
  }

  abrir(consultorio?: Consultorio): void {
    this.editandoId = consultorio?.id ?? null;
    this.datos = consultorio
      ? { codigo: consultorio.codigo, nombre: consultorio.nombre, piso: consultorio.piso, descripcion: consultorio.descripcion ?? '' }
      : { codigo: '', nombre: '', piso: 1, descripcion: '' };
    this.formularioAbierto = true;
  }

  guardar(): void {
    const { codigo, nombre, piso, descripcion } = this.datos;
    if (this.ocupado() || !codigo.trim() || !nombre.trim() || piso === null
        || !Number.isInteger(piso) || piso < 1 || piso > 20) return;
    const registro = { codigo: codigo.trim(), nombre: nombre.trim(), piso, descripcion: descripcion.trim() || null };
    const id = this.editandoId;
    const solicitud = id === null
      ? this.http.post<Consultorio>(API, registro)
      : this.http.put<Consultorio>(API + '/' + id, registro);
    this.ocupado.set(true);
    solicitud.pipe(takeUntilDestroyed(this.destroy), finalize(() => this.ocupado.set(false))).subscribe({
      next: guardado => {
        this.actualizarFila(guardado);
        this.formularioAbierto = false;
        if (id === null) this.paginas.page = Math.floor(this.filas().findIndex(fila => fila.id === guardado.id) / this.paginas.pageSize) + 1;
        void this.exito(id === null ? 'Consultorio agregado' : 'Consultorio actualizado');
      },
      error: error => this.error(error, 'No se pudo guardar el consultorio'),
    });
  }

  cambiarEstado(consultorio: Consultorio): void {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.http.patch<Consultorio>(`${API}/${consultorio.id}/estado`, { activo: !consultorio.activo }).pipe(
      takeUntilDestroyed(this.destroy),
      finalize(() => this.ocupado.set(false)),
    ).subscribe({
      next: actualizado => {
        this.actualizarFila(actualizado);
        void this.exito(actualizado.activo ? 'Consultorio activado' : 'Consultorio desactivado');
      },
      error: error => this.error(error, 'No se pudo cambiar el estado'),
    });
  }

  async eliminar(consultorio: Consultorio): Promise<void> {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    const respuesta = await Swal.fire({
      title: '¿Eliminar consultorio?',
      text: consultorio.codigo + ' · ' + consultorio.nombre,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#bd4444',
    });
    if (!respuesta.isConfirmed || this.destroy.destroyed) {
      this.ocupado.set(false);
      return;
    }

    this.http.delete(`${API}/${consultorio.id}`).pipe(
      takeUntilDestroyed(this.destroy),
      finalize(() => this.ocupado.set(false)),
    ).subscribe({
      next: () => {
        this.consultorios.update(filas => filas.filter(fila => fila.id !== consultorio.id));
        this.paginas.page = this.paginas.current(this.filas().length);
        if (this.editandoId === consultorio.id) this.formularioAbierto = false;
        void this.exito('Consultorio eliminado');
      },
      error: error => this.error(error, 'No se pudo eliminar el consultorio'),
    });
  }

  private actualizarFila(consultorio: Consultorio): void {
    this.consultorios.update(filas => [...filas.filter(fila => fila.id !== consultorio.id), consultorio]);
  }

  private exito(title: string) {
    return Swal.fire({ icon: 'success', title, confirmButtonColor: '#148b7e' });
  }

  private error(error: HttpErrorResponse, title: string): void {
    void Swal.fire({ icon: 'error', title, text: this.mensaje(error, 'Inténtalo nuevamente.'), confirmButtonColor: '#148b7e' });
  }

  private mensaje(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 401) return 'Tu sesión venció. Vuelve a iniciar sesión.';
    if (error.status === 403) return 'Se requiere una cuenta de administrador.';
    return error.error?.mensaje || fallback;
  }
}
