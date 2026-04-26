import { Component, effect, inject, input, signal, computed } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { FilterService } from '../../core/services/filter.service';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { Filter, Project, Task, getColor } from '../../core/models';

@Component({
  selector: 'app-filter-tasks',
  imports: [TaskItemComponent, TaskDetailComponent],
  host: { class: 'block h-full' },
  template: `
    <div class="h-full flex flex-col overflow-hidden">

      <!-- Header -->
      <div class="px-8 py-5 border-b border-gray-100 dark:border-gray-800 flex items-center gap-3 flex-shrink-0">
        <span class="w-5 h-5 rounded-full flex-shrink-0" [style.background-color]="filterColor()"></span>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ filter()?.name }}</h1>
        <span class="text-sm text-gray-400">{{ tasks().length }}</span>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-auto">
        <div class="max-w-2xl mx-auto px-8 py-6">

          <!-- Criteria pills -->
          @if (filter()) {
            <div class="flex flex-wrap gap-2 mb-5">
              @if (filter()!.projectId) {
                <span class="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full">
                  Project: {{ getProjectName(filter()!.projectId!) }}
                </span>
              }
              @if (filter()!.hasDate === true) {
                <span class="text-xs px-2 py-0.5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full">
                  Has due date
                </span>
              }
              @if (filter()!.hasDate === false) {
                <span class="text-xs px-2 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 rounded-full">
                  No due date
                </span>
              }
              @if (!filter()!.projectId && filter()!.hasDate == null) {
                <span class="text-xs text-gray-400">All active tasks</span>
              }
            </div>
          }

          <!-- Tasks -->
          <div class="space-y-0.5">
            @for (task of tasks(); track task.id) {
              <app-task-item
                [task]="task"
                (complete)="completeTask($event)"
                (taskClicked)="openDetail($event)" />
            }
            @if (tasks().length === 0) {
              <p class="text-sm text-gray-400 dark:text-gray-500 text-center py-8">No tasks match this filter</p>
            }
          </div>

        </div>
      </div>
    </div>

    @if (selectedTask()) {
      <app-task-detail
        [task]="selectedTask()!"
        (close)="selectedTask.set(null)"
        (taskUpdated)="onTaskUpdated($event)"
        (taskDeleted)="onTaskDeleted($event)" />
    }
  `
})
export class FilterTasksComponent {
  id = input<string>('');

  private filterService = inject(FilterService);
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);

  private allFilters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  private allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private fetchedFilter = signal<Filter | null>(null);

  // Use cached list first, fall back to direct fetch result
  filter = computed(() =>
    this.allFilters().find(f => f.id === this.id()) ?? this.fetchedFilter()
  );

  tasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);

  constructor() {
    effect(() => {
      const id = this.id();
      if (!id) return;
      this.filterService.getFilter(id).subscribe(f => this.fetchedFilter.set(f));
      this.filterService.getFilterTasks(id).subscribe(t => this.tasks.set(t));
    });
  }

  filterColor(): string {
    return getColor(this.filter()?.color ?? 'charcoal');
  }

  getProjectName(projectId: string): string {
    return this.allProjects().find(p => p.id === projectId)?.name ?? 'Unknown';
  }

  completeTask(task: Task): void {
    this.taskService.closeTask(task.id).subscribe(() => {
      this.tasks.update(t => t.filter(t2 => t2.id !== task.id));
    });
  }

  openDetail(task: Task): void { this.selectedTask.set(task); }

  onTaskUpdated(task: Task): void {
    this.tasks.update(t => t.map(t2 => t2.id === task.id ? task : t2));
    this.selectedTask.set(task);
  }

  onTaskDeleted(id: string): void {
    this.tasks.update(t => t.filter(t2 => t2.id !== id));
    this.selectedTask.set(null);
  }
}
