import { Component, EventEmitter, Output } from '@angular/core';

@Component({ selector: 'app-inicio', standalone: true, templateUrl: './inicio.component.html', styleUrls: ['./inicio.component.css', './inicio-extra.css'] })
export class InicioComponent {
  @Output() openLogin = new EventEmitter<void>();
  @Output() openRegister = new EventEmitter<void>();
}

