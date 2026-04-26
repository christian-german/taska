import { Component, ViewChild, input, output, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PriorityPickerComponent } from '../priority-picker/priority-picker.component';
import { DueDatePickerComponent } from '../due-date-picker/due-date-picker.component';
import { LabelPickerComponent } from '../label-picker/label-picker.component';
import { SmartTaskInputComponent, SmartParsed } from '../smart-task-input/smart-task-input.component';
import { TaskService } from '../../../core/services/task.service';
import { Task, getColor } from '../../../core/models';
import { toSignal } from '@angular/core/rxjs-interop';
import { LabelService } from '../../../core/services/label.service';

@Component({
  selector: 'app-add-task-form',
  imports: [FormsModule, PriorityPickerComponent, DueDatePickerComponent, LabelPickerComponent, SmartTaskInputComponent],
  template: `
    @if (!visible()) {
      <button
        (click)="open()"
        class="flex items-center gap-2 w-full px-3 py-1.5 text-sm text-gray-400 hover:text-red-500 group transition-colors mt-1">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 group-hover:text-red-500 transition-colors" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        Add task
      </button>
    }

    @if (visible()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        (click)="cancel()">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-lg p-5"
          (click)="$event.stopPropagation()">

          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200 mb-3">Add task</h3>

          <app-smart-task-input #smartInput
            placeholder="Task name — type # for project, @ for label, p1-p4 for priority, tod/tom/DD/MM for date"
            (parsedChange)="onParsed($event)"
            (enter)="submit()"
            (escape)="cancel()" />

          @if (parsed().labels.length > 0) {
            <div class="flex flex-wrap gap-1 mt-2">
              @for (name of parsed().labels; track name) {
                <span class="flex items-center gap-1 px-2 py-0.5 rounded-full text-xs text-white"
                  [style.background-color]="getLabelColor(name)">
                  {{ name }}
                </span>
              }
            </div>
          }

          <div class="flex items-center gap-2 flex-wrap mt-3">
            <app-due-date-picker [dueDate]="dueDate()" (dueDateChange)="dueDate.set($event)" />
            <app-priority-picker [priority]="priority()" (priorityChange)="priority.set($event)" />
            <app-label-picker [selected]="selectedLabels()" (labelsChange)="selectedLabels.set($event)" />
          </div>

          <div class="flex items-center gap-2 mt-4 pt-3 border-t border-gray-100 dark:border-gray-700">
            <button (click)="submit()"
              [disabled]="!parsed().content.trim()"
              class="px-3 py-1.5 text-xs font-medium bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
              Add task
            </button>
            <button (click)="cancel()"
              class="px-3 py-1.5 text-xs text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
              Cancel
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class AddTaskFormComponent {
  projectId = input<string>();
  sectionId = input<string>();
  parentId = input<string>();
  initialDueDate = input<string>();

  taskCreated = output<Task>();

  @ViewChild('smartInput') smartInput!: SmartTaskInputComponent;

  private taskService = inject(TaskService);
  private labelService = inject(LabelService);
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] });

  visible = signal(false);

  parsed = signal<SmartParsed>({ content: '', priority: 1, labels: [] });
  priority = signal<1 | 2 | 3 | 4>(1);
  dueDate = signal<string | undefined>(undefined);
  selectedLabels = signal<string[]>([]);

  open(): void {
    this.dueDate.set(this.initialDueDate());
    this.visible.set(true);
  }

  onParsed(p: SmartParsed): void {
    this.parsed.set(p);
    this.priority.set(p.priority);
    if (p.dueDate) this.dueDate.set(p.dueDate);
    if (p.labels.length) this.selectedLabels.set(p.labels);
  }

  getLabelColor(name: string): string {
    const label = this.allLabels().find(l => l.name === name);
    return getColor(label?.color ?? 'charcoal');
  }

  submit(): void {
    const p = this.parsed();
    if (!p.content.trim()) return;
    this.taskService.createTask({
      content: p.content,
      projectId: p.projectId ?? this.projectId(),
      sectionId: this.sectionId(),
      parentId: this.parentId(),
      priority: this.priority(),
      dueDate: this.dueDate(),
      labels: this.selectedLabels().length ? this.selectedLabels() : p.labels,
    }).subscribe(task => {
      this.taskCreated.emit(task);
      this.reset();
    });
  }

  cancel(): void {
    this.reset();
  }

  private reset(): void {
    this.parsed.set({ content: '', priority: 1, labels: [] });
    this.priority.set(1);
    this.dueDate.set(undefined);
    this.selectedLabels.set([]);
    this.visible.set(false);
    this.smartInput?.reset();
  }
}
