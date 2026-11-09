import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-portal-role',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './portal-role.component.html',
  styleUrls: ['./portal-role.component.css'],
})
export class PortalRoleComponent {
  @Output() openProfile = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  section = 'inicio';
  private readonly http = inject(HttpClient);
  especialidades: any[] = []; medicos: any[] = []; consultorios: any[] = [];
  citas: any[] = [];
  reserva = { especialidadId: null as number | null, medicoId: null as number | null, consultorioId: null as number | null, fecha: '', hora: '', motivo: '', metodoPago: '' };
  role = localStorage.getItem('cg_role') ?? '';
  name = localStorage.getItem('cg_name') ?? 'Usuario';
  get patient(): boolean { return this.role === 'PACIENTE'; }
  get doctor(): boolean { return this.role === 'MEDICO'; }
  medicoId: number | null = null; horario = { diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '17:00', consultorioId: null as number | null };
  ngOnInit(): void { if (this.patient) { this.http.get<any[]>('/api/especialidades').subscribe(v => this.especialidades = v); this.http.get<any[]>('/api/medicos').subscribe(v => this.medicos = v); this.http.get<any[]>('/api/consultorios').subscribe(v => this.consultorios = v); } if (this.doctor) { this.http.get<any>('/api/medicos/me').subscribe(v => this.medicoId = v.id); this.http.get<any[]>('/api/consultorios').subscribe(v => this.consultorios = v); } }
  setSection(section: string): void { this.section = section; if (section === 'agendar' && !this.especialidades.length) this.ngOnInit(); if ((section === 'mis-citas' || section === 'historial') && this.patient) this.http.get<any[]>('/api/citas/mis-citas').subscribe(v => this.citas = v); }
  medicosFiltrados(): any[] { return this.reserva.especialidadId ? this.medicos.filter(m => Number(m.especialidadId) === Number(this.reserva.especialidadId)) : this.medicos; }
  reservar(): void { if (!this.reserva.medicoId || !this.reserva.fecha || !this.reserva.hora || !this.reserva.motivo.trim()) { Swal.fire({ icon:'warning', title:'Completa los datos', text:'Selecciona médico, fecha, hora y motivo de consulta.', confirmButtonColor:'#148b7e' }); return; } const payload = { medicoId: Number(this.reserva.medicoId), fecha: this.reserva.fecha, hora: this.reserva.hora, motivo: this.reserva.motivo.trim() }; this.http.post<any>('/api/citas', payload).subscribe({ next: (cita) => Swal.fire({ icon:'success', title:'Cita registrada', html:`Tu código de cita es <strong>${cita.codigo ?? 'Pendiente'}</strong>`, confirmButtonColor:'#148b7e' }).then(() => { this.section = 'mis-citas'; }), error: e => Swal.fire({ icon:'error', title:'No se pudo agendar', text:e.error?.mensaje ?? 'Verifica el médico, fecha y horario.', confirmButtonColor:'#c85b5b' }) }); }
  guardarHorario(): void { if (!this.medicoId || !this.horario.consultorioId) return; this.http.post('/api/horarios/me', { medicoId: this.medicoId, consultorioId: this.horario.consultorioId, diaSemana: this.horario.diaSemana, horaInicio: this.horario.horaInicio, horaFin: this.horario.horaFin }).subscribe({ next: () => Swal.fire({ icon:'success', title:'Horario guardado', text:'Ya podrán reservar citas en ese horario.', confirmButtonColor:'#148b7e' }), error: e => Swal.fire({ icon:'error', title:'No se pudo guardar', text:e.error?.mensaje ?? 'Revisa que no se cruce con otro horario.', confirmButtonColor:'#c85b5b' }) }); }
}
