import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { FilterService } from '../../core/services/filter.service';
import { ProjectService } from '../../core/services/project.service';
import { Filter, Project, PROJECT_COLORS, getColor } from '../../core/models';

interface FilterForm {
  name: string;
  color: string;
  projectId: string;
  hasDate: string; // 'any' | 'true' | 'false'
  isFavorite: boolean;
}

@Component({
  selector: 'app-filters',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="h-full flex flex-col overflow-hidden">

      <!-- Header -->
      <div class="px-8 py-5 border-b border-gray-100 dark:border-gray-800 flex items-center gap-3 flex-shrink-0">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
        </svg>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Filters</h1>
        <span class="text-sm text-gray-400">{{ filters().length }}</span>
      </div>

      <div class="flex-1 overflow-auto">
        <div class="max-w-2xl mx-auto px-8 py-6">

      <!-- Filter list -->
      <div class="space-y-1 mb-6">
        @for (filter of filters(); track filter.id) {

          @if (editingId() === filter.id) {
            <!-- Edit form -->
            <div class="border border-gray-300 dark:border-gray-600 rounded-xl p-4 bg-white dark:bg-gray-800 shadow-sm">
              <div class="mb-3">
                <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Name</label>
                <input [(ngModel)]="editForm.name"
                  (keydown.enter)="saveEdit(filter)" (keydown.escape)="cancelEdit()"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                         bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                         focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>

              <div class="mb-3">
                <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Color</label>
                <div class="flex flex-wrap gap-2">
                  @for (entry of colorEntries; track entry.key) {
                    <button type="button" (click)="editForm.color = entry.key"
                      [style.background-color]="entry.value"
                      [class]="editForm.color === entry.key ? 'ring-2 ring-offset-2 ring-gray-400 dark:ring-offset-gray-800 scale-110' : 'hover:scale-105'"
                      class="w-6 h-6 rounded-full transition-transform flex-shrink-0">
                      @if (editForm.color === entry.key) {
                        <svg xmlns="http://www.w3.org/2000/svg" class="w-full h-full p-0.5 text-white" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                        </svg>
                      }
                    </button>
                  }
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3 mb-3">
                <div>
                  <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Project</label>
                  <select [(ngModel)]="editForm.projectId"
                    class="w-full text-sm bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1.5 text-gray-700 dark:text-gray-300 outline-none">
                    <option value="">Any project</option>
                    @for (p of projects(); track p.id) {
                      @if (!p.isInboxProject) {
                        <option [value]="p.id">{{ p.name }}</option>
                      }
                    }
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Due date</label>
                  <select [(ngModel)]="editForm.hasDate"
                    class="w-full text-sm bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1.5 text-gray-700 dark:text-gray-300 outline-none">
                    <option value="any">Any</option>
                    <option value="true">Has due date</option>
                    <option value="false">No due date</option>
                  </select>
                </div>
              </div>

              <div class="flex items-center justify-between mb-4">
                <span class="text-sm text-gray-700 dark:text-gray-300">Add to favorites</span>
                <button type="button" (click)="editForm.isFavorite = !editForm.isFavorite"
                  [class]="editForm.isFavorite ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
                  class="relative w-10 h-5 rounded-full transition-colors">
                  <span [class]="editForm.isFavorite ? 'translate-x-5' : 'translate-x-0.5'"
                    class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
                </button>
              </div>

              <div class="flex gap-2">
                <button (click)="saveEdit(filter)"
                  [disabled]="!editForm.name.trim()"
                  class="px-4 py-1.5 text-xs font-medium bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 disabled:opacity-50 transition-colors">
                  Save
                </button>
                <button (click)="cancelEdit()"
                  class="px-4 py-1.5 text-xs text-gray-500 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
                  Cancel
                </button>
              </div>
            </div>

          } @else {
            <!-- Display row -->
            <div class="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800/60 group transition-colors">
              <span class="w-4 h-4 rounded-full flex-shrink-0" [style.background-color]="getColor(filter.color)"></span>

              <div class="flex-1 min-w-0">
                <a [routerLink]="['/filter', filter.id]"
                  class="text-sm font-medium text-gray-800 dark:text-gray-200 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors">
                  {{ filter.name }}
                </a>
                <div class="flex flex-wrap gap-1.5 mt-0.5">
                  @if (filter.projectId) {
                    <span class="text-xs text-blue-500">{{ getProjectName(filter.projectId) }}</span>
                  }
                  @if (filter.hasDate === true) {
                    <span class="text-xs text-green-500">Has date</span>
                  }
                  @if (filter.hasDate === false) {
                    <span class="text-xs text-gray-400">No date</span>
                  }
                  @if (!filter.projectId && filter.hasDate == null) {
                    <span class="text-xs text-gray-300 dark:text-gray-600">All active tasks</span>
                  }
                </div>
              </div>

              <!-- Favorite star -->
              <button (click)="toggleFavorite(filter)"
                class="p-1 rounded transition-colors"
                [class]="filter.isFavorite ? 'text-yellow-400' : 'text-gray-300 opacity-0 group-hover:opacity-100 hover:text-yellow-400'"
                title="Toggle favorite">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20"
                  [attr.fill]="filter.isFavorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.5">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
              </button>

              <!-- Edit button -->
              <button (click)="startEdit(filter)"
                class="opacity-0 group-hover:opacity-100 p-1 rounded text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </button>

              <!-- Delete button -->
              <button (click)="deleteFilter(filter.id)"
                class="opacity-0 group-hover:opacity-100 p-1 rounded text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </div>
          }
        }

        @if (filters().length === 0) {
          <p class="text-sm text-gray-400 dark:text-gray-500 text-center py-8">No filters yet. Create one below.</p>
        }
      </div>

      <!-- Add button -->
      <button (click)="openAdd()"
        class="flex items-center gap-2 text-sm text-gray-400 hover:text-indigo-500 transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        Add filter
      </button>
        </div>
      </div>
    </div>

    <!-- Create popup -->
    @if (showAdd()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        (click)="closeAdd()">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-md p-6 space-y-4"
          (click)="$event.stopPropagation()">

          <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100">New filter</h3>

          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Name</label>
            <input [ngModel]="newName()" (ngModelChange)="newName.set($event)"
              (keydown.enter)="addFilter()" (keydown.escape)="closeAdd()"
              placeholder="Filter name"
              autofocus
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                     bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                     focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Color</label>
            <div class="flex flex-wrap gap-2">
              @for (entry of colorEntries; track entry.key) {
                <button type="button" (click)="newColor.set(entry.key)"
                  [style.background-color]="entry.value"
                  [class]="newColor() === entry.key ? 'ring-2 ring-offset-2 ring-gray-400 dark:ring-offset-gray-800 scale-110' : 'hover:scale-105'"
                  class="w-6 h-6 rounded-full transition-transform flex-shrink-0">
                  @if (newColor() === entry.key) {
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-full h-full p-0.5 text-white" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  }
                </button>
              }
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Project</label>
              <select [ngModel]="newProjectId()" (ngModelChange)="newProjectId.set($event)"
                class="w-full text-sm bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg px-2 py-1.5 text-gray-700 dark:text-gray-300 outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">Any project</option>
                @for (p of projects(); track p.id) {
                  @if (!p.isInboxProject) {
                    <option [value]="p.id">{{ p.name }}</option>
                  }
                }
              </select>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Due date</label>
              <select [ngModel]="newHasDate()" (ngModelChange)="newHasDate.set($event)"
                class="w-full text-sm bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg px-2 py-1.5 text-gray-700 dark:text-gray-300 outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="any">Any</option>
                <option value="true">Has due date</option>
                <option value="false">No due date</option>
              </select>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-700 dark:text-gray-300">Add to favorites</span>
            <button type="button" (click)="newFavorite.set(!newFavorite())"
              [class]="newFavorite() ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
              class="relative w-10 h-5 rounded-full transition-colors">
              <span [class]="newFavorite() ? 'translate-x-5' : 'translate-x-0.5'"
                class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
            </button>
          </div>

          <div class="flex gap-2 pt-1">
            <button (click)="addFilter()"
              [disabled]="!newName().trim()"
              class="px-4 py-1.5 text-xs font-medium bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              Create filter
            </button>
            <button (click)="closeAdd()"
              class="px-4 py-1.5 text-xs text-gray-500 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
              Cancel
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class FiltersComponent implements OnInit {
  private filterService = inject(FilterService);
  private projectService = inject(ProjectService);

  filters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  editingId = signal<string | null>(null);
  editForm: FilterForm = { name: '', color: 'charcoal', projectId: '', hasDate: 'any', isFavorite: false };

  showAdd = signal(false);
  newName = signal('');
  newColor = signal('charcoal');
  newProjectId = signal('');
  newHasDate = signal('any');
  newFavorite = signal(false);

  readonly colorEntries = Object.entries(PROJECT_COLORS).map(([key, value]) => ({ key, value }));
  readonly getColor = getColor;

  ngOnInit(): void {
    this.filterService.loadFilters().subscribe();
  }

  getProjectName(projectId: string): string {
    return this.projects().find(p => p.id === projectId)?.name ?? 'Unknown';
  }

  openAdd(): void {
    this.newName.set('');
    this.newColor.set('charcoal');
    this.newProjectId.set('');
    this.newHasDate.set('any');
    this.newFavorite.set(false);
    this.showAdd.set(true);
  }

  closeAdd(): void {
    this.showAdd.set(false);
  }

  addFilter(): void {
    const name = this.newName().trim();
    if (!name) return;
    this.filterService.createFilter({
      name,
      color: this.newColor(),
      projectId: this.newProjectId() || undefined,
      hasDate: this.newHasDate() === 'any' ? undefined : this.newHasDate() === 'true',
      isFavorite: this.newFavorite(),
      order: this.filters().length,
    }).subscribe();
    this.closeAdd();
  }

  startEdit(filter: Filter): void {
    this.editingId.set(filter.id);
    this.editForm = {
      name: filter.name,
      color: filter.color,
      projectId: filter.projectId ?? '',
      hasDate: filter.hasDate == null ? 'any' : String(filter.hasDate),
      isFavorite: filter.isFavorite,
    };
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(filter: Filter): void {
    if (!this.editForm.name.trim()) return;
    const hasProjectChanged = (this.editForm.projectId || '') !== (filter.projectId ?? '');
    this.filterService.updateFilter(filter.id, {
      name: this.editForm.name.trim(),
      color: this.editForm.color,
      isFavorite: this.editForm.isFavorite,
      projectId: this.editForm.projectId || undefined,
      clearProject: hasProjectChanged && !this.editForm.projectId ? true : undefined,
      hasDate: this.editForm.hasDate === 'any' ? undefined : this.editForm.hasDate === 'true',
    }).subscribe();
    this.editingId.set(null);
  }

  toggleFavorite(filter: Filter): void {
    this.filterService.updateFilter(filter.id, { isFavorite: !filter.isFavorite }).subscribe();
  }

  deleteFilter(id: string): void {
    this.filterService.deleteFilter(id).subscribe();
  }
}
