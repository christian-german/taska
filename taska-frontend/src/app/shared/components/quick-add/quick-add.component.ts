import { Component, OnInit, ViewChild, inject, output, signal } from '@angular/core';
import { SmartTaskInputComponent, SmartParsed } from '../smart-task-input/smart-task-input.component';
import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { Project } from '../../../core/models';

@Component({
  selector: 'app-quick-add',
  imports: [SmartTaskInputComponent],
  template: `
    <!-- Backdrop -->
    <div class="fixed inset-0 bg-black/40 z-50 flex items-start justify-center pt-32" (click)="close.emit()">
      <!-- Modal -->
      <div class="bg-white dark:bg-gray-900 rounded-xl shadow-2xl w-full max-w-lg border border-gray-200 dark:border-gray-700"
        (click)="$event.stopPropagation()">

        <div class="p-4 pb-3">
          <app-smart-task-input #smartInput
            placeholder="Add a task… type # for project, @ for label, p1-p4 for priority"
            (parsedChange)="parsed.set($event)"
            (enter)="submit()"
            (escape)="close.emit()" />

          <!-- Parsed tokens preview -->
          @if (parsed().projectName || parsed().labels.length || parsed().dueDate || parsed().priority > 1) {
            <div class="flex flex-wrap gap-2 mt-3">
              @if (parsed().projectName) {
                <span class="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full">
                  #{{ parsed().projectName }}
                </span>
              }
              @for (label of parsed().labels; track label) {
                <span class="text-xs px-2 py-0.5 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-full">
                  &#64;{{ label }}
                </span>
              }
              @if (parsed().dueDate) {
                <span class="text-xs px-2 py-0.5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full">
                  {{ formatDate(parsed().dueDate!) }}
                </span>
              }
              @if (parsed().priority > 1) {
                <span class="text-xs px-2 py-0.5 bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 rounded-full">
                  P{{ parsed().priority }}
                </span>
              }
            </div>
          }
        </div>

        <div class="px-4 pb-4 flex items-center gap-2 border-t border-gray-100 dark:border-gray-800 pt-3">
          <div class="flex-1"></div>
          <button (click)="close.emit()"
            class="px-3 py-1.5 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
            Cancel
          </button>
          <button (click)="submit()"
            [disabled]="!parsed().content.trim()"
            class="px-4 py-1.5 text-sm font-medium bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
            Add task
          </button>
        </div>
      </div>
    </div>
  `
})
export class QuickAddComponent implements OnInit {
  close = output<void>();

  @ViewChild('smartInput') smartInput!: SmartTaskInputComponent;

  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);

  projects = signal<Project[]>([]);
  parsed = signal<SmartParsed>({ content: '', priority: 1, labels: [] });

  ngOnInit(): void {
    this.projectService.projects$.subscribe(p => this.projects.set(p));
  }

  formatDate(iso: string): string {
    const d = new Date(iso + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    if (d.getTime() === today.getTime()) return 'Today';
    if (d.getTime() === tomorrow.getTime()) return 'Tomorrow';
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  submit(): void {
    const p = this.parsed();
    if (!p.content.trim()) return;
    this.taskService.createTask({
      content: p.content,
      projectId: p.projectId,
      labels: p.labels,
      priority: p.priority,
      dueDate: p.dueDate,
    }).subscribe(() => this.close.emit());
  }
}
