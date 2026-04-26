import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { QuickAddComponent } from './shared/components/quick-add/quick-add.component';
import { TaskDetailComponent } from './features/task-detail/task-detail.component';
import { ThemeService } from './core/services/theme.service';
import { ProjectService } from './core/services/project.service';
import { LabelService } from './core/services/label.service';
import { FilterService } from './core/services/filter.service';
import { Task } from './core/models';
import {OidcSecurityService} from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SidebarComponent, QuickAddComponent, TaskDetailComponent],
  template: `
    <div class="h-screen flex overflow-hidden bg-white text-gray-900 dark:bg-gray-900 dark:text-gray-100">
      <app-sidebar class="flex-shrink-0" (taskDetailRequested)="openTaskDetail($event)" />
      <main class="flex-1 overflow-auto relative">
        <router-outlet />
      </main>

      @if (selectedTask()) {
        <app-task-detail
          [task]="selectedTask()!"
          (close)="selectedTask.set(null)"
          (taskUpdated)="selectedTask.set($event)"
          (taskDeleted)="selectedTask.set(null)" />
      }

      @if (showQuickAdd()) {
        <app-quick-add (close)="showQuickAdd.set(false)" />
      }
    </div>
  `
})
export class App implements OnInit {
  private oidcSecurityService = inject(OidcSecurityService);
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);
  private filterService = inject(FilterService);
  themeService = inject(ThemeService);

  isAuthenticated = signal(false);
  showQuickAdd = signal(false);
  selectedTask = signal<Task | null>(null);

  constructor() {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated.set(isAuthenticated);
    });
  }

  ngOnInit(): void {
    this.projectService.loadProjects().subscribe();
    this.labelService.loadLabels().subscribe();
    this.filterService.loadFilters().subscribe();
  }

  openTaskDetail(task: Task): void {
    this.selectedTask.set(task);
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    const isInput = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable;

    if (event.key === 'q' && !event.ctrlKey && !event.metaKey && !isInput) {
      event.preventDefault();
      this.showQuickAdd.set(true);
    }
    if (event.key === 'Escape') {
      this.showQuickAdd.set(false);
      this.selectedTask.set(null);
    }
  }
  
  logout() {
    this.oidcSecurityService.logoff().subscribe();
  }
}
