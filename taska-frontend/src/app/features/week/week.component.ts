import {Component, DestroyRef, OnInit, computed, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Project, Task, daysDiff, fmtDateShort, sameDay, startOfDay } from '../../core/models';
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
    <app-page-header [title]="'Cette semaine'" [subtitle]="subtitle()" />

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
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  selectedId = computed(() => this.ui.selectedTask()?.id ?? null);

  groups = computed<TaskGroup[]>(() => {
    const today = new Date();
    const days: TaskGroup[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(today);
      d.setDate(d.getDate() + i);
      const label = i === 0 ? "Aujourd'hui"
                  : i === 1 ? 'Demain'
                  : `${FR_DAYS[d.getDay()]} ${d.getDate()}`;
      days.push({ key: 'd' + i, label, tasks: [] });
    }
    for (const t of this.tasks()) {
      if (!t.dueAt || t.isCompleted) continue;
      const due = new Date(t.dueAt);
      const diff = daysDiff(today, due);
      if (diff >= 0 && diff < 7) days[diff].tasks.push(t);
    }
    return days;
  });

  subtitle = computed(() => {
    const today = new Date();
    const end = new Date(today);
    end.setDate(end.getDate() + 7);
    return `du ${fmtDateShort(today)} au ${fmtDateShort(end)}`;
  });

  isEmpty = computed(() => this.groups().every(g => g.tasks.length === 0));

  ngOnInit(): void {
    this.refresh();
    this.ui.taskCreated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(task => {
      const diff = task.dueAt ? daysDiff(new Date(), new Date(task.dueAt)) : -1;
      if (!task.isCompleted && diff >= 0 && diff < 7) {
        this.tasks.update(list => [...list, task]);
      }
    });
    this.ui.taskDeleted$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(id => {
      this.tasks.update(list => list.filter(t => t.id !== id));
    });
    this.ui.taskUpdated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(task => {
      this.tasks.update(list => {
        const today = new Date();
        const diff = task.dueAt ? daysDiff(today, new Date(task.dueAt)) : -1;
        const qualifies = !task.isCompleted && diff >= 0 && diff < 7;
        const inList = list.some(t => t.id === task.id);
        if (inList && qualifies) return list.map(t => t.id === task.id ? task : t);
        if (inList && !qualifies) return list.filter(t => t.id !== task.id);
        if (!inList && qualifies) return [...list, task];
        return list;
      });
    });
  }

  private refresh(): void {
    this.taskService.getTasks({ showCompleted: false }).subscribe(t => this.tasks.set(t));
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
