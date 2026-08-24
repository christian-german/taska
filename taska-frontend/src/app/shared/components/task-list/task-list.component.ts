import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import { Project, Task, getColor } from '../../../core/models';
import { TaskRowComponent } from '../task-row/task-row.component';
import { IconComponent } from '../icon/icon.component';

export interface TaskGroup {
  key: string;
  label: string;
  tone?: 'overdue' | 'normal';
  icon?: string;
  color?: string;
  empty?: string;
  tasks: Task[];
}

@Component({
  selector: 'app-task-list',
  imports: [TaskRowComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      @for (group of groups(); track group.key) {
        <div style="margin-bottom: 8px;">
          <div class="section-h">
            <div style="display: flex; align-items: center; gap: 8px;">
              @if (group.icon) {
                <app-icon [name]="group.icon" [size]="14" [color]="group.color || 'var(--mute)'" />
              }
              <span class="label" [class.overdue]="group.tone === 'overdue'"
                    [style.color]="group.color && group.tone !== 'overdue' ? group.color : null">
                {{ group.label }}
              </span>
              <span class="count">({{ group.tasks.length }})</span>
            </div>
          </div>

          @if (group.tasks.length === 0) {
            <div style="padding: 6px 14px; color: var(--mute); font-size: 12.5px; font-style: italic;">
              {{ group.empty || '—' }}
            </div>
          } @else {
            @for (task of group.tasks; track occurrenceKey(task)) {
              <app-task-row
                [task]="task"
                [project]="projectFor(task)"
                [selected]="selectedId() === task.id"
                (toggled)="toggled.emit($event)"
                (selectTask)="selectTask.emit($event)"
                (updated)="updated.emit($event)" />
            }
          }
        </div>
      }
    </div>
  `,
})
export class TaskListComponent {
  groups = input.required<TaskGroup[]>();
  projects = input<Project[]>([]);
  selectedId = input<string | null>(null);

  toggled = output<Task>();
  selectTask = output<Task>();
  updated = output<{ id: string; patch: Partial<Task> }>();

  private projectMap = computed(() => {
    const m: Record<string, Project> = {};
    for (const p of this.projects()) m[p.id] = p;
    return m;
  });

  projectFor(task: Task): Project | null {
    if (!task.projectId) return null;
    return this.projectMap()[task.projectId] ?? null;
  }

  occurrenceKey(task: Task): string {
    if (task.instanceId) return task.instanceId;
    if (task.occurrenceScheduledAt) return `${task.id}:${task.occurrenceScheduledAt}`;
    return task.id;
  }

  getColor = getColor;
}
