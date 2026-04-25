import { Component, OnInit, effect, inject, input, signal } from '@angular/core';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { TaskService } from '../../core/services/task.service';
import { LabelService } from '../../core/services/label.service';
import { Task, Label, getColor } from '../../core/models';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-label-tasks',
  standalone: true,
  imports: [TaskItemComponent, TaskDetailComponent],
  template: `
    <div class="max-w-2xl mx-auto px-8 py-8">
      <div class="flex items-center gap-3 mb-6">
        <span class="w-5 h-5 rounded-full flex-shrink-0" [style.background-color]="labelColor()"></span>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ name() }}</h1>
        @if (tasks().length > 0) {
          <span class="text-sm text-gray-400">{{ tasks().length }}</span>
        }
      </div>

      <div class="space-y-0.5">
        @for (task of tasks(); track task.id) {
          <app-task-item
            [task]="task"
            (complete)="completeTask($event)"
            (taskClicked)="openDetail($event)" />
        }
        @if (tasks().length === 0) {
          <p class="text-sm text-gray-400 dark:text-gray-500 text-center py-8">No tasks with this label</p>
        }
      </div>
    </div>

    @if (selectedTask()) {
      <app-task-detail
        [task]="selectedTask()!"
        (close)="selectedTask.set(null)"
        (taskUpdated)="onTaskUpdated($event)" />
    }
  `
})
export class LabelTasksComponent implements OnInit {
  name = input.required<string>();

  private taskService = inject(TaskService);
  private labelService = inject(LabelService);

  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  tasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);

  labelColor() {
    const label = this.labels().find(l => l.name === this.name());
    return getColor(label?.color ?? 'charcoal');
  }

  constructor() {
    effect(() => {
      const name = this.name();
      if (name) this.loadTasks(name);
    });
  }

  ngOnInit(): void {}

  private loadTasks(label: string): void {
    this.taskService.getTasks({ label }).subscribe(t => this.tasks.set(t));
  }

  completeTask(task: Task): void {
    this.taskService.closeTask(task.id).subscribe(() => {
      this.tasks.update(t => t.filter(t2 => t2.id !== task.id));
    });
  }

  openDetail(task: Task): void {
    this.selectedTask.set(task);
  }

  onTaskUpdated(task: Task): void {
    this.tasks.update(t => t.map(t2 => t2.id === task.id ? task : t2));
    this.selectedTask.set(task);
  }
}
