import { Component, HostListener, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { LabelService } from '../../../core/services/label.service';
import { Label, getColor } from '../../../core/models';

@Component({
  selector: 'app-label-picker',
  standalone: true,
  host: { '(click)': '$event.stopPropagation()' },
  template: `
    <div class="relative">
      <button (click)="toggle($event)"
        class="flex items-center gap-1 px-2 py-1 text-xs rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
        [class]="selected().length > 0 ? 'text-purple-500' : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300'">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
        </svg>
        @if (selected().length > 0) {
          Labels ({{ selected().length }})
        } @else {
          Labels
        }
      </button>

      @if (open()) {
        <div class="absolute left-0 top-full mt-1 w-52 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 z-50 py-1 max-h-60 overflow-y-auto">
          @if (labels().length === 0) {
            <p class="text-xs text-gray-400 px-3 py-3 text-center">No labels yet</p>
          }
          @for (label of labels(); track label.id) {
            <button (click)="toggleLabel(label.name)"
              class="flex items-center gap-2.5 w-full px-3 py-1.5 text-sm text-left hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <span class="w-3 h-3 rounded-full flex-shrink-0" [style.background-color]="getColor(label.color)"></span>
              <span class="flex-1 truncate text-gray-700 dark:text-gray-300">{{ label.name }}</span>
              @if (isSelected(label.name)) {
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5 text-red-500 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
              }
            </button>
          }
        </div>
      }
    </div>
  `
})
export class LabelPickerComponent {
  selected = input.required<string[]>();
  labelsChange = output<string[]>();

  private labelService = inject(LabelService);
  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  open = signal(false);
  getColor = getColor;

  @HostListener('document:click')
  onDocumentClick() { this.open.set(false); }

  toggle(e: Event): void {
    e.stopPropagation();
    this.open.set(!this.open());
  }

  isSelected(name: string): boolean {
    return this.selected().includes(name);
  }

  toggleLabel(name: string): void {
    const current = this.selected();
    const next = current.includes(name)
      ? current.filter(l => l !== name)
      : [...current, name];
    this.labelsChange.emit(next);
  }
}
