import { Component, input, output, signal } from '@angular/core';
import { PRIORITY_TEXT_COLORS, PRIORITY_LABELS } from '../../../core/models';

@Component({
  selector: 'app-priority-picker',
  template: `
    <div class="relative">
      <button
        (click)="open.set(!open())"
        class="flex items-center gap-1 px-2 py-1 text-xs rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
        [class]="textColor()">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 7l2.55 2.4A1 1 0 0116 11H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd"/>
        </svg>
        P{{ priority() }}
      </button>

      @if (open()) {
        <div class="absolute left-0 top-full mt-1 w-36 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50 py-1">
          @for (p of priorities; track p) {
            <button
              (click)="select(p)"
              class="w-full flex items-center gap-2 px-3 py-1.5 text-xs hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
              [class]="getTextColor(p)">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-3 h-3" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 7l2.55 2.4A1 1 0 0116 11H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd"/>
              </svg>
              P{{ p }} – {{ labels[p] }}
            </button>
          }
        </div>
      }
    </div>
  `
})
export class PriorityPickerComponent {
  priority = input<1 | 2 | 3 | 4>(1);
  priorityChange = output<1 | 2 | 3 | 4>();

  open = signal(false);
  priorities: (1 | 2 | 3 | 4)[] = [4, 3, 2, 1];
  labels = PRIORITY_LABELS;

  textColor() { return PRIORITY_TEXT_COLORS[this.priority()] ?? 'text-gray-400'; }
  getTextColor(p: number) { return PRIORITY_TEXT_COLORS[p] ?? 'text-gray-400'; }

  select(p: 1 | 2 | 3 | 4): void {
    this.priorityChange.emit(p);
    this.open.set(false);
  }
}
