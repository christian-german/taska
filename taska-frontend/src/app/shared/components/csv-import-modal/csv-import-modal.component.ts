import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { from } from 'rxjs';
import { concatMap } from 'rxjs/operators';
import { TaskService } from '../../../core/services/task.service';
import { UiStateService } from '../../../core/services/ui-state.service';
import { IconComponent } from '../icon/icon.component';

interface ParsedTask {
  content: string;
  description?: string;
  priority: 1 | 2 | 3 | 4;
  scheduledAt?: string;
}

const EN_MONTHS: Record<string, number> = {
  jan: 1, feb: 2, mar: 3, apr: 4, may: 5, jun: 6,
  jul: 7, aug: 8, sep: 9, oct: 10, nov: 11, dec: 12,
};

function parseTodoistDate(raw: string): string | undefined {
  if (!raw.trim()) return undefined;
  const parts = raw.trim().split(/\s+/);
  const month = EN_MONTHS[parts[0]?.toLowerCase().slice(0, 3)];
  const day = parseInt(parts[1] ?? '');
  if (!month || !day) return undefined;
  const year = parts.length >= 3 ? parseInt(parts[2]) : new Date().getFullYear();
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

function parseCsv(raw: string): string[][] {
  const text = raw.replace(/^﻿/, '');
  const rows: string[][] = [];
  let i = 0;
  const n = text.length;

  while (i < n) {
    const row: string[] = [];
    let endOfRow = false;

    while (i < n && !endOfRow) {
      let field = '';
      if (text[i] === '"') {
        i++;
        while (i < n) {
          if (text[i] === '"' && text[i + 1] === '"') { field += '"'; i += 2; }
          else if (text[i] === '"') { i++; break; }
          else { field += text[i++]; }
        }
        if (i < n && text[i] === ',') i++;
        else { while (i < n && (text[i] === '\r' || text[i] === '\n')) i++; endOfRow = true; }
      } else {
        while (i < n && text[i] !== ',' && text[i] !== '\r' && text[i] !== '\n') field += text[i++];
        if (i < n && text[i] === ',') i++;
        else { while (i < n && (text[i] === '\r' || text[i] === '\n')) i++; endOfRow = true; }
      }
      row.push(field);
    }
    if (row.some(f => f !== '')) rows.push(row);
  }
  return rows;
}

function parseTodoistCsv(text: string): ParsedTask[] {
  const rows = parseCsv(text);
  const tasks: ParsedTask[] = [];
  let current: ParsedTask | null = null;

  for (const row of rows) {
    const type = row[0]?.toLowerCase();
    if (type === 'task') {
      const rawPriority = parseInt(row[4] ?? '1') as 1 | 2 | 3 | 4;
      const priority: 1 | 2 | 3 | 4 = [1, 2, 3, 4].includes(rawPriority) ? rawPriority : 1;
      const parsedDate = parseTodoistDate(row[8] ?? '');
      current = {
        content: row[1] ?? '',
        description: row[2] || undefined,
        priority,
        scheduledAt: parsedDate ? parsedDate + 'T00:00:00' : undefined,
      };
      if (current.content.trim()) tasks.push(current);
    } else if (type === 'note' && current) {
      const noteText = row[1] ?? '';
      if (noteText.trim()) {
        current.description = current.description
          ? `${current.description}\n${noteText}`
          : noteText;
      }
    }
  }
  return tasks;
}

@Component({
  selector: 'app-csv-import-modal',
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()"
           style="width: min(500px, 92vw); padding: 24px 24px 20px;">

        <div style="font-size: 17px; font-weight: 600; color: var(--ink); margin-bottom: 6px;">
          Importer depuis Todoist
        </div>
        <div class="mono" style="font-size: 12px; color: var(--mute); margin-bottom: 18px; line-height: 1.5;">
          Fichier CSV exporté depuis Todoist · sections ignorées · tâches importées telles quelles
        </div>

        @if (!parsedTasks().length && !importing()) {
          <label style="display: flex; flex-direction: column; align-items: center; justify-content: center;
                         gap: 10px; padding: 28px; border: 2px dashed var(--line-2); border-radius: 10px;
                         cursor: pointer; color: var(--mute); transition: border-color .15s;"
                 (dragover)="$event.preventDefault()"
                 (drop)="onDrop($event)">
            <app-icon name="upload" [size]="22" />
            <span style="font-size: 13px;">
              @if (fileName()) {
                {{ fileName() }}
              } @else {
                Cliquer ou déposer un fichier .csv
              }
            </span>
            <input type="file" accept=".csv" style="display: none;"
                   (change)="onFileSelect($event)" />
          </label>
        }

        @if (parsedTasks().length && !importing() && !done()) {
          <div style="margin-bottom: 14px;">
            <div style="font-size: 13px; color: var(--ink); margin-bottom: 10px; font-weight: 500;">
              {{ parsedTasks().length }} tâche{{ parsedTasks().length > 1 ? 's' : '' }} prête{{ parsedTasks().length > 1 ? 's' : '' }} à importer
            </div>
            <div style="max-height: 220px; overflow-y: auto; display: flex; flex-direction: column; gap: 4px;">
              @for (t of previewTasks(); track $index) {
                <div style="display: flex; align-items: flex-start; gap: 8px; padding: 7px 10px;
                             background: var(--bg-2); border-radius: 7px; font-size: 13px;">
                  <span class="mono" style="font-size: 10px; color: var(--mute); flex-shrink: 0; margin-top: 2px;">
                    P{{ t.priority }}
                  </span>
                  <div style="flex: 1; min-width: 0;">
                    <div style="color: var(--ink); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                      {{ t.content }}
                    </div>
                    @if (t.scheduledAt) {
                      <div class="mono" style="font-size: 10.5px; color: var(--mute); margin-top: 2px;">
                        {{ t.scheduledAt.slice(0, 10) }}
                      </div>
                    }
                  </div>
                </div>
              }
              @if (parsedTasks().length > 8) {
                <div style="text-align: center; font-size: 12px; color: var(--mute); padding: 4px;">
                  … et {{ parsedTasks().length - 8 }} de plus
                </div>
              }
            </div>
          </div>
        }

        @if (importing()) {
          <div style="margin-bottom: 14px;">
            <div style="font-size: 13px; color: var(--ink); margin-bottom: 8px;">
              Import en cours… {{ importedCount() }} / {{ parsedTasks().length }}
            </div>
            <div style="height: 6px; background: var(--bg-2); border-radius: 3px; overflow: hidden;">
              <div style="height: 100%; background: var(--accent); border-radius: 3px; transition: width .2s;"
                   [style.width.%]="(importedCount() / parsedTasks().length) * 100"></div>
            </div>
          </div>
        }

        @if (done()) {
          <div style="display: flex; align-items: center; gap: 10px; padding: 14px; background: var(--bg-2);
                      border-radius: 8px; color: var(--ink); font-size: 13px; margin-bottom: 14px;">
            <app-icon name="check" [size]="16" color="var(--accent)" />
            {{ parsedTasks().length }} tâche{{ parsedTasks().length > 1 ? 's' : '' }} importée{{ parsedTasks().length > 1 ? 's' : '' }} avec succès.
          </div>
        }

        <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 4px;">
          <button class="btn btn-ghost" (click)="close.emit()" [disabled]="importing()">
            {{ done() ? 'Fermer' : 'Annuler' }}
          </button>
          @if (parsedTasks().length && !importing() && !done()) {
            <button class="btn btn-primary" (click)="runImport()">
              Importer {{ parsedTasks().length }} tâche{{ parsedTasks().length > 1 ? 's' : '' }}
            </button>
          }
        </div>
      </div>
    </div>
  `,
})
export class CsvImportModalComponent {
  projectId = input.required<string>();
  close = output<void>();

  private taskService = inject(TaskService);
  private ui = inject(UiStateService);

  parsedTasks = signal<ParsedTask[]>([]);
  fileName = signal('');
  importing = signal(false);
  importedCount = signal(0);
  done = signal(false);

  previewTasks = computed(() => this.parsedTasks().slice(0, 8));

  onFileSelect(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (file) this.readFile(file);
  }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    const file = e.dataTransfer?.files?.[0];
    if (file) this.readFile(file);
  }

  private readFile(file: File): void {
    this.fileName.set(file.name);
    const reader = new FileReader();
    reader.onload = (ev) => {
      const text = ev.target?.result as string;
      this.parsedTasks.set(parseTodoistCsv(text));
    };
    reader.readAsText(file, 'utf-8');
  }

  runImport(): void {
    const tasks = this.parsedTasks();
    if (!tasks.length) return;
    this.importing.set(true);
    this.importedCount.set(0);

    from(tasks).pipe(
      concatMap(t => this.taskService.createTask({
        content: t.content,
        description: t.description,
        priority: t.priority,
        scheduledAt: t.scheduledAt ?? null,
        allDay: !!t.scheduledAt,
        projectId: this.projectId(),
        labels: [],
      }))
    ).subscribe({
      next: created => {
        this.importedCount.update(n => n + 1);
        this.ui.taskCreated$.next(created);
      },
      complete: () => {
        this.importing.set(false);
        this.done.set(true);
      },
    });
  }
}
