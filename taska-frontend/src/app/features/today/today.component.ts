import {Component, DestroyRef, OnInit, computed, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  Project,
  Task,
  fmtDateLong,
  fmtEstimate,
  isOverdue,
  sameDay
} from '../../core/models';
import { TaskService } from '../../core/services/task.service';
import { ProjectService } from '../../core/services/project.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TaskListComponent, TaskGroup } from '../../shared/components/task-list/task-list.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/atoms/atoms.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

function todayISO(): string {
  return new Date().toISOString().slice(0, 10);
}

@Component({
  selector: 'app-today',
  imports: [TaskListComponent, PageHeaderComponent, EmptyStateComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [title]="'Aujourd\\'hui'" [subtitle]="subtitle()">
    </app-page-header>

    <div class="scroll" style="flex: 1; overflow-y: auto; padding: 8px 12px 60px;">
      @if (isEmpty()) {
        <app-empty-state title="rien à faire ici" hint="Profite ✨">
          <app-icon icon name="sparkle" [size]="28" color="var(--mute)" />
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
export class TodayComponent implements OnInit {
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private ui = inject(UiStateService);
  private destroyRef = inject(DestroyRef);

  tasks = signal<Task[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);

  groups = computed<TaskGroup[]>(() => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const tasks = this.tasks();
    const overdue = tasks.filter(t => isOverdue(t));
    const todayDue = tasks.filter(t => t.scheduledAt && sameDay(new Date(t.scheduledAt), today));
    const tomorrowDue = tasks.filter(t =>
      !t.isCompleted &&
      t.scheduledAt &&
      sameDay(new Date(t.scheduledAt), tomorrow)
    );

    const groups: TaskGroup[] = [];
    if (overdue.length) {
      groups.push({
        key: 'overdue',
        label: 'En retard',
        tone: 'overdue',
        icon: 'alarm',
        tasks: this.sortTasks(overdue),
      });
    }
    groups.push({
      key: 'today',
      label: "Aujourd'hui",
      tasks: this.sortTasks(todayDue),
      empty: 'rien de prévu aujourd\'hui',
    });
    groups.push({
      key: 'tomorrow',
      label: 'Demain',
      tasks: this.sortTasks(tomorrowDue),
      empty: 'rien de prévu demain',
    });
    return groups;
  });

  subtitle = computed(() => {
    const today = new Date();
    const todayDue = this.tasks().filter(t => t.scheduledAt && sameDay(new Date(t.scheduledAt), today));
    const overdue = this.tasks().filter(t => isOverdue(t));
    const totalEst = todayDue.filter(t => !t.isCompleted).reduce((a, b) => a + (b.estimateMinutes || 0), 0);
    let s = `${fmtDateLong(today)} · ${todayDue.filter(t => !t.isCompleted).length} tâches`;
    if (totalEst) s += ` · ~${fmtEstimate(totalEst)} estimées`;
    if (overdue.length) s += ` · ${overdue.length} en retard`;
    return s;
  });

  isEmpty = computed(() => this.groups().every(g => g.tasks.length === 0));

  ngOnInit(): void {
    this.refresh();
    this.ui.taskCreated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(task => {
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const qualifies = !task.isCompleted && !!task.scheduledAt && (
        isOverdue(task) ||
        sameDay(new Date(task.scheduledAt), today) ||
        sameDay(new Date(task.scheduledAt), tomorrow)
      );
      if (qualifies) this.tasks.update(list => [...list, task]);
    });
    this.ui.taskDeleted$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(id => {
      this.tasks.update(list => list.filter(t => t.id !== id));
    });
    this.ui.taskUpdated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.refresh());
  }

  private refresh(): void {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const from = todayISO();
    const to = tomorrow.toISOString().slice(0, 10);
    this.taskService.getTasks({ from, to }).subscribe(tasks => this.tasks.set(tasks));
  }

  onToggle(t: Task): void {
    const occurrenceScheduledAt = t.occurrenceScheduledAt ?? undefined;
    const op = t.isCompleted
      ? this.taskService.reopenTask(t.id, occurrenceScheduledAt)
      : this.taskService.closeTask(t.id, occurrenceScheduledAt);
    op.subscribe(() => this.refresh());
  }

  onSelect(t: Task): void {
    this.ui.openTaskDetail(t);
  }

  onUpdate(payload: { id: string; patch: Partial<Task> }): void {
    this.taskService.updateTask(payload.id, payload.patch).subscribe(() => this.refresh());
  }

  private sortTasks(arr: Task[]): Task[] {
    return [...arr].sort((a, b) => {
      if (a.isCompleted !== b.isCompleted) return a.isCompleted ? 1 : -1;
      if (a.priority !== b.priority) return (b.priority ?? 0) - (a.priority ?? 0);
      if (a.scheduledAt && b.scheduledAt) return a.scheduledAt.localeCompare(b.scheduledAt);
      return 0;
    });
  }
}
