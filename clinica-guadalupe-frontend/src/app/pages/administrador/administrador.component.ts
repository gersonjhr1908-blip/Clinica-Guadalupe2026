import { Component, EventEmitter, Output } from '@angular/core';
import { DashboardComponent } from '../dashboard/dashboard.component';

@Component({ selector: 'app-administrador', standalone: true, imports: [DashboardComponent], templateUrl: './administrador.component.html' })
export class AdministradorComponent { @Output() openProfile = new EventEmitter<void>(); @Output() logout = new EventEmitter<void>(); }
