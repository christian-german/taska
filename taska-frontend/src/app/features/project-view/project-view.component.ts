import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Project, Section, Task, getColor } from '../../core/models';
import { ProjectService } from '../../core/services/project.service';
import { SectionService } from '../../core/services/section.service';
import { TaskService } from '../../core/services/task.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-project-view',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (project(); as proj) {
      <app-page-header [title]="proj.name" [subtitle]="subtitle()">
        <div hero>
          <div style="margin-top: 12px;">
            <div class="progress" style="max-width: 280px;">
              <div [style.width.%]="percent()" [style.background]="projectColor()"></div>
            </div>
          </div>
        </div>
      </app-page-header>

      <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
        @if (allItems().length === 0) {
          <app-empty-state title="aucune tâche pour ce projet" hint="Ajoute la première avec ⌘N">
            <app-icon icon name="folder" [size]="28" color="var(--mute)" />
          </app-empty-state>
        } @else {
          <app-task-list
            [groups]="groups()"
            [projects]="allProjects()"
            [selectedId]="selectedId()"
            (toggled)="onToggle($event)"
            (selectTask)="onSelect($event)"
            (updated)="onUpdate($event)" />
        }
      </div>
    }
  `,
})
export class ProjectViewComponent implements OnInit {
  id = input<string>('');

  private projectService = inject(ProjectService);
  private sectionService = inject(SectionService);
  private taskService = inject(TaskService);
  private ui = inject(UiStateService);

  allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  project = computed(() => this.allProjects().find(p => p.id === this.id()) ?? null);

  sections = signal<Section[]>([]);
  allItems = signal<Task[]>([]);

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);

  todoTasks = computed(() => this.allItems().filter(t => !t.isCompleted && !this.isInProgress(t)));
  doingTasks = computed(() => this.allItems().filter(t => !t.isCompleted && this.isInProgress(t)));
  doneTasks = computed(() => this.allItems().filter(t => t.isCompleted));

  groups = computed<TaskGroup[]>(() => {
    const groups: TaskGroup[] = [{ key: 'todo', label: 'à faire', tasks: this.sortTasks(this.todoTasks()) }];
    if (this.doingTasks().length) {
      groups.push({ key: 'doing', label: 'en cours', tasks: this.doingTasks() });
    }
    groups.push({ key: 'done', label: 'terminées', tasks: this.doneTasks() });
    return groups;
  });

  percent = computed(() => {
    const all = this.allItems();
    return all.length ? Math.round((this.doneTasks().length / all.length) * 100) : 0;
  });

  subtitle = computed(() => {
    const total = this.allItems().length;
    return `${total} tâches · ${this.doneTasks().length} terminées · ${this.percent()}%`;
  });

  projectColor = computed(() => getColor(this.project()?.color ?? 'charcoal'));

  private lastLoadedId = '';

  constructor() {
    effect(() => {
      const id = this.id();
      if (!id || id === this.lastLoadedId) return;
      this.lastLoadedId = id;
      this.load(id);
    });
    this.ui.taskCreated$.pipe(takeUntilDestroyed()).subscribe(task => {
      if (task.projectId === this.id()) {
        this.allItems.update(list => [...list, task]);
      }
    });
    this.ui.taskDeleted$.pipe(takeUntilDestroyed()).subscribe(id => {
      this.allItems.update(list => list.filter(t => t.id !== id));
    });
    this.ui.taskUpdated$.pipe(takeUntilDestroyed()).subscribe(task => {
      this.allItems.update(list => {
        const inList = list.some(t => t.id === task.id);
        const belongs = task.projectId === this.id();
        if (inList && belongs) return list.map(t => t.id === task.id ? task : t);
        if (inList && !belongs) return list.filter(t => t.id !== task.id);
        if (!inList && belongs) return [...list, task];
        return list;
      });
    });
  }

  ngOnInit(): void {}

  private load(id: string): void {
    this.projectService.getProjectSections(id).subscribe(s => this.sections.set(s));
    this.taskService.getTasks({ projectId: id, showCompleted: true }).subscribe(t => this.allItems.set(t));
  }

  onToggle(t: Task): void {
    const op = t.isCompleted ? this.taskService.reopenTask(t.id) : this.taskService.closeTask(t.id);
    op.subscribe(updated => {
      this.allItems.update(list => list.map(x => x.id === updated.id ? updated : x));
    });
  }

  onSelect(t: Task): void {
    this.ui.openTaskDetail(t);
  }

  onUpdate(payload: { id: string; patch: Partial<Task> }): void {
    this.taskService.updateTask(payload.id, payload.patch).subscribe(updated => {
      this.allItems.update(list => list.map(x => x.id === updated.id ? updated : x));
    });
  }

  private isInProgress(t: Task): boolean {
    // Approximation: a parent task with at least one closed subtask is "in progress"
    const subs = this.allItems().filter(x => x.parentId === t.id);
    return subs.length > 0 && subs.some(s => s.isCompleted);
  }

  private sortTasks(arr: Task[]): Task[] {
    return [...arr].sort((a, b) => {
      if (a.priority !== b.priority) return b.priority - a.priority;
      if (a.dueDate && b.dueDate) return a.dueDate.localeCompare(b.dueDate);
      return 0;
    });
  }
}
