import { Component, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-downward-select',
  template: `
    <div class="field" (focusout)="closeOnBlur($event)"
         (keydown.escape)="open.set(false); trigger.focus()">
      <span>{{ label() }}</span>
      <button #trigger type="button" class="trigger" [disabled]="disabled()"
              [attr.aria-label]="label() + ': ' + selectedLabel()"
              [attr.aria-expanded]="open()" (click)="open.set(!open())">
        {{ selectedLabel() }} <span aria-hidden="true">▾</span>
      </button>
      @if (open() && !disabled()) {
        <ul>
          @for (option of options(); track $index) {
            <li><button type="button" [attr.aria-pressed]="value() === option.value"
                        (click)="valueChange.emit(option.value); open.set(false); trigger.focus()">
              {{ option.label }}
            </button></li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    :host { display:block; min-width:0; }
    .field { position:relative; display:grid; gap:7px; color:#456d72; font-size:.85rem; font-weight:600; }
    .trigger, ul { background:#fff; border:1px solid #cfe1df; border-radius:9px; }
    button { color:inherit; font:inherit; cursor:pointer; text-align:left; }
    .trigger { display:flex; justify-content:space-between; align-items:center; gap:8px; padding:12px; }
    .trigger:disabled { opacity:.65; cursor:not-allowed; }
    ul { position:absolute; inset:calc(100% + 4px) 0 auto; z-index:3; max-height:min(16rem,45vh);
      overflow-y:auto; overflow-x:hidden; margin:0; padding:4px; list-style:none; box-shadow:0 8px 25px #216d6426; }
    li button { width:100%; box-sizing:border-box; overflow-wrap:anywhere; padding:8px 10px; border:0; border-radius:6px; background:transparent; }
    li button:hover, li button[aria-pressed="true"] { background:#e4f5f2; color:#086e64; }
  `,
})
export class DownwardSelectComponent<T extends string | number | null> {
  readonly label = input.required<string>();
  readonly placeholder = input('Seleccionar');
  readonly options = input.required<readonly { value: T; label: string }[]>();
  readonly value = input.required<T>();
  readonly disabled = input(false);
  readonly valueChange = output<T>();
  readonly open = signal(false);

  selectedLabel(): string {
    return this.options().find(option => option.value === this.value())?.label ?? this.placeholder();
  }

  closeOnBlur(event: FocusEvent): void {
    if (!(event.currentTarget as HTMLElement).contains(event.relatedTarget as Node | null)) {
      this.open.set(false);
    }
  }
}
