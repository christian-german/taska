import { Component, ElementRef, OnInit, ViewChild, computed, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { NlParserService, NlParsed } from '../../../core/services/nl-parser.service';
import {
  Project,
  fmtRel,
  fmtTime,
  fmtEstimate,
  hexToRgba,
} from '../../../core/models';
import { IconComponent } from '../icon/icon.component';

interface DetectedChip {
  k: string;
  v: string;
  color: string;
}

@Component({
  selector: 'app-quick-add',
  imports: [FormsModule, IconComponent],
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()">
        <div style="padding: 20px 22px 12px;">
          <div class="script" style="font-size: 22px; color: var(--mute); margin-bottom: 10px;">
            nouvelle tâche
          </div>

          <div style="position: relative;">
            <!-- Visible highlight layer -->
            <div aria-hidden="true"
                 style="position: absolute; inset: 0; padding: 10px 12px;
                        font-family: 'Caveat', cursive; font-size: 22px; line-height: 32px;
                        color: var(--ink); white-space: pre-wrap; word-break: break-word; pointer-events: none;">
              @if (input(); as txt) {
                @for (seg of segments(); track $index) {
                  <span [class]="segClass(seg.type)">{{ seg.text }}</span>
                }
              } @else {
                <span style="color: var(--mute);">Finir slide deck Q2 demain 14h #boulot !!</span>
              }
            </div>
            <input #inputEl
                   [ngModel]="input()"
                   (ngModelChange)="input.set($event)"
                   (keydown.enter)="$event.preventDefault(); submit()"
                   (keydown.escape)="close.emit()"
                   style="width: 100%; padding: 10px 12px;
                          font-family: 'Caveat', cursive; font-size: 22px; line-height: 32px;
                          background: transparent; border: 0; outline: none;
                          color: transparent; caret-color: var(--ink);
                          position: relative; z-index: 2;" />
          </div>

          @if (detected().length > 0) {
            <div style="margin-top: 14px; display: flex; flex-wrap: wrap; gap: 6px;
                        align-items: center; font-size: 12px;">
              <span class="mono" style="color: var(--mute); font-size: 11px;">→ détecté:</span>
              @for (d of detected(); track d.k) {
                <span class="chip"
                      [style.background]="rgba(d.color, 0.15)"
                      [style.color]="d.color"
                      style="font-size: 11.5px;">
                  {{ d.v }}
                </span>
              }
            </div>
          }

          <hr class="dash-hr" style="margin: 18px 0 12px;" />

          <div style="display: flex; flex-direction: column; gap: 8px; color: var(--ink-2);">
            <div style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
              <app-icon name="calendar" [size]="14" color="#FF8A3D" />
              <span>{{ dateRowLabel() }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
              <app-icon name="folder" [size]="14" color="#3AA3FF" />
              <span>{{ projectRowLabel() }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
              <app-icon name="grid" [size]="14" color="#FF5E7D" />
              <span>{{ tagRowLabel() }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
              <app-icon name="clock" [size]="14" color="#7AD36B" />
              <span>{{ estimateRowLabel() }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
              <app-icon name="repeat" [size]="14" color="#3AA3FF" />
              <span>{{ recurrenceRowLabel() }}</span>
            </div>
          </div>
        </div>

        <div style="padding: 12px 22px; border-top: 1px solid var(--line);
                    display: flex; justify-content: space-between; align-items: center;">
          <button class="btn btn-ghost" (click)="close.emit()">
            annuler <span class="kbd" style="margin-left: 4px;">esc</span>
          </button>
          <button class="btn btn-primary" (click)="submit()" [disabled]="!parsed().title">
            + ajouter
            <span class="kbd"
                  style="margin-left: 6px; background: rgba(255,255,255,0.15);
                         color: rgba(255,255,255,0.85); border: 0;">↵</span>
          </button>
        </div>
      </div>
    </div>
  `,
})
export class QuickAddComponent implements OnInit {
  close = output<void>();

  @ViewChild('inputEl') inputEl?: ElementRef<HTMLInputElement>;

  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);
  private parser = inject(NlParserService);

  private projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  input = signal('');
  parsed = computed<NlParsed>(() => this.parser.parse(this.input()));
  segments = computed(() => this.parser.segments(this.input(), this.parsed()));

  detected = computed<DetectedChip[]>(() => {
    const p = this.parsed();
    const list: DetectedChip[] = [];
    const dateTok = p.tokens.find(t => t.type === 'date');
    if (dateTok) list.push({ k: 'date', v: dateTok.text, color: '#FF8A3D' });
    if (p.hasTime) {
      const tt = p.tokens.find(t => t.type === 'time');
      if (tt) list.push({ k: 'time', v: tt.text, color: '#FF8A3D' });
    }
    if (p.tags.length) list.push({ k: 'tags', v: p.tags.map(t => '#' + t).join(' '), color: '#3AA3FF' });
    if (p.context) list.push({ k: 'ctx', v: '@' + p.context, color: '#FF5E7D' });
    if (p.priority) {
      const display = (5 - p.priority);
      list.push({ k: 'prio', v: 'P' + display, color: '#E5484D' });
    }
    if (p.estimateMinutes) list.push({ k: 'est', v: fmtEstimate(p.estimateMinutes), color: '#7AD36B' });
    if (p.recurrence) list.push({ k: 'recur', v: p.recurrence, color: '#3AA3FF' });
    return list;
  });

  ngOnInit(): void {
    setTimeout(() => this.inputEl?.nativeElement.focus(), 0);
  }

  segClass(type: string | null): string {
    if (!type) return '';
    return 'nl-token nl-' + type;
  }

  rgba = (hex: string, a: number) => hexToRgba(hex, a);

  dateRowLabel(): string {
    const p = this.parsed();
    if (!p.dueAt) return 'pas de date';
    const d = new Date(p.dueAt);
    return fmtRel(d) + (p.hasTime ? ' · ' + fmtTime(d) : '');
  }

  projectRowLabel(): string {
    const p = this.parsed();
    return p.projectName || 'Inbox';
  }

  tagRowLabel(): string {
    const p = this.parsed();
    const tags = p.tags.length ? p.tags.map(t => '#' + t).join(' ') : 'pas de tag';
    const ctx = p.context ? '  @' + p.context : '';
    return tags + ctx;
  }

  estimateRowLabel(): string {
    const p = this.parsed();
    if (!p.estimateMinutes) return "pas d'estimation";
    return fmtEstimate(p.estimateMinutes) + ' estimées';
  }

  recurrenceRowLabel(): string {
    const p = this.parsed();
    return p.recurrence || 'ne se répète pas';
  }

  submit(): void {
    const p = this.parsed();
    if (!p.title) return;
    const inboxId = this.projects().find(pr => pr.isInboxProject)?.id;
    this.taskService.createTask({
      content: p.title,
      projectId: inboxId,
      labels: p.tags,
      priority: p.priority ?? 1,
      dueDate: p.dueDate,
      dueDateTime: p.dueDateTime,
      mentionContext: p.context,
      estimateMinutes: p.estimateMinutes,
      recurrenceRule: p.recurrence,
      isRecurring: !!p.recurrence,
    } as any).subscribe(() => this.close.emit());
  }
}
