import { Component, OnInit, inject, input, signal, computed, effect } from '@angular/core';
import { CdkDragDrop, moveItemInArray, DragDropModule } from '@angular/cdk/drag-drop';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { TaskItemComponent } from '../../shared/components/task-item/task-item.component';
import { AddTaskFormComponent } from '../../shared/components/add-task-form/add-task-form.component';
import { TaskDetailComponent } from '../task-detail/task-detail.component';
import { DisplayPanelComponent } from '../../shared/components/display-panel/display-panel.component';
import { CalendarViewComponent } from '../../shared/components/calendar-view/calendar-view.component';
import { ProjectService } from '../../core/services/project.service';
import { SectionService } from '../../core/services/section.service';
import { TaskService } from '../../core/services/task.service';
import { Project, Section, Task, ViewStyle, getColor } from '../../core/models';

@Component({
  selector: 'app-project-view',
  imports: [FormsModule, DragDropModule, TaskItemComponent, AddTaskFormComponent, TaskDetailComponent, DisplayPanelComponent, CalendarViewComponent],
  templateUrl: './project-view.component.html'
})
export class ProjectViewComponent implements OnInit {
  id = input<string>('');

  private projectService = inject(ProjectService);
  private sectionService = inject(SectionService);
  private taskService = inject(TaskService);

  private allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  project = computed(() => this.allProjects().find(p => p.id === this.id()) ?? null);

  sections = signal<Section[]>([]);
  tasks = signal<Task[]>([]);
  selectedTask = signal<Task | null>(null);
  viewStyle = signal<ViewStyle>('LIST');
  showCompleted = signal(false);
  showDisplayPanel = signal(false);
  showAddSection = signal(false);
  newSectionName = signal('');
  collapsedSections = new Set<string>();
  getColor = getColor;

  activeTasks = computed(() => this.tasks().filter(t => !t.isCompleted));
  completedTasks = computed(() => this.tasks().filter(t => t.isCompleted));
  unassignedTasks = computed(() => this.activeTasks().filter(t => !t.sectionId));
  sectionListIds = computed(() => ['no-section', ...this.sections().map(s => s.id)]);

  private lastLoadedId = '';

  constructor() {
    effect(() => {
      const id = this.id();
      if (!id || id === this.lastLoadedId) return;
      this.lastLoadedId = id;
      const proj = this.project();
      if (proj) this.viewStyle.set(proj.viewStyle ?? 'LIST');
      this.load(id);
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {}

  private load(id: string): void {
    this.projectService.getProjectSections(id).subscribe(s => this.sections.set(s));
    this.loadTasks(id);
  }

  private loadTasks(id: string): void {
    this.taskService.getTasks({ projectId: id, showCompleted: this.showCompleted() }).subscribe(t => this.tasks.set(t));
  }

  getTasksForSection(sectionId: string): Task[] {
    return this.activeTasks().filter(t => t.sectionId === sectionId);
  }

  isSectionCollapsed(sectionId: string): boolean {
    return this.collapsedSections.has(sectionId);
  }

  toggleSection(sectionId: string): void {
    if (this.collapsedSections.has(sectionId)) {
      this.collapsedSections.delete(sectionId);
    } else {
      this.collapsedSections.add(sectionId);
    }
  }

  setViewStyle(style: ViewStyle): void {
    this.viewStyle.set(style);
    const proj = this.project();
    if (proj) {
      this.projectService.updateProject(proj.id, {
        viewStyle: style,
        parentId: proj.parentId,
      } as any).subscribe();
    }
  }

  setShowCompleted(value: boolean): void {
    this.showCompleted.set(value);
    if (this.id()) this.loadTasks(this.id());
  }

  toggleDisplayPanel(e: Event): void {
    e.stopPropagation();
    this.showDisplayPanel.set(!this.showDisplayPanel());
  }

  onTaskCreated(task: Task): void {
    this.tasks.update(t => [...t, task]);
  }

  completeTask(task: Task): void {
    this.taskService.closeTask(task.id).subscribe(updated => {
      if (this.showCompleted()) {
        this.tasks.update(t => t.map(t2 => t2.id === task.id ? updated : t2));
      } else {
        this.tasks.update(t => t.filter(t2 => t2.id !== task.id));
      }
    });
  }

  reopenTask(task: Task): void {
    this.taskService.reopenTask(task.id).subscribe(updated => {
      this.tasks.update(t => t.map(t2 => t2.id === task.id ? updated : t2));
    });
  }

  openDetail(task: Task): void {
    this.selectedTask.set(task);
  }

  onTaskUpdated(task: Task): void {
    this.tasks.update(t => t.map(t2 => t2.id === task.id ? task : t2));
    this.selectedTask.set(task);
  }

  onTaskDeleted(id: string): void {
    this.tasks.update(t => t.filter(t2 => t2.id !== id));
    this.selectedTask.set(null);
  }

  createSection(): void {
    const name = this.newSectionName().trim();
    if (!name || !this.project()) return;
    this.sectionService.createSection({ name, projectId: this.project()!.id, order: this.sections().length }).subscribe(s => {
      this.sections.update(sections => [...sections, s]);
      this.newSectionName.set('');
      this.showAddSection.set(false);
    });
  }

  cancelAddSection(): void {
    this.showAddSection.set(false);
    this.newSectionName.set('');
  }

  onTaskDrop(event: CdkDragDrop<Task[]>, sectionId?: string): void {
    const tasks = sectionId
      ? this.getTasksForSection(sectionId)
      : this.unassignedTasks();
    const mutable = [...tasks];
    moveItemInArray(mutable, event.previousIndex, event.currentIndex);
    mutable.forEach((t, i) => {
      this.taskService.updateTask(t.id, { order: i, sectionId: sectionId ?? undefined }).subscribe();
    });
    this.tasks.update(all => {
      const otherTasks = all.filter(t => sectionId ? t.sectionId !== sectionId : t.sectionId !== undefined);
      return [...otherTasks, ...mutable];
    });
  }

  getProjectColor(): string {
    return getColor(this.project()?.color ?? 'charcoal');
  }
}
