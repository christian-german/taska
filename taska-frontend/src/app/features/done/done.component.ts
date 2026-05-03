import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
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
  selector: 'app-done',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [title]="'Terminées'" [subtitle]="subtitle()" />

    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (tasks().length === 0) {
        <app-empty-state title="aucune tâche terminée" hint="Coche-en quelques-unes pour les voir apparaître ici">
          <app-icon icon name="check" [size]="28" color="var(--mute)" />
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
export class DoneComponent implements OnInit {
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);

  tasks = signal<Task[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);
  subtitle = computed(() => `${this.tasks().length} dernières`);

  groups = computed<TaskGroup[]>(() => [
    { key: 'done', label: 'Récentes', tasks: this.tasks() },
  ]);

  ngOnInit(): void {
    this.refresh();
  }

  private refresh(): void {
    this.taskService.getTasks({ showCompleted: true }).subscribe(all => {
      this.tasks.set(
        all.filter(t => t.isCompleted)
           .sort((a, b) => (b.completedAt ?? '').localeCompare(a.completedAt ?? ''))
           .slice(0, 60)
      );
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
