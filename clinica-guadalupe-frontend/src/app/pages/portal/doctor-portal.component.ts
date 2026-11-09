import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

@Component({selector:'app-doctor-portal',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./doctor-portal.component.html',styleUrls:['./doctor-portal.component.css']})
export class DoctorPortalComponent {
  @Output() openProfile=new EventEmitter<void>(); @Output() logout=new EventEmitter<void>();
  private readonly http=inject(HttpClient); name=localStorage.getItem('cg_name')??'Médico'; section='inicio'; medicoId:number|null=null; consultorios:any[]=[]; horarios:any[]=[];
  horario={diaSemana:'LUNES',horaInicio:'08:00',horaFin:'17:00',consultorioId:null as number|null}; dias=['LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO'];
  ngOnInit(){this.http.get<any>('/api/medicos/me').subscribe(v=>{this.medicoId=v.id;this.cargarHorarios()});this.http.get<any[]>('/api/consultorios').subscribe(v=>this.consultorios=v)}
  cargarHorarios(){if(this.medicoId)this.http.get<any[]>(`/api/horarios/medico/${this.medicoId}`).subscribe(v=>this.horarios=v)}
  guardarHorario(){if(!this.medicoId||!this.horario.consultorioId)return;this.http.post('/api/horarios/me',{medicoId:this.medicoId,...this.horario}).subscribe({next:()=>{Swal.fire({icon:'success',title:'Horario guardado',text:'El horario ya está disponible para reservas.',confirmButtonColor:'#148b7e'});this.cargarHorarios()},error:e=>Swal.fire({icon:'error',title:'No se pudo guardar',text:e.error?.mensaje??'Revisa cruces de horarios.',confirmButtonColor:'#c85b5b'})})}
}
