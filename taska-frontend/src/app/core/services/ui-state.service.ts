import { Injectable, signal } from '@angular/core';
import { Task } from '../models';

@Injectable({ providedIn: 'root' })
export class UiStateService {
  showQuickAdd = signal(false);
  showPalette = signal(false);
  showHelp = signal(false);
  selectedTask = signal<Task | null>(null);

  openTaskDetail(task: Task): void {
    this.selectedTask.set(task);
  }

  closeTaskDetail(): void {
    this.selectedTask.set(null);
  }

  openQuickAdd(): void {
    this.showQuickAdd.set(true);
  }

  closeAll(): void {
    this.showQuickAdd.set(false);
    this.showPalette.set(false);
    this.showHelp.set(false);
    this.selectedTask.set(null);
  }
}
