import { Component, HostListener, OnInit, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PROJECT_COLORS, Project, getColor } from '../../../core/models';

export interface ProjectEditResult {
  name: string;
  color: string;
  parentId?: string;
  clearParent: boolean;
  isFavorite: boolean;
}

@Component({
  selector: 'app-project-edit-modal',
  standalone: true,
  imports: [FormsModule],
  template: `
    <!-- Backdrop -->
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4"
      (click)="cancelled.emit()">

      <!-- Modal -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-md border border-gray-200 dark:border-gray-700"
        (click)="$event.stopPropagation()">

        <div class="p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-5">Edit project</h2>

          <!-- Name -->
          <div class="mb-4">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Name</label>
            <input [ngModel]="name()" (ngModelChange)="name.set($event)"
              (keydown.enter)="save()" (keydown.escape)="cancelled.emit()"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                     bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                     focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent" />
          </div>

          <!-- Color -->
          <div class="mb-4">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Color</label>
            <div class="flex flex-wrap gap-2">
              @for (key of colorKeys; track key) {
                <button (click)="color.set(key)"
                  [style.background-color]="getColor(key)"
                  [class]="color() === key ? 'ring-2 ring-offset-2 ring-gray-400 dark:ring-offset-gray-800 scale-110' : 'hover:scale-105'"
                  class="w-6 h-6 rounded-full transition-transform flex-shrink-0"
                  [title]="key">
                  @if (color() === key) {
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-full h-full p-0.5 text-white" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  }
                </button>
              }
            </div>
          </div>

          <!-- Parent project -->
          <div class="mb-4">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Parent project</label>
            <select [ngModel]="parentIdStr()" (ngModelChange)="setParentFromStr($event)"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                     bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                     focus:outline-none focus:ring-2 focus:ring-red-500">
              <option value="none">No parent</option>
              @for (p of availableParents(); track p.id) {
                <option [value]="p.id">{{ p.name }}</option>
              }
            </select>
          </div>

          <!-- Favorite -->
          <div class="mb-6">
            <label class="flex items-center justify-between cursor-pointer">
              <span class="text-sm text-gray-700 dark:text-gray-300">Add to favorites</span>
              <button (click)="isFavorite.set(!isFavorite())"
                [class]="isFavorite() ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
                class="relative w-10 h-5 rounded-full transition-colors flex-shrink-0">
                <span [class]="isFavorite() ? 'translate-x-5' : 'translate-x-0.5'"
                  class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
              </button>
            </label>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-3">
            <button (click)="save()"
              [disabled]="!name().trim()"
              class="flex-1 py-2 bg-red-500 text-white text-sm font-medium rounded-lg hover:bg-red-600
                     disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              Save changes
            </button>
            <button (click)="cancelled.emit()"
              class="flex-1 py-2 text-sm text-gray-600 dark:text-gray-400 border border-gray-300 dark:border-gray-600
                     rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              Cancel
            </button>
            @if (!project().isInboxProject) {
              <button (click)="deleted.emit()"
                class="py-2 px-3 text-sm text-red-500 border border-red-200 dark:border-red-900 rounded-lg
                       hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                Delete
              </button>
            }
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProjectEditModalComponent implements OnInit {
  project = input.required<Project>();
  allProjects = input.required<Project[]>();
  saved = output<ProjectEditResult>();
  deleted = output<void>();
  cancelled = output<void>();

  name = signal('');
  color = signal('charcoal');
  parentId = signal<string | null>(null);
  isFavorite = signal(false);

  readonly colorKeys = Object.keys(PROJECT_COLORS);
  readonly getColor = getColor;

  availableParents = computed(() =>
    this.allProjects().filter(p =>
      p.id !== this.project().id &&
      !p.isInboxProject &&
      !this.isDescendant(p.id, this.project().id)
    )
  );

  parentIdStr = computed(() => this.parentId() ?? 'none');

  ngOnInit(): void {
    const p = this.project();
    this.name.set(p.name);
    this.color.set(p.color);
    this.parentId.set(p.parentId ?? null);
    this.isFavorite.set(p.isFavorite);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cancelled.emit();
  }

  setParentFromStr(val: string): void {
    this.parentId.set(val === 'none' ? null : val);
  }

  save(): void {
    if (!this.name().trim()) return;
    const parentId = this.parentId();
    this.saved.emit({
      name: this.name().trim(),
      color: this.color(),
      parentId: parentId ?? undefined,
      clearParent: parentId === null,
      isFavorite: this.isFavorite(),
    });
  }

  private isDescendant(projectId: string, ancestorId: string): boolean {
    const proj = this.allProjects().find(p => p.id === projectId);
    if (!proj?.parentId) return false;
    if (proj.parentId === ancestorId) return true;
    return this.isDescendant(proj.parentId, ancestorId);
  }
}
