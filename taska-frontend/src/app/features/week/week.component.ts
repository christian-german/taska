import {Component, DestroyRef, OnInit, computed, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Project, Task, daysDiff, fmtDateShort } from '../../core/models';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

const FR_DAYS = ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'];

@Component({
  selector: 'app-week',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [title]="title()" [subtitle]="subtitle()">
      <div actions style="display: flex; align-items: center; gap: 4px;">
        <button class="btn btn-ghost" style="padding: 5px 8px;"
                (click)="prevWeek()" title="Semaine précédente">
          <app-icon name="chevron-left" [size]="16" />
        </button>
        @if (weekOffset() !== 0) {
          <button class="btn btn-ghost" style="padding: 5px 10px; font-size: 12px;"
                  (click)="resetWeek()">aujourd'hui</button>
        }
        <button class="btn btn-ghost" style="padding: 5px 8px;"
                (click)="nextWeek()" title="Semaine suivante">
          <app-icon name="chevron-right" [size]="16" />
        </button>
      </div>
    </app-page-header>

    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (isEmpty()) {
        <app-empty-state title="rien à faire cette semaine" hint="Profite ✨">
          <app-icon icon name="calendar" [size]="28" color="var(--mute)" />
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
export class WeekComponent implements OnInit {
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);
  private destroyRef = inject(DestroyRef);

  tasks = signal<Task[]>([]);
  weekOffset = signal(0);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);

  weekStart = computed(() => {
    const d = new Date();
    d.setDate(d.getDate() + this.weekOffset() * 7);
    d.setHours(0, 0, 0, 0);
    return d;
  });

  title = computed(() => {
    const o = this.weekOffset();
    if (o === 0) return 'Cette semaine';
    if (o === 1) return 'Semaine prochaine';
    if (o === -1) return 'Semaine dernière';
    return o > 0 ? `Dans ${o} semaines` : `Il y a ${Math.abs(o)} semaines`;
  });

  groups = computed<TaskGroup[]>(() => {
    const start = this.weekStart();
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const days: TaskGroup[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(start);
      d.setDate(d.getDate() + i);
      const diffFromToday = Math.round((d.getTime() - today.getTime()) / 86400000);
      const label = diffFromToday === 0 ? "Aujourd'hui"
                  : diffFromToday === 1 ? 'Demain'
                  : `${FR_DAYS[d.getDay()]} ${d.getDate()}`;
      days.push({ key: 'd' + i, label, tasks: [] });
    }
    for (const t of this.tasks()) {
      if (!t.dueAt || t.isCompleted) continue;
      const due = new Date(t.dueAt);
      const diff = daysDiff(start, due);
      if (diff >= 0 && diff < 7) days[diff].tasks.push(t);
    }
    return days;
  });

  subtitle = computed(() => {
    const start = this.weekStart();
    const end = new Date(start);
    end.setDate(end.getDate() + 6);
    return `du ${fmtDateShort(start)} au ${fmtDateShort(end)}`;
  });

  isEmpty = computed(() => this.groups().every(g => g.tasks.length === 0));

  ngOnInit(): void {
    this.refresh();
    this.ui.taskCreated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.refresh());
    this.ui.taskDeleted$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(id => {
      this.tasks.update(list => list.filter(t => t.id !== id));
    });
    this.ui.taskUpdated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.refresh());
  }

  prevWeek(): void { this.weekOffset.update(o => o - 1); this.refresh(); }
  nextWeek(): void { this.weekOffset.update(o => o + 1); this.refresh(); }
  resetWeek(): void { this.weekOffset.set(0); this.refresh(); }

  private refresh(): void {
    const start = this.weekStart();
    const end = new Date(start);
    end.setDate(end.getDate() + 6);
    const from = start.toISOString().slice(0, 10);
    const to = end.toISOString().slice(0, 10);
    this.taskService.getTasks({ from, to }).subscribe(t => this.tasks.set(t));
  }

  onToggle(t: Task): void {
    const scheduledAt = t.scheduledAt ?? undefined;
    const op = t.isCompleted
      ? this.taskService.reopenTask(t.id, scheduledAt)
      : this.taskService.closeTask(t.id, scheduledAt);
    op.subscribe(() => this.refresh());
  }

  onSelect(t: Task): void {
    this.ui.openTaskDetail(t);
  }

  onUpdate(payload: { id: string; patch: Partial<Task> }): void {
    this.taskService.updateTask(payload.id, payload.patch).subscribe(() => this.refresh());
  }
}
