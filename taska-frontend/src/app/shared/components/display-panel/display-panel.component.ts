import { Component, HostListener, input, output } from '@angular/core';
import { ViewStyle } from '../../../core/models';

@Component({
  selector: 'app-display-panel',
  standalone: true,
  host: { '(click)': '$event.stopPropagation()' },
  template: `
    @if (isOpen()) {
      <div class="absolute right-0 top-full mt-2 w-64 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 z-50 p-4">
        <p class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-3">Layout</p>

        <div class="flex gap-1.5 mb-4">
          <button (click)="viewStyleChange.emit('LIST')"
            [class]="viewStyle() === 'LIST' ? 'ring-2 ring-red-500 bg-gray-100 dark:bg-gray-700' : 'hover:bg-gray-50 dark:hover:bg-gray-700'"
            class="flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-lg text-xs transition-all text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"/>
            </svg>
            List
          </button>

          <button (click)="viewStyleChange.emit('BOARD')"
            [class]="viewStyle() === 'BOARD' ? 'ring-2 ring-red-500 bg-gray-100 dark:bg-gray-700' : 'hover:bg-gray-50 dark:hover:bg-gray-700'"
            class="flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-lg text-xs transition-all text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2"/>
            </svg>
            Board
          </button>

          <button (click)="viewStyleChange.emit('CALENDAR')"
            [class]="viewStyle() === 'CALENDAR' ? 'ring-2 ring-red-500 bg-gray-100 dark:bg-gray-700' : 'hover:bg-gray-50 dark:hover:bg-gray-700'"
            class="flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-lg text-xs transition-all text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
            Calendar
          </button>
        </div>

        <div class="border-t border-gray-100 dark:border-gray-700 pt-3">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-700 dark:text-gray-300">Show completed</span>
            <button (click)="showCompletedChange.emit(!showCompleted())"
              [class]="showCompleted() ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
              class="relative w-10 h-5 rounded-full transition-colors flex-shrink-0">
              <span [class]="showCompleted() ? 'translate-x-5' : 'translate-x-0.5'"
                class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class DisplayPanelComponent {
  isOpen = input<boolean>(false);
  viewStyle = input<ViewStyle>('LIST');
  showCompleted = input<boolean>(false);
  viewStyleChange = output<ViewStyle>();
  showCompletedChange = output<boolean>();
  closed = output<void>();

  @HostListener('document:click')
  onDocumentClick() {
    if (this.isOpen()) this.closed.emit();
  }
}
