import {Component, DestroyRef, OnInit, computed, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { FilterService } from '../../core/services/filter.service';
import { TaskService } from '../../core/services/task.service';
import { ThemeService } from '../../core/services/theme.service';
import { VersionService } from '../../core/services/version.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { Filter, Label, Project, Task, getColor, isOverdue } from '../../core/models';
import { APP_VERSION } from '../../core/constants/app-version';
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
  host: { style: 'display: block; height: 100%; min-height: 0; overflow: hidden;' },
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
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);
  private filterService = inject(FilterService);
  private taskService = inject(TaskService);
  private versionService = inject(VersionService);
  ui = inject(UiStateService);
  themeService = inject(ThemeService);

  private userData = toSignal(
    this.oidcSecurityService.userData$.pipe(map(({ userData }) => userData)),
    { initialValue: null as any }
  );

  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  filters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  apiVersion = toSignal(this.versionService.getVersion(), { initialValue: '...' });

  readonly frontendVersion = APP_VERSION;

  showProjectModal = signal(false);
  editingProject = signal<Project | null>(null);
  showUserMenu = signal(false);
  showAboutModal = signal(false);
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
    const inWeek = (dueAt?: string | null) => {
      if (!dueAt) return false;
      const d = new Date(dueAt).getTime();
      const now = Date.now();
      return d >= now - 86400000 && d <= now + 7 * 86400000;
    };

    const inboxId = this.projects().find(p => p.isInboxProject)?.id;
    const inbox = tasks.filter(t => !t.isCompleted && t.projectId === inboxId).length;
    const today = tasks.filter(t =>
      !t.isCompleted &&
      t.dueAt &&
      (t.dueAt.slice(0, 10) <= todayStr || isOverdue(t))
    ).length;
    const week = tasks.filter(t => !t.isCompleted && inWeek(t.dueAt)).length;
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
      { id: 'time', label: 'Time tracker', icon: 'clock', route: '/time', count: 0 },
    ];
  });

  ngOnInit(): void {
    this.refreshAllTasks();
    // Fermer la sidebar sur mobile lors d'une navigation
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.ui.sidebarOpen.set(false));
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

  openAbout(): void {
    this.showUserMenu.set(false);
    this.showAboutModal.set(true);
  }

  userName = computed(() => {
    const d = this.userData();
    return d?.name ?? d?.given_name ?? d?.preferred_username ?? d?.email ?? '?';
  });

  userInitial = computed(() => this.userName().charAt(0).toUpperCase());
}
