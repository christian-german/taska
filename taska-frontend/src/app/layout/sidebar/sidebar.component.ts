import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { FilterService } from '../../core/services/filter.service';
import { TaskService } from '../../core/services/task.service';
import { ThemeService } from '../../core/services/theme.service';
import { VersionService } from '../../core/services/version.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { Filter, Label, Project, Task, getColor, isOverdue } from '../../core/models';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ProjectDotComponent, TagChipComponent } from '../../shared/components/atoms/atoms.component';

interface SidebarCount {
  inbox: number;
  today: number;
  week: number;
  done: number;
  byProject: Record<string, number>;
}

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    FormsModule,
    IconComponent,
    ProjectDotComponent,
    TagChipComponent,
  ],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  private oidcSecurityService = inject(OidcSecurityService);
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);
  private filterService = inject(FilterService);
  private taskService = inject(TaskService);
  private versionService = inject(VersionService);
  private ui = inject(UiStateService);
  themeService = inject(ThemeService);

  private userData = toSignal(
    this.oidcSecurityService.userData$.pipe(map(({ userData }) => userData)),
    { initialValue: null as any }
  );

  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  filters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  appVersion = toSignal(this.versionService.getVersion(), { initialValue: '...' });

  showAddProject = signal(false);
  newProjectName = signal('');

  allTasks = signal<Task[]>([]);

  activeProjects = computed(() =>
    this.projects()
      .filter(p => !p.isInboxProject)
      .sort((a, b) => a.order - b.order)
  );

  counts = computed<SidebarCount>(() => {
    const tasks = this.allTasks();
    const todayStr = new Date().toISOString().split('T')[0];
    const inWeek = (s?: string) => {
      if (!s) return false;
      const d = new Date(s + 'T00:00:00').getTime();
      const now = Date.now();
      return d >= now - 86400000 && d <= now + 7 * 86400000;
    };

    const inboxId = this.projects().find(p => p.isInboxProject)?.id;
    const inbox = tasks.filter(t => !t.isCompleted && t.projectId === inboxId).length;
    const today = tasks.filter(t =>
      !t.isCompleted &&
      t.dueDate &&
      (t.dueDate <= todayStr || isOverdue(t))
    ).length;
    const week = tasks.filter(t => !t.isCompleted && inWeek(t.dueDate)).length;
    const done = tasks.filter(t => t.isCompleted).length;

    const byProject: Record<string, number> = {};
    this.projects().forEach(p => {
      byProject[p.id] = tasks.filter(t => t.projectId === p.id && !t.isCompleted).length;
    });

    return { inbox, today, week, done, byProject };
  });

  viewItems = computed(() => {
    const c = this.counts();
    return [
      { id: 'inbox', label: 'Inbox', icon: 'inbox', route: '/inbox', count: c.inbox },
      { id: 'today', label: "Aujourd'hui", icon: 'star', route: '/today', count: c.today },
      { id: 'week', label: 'Semaine', icon: 'calendar', route: '/week', count: c.week },
      { id: 'done', label: 'Terminées', icon: 'check', route: '/done', count: 0 },
    ];
  });

  ngOnInit(): void {
    this.refreshAllTasks();
  }

  private refreshAllTasks(): void {
    this.taskService.getTasks({ showCompleted: true }).subscribe(t => this.allTasks.set(t));
  }

  getColor = getColor;

  openSearch(): void {
    this.ui.showPalette.set(true);
  }

  createProject(): void {
    const name = this.newProjectName().trim();
    if (!name) return;
    this.projectService.createProject({ name, color: 'charcoal' }).subscribe(() => {
      this.newProjectName.set('');
      this.showAddProject.set(false);
    });
  }

  cancelAddProject(): void {
    this.showAddProject.set(false);
    this.newProjectName.set('');
  }

  userName = computed(() => {
    const d = this.userData();
    return d?.name ?? d?.given_name ?? d?.preferred_username ?? d?.email ?? '?';
  });

  userInitial = computed(() => this.userName().charAt(0).toUpperCase());
}
