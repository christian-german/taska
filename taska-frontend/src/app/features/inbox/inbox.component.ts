import {ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { toSignal } from '@angular/core/rxjs-interop';
import { Project, Task } from '../../core/models';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-inbox',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [title]="'Inbox'" [subtitle]="subtitle()" />

    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (tasks().length === 0) {
        <app-empty-state title="rien dans la boîte" hint="Capture une idée avec ⌘N">
          <app-icon icon name="inbox" [size]="28" color="var(--mute)" />
        </app-empty-state>
      } @else {
        <app-task-list
          [groups]="groups()"
          [projects]="projects()"
          [selectedId]="selectedId()"
          (toggled)="onToggle($event)"
          (selectTask)="onSelect($event)"
          (updated)="onUpdate($event)" />
      }
    </div>
  `,
})
export class InboxComponent implements OnInit {
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);
  private destroyRef = inject(DestroyRef);

  tasks = signal<Task[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);
  subtitle = computed(() => `${this.tasks().length} tâches non triées`);

  groups = computed<TaskGroup[]>(() => [
    { key: 'inbox', label: 'À traiter', tasks: this.tasks() },
  ]);

  ngOnInit(): void {
    this.refresh();
    this.ui.taskCreated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(task => {
      const inboxId = this.projects().find(p => p.isInboxProject)?.id;
      if (!task.isCompleted && task.projectId === inboxId) {
        this.tasks.update(list => [...list, task]);
      }
    });
    this.ui.taskDeleted$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(id => {
      this.tasks.update(list => list.filter(t => t.id !== id));
    });
    this.ui.taskUpdated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(task => {
      this.tasks.update(list => {
        const inboxId = this.projects().find(p => p.isInboxProject)?.id;
        const inList = list.some(t => t.id === task.id);
        const belongs = task.projectId === inboxId;
        if (inList && belongs) return list.map(t => t.id === task.id ? task : t);
        if (inList && !belongs) return list.filter(t => t.id !== task.id);
        if (!inList && belongs) return [...list, task];
        return list;
      });
    });
  }

  private refresh(): void {
    this.projectService.projects$.subscribe(projects => {
      const inbox = projects.find(p => p.isInboxProject);
      if (!inbox) return;
      this.taskService.getTasks({ projectId: inbox.id, showCompleted: false }).subscribe(t => {
        this.tasks.set(t);
      });
    });
  }

  onToggle(t: Task): void {
    const op = t.isCompleted ? this.taskService.reopenTask(t.id) : this.taskService.closeTask(t.id);
    op.subscribe(() => this.refresh());
  }

  onSelect(t: Task): void {
    this.ui.openTaskDetail(t);
  }

  onUpdate(payload: { id: string; patch: Partial<Task> }): void {
    this.taskService.updateTask(payload.id, payload.patch).subscribe(updated => {
      this.tasks.update(list => list.map(x => x.id === updated.id ? updated : x));
    });
  }
}
