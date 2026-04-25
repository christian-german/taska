import { Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { formatDueDate, isOverdue, isToday } from '../../../core/models';

@Component({
  selector: 'app-due-date-picker',
  imports: [FormsModule],
  template: `
    <div class="relative">
      <button
        (click)="open.set(!open())"
        class="flex items-center gap-1 px-2 py-1 text-xs rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
        [class]="dateClass()">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
        </svg>
        {{ label() }}
      </button>

      @if (open()) {
        <div class="absolute left-0 top-full mt-1 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50 p-2">
          <!-- Shortcuts -->
          <div class="flex flex-col gap-0.5 mb-2">
            <button (click)="setDate('today')" class="text-left text-xs px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-green-600">Today</button>
            <button (click)="setDate('tomorrow')" class="text-left text-xs px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-orange-500">Tomorrow</button>
            <button (click)="setDate('next-week')" class="text-left text-xs px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-blue-500">Next week</button>
            @if (dueDate()) {
              <button (click)="clearDate()" class="text-left text-xs px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-red-500">No date</button>
            }
          </div>
          <!-- Manual date input -->
          <input
            type="date"
            [ngModel]="dueDate()"
            (ngModelChange)="onDateChange($event)"
            class="text-xs w-full bg-transparent border border-gray-200 dark:border-gray-600 rounded px-2 py-1 text-gray-900 dark:text-gray-100 outline-none" />
        </div>
      }
    </div>
  `
})
export class DueDatePickerComponent {
  dueDate = input<string | undefined>(undefined);
  dueDateChange = output<string | undefined>();

  open = signal(false);

  label() { return this.dueDate() ? formatDueDate(this.dueDate()) : 'Date'; }
  dateClass() {
    if (!this.dueDate()) return 'text-gray-400 dark:text-gray-500';
    if (isOverdue(this.dueDate())) return 'text-red-500';
    if (isToday(this.dueDate())) return 'text-green-600';
    return 'text-blue-500';
  }

  setDate(when: 'today' | 'tomorrow' | 'next-week'): void {
    const d = new Date();
    if (when === 'tomorrow') d.setDate(d.getDate() + 1);
    if (when === 'next-week') d.setDate(d.getDate() + 7);
    this.dueDateChange.emit(d.toISOString().split('T')[0]);
    this.open.set(false);
  }

  onDateChange(value: string): void {
    this.dueDateChange.emit(value || undefined);
    this.open.set(false);
  }

  clearDate(): void {
    this.dueDateChange.emit(undefined);
    this.open.set(false);
  }
}
