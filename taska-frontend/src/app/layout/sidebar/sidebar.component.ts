import {Component, OnInit, computed, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
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
import { AddProjectModalComponent } from '../../shared/components/add-project-modal/add-project-modal.component';
import { CsvImportModalComponent } from '../../shared/components/csv-import-modal/csv-import-modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

interface SidebarCount {
  inbox: number;
  today: number;
  week: number;
  done: number;
  byProject: Record<string, number>;
}

interface ProjectNode {
  project: Project;
  depth: number;
  hasChildren: boolean;
}

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    IconComponent,
    ProjectDotComponent,
    TagChipComponent,
    AddProjectModalComponent,
    CsvImportModalComponent,
    ConfirmDialogComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
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

  showProjectModal = signal(false);
  editingProject = signal<Project | null>(null);
  showUserMenu = signal(false);
  hoveredProjectId = signal<string | null>(null);
  activeMenuId = signal<string | null>(null);
  deletingProject = signal<Project | null>(null);
  importProjectId = signal<string | null>(null);

  allTasks = signal<Task[]>([]);
  collapsedProjectIds = signal<Set<string>>(new Set());

  activeProjects = computed(() =>
    this.projects()
      .filter(p => !p.isInboxProject)
      .sort((a, b) => a.order - b.order)
  );

  projectTree = computed<ProjectNode[]>(() => {
    const projects = this.activeProjects();
    const collapsed = this.collapsedProjectIds();
    const allIds = new Set(projects.map(p => p.id));

    const byParent = new Map<string, Project[]>();
    for (const p of projects) {
      const key = (p.parentId && allIds.has(p.parentId)) ? p.parentId : '';
      const list = byParent.get(key) ?? [];
      list.push(p);
      byParent.set(key, list);
    }

    const result: ProjectNode[] = [];
    const addNodes = (parentId: string, depth: number): void => {
      for (const p of byParent.get(parentId) ?? []) {
        const hasChildren = (byParent.get(p.id)?.length ?? 0) > 0;
        result.push({ project: p, depth, hasChildren });
        if (hasChildren && !collapsed.has(p.id)) {
          addNodes(p.id, depth + 1);
        }
      }
    };
    addNodes('', 0);
    return result;
  });

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

  openCreateProject(): void {
    this.editingProject.set(null);
    this.showProjectModal.set(true);
  }

  openEditProject(p: Project, e: Event): void {
    e.stopPropagation();
    e.preventDefault();
    this.activeMenuId.set(null);
    this.editingProject.set(p);
    this.showProjectModal.set(true);
  }

  openImportCsv(id: string, e: Event): void {
    e.stopPropagation();
    e.preventDefault();
    this.activeMenuId.set(null);
    this.importProjectId.set(id);
  }

  requestDeleteProject(p: Project, e: Event): void {
    e.stopPropagation();
    e.preventDefault();
    this.activeMenuId.set(null);
    this.deletingProject.set(p);
  }

  confirmDeleteProject(): void {
    const p = this.deletingProject();
    if (!p) return;
    this.deletingProject.set(null);
    this.projectService.deleteProject(p.id).subscribe();
  }

  toggleProjectMenu(id: string, e: Event): void {
    e.stopPropagation();
    e.preventDefault();
    this.activeMenuId.set(this.activeMenuId() === id ? null : id);
  }

  toggleCollapse(id: string, e: Event): void {
    e.stopPropagation();
    e.preventDefault();
    this.collapsedProjectIds.update(set => {
      const next = new Set(set);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  }

  onProjectLeave(id: string): void {
    this.hoveredProjectId.set(null);
    // keep menu open even after mouse leaves
  }

  logout(): void {
    this.oidcSecurityService.logoff().subscribe();
  }

  userName = computed(() => {
    const d = this.userData();
    return d?.name ?? d?.given_name ?? d?.preferred_username ?? d?.email ?? '?';
  });

  userInitial = computed(() => this.userName().charAt(0).toUpperCase());
}
