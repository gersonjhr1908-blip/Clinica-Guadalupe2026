import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

import { CitasMedicoComponent } from './citas-medico.component';
import { Pagination, PaginationComponent } from '../pagination';
import { DownwardSelectComponent } from '../downward-select';

@Component({selector:'app-doctor-portal',standalone:true,imports:[CommonModule,FormsModule,PaginationComponent,DownwardSelectComponent,CitasMedicoComponent],templateUrl:'./doctor-portal.component.html',styleUrls:['./doctor-portal.component.css']})
export class DoctorPortalComponent {
  @Output() openProfile=new EventEmitter<void>(); @Output() logout=new EventEmitter<void>();
  private readonly http=inject(HttpClient); name=localStorage.getItem('cg_name')??'Médico'; section='inicio'; medicoId:number|null=null; consultorios:any[]=[]; horarios:any[]=[];
  horario={diaSemana:'LUNES',horaInicio:'08:00',horaFin:'17:00',consultorioId:null as number|null}; dias=['LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO'];
  readonly schedulePages = new Pagination();

  horariosOrdenados(): typeof this.horarios {
    return [...this.horarios].sort((a, b) =>
      this.dias.indexOf(a.diaSemana) - this.dias.indexOf(b.diaSemana)
      || a.horaInicio.localeCompare(b.horaInicio));
  }
  readonly dayOptions = this.dias.map(dia => ({ value: dia, label: dia }));
  readonly hourOptions = this.timeOptions(24);
  readonly minuteOptions = this.timeOptions(60);

  roomOptions(): { value: number | null; label: string }[] {
    return this.consultorios.map(room => ({ value: Number(room.id), label: room.nombre + ' (' + room.codigo + ')' }));
  }

  changeTime(field: 'horaInicio' | 'horaFin', part: 'hour' | 'minute', value: string): void {
    const [hour, minute] = this.horario[field].split(':');
    this.horario[field] = part === 'hour' ? value + ':' + minute : hour + ':' + value;
  }

  private timeOptions(count: number): { value: string; label: string }[] {
    return Array.from({ length: count }, (_, index) => {
      const value = String(index).padStart(2, '0');
      return { value, label: value };
    });
  }

  ngOnInit(){this.http.get<any>('/api/medicos/me').subscribe(v=>{this.medicoId=v.id;this.cargarHorarios()});this.http.get<any[]>('/api/consultorios').subscribe(v=>this.consultorios=v)}
  cargarHorarios(){if(this.medicoId)this.http.get<any[]>(`/api/horarios/medico/${this.medicoId}`).subscribe(v=>this.horarios=v)}
  guardarHorario(){if(!this.medicoId||!this.horario.consultorioId)return;this.http.post('/api/horarios/me',{medicoId:this.medicoId,...this.horario}).subscribe({next:()=>{Swal.fire({icon:'success',title:'Horario guardado',text:'El horario ya está disponible para reservas.',confirmButtonColor:'#148b7e'});this.cargarHorarios()},error:e=>Swal.fire({icon:'error',title:'No se pudo guardar',text:e.error?.mensaje??'Revisa cruces de horarios.',confirmButtonColor:'#c85b5b'})})}
}
