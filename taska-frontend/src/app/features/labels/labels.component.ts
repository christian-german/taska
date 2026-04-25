import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { LabelService } from '../../core/services/label.service';
import { Label, PROJECT_COLORS, getColor } from '../../core/models';

@Component({
  selector: 'app-labels',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="max-w-2xl mx-auto px-8 py-8">

      <!-- Header -->
      <div class="flex items-center gap-3 mb-8">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
        </svg>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Labels</h1>
        <span class="text-sm text-gray-400">{{ labels().length }}</span>
      </div>

      <!-- Labels list -->
      <div class="space-y-1 mb-6">
        @for (label of labels(); track label.id) {
          @if (editingId() === label.id) {
            <!-- Edit row -->
            <div class="border border-gray-300 dark:border-gray-600 rounded-xl p-4 bg-white dark:bg-gray-800 shadow-sm">
              <div class="mb-3">
                <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Name</label>
                <input [ngModel]="editName()" (ngModelChange)="editName.set($event)"
                  (keydown.enter)="saveEdit(label)" (keydown.escape)="cancelEdit()"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                         bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                         focus:outline-none focus:ring-2 focus:ring-red-500" />
              </div>

              <div class="mb-3">
                <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Color</label>
                <div class="flex flex-wrap gap-2">
                  @for (key of colorKeys; track key) {
                    <button (click)="editColor.set(key)"
                      [style.background-color]="getColor(key)"
                      [class]="editColor() === key ? 'ring-2 ring-offset-2 ring-gray-400 dark:ring-offset-gray-800 scale-110' : 'hover:scale-105'"
                      class="w-6 h-6 rounded-full transition-transform flex-shrink-0">
                      @if (editColor() === key) {
                        <svg xmlns="http://www.w3.org/2000/svg" class="w-full h-full p-0.5 text-white" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                        </svg>
                      }
                    </button>
                  }
                </div>
              </div>

              <div class="flex items-center justify-between mb-4">
                <span class="text-sm text-gray-700 dark:text-gray-300">Add to favorites</span>
                <button (click)="editFavorite.set(!editFavorite())"
                  [class]="editFavorite() ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
                  class="relative w-10 h-5 rounded-full transition-colors">
                  <span [class]="editFavorite() ? 'translate-x-5' : 'translate-x-0.5'"
                    class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
                </button>
              </div>

              <div class="flex gap-2">
                <button (click)="saveEdit(label)"
                  [disabled]="!editName().trim()"
                  class="px-4 py-1.5 text-xs font-medium bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 transition-colors">
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
              <span class="w-4 h-4 rounded-full flex-shrink-0" [style.background-color]="getColor(label.color)"></span>
              <span class="flex-1 text-sm text-gray-800 dark:text-gray-200">{{ label.name }}</span>

              <!-- Favorite star -->
              <button (click)="toggleFavorite(label)"
                class="p-1 rounded transition-colors"
                [class]="label.isFavorite ? 'text-yellow-400' : 'text-gray-300 opacity-0 group-hover:opacity-100 hover:text-yellow-400'"
                [title]="label.isFavorite ? 'Remove from favorites' : 'Add to favorites'">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20"
                  [attr.fill]="label.isFavorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.5">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
              </button>

              <!-- Edit button -->
              <button (click)="startEdit(label)"
                class="opacity-0 group-hover:opacity-100 p-1 rounded text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </button>

              <!-- Delete button -->
              <button (click)="deleteLabel(label)"
                class="opacity-0 group-hover:opacity-100 p-1 rounded text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </div>
          }
        }

        @if (labels().length === 0 && !showAdd()) {
          <p class="text-sm text-gray-400 dark:text-gray-500 text-center py-8">No labels yet. Create one below.</p>
        }
      </div>

      <!-- Add label form -->
      @if (showAdd()) {
        <div class="border border-gray-300 dark:border-gray-600 rounded-xl p-4 bg-white dark:bg-gray-800 shadow-sm">
          <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">New label</h3>

          <div class="mb-3">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Name</label>
            <input [ngModel]="newName()" (ngModelChange)="newName.set($event)"
              (keydown.enter)="createLabel()" (keydown.escape)="showAdd.set(false)"
              placeholder="Label name"
              autofocus
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                     bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                     focus:outline-none focus:ring-2 focus:ring-red-500" />
          </div>

          <div class="mb-3">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5 uppercase tracking-wide">Color</label>
            <div class="flex flex-wrap gap-2">
              @for (key of colorKeys; track key) {
                <button (click)="newColor.set(key)"
                  [style.background-color]="getColor(key)"
                  [class]="newColor() === key ? 'ring-2 ring-offset-2 ring-gray-400 dark:ring-offset-gray-800 scale-110' : 'hover:scale-105'"
                  class="w-6 h-6 rounded-full transition-transform flex-shrink-0">
                  @if (newColor() === key) {
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-full h-full p-0.5 text-white" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  }
                </button>
              }
            </div>
          </div>

          <div class="flex items-center justify-between mb-4">
            <span class="text-sm text-gray-700 dark:text-gray-300">Add to favorites</span>
            <button (click)="newFavorite.set(!newFavorite())"
              [class]="newFavorite() ? 'bg-red-500' : 'bg-gray-200 dark:bg-gray-600'"
              class="relative w-10 h-5 rounded-full transition-colors">
              <span [class]="newFavorite() ? 'translate-x-5' : 'translate-x-0.5'"
                class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-transform block"></span>
            </button>
          </div>

          <div class="flex gap-2">
            <button (click)="createLabel()"
              [disabled]="!newName().trim()"
              class="px-4 py-1.5 text-xs font-medium bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 transition-colors">
              Create label
            </button>
            <button (click)="showAdd.set(false)"
              class="px-4 py-1.5 text-xs text-gray-500 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
              Cancel
            </button>
          </div>
        </div>
      } @else {
        <button (click)="showAdd.set(true)"
          class="flex items-center gap-2 text-sm text-gray-400 hover:text-red-500 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
          </svg>
          Add label
        </button>
      }
    </div>
  `
})
export class LabelsComponent implements OnInit {
  private labelService = inject(LabelService);

  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  readonly colorKeys = Object.keys(PROJECT_COLORS);
  readonly getColor = getColor;

  showAdd = signal(false);
  newName = signal('');
  newColor = signal('charcoal');
  newFavorite = signal(false);

  editingId = signal<string | null>(null);
  editName = signal('');
  editColor = signal('charcoal');
  editFavorite = signal(false);

  ngOnInit(): void {}

  createLabel(): void {
    const name = this.newName().trim();
    if (!name) return;
    this.labelService.createLabel({
      name,
      color: this.newColor(),
      isFavorite: this.newFavorite(),
    }).subscribe(() => {
      this.newName.set('');
      this.newColor.set('charcoal');
      this.newFavorite.set(false);
      this.showAdd.set(false);
    });
  }

  startEdit(label: Label): void {
    this.editingId.set(label.id);
    this.editName.set(label.name);
    this.editColor.set(label.color);
    this.editFavorite.set(label.isFavorite);
  }

  saveEdit(label: Label): void {
    const name = this.editName().trim();
    if (!name) return;
    this.labelService.updateLabel(label.id, {
      name,
      color: this.editColor(),
      isFavorite: this.editFavorite(),
    } as any).subscribe();
    this.editingId.set(null);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  toggleFavorite(label: Label): void {
    this.labelService.updateLabel(label.id, {
      name: label.name,
      color: label.color,
      isFavorite: !label.isFavorite,
    } as any).subscribe();
  }

  deleteLabel(label: Label): void {
    this.labelService.deleteLabel(label.id).subscribe();
  }
}
