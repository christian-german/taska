import { Component, OnInit, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Label, Project, Task, getColor } from '../../core/models';
import { TaskService } from '../../core/services/task.service';
import { LabelService } from '../../core/services/label.service';
import { ProjectService } from '../../core/services/project.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-label-tasks',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  template: `
    <app-page-header [title]="'#' + name()" [subtitle]="subtitle()" />
    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (tasks().length === 0) {
        <app-empty-state title="aucune tâche avec ce tag" hint="Ajoute #{{ name() }} à une tâche pour la voir ici">
          <app-icon icon name="tag" [size]="28" color="var(--mute)" />
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
export class LabelTasksComponent implements OnInit {
  name = input<string>('');

  private taskService = inject(TaskService);
  private labelService = inject(LabelService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);

  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  tasks = signal<Task[]>([]);

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);
  subtitle = computed(() => `${this.tasks().length} tâches`);

  groups = computed<TaskGroup[]>(() => [
    { key: 'tag', label: 'Toutes', tasks: this.tasks() },
  ]);

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

  onToggle(t: Task): void {
    const op = t.isCompleted ? this.taskService.reopenTask(t.id) : this.taskService.closeTask(t.id);
    op.subscribe(() => this.loadTasks(this.name()));
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
