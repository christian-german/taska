import { Component, computed, input, output, signal } from '@angular/core';
import { Task } from '../../../core/models';

interface CalendarDay {
  date: Date | null;
  dateStr: string;
  tasks: Task[];
  isToday: boolean;
}

@Component({
  selector: 'app-calendar-view',
  standalone: true,
  template: `
    <div class="p-6 h-full flex flex-col">
      <!-- Month navigation -->
      <div class="flex items-center justify-between mb-4 flex-shrink-0">
        <button (click)="prevMonth()"
          class="p-1.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-600 dark:text-gray-400 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <h2 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ monthLabel() }}</h2>
        <button (click)="nextMonth()"
          class="p-1.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-600 dark:text-gray-400 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>

      <!-- Days of week header -->
      <div class="grid grid-cols-7 mb-1 flex-shrink-0">
        @for (day of weekDays; track day) {
          <div class="text-xs text-center font-medium text-gray-400 dark:text-gray-500 py-1.5">{{ day }}</div>
        }
      </div>

      <!-- Calendar grid -->
      <div class="grid grid-cols-7 border-t border-l border-gray-200 dark:border-gray-700 flex-1">
        @for (day of calendarDays(); track $index) {
          <div class="border-b border-r border-gray-200 dark:border-gray-700 min-h-28 p-1.5"
            [class.bg-gray-50]="!day.date"
            [class.dark:bg-gray-900]="!day.date">
            @if (day.date) {
              <div class="mb-1">
                <span [class]="day.isToday
                    ? 'bg-red-500 text-white w-6 h-6 flex items-center justify-center rounded-full text-xs font-semibold'
                    : 'text-xs text-gray-500 dark:text-gray-400 font-medium px-1'">
                  {{ day.date.getDate() }}
                </span>
              </div>
              <div class="space-y-0.5">
                @for (task of day.tasks.slice(0, 3); track task.id) {
                  <div (click)="taskClicked.emit(task)"
                    class="text-xs truncate rounded px-1.5 py-0.5 cursor-pointer transition-colors"
                    [class]="task.isCompleted
                      ? 'text-gray-400 dark:text-gray-500 line-through bg-gray-100 dark:bg-gray-800'
                      : 'bg-red-50 dark:bg-red-900/20 text-gray-700 dark:text-gray-300 hover:bg-red-100 dark:hover:bg-red-900/30'">
                    {{ task.content }}
                  </div>
                }
                @if (day.tasks.length > 3) {
                  <div class="text-xs text-gray-400 dark:text-gray-500 px-1">+{{ day.tasks.length - 3 }} more</div>
                }
              </div>
            }
          </div>
        }
      </div>
    </div>
  `
})
export class CalendarViewComponent {
  tasks = input.required<Task[]>();
  taskClicked = output<Task>();

  readonly weekDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  currentDate = signal(new Date());

  monthLabel = computed(() =>
    this.currentDate().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
  );

  calendarDays = computed((): CalendarDay[] => {
    const ref = this.currentDate();
    const year = ref.getFullYear();
    const month = ref.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const todayStr = new Date().toISOString().split('T')[0];
    const startOffset = (firstDay.getDay() + 6) % 7;

    const days: CalendarDay[] = [];

    for (let i = 0; i < startOffset; i++) {
      days.push({ date: null, dateStr: '', tasks: [], isToday: false });
    }

    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      days.push({
        date,
        dateStr,
        tasks: this.tasks().filter(t => t.dueDate === dateStr),
        isToday: dateStr === todayStr,
      });
    }

    return days;
  });

  prevMonth(): void {
    const d = this.currentDate();
    this.currentDate.set(new Date(d.getFullYear(), d.getMonth() - 1, 1));
  }

  nextMonth(): void {
    const d = this.currentDate();
    this.currentDate.set(new Date(d.getFullYear(), d.getMonth() + 1, 1));
  }
}
