import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { TaskService } from '../../core/services/task.service';
import { Task } from '../../core/models';

interface DayGroup {
  date: string;
  label: string;
  tasks: Task[];
}

@Component({
  selector: 'app-upcoming',
  imports: [TaskItemComponent, TaskDetailComponent],
  template: `
    <div class="max-w-2xl mx-auto px-6 py-8">
      <div class="flex items-center gap-3 mb-6">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-purple-500" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
        </svg>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Upcoming</h1>
      </div>

      @for (group of groups(); track group.date) {
        <div class="mb-6">
          <h2 class="text-sm font-semibold text-gray-500 dark:text-gray-400 mb-2 pb-1 border-b border-gray-100 dark:border-gray-800">
            {{ group.label }}
          </h2>
          @for (task of group.tasks; track task.id) {
            <app-task-item [task]="task" (complete)="completeTask($event)" (taskClicked)="openDetail($event)" />
          }
        </div>
      }

      @if (groups().length === 0) {
        <div class="text-center py-12">
          <p class="text-lg text-gray-400 dark:text-gray-500">Nothing upcoming</p>
        </div>
      }
    </div>

    @if (selectedTask()) {
      <app-task-detail [task]="selectedTask()!" (close)="selectedTask.set(null)" (taskUpdated)="onTaskUpdated($event)" />
    }
  `
})
export class UpcomingComponent implements OnInit {
  private taskService = inject(TaskService);

  tasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);

  groups = computed<DayGroup[]>(() => {
    const byDate = new Map<string, Task[]>();
    for (const t of this.tasks()) {
      if (!t.dueDate) continue;
      const existing = byDate.get(t.dueDate) ?? [];
      byDate.set(t.dueDate, [...existing, t]);
    }
    return Array.from(byDate.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, tasks]) => ({
        date,
        label: this.formatGroupDate(date),
        tasks
      }));
  });

  ngOnInit(): void {
    this.taskService.getTasks({ filter: 'upcoming' }).subscribe(tasks => this.tasks.set(tasks));
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

  private formatGroupDate(dateStr: string): string {
    const date = new Date(dateStr + 'T00:00:00');
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    if (date.getTime() === tomorrow.getTime()) return 'Tomorrow';
    return date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
  }
}
