import {ChangeDetectionStrategy, Component, computed, HostListener, inject, OnInit, signal} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {IconComponent} from './shared/components/icon/icon.component';
import {SidebarComponent} from './layout/sidebar/sidebar.component';
import {QuickAddComponent} from './shared/components/quick-add/quick-add.component';
import {TaskDetailComponent} from './features/task-detail/task-detail.component';
import {CommandPaletteComponent} from './shared/components/command-palette/command-palette.component';
import {ShortcutsModalComponent} from './shared/components/shortcuts-modal/shortcuts-modal.component';
import {ProjectService} from './core/services/project.service';
import {LabelService} from './core/services/label.service';
import {FilterService} from './core/services/filter.service';
import {UiStateService} from './core/services/ui-state.service';
import {Project, Task} from './core/models';
import {attachConsole} from '@tauri-apps/plugin-log';
import {onOpenUrl} from '@tauri-apps/plugin-deep-link';
import {UpdateService} from './core/services/update.service';
import {UpdateDialogComponent} from './layout/update-dialog/UpdateDialogComponent';
import {AboutDialogComponent} from './layout/about-dialog/about-dialog.component';
import {AddProjectModalComponent} from './shared/components/add-project-modal/add-project-modal.component';
import {CsvImportModalComponent} from './shared/components/csv-import-modal/csv-import-modal.component';
import {ConfirmDialogComponent} from './shared/components/confirm-dialog/confirm-dialog.component';
import {interval} from 'rxjs';
import {TaskCreatedToastComponent} from './shared/components/task-created-toast/task-created-toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, IconComponent, SidebarComponent, QuickAddComponent, TaskDetailComponent, CommandPaletteComponent, ShortcutsModalComponent, UpdateDialogComponent, AboutDialogComponent, AddProjectModalComponent, CsvImportModalComponent, ConfirmDialogComponent, TaskCreatedToastComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (ui.sidebarOpen()) {
      <div class="sidebar-backdrop" (click)="ui.sidebarOpen.set(false)"></div>
    }

    <button class="sidebar-toggle btn btn-ghost btn-icon"
            (click)="ui.sidebarOpen.set(true)"
            title="Menu">
      <app-icon name="menu" [size]="18"/>
    </button>

    <div class="app" [class.has-detail]="hasDetail()">
      <app-sidebar
        (projectModalRequested)="projectModal.set($event)"
        (csvImportRequested)="csvImportProjectId.set($event)"
        (projectDeleteRequested)="projectPendingDeletion.set($event)" />

      <main class="scroll" (click)="ui.closeTaskDetail()"
            style="overflow-y: auto; display: flex; flex-direction: column;">
        <router-outlet/>
      </main>

      @if (ui.selectedTask(); as task) {
        <app-task-detail
          [task]="task"
          (close)="ui.closeTaskDetail()"
          (taskUpdated)="onTaskUpdated($event)"
          (taskDeleted)="onTaskDeleted($event)"/>
      }
    </div>

    @if (ui.showQuickAdd()) {
      <app-quick-add (close)="ui.showQuickAdd.set(false)"/>
    }
    @if (ui.showPalette()) {
      <app-command-palette (close)="ui.showPalette.set(false)"/>
    }
    @if (ui.showHelp()) {
      <app-shortcuts-modal (close)="ui.showHelp.set(false)"/>
    }
    @if (ui.showAbout()) {
      <app-about-dialog (close)="ui.showAbout.set(false)"/>
    }
    <app-update-dialog/>

    @if (projectModal() !== undefined) {
      <app-add-project-modal [project]="projectModal() ?? null" (close)="projectModal.set(undefined)" />
    }
    @if (csvImportProjectId(); as projectId) {
      <app-csv-import-modal [projectId]="projectId" (close)="csvImportProjectId.set(null)" />
    }
    @if (projectPendingDeletion(); as project) {
      <app-confirm-dialog
        [title]="'Supprimer « ' + project.name + ' » ?'"
        message="Le projet et toutes ses tâches seront supprimés définitivement."
        (confirmed)="deleteProject(project)"
        (cancelled)="projectPendingDeletion.set(null)" />
    }
    <app-task-created-toast/>
  `,
})
export class AppComponent implements OnInit {
  private detachConsole?: () => void;
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);
  private filterService = inject(FilterService);
  private router = inject(Router);
  protected ui = inject(UiStateService);
  private updateService = inject(UpdateService);

  hasDetail = computed(() => this.ui.selectedTask() !== null);
  projectModal = signal<Project | null | undefined>(undefined);
  csvImportProjectId = signal<string | null>(null);
  projectPendingDeletion = signal<Project | null>(null);

  deleteProject(project: Project): void {
    this.projectPendingDeletion.set(null);
    this.projectService.deleteProject(project.id).subscribe();
  }

  ngOnInit(): void {

    // Check if we are running in Tauri.
    const isTauri = typeof window !== 'undefined' && '__TAURI_INTERNALS__' in window;

    if (isTauri) {
      // Enable console attach for Tauri to log to a file.
      attachConsole().then(detach => {
        this.detachConsole = detach;
      });

      // Callback management for the scheme "tauri://"
      onOpenUrl((urls) => {
        const callbackUrl = urls[0];
        if (callbackUrl.includes('code=')) {
          const queryString = callbackUrl.split('?')[1];
          this.router.navigateByUrl(`/callback?${queryString}`);
        }
      }).then();

      // Check update at startup.
      setTimeout(() => this.updateService.checkForUpdates(), 3000);

      // Check update every 5 minutes.
      interval(5 * 60 * 1000).subscribe(() => {
        this.updateService.checkForUpdates().then(
          () => console.log("Update check OK")
        );
      });
    }

    // Load initial data.
    this.projectService.loadProjects().subscribe();
    this.labelService.loadLabels().subscribe();
    this.filterService.loadFilters().subscribe();
  }

  ngOnDestroy() {
    this.detachConsole?.();
  }

  onTaskUpdated(task: Task): void {
    this.ui.selectedTask.set(task);
    this.ui.taskUpdated$.next(task);
  }

  onTaskDeleted(id: string): void {
    this.ui.closeTaskDetail();
    this.ui.taskDeleted$.next(id);
  }

  // Buffered key sequence for "g + t / g + i / g + w / g + s"
  private gBuffer = '';
  private gBufferTime = 0;

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    const isInput =
      target?.tagName === 'INPUT' ||
      target?.tagName === 'TEXTAREA' ||
      target?.isContentEditable;

    // Cmd/Ctrl shortcuts work even in inputs
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.ui.showPalette.set(true);
      return;
    }
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'n') {
      event.preventDefault();
      this.ui.openQuickAdd();
      return;
    }

    if (event.key === 'Escape') {
      this.ui.closeAll();
      return;
    }

    if (isInput) return;

    if (event.key === 'q' && !event.metaKey && !event.ctrlKey) {
      event.preventDefault();
      this.ui.openQuickAdd();
      return;
    }
    if (event.key === '?') {
      this.ui.showHelp.set(true);
      return;
    }

    // g + key sequences
    if (Date.now() - this.gBufferTime > 1000) this.gBuffer = '';
    this.gBuffer += event.key.toLowerCase();
    this.gBufferTime = Date.now();
    if (this.gBuffer.endsWith('gt')) {
      this.router.navigateByUrl('/today');
      this.gBuffer = '';
      return;
    }
    if (this.gBuffer.endsWith('gi')) {
      this.router.navigateByUrl('/inbox');
      this.gBuffer = '';
      return;
    }
    if (this.gBuffer.endsWith('gw')) {
      this.router.navigateByUrl('/week');
      this.gBuffer = '';
      return;
    }
    if (this.gBuffer.endsWith('gs')) {
      this.router.navigateByUrl('/stats');
      this.gBuffer = '';
      return;
    }
    if (this.gBuffer.endsWith('gd')) {
      this.router.navigateByUrl('/done');
      this.gBuffer = '';
      return;
    }
  }
}
