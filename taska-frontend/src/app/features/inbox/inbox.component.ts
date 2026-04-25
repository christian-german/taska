import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { AddTaskFormComponent } from '../../shared/components/add-task-form/add-task-form.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { DisplayPanelComponent } from '../../shared/components/display-panel/display-panel.component';
import { CalendarViewComponent } from '../../shared/components/calendar-view/calendar-view.component';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { Task, ViewStyle } from '../../core/models';

@Component({
  selector: 'app-inbox',
  imports: [TaskItemComponent, AddTaskFormComponent, TaskDetailComponent, DisplayPanelComponent, CalendarViewComponent],
  template: `
    <div class="h-full flex flex-col overflow-hidden">
      <!-- Header -->
      <div class="px-8 py-5 border-b border-gray-100 dark:border-gray-800 flex items-center justify-between flex-shrink-0">
        <div class="flex items-center gap-3">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-blue-500" viewBox="0 0 20 20" fill="currentColor">
            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
          </svg>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Inbox</h1>
          @if (activeTasks().length > 0) {
            <span class="text-sm text-gray-400">{{ activeTasks().length }}</span>
          }
        </div>

        <!-- Display button -->
        <div class="relative">
          <button (click)="toggleDisplayPanel($event)"
            [class]="showDisplayPanel() ? 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-100' : 'text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 hover:text-gray-700 dark:hover:text-gray-200'"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 4h2m0 0a1 1 0 011-1h1a1 1 0 011 1m-3 0v2m0-2h3m0 0v2M3 4v2m0 0h3M3 12h5m0 0a1 1 0 011-1h1a1 1 0 011 1m-3 0v2m0-2h3m0 0v2m-3-2h3M3 12v2m0 0h5M3 20h8m0 0a1 1 0 011-1h1a1 1 0 011 1m-3 0v2m0-2h3m0 0v2m-3-2h3M3 20v2m0 0h8"/>
            </svg>
            Display
          </button>

          <app-display-panel
            [isOpen]="showDisplayPanel()"
            [viewStyle]="viewStyle()"
            [showCompleted]="showCompleted()"
            (viewStyleChange)="viewStyle.set($event)"
            (showCompletedChange)="setShowCompleted($event)"
            (closed)="showDisplayPanel.set(false)" />
        </div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-auto">

        @if (viewStyle() === 'LIST') {
          <div class="max-w-2xl mx-auto px-8 py-6">
            <div class="space-y-0.5">
              @for (task of activeTasks(); track task.id) {
                <app-task-item
                  [task]="task"
                  (complete)="completeTask($event)"
                  (taskClicked)="openDetail($event)" />
              }
              @if (activeTasks().length === 0 && !showCompleted()) {
                <p class="text-sm text-gray-400 dark:text-gray-500 py-4 text-center">No tasks in inbox</p>
              }
            </div>

            <app-add-task-form [projectId]="inboxProjectId()" (taskCreated)="onTaskCreated($event)" />

            <!-- Completed tasks -->
            @if (showCompleted() && completedTasks().length > 0) {
              <div class="mt-8">
                <div class="flex items-center gap-2 mb-2 pb-2 border-b border-gray-200 dark:border-gray-700">
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                  </svg>
                  <h2 class="font-semibold text-sm text-gray-500 dark:text-gray-400">Completed</h2>
                  <span class="text-xs text-gray-400">{{ completedTasks().length }}</span>
                </div>
                <div class="space-y-0.5">
                  @for (task of completedTasks(); track task.id) {
                    <div class="flex items-center gap-3 py-2 px-1">
                      <button (click)="reopenTask(task)"
                        class="w-4 h-4 rounded-full border-2 border-gray-300 dark:border-gray-600 flex items-center justify-center bg-gray-100 dark:bg-gray-700 flex-shrink-0 hover:border-gray-400 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="w-2.5 h-2.5 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                        </svg>
                      </button>
                      <span (click)="openDetail(task)"
                        class="text-sm text-gray-400 dark:text-gray-500 line-through cursor-pointer hover:text-gray-600 dark:hover:text-gray-400 transition-colors">
                        {{ task.content }}
                      </span>
                    </div>
                  }
                </div>
              </div>
            }
          </div>

        } @else if (viewStyle() === 'BOARD') {
          <div class="flex gap-4 p-6 h-full overflow-x-auto">
            <div class="w-72 flex-shrink-0 flex flex-col">
              <div class="bg-gray-100 dark:bg-gray-800 rounded-xl p-4 flex-1 flex flex-col">
                <h2 class="font-semibold text-sm text-gray-700 dark:text-gray-300 mb-3">Inbox</h2>
                <div class="space-y-2 flex-1">
                  @for (task of activeTasks(); track task.id) {
                    <div (click)="openDetail(task)"
                      class="bg-white dark:bg-gray-700 rounded-lg p-3 shadow-sm cursor-pointer hover:shadow-md transition-shadow border border-gray-100 dark:border-gray-600">
                      <p class="text-sm text-gray-900 dark:text-gray-100">{{ task.content }}</p>
                      @if (task.dueDate) {
                        <span class="text-xs text-gray-400 mt-1 block">{{ task.dueDate }}</span>
                      }
                    </div>
                  }
                </div>
                <app-add-task-form [projectId]="inboxProjectId()" (taskCreated)="onTaskCreated($event)" />
              </div>
            </div>
          </div>

        } @else {
          <app-calendar-view [tasks]="tasks()" (taskClicked)="openDetail($event)" />
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
export class InboxComponent implements OnInit {
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);

  tasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);
  inboxProjectId = signal<string | undefined>(undefined);
  viewStyle = signal<ViewStyle>('LIST');
  showCompleted = signal(false);
  showDisplayPanel = signal(false);

  activeTasks = computed(() => this.tasks().filter(t => !t.isCompleted));
  completedTasks = computed(() => this.tasks().filter(t => t.isCompleted));

  ngOnInit(): void {
    this.projectService.projects$.subscribe(projects => {
      const inbox = projects.find(p => p.isInboxProject);
      if (inbox) {
        this.inboxProjectId.set(inbox.id);
        this.loadTasks(inbox.id);
      }
    });
  }

  private loadTasks(projectId: string): void {
    this.taskService.getTasks({ projectId, showCompleted: this.showCompleted() }).subscribe(tasks => this.tasks.set(tasks));
  }

  setShowCompleted(value: boolean): void {
    this.showCompleted.set(value);
    const id = this.inboxProjectId();
    if (id) this.loadTasks(id);
  }

  toggleDisplayPanel(e: Event): void {
    e.stopPropagation();
    this.showDisplayPanel.set(!this.showDisplayPanel());
  }

  onTaskCreated(task: Task): void {
    this.tasks.update(t => [...t, task]);
  }

  completeTask(task: Task): void {
    this.taskService.closeTask(task.id).subscribe(updated => {
      if (this.showCompleted()) {
        this.tasks.update(t => t.map(t2 => t2.id === task.id ? updated : t2));
      } else {
        this.tasks.update(t => t.filter(t2 => t2.id !== task.id));
      }
    });
  }

  reopenTask(task: Task): void {
    this.taskService.reopenTask(task.id).subscribe(updated => {
      this.tasks.update(t => t.map(t2 => t2.id === task.id ? updated : t2));
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
