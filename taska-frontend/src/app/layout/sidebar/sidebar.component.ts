import { Component, OnInit, computed, inject, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ProjectService, ReorderItem } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { FilterService } from '../../core/services/filter.service';
import { ThemeService } from '../../core/services/theme.service';
import { VersionService } from '../../core/services/version.service';
import { Filter, getColor, Label, Project, Task } from '../../core/models';
import { toSignal } from '@angular/core/rxjs-interop';
import { ProjectEditModalComponent, ProjectEditResult } from '../../shared/components/project-edit-modal/project-edit-modal.component';

export interface ProjectNode {
  project: Project;
  children: ProjectNode[];
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, FormsModule, DragDropModule, ProjectEditModalComponent],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit {
  taskDetailRequested = output<Task>();

  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);
  private filterService = inject(FilterService);
  themeService = inject(ThemeService);
  private versionService = inject(VersionService);

  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  filters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  appVersion = toSignal(this.versionService.getVersion(), { initialValue: '...' });
  inboxProject = computed(() => this.projects().find(p => p.isInboxProject));

  favoriteProjects = computed(() =>
    this.projects().filter(p => p.isFavorite && !p.isInboxProject)
  );
  favoriteLabels = computed(() => this.labels().filter(l => l.isFavorite));
  favoriteFilters = computed(() => this.filters().filter(f => f.isFavorite));

  projectTree = computed((): ProjectNode[] => {
    const all = this.projects().filter(p => !p.isInboxProject);
    const rootProjects = all.filter(p => !p.parentId).sort((a, b) => a.order - b.order);
    return rootProjects.map(p => this.buildNode(p, all));
  });

  showAddProject = signal(false);
  newProjectName = signal('');
  newProjectColor = signal('charcoal');
  collapsedProjects = signal(new Set<string>());
  editingProject = signal<Project | null>(null);
  sidebarCollapsed = signal(false);

  ngOnInit(): void {}

  getColor = getColor;

  private buildNode(project: Project, all: Project[]): ProjectNode {
    const children = all
      .filter(p => p.parentId === project.id)
      .sort((a, b) => a.order - b.order)
      .map(p => this.buildNode(p, all));
    return { project, children };
  }

  isCollapsed(id: string): boolean {
    return this.collapsedProjects().has(id);
  }

  toggleCollapse(id: string): void {
    this.collapsedProjects.update(set => {
      const next = new Set(set);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  openEdit(project: Project, e: Event): void {
    e.preventDefault();
    e.stopPropagation();
    this.editingProject.set(project);
  }

  onEditSaved(result: ProjectEditResult): void {
    const project = this.editingProject();
    if (!project) return;
    this.projectService.updateProject(project.id, {
      name: result.name,
      color: result.color,
      parentId: result.parentId,
      clearParent: result.clearParent,
      isFavorite: result.isFavorite,
    } as any).subscribe(() => {
      this.projectService.loadProjects().subscribe();
    });
    this.editingProject.set(null);
  }

  onEditDeleted(): void {
    const project = this.editingProject();
    if (!project) return;
    this.projectService.deleteProject(project.id).subscribe();
    this.editingProject.set(null);
  }

  createProject(): void {
    const name = this.newProjectName().trim();
    if (!name) return;
    this.projectService.createProject({ name, color: this.newProjectColor() }).subscribe(() => {
      this.newProjectName.set('');
      this.showAddProject.set(false);
    });
  }

  cancelAdd(): void {
    this.showAddProject.set(false);
    this.newProjectName.set('');
  }

  onRootDrop(event: CdkDragDrop<ProjectNode[]>): void {
    const nodes = [...this.projectTree()];
    moveItemInArray(nodes, event.previousIndex, event.currentIndex);
    const items: ReorderItem[] = nodes.map((n, i) => ({ id: n.project.id, order: i }));
    this.projectService.reorderProjects(items).subscribe();
  }

  onChildDrop(event: CdkDragDrop<ProjectNode[]>, parentId: string): void {
    const parentNode = this.projectTree().find(n => n.project.id === parentId);
    if (!parentNode) return;
    const children = [...parentNode.children];
    moveItemInArray(children, event.previousIndex, event.currentIndex);
    const items: ReorderItem[] = children.map((n, i) => ({ id: n.project.id, order: i }));
    this.projectService.reorderProjects(items).subscribe();
  }
}
