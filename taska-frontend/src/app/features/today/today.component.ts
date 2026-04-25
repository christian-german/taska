import { Component, OnInit, inject, signal } from '@angular/core';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { AddTaskFormComponent } from '../../shared/components/add-task-form/add-task-form.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { TaskService } from '../../core/services/task.service';
import { Task } from '../../core/models';

@Component({
  selector: 'app-today',
  imports: [TaskItemComponent, AddTaskFormComponent, TaskDetailComponent],
  template: `
    <div class="max-w-2xl mx-auto px-6 py-8">
      <div class="flex items-center gap-3 mb-2">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-green-500" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
        </svg>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Today</h1>
        <span class="text-sm text-gray-400">{{ todayDate() }}</span>
      </div>

      @if (overdueTasks().length > 0) {
        <div class="mb-6">
          <h2 class="text-sm font-semibold text-red-500 mb-2 flex items-center gap-1">
            <span>Overdue</span>
            <span class="text-xs font-normal">{{ overdueTasks().length }}</span>
          </h2>
          @for (task of overdueTasks(); track task.id) {
            <app-task-item [task]="task" (complete)="completeTask($event)" (taskClicked)="openDetail($event)" />
          }
        </div>
      }

      <div class="mt-4">
        @for (task of todayTasks(); track task.id) {
          <app-task-item [task]="task" (complete)="completeTask($event)" (taskClicked)="openDetail($event)" />
        }
        @if (todayTasks().length === 0 && overdueTasks().length === 0) {
          <div class="text-center py-12">
            <p class="text-lg text-gray-400 dark:text-gray-500">Nothing due today</p>
            <p class="text-sm text-gray-300 dark:text-gray-600 mt-1">Enjoy your free time!</p>
          </div>
        }
        <app-add-task-form [initialDueDate]="todayStr" (taskCreated)="onTaskCreated($event)" />
      </div>
    </div>

    @if (selectedTask()) {
      <app-task-detail [task]="selectedTask()!" (close)="selectedTask.set(null)" (taskUpdated)="onTaskUpdated($event)" />
    }
  `
})
export class TodayComponent implements OnInit {
  private taskService = inject(TaskService);

  allTasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);

  todayStr = new Date().toISOString().split('T')[0];

  todayDate() {
    return new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
  }

  todayTasks() { return this.allTasks().filter(t => t.dueDate === this.todayStr); }
  overdueTasks() {
    return this.allTasks().filter(t => t.dueDate && t.dueDate < this.todayStr);
  }

  ngOnInit(): void {
    this.taskService.getTasks({ filter: 'today' }).subscribe(tasks => {
      this.taskService.getTasks({ filter: 'overdue' }).subscribe(overdue => {
        this.allTasks.set([...overdue, ...tasks]);
      });
    });
  }

  onTaskCreated(task: Task): void {
    this.allTasks.update(t => [...t, task]);
  }

  completeTask(task: Task): void {
    this.taskService.closeTask(task.id).subscribe(() => {
      this.allTasks.update(t => t.filter(t2 => t2.id !== task.id));
    });
  }

  openDetail(task: Task): void { this.selectedTask.set(task); }

  onTaskUpdated(task: Task): void {
    this.allTasks.update(t => t.map(t2 => t2.id === task.id ? task : t2));
    this.selectedTask.set(task);
  }
}
