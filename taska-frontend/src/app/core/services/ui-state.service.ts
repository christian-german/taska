import { Injectable, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { Task } from '../models';

@Injectable({ providedIn: 'root' })
export class UiStateService {
  showQuickAdd = signal(false);
  showPalette = signal(false);
  showHelp = signal(false);
  sidebarOpen = signal(false);
  selectedTask = signal<Task | null>(null);
  defaultProjectId = signal<string | null>(null);

  readonly taskCreated$ = new Subject<Task>();
  readonly taskUpdated$ = new Subject<Task>();
  readonly taskDeleted$ = new Subject<string>();

  openTaskDetail(task: Task): void {
    this.selectedTask.set(task);
  }

  closeTaskDetail(): void {
    this.selectedTask.set(null);
  }

  openQuickAdd(projectId?: string): void {
    this.defaultProjectId.set(projectId ?? null);
    this.showQuickAdd.set(true);
  }

  closeAll(): void {
    this.showQuickAdd.set(false);
    this.showPalette.set(false);
    this.showHelp.set(false);
    this.selectedTask.set(null);
  }
}
