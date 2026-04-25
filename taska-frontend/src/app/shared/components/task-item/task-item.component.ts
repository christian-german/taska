import { Component, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Task, Label, PRIORITY_BORDER_COLORS, PRIORITY_TEXT_COLORS, formatDueDate, isOverdue, isToday, getColor } from '../../../core/models';
import { LabelService } from '../../../core/services/label.service';

@Component({
  selector: 'app-task-item',
  template: `
    <div
      [class]="completing() ? 'task-complete-animation' : ''"
      class="group flex items-start gap-3 px-3 py-2 hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg cursor-pointer transition-colors"
      (click)="taskClicked.emit(task())">

      <!-- Priority checkbox -->
      <button
        class="flex-shrink-0 mt-0.5 w-4 h-4 rounded-full border-2 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 transition-all"
        [class]="priorityBorder()"
        (click)="onComplete($event)"
        [title]="'Mark complete'">
        @if (task().isCompleted) {
          <svg class="w-2.5 h-2.5" [class]="priorityText()" viewBox="0 0 12 12" fill="currentColor">
            <path d="M10.28 1.28L3.989 7.575 1.695 5.28A1 1 0 00.28 6.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 1.28z"/>
          </svg>
        }
      </button>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <p class="text-sm text-gray-900 dark:text-gray-100 leading-snug"
          [class.line-through]="task().isCompleted"
          [class.text-gray-400]="task().isCompleted">
          {{ task().content }}
        </p>

        <!-- Meta row -->
        <div class="flex flex-wrap items-center gap-2 mt-0.5">
          @if (task().dueDate) {
            <span class="text-xs flex items-center gap-0.5"
              [class.text-red-500]="dueDateOverdue()"
              [class.text-green-600]="dueDateToday() && !dueDateOverdue()"
              [class.text-gray-400]="!dueDateOverdue() && !dueDateToday()">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-3 h-3" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
              </svg>
              {{ formattedDate() }}
            </span>
          }
          @for (labelName of task().labels; track labelName) {
            <span class="flex items-center gap-1 px-1.5 py-0.5 rounded-full text-xs text-white"
              [style.background-color]="getLabelColor(labelName)">
              {{ labelName }}
            </span>
          }
        </div>
      </div>
    </div>
  `
})
export class TaskItemComponent {
  task = input.required<Task>();
  complete = output<Task>();
  taskClicked = output<Task>();

  completing = signal(false);

  private labelService = inject(LabelService);
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  priorityBorder() { return PRIORITY_BORDER_COLORS[this.task().priority] ?? PRIORITY_BORDER_COLORS[1]; }
  priorityText() { return PRIORITY_TEXT_COLORS[this.task().priority] ?? PRIORITY_TEXT_COLORS[1]; }
  formattedDate() { return formatDueDate(this.task().dueDate); }
  dueDateOverdue() { return isOverdue(this.task().dueDate); }
  dueDateToday() { return isToday(this.task().dueDate); }

  getLabelColor(name: string): string {
    const label = this.allLabels().find(l => l.name === name);
    return getColor(label?.color ?? 'charcoal');
  }

  onComplete(event: MouseEvent): void {
    event.stopPropagation();
    this.completing.set(true);
    setTimeout(() => {
      this.complete.emit(this.task());
      this.completing.set(false);
    }, 250);
  }
}
