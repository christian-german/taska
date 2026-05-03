import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Filter, Project, Task, getColor } from '../../core/models';
import { FilterService } from '../../core/services/filter.service';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-filter-tasks',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [title]="filter()?.name || 'Filtre'" [subtitle]="subtitle()" />
    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (tasks().length === 0) {
        <app-empty-state title="aucune tâche pour ce filtre" hint="">
          <app-icon icon name="filter" [size]="28" color="var(--mute)" />
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
export class FilterTasksComponent {
  id = input<string>('');

  private filterService = inject(FilterService);
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);

  private allFilters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private fetchedFilter = signal<Filter | null>(null);

  filter = computed(() =>
    this.allFilters().find(f => f.id === this.id()) ?? this.fetchedFilter()
  );

  tasks = signal<Task[]>([]);

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);

  subtitle = computed(() => {
    const f = this.filter();
    if (!f) return '';
    const parts: string[] = [`${this.tasks().length} tâches`];
    if (f.projectId) {
      const p = this.projects().find(x => x.id === f.projectId);
      if (p) parts.push(`projet ${p.name}`);
    }
    if (f.hasDate === true) parts.push('avec échéance');
    if (f.hasDate === false) parts.push('sans échéance');
    return parts.join(' · ');
  });

  groups = computed<TaskGroup[]>(() => [
    { key: 'filter', label: 'Tâches', tasks: this.tasks() },
  ]);

  constructor() {
    effect(() => {
      const id = this.id();
      if (!id) return;
      this.filterService.getFilter(id).subscribe(f => this.fetchedFilter.set(f));
      this.filterService.getFilterTasks(id).subscribe(t => this.tasks.set(t));
    });
  }

  onToggle(t: Task): void {
    const op = t.isCompleted ? this.taskService.reopenTask(t.id) : this.taskService.closeTask(t.id);
    op.subscribe(() => {
      const id = this.id();
      if (id) this.filterService.getFilterTasks(id).subscribe(tasks => this.tasks.set(tasks));
    });
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
