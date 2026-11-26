import { Component, input, output } from '@angular/core';

export class Pagination {
  page = 1;
  readonly pageSize = 6;

  count(total: number): number {
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  current(total: number): number {
    return Math.min(this.page, this.count(total));
  }

  slice<T>(items: T[]): T[] {
    const start = (this.current(items.length) - 1) * this.pageSize;
    return items.slice(start, start + this.pageSize);
  }
}

@Component({
  selector: 'app-pagination',
  template: `
    @if (pages() > 1) {
      <nav aria-label="Paginación">
        <button type="button" aria-label="Página anterior" [disabled]="page() === 1"
                (click)="pageChange.emit(page() - 1)">‹</button>
        @for (number of visiblePages(); track $index) {
          @if (number === null) {
            <span aria-hidden="true">…</span>
          } @else {
            <button type="button" [attr.aria-label]="'Página ' + number"
                    [attr.aria-current]="page() === number ? 'page' : null"
                    (click)="pageChange.emit(number)">{{ number }}</button>
          }
        }
        <button type="button" aria-label="Página siguiente" [disabled]="page() === pages()"
                (click)="pageChange.emit(page() + 1)">›</button>
      </nav>
    }
  `,
  styles: `
    nav { display:flex; flex-wrap:wrap; justify-content:center; align-items:center; gap:4px; padding:12px; }
    button { min-width:36px; min-height:36px; border:1px solid #cfe1df; border-radius:8px;
      background:#fff; color:#155159; font:inherit; cursor:pointer; }
    button[aria-current="page"] { background:#08776d; color:#fff; border-color:#08776d; }
    button:disabled { opacity:.45; cursor:default; }
    span { padding:0 4px; }
  `,
})
export class PaginationComponent {
  readonly page = input.required<number>();
  readonly pages = input.required<number>();
  readonly pageChange = output<number>();

  visiblePages(): (number | null)[] {
    const last = this.pages();
    const current = this.page();
    const result: (number | null)[] = [1];
    const start = Math.max(2, current - 1);
    const end = Math.min(last - 1, current + 1);
    if (start > 2) result.push(null);
    for (let page = start; page <= end; page++) result.push(page);
    if (end < last - 1) result.push(null);
    if (last > 1) result.push(last);
    return result;
  }
}
