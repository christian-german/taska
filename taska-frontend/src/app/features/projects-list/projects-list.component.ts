import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Project, Task, getColor } from '../../core/models';
import { ProjectService } from '../../core/services/project.service';
import { TaskService } from '../../core/services/task.service';
import { ProjectDotComponent } from '../../shared/components/atoms/atoms.component';

@Component({
  selector: 'app-projects-list',
  imports: [RouterLink, ProjectDotComponent],
  template: `
    <div style="padding: 32px 28px;">
      <div class="script" style="font-size: 36px;">Projets</div>
      <div style="margin-top: 18px; display: grid; gap: 10px;">
        @for (p of activeProjects(); track p.id) {
          <a [routerLink]="['/project', p.id]"
             style="background: var(--bg-2); border-radius: 12px; padding: 14px;
                    display: flex; align-items: center; gap: 12px; text-decoration: none;
                    color: inherit; cursor: pointer;">
            <app-project-dot [color]="getColor(p.color)" [size]="12" />
            <div style="flex: 1;">
              <div class="script" style="font-size: 22px;">{{ p.name }}</div>
              <div class="mono" style="font-size: 11px; color: var(--mute);">
                {{ counts()[p.id]?.total || 0 }} tâches ·
                {{ counts()[p.id]?.done || 0 }} terminées
              </div>
            </div>
          </a>
        }
      </div>
    </div>
  `,
})
export class ProjectsListComponent implements OnInit {
  private projectService = inject(ProjectService);
  private taskService = inject(TaskService);

  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  allTasks = signal<Task[]>([]);

  activeProjects = computed(() =>
    this.projects().filter(p => !p.isInboxProject).sort((a, b) => a.order - b.order)
  );

  counts = computed(() => {
    const out: Record<string, { total: number; done: number }> = {};
    for (const p of this.projects()) out[p.id] = { total: 0, done: 0 };
    for (const t of this.allTasks()) {
      if (!t.projectId || !out[t.projectId]) continue;
      out[t.projectId].total++;
      if (t.isCompleted) out[t.projectId].done++;
    }
    return out;
  });

  ngOnInit(): void {
    this.taskService.getTasks({ showCompleted: true }).subscribe(t => this.allTasks.set(t));
  }

  getColor = getColor;
}
