import {
  Component,
  ElementRef,
  ViewChild,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  ChangeDetectionStrategy
} from '@angular/core';

const RRULE_LABELS: Record<string, string> = {
  'daily': 'quotidien', 'freq=daily': 'quotidien',
  'weekly': 'hebdomadaire', 'freq=weekly': 'hebdomadaire',
  'monthly': 'mensuel', 'freq=monthly': 'mensuel',
  'yearly': 'annuel', 'freq=yearly': 'annuel',
};
function rruleToLabel(rule: string | null | undefined): string {
  if (!rule) return 'récurrente';
  return RRULE_LABELS[rule.toLowerCase()] ?? rule;
}
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  Project,
  Task,
  Label,
  fmtRel,
  fmtTime,
  fmtEstimate,
  getColor,
  hexToRgba,
  isOverdue,
  getTaskDueDate,
  isTaskAllDay,
} from '../../../core/models';
import { LabelService } from '../../../core/services/label.service';
import { IconComponent } from '../icon/icon.component';
import { CheckboxComponent, PriorityFlagComponent, ProjectDotComponent, TagChipComponent } from '../atoms/atoms.component';

@Component({
  selector: 'app-task-row',
  imports: [FormsModule, IconComponent, CheckboxComponent, PriorityFlagComponent, ProjectDotComponent, TagChipComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="row"
         [class.selected]="selected()"
         [class.done]="task().isCompleted"
         [class.completing]="completing()"
         [attr.data-task-id]="task().id"
         (click)="onSelect(); $event.stopPropagation()">
      <app-checkbox
        [checked]="task().isCompleted"
        (toggled)="onToggle($event)" />

      <div style="min-width: 0;">
        <div style="display: flex; align-items: center; gap: 6px; flex-wrap: wrap;">
          <app-priority-flag [priority]="task().priority" />
          @if (editing()) {
            <input #editInput
                   class="inline-edit script"
                   style="font-size: 17px;"
                   [ngModel]="draft()"
                   (ngModelChange)="draft.set($event)"
                   (blur)="commitEdit()"
                   (keydown.enter)="commitEdit(); $event.preventDefault()"
                   (keydown.escape)="cancelEdit()"
                   (click)="$event.stopPropagation()" />
          } @else {
            <span class="title script"
                  style="font-size: 17px; line-height: 1.2; cursor: pointer;"
                  (dblclick)="startEdit($event)">{{ task().content }}</span>
          }
          @if (suggested()) {
            <span class="chip"
                  style="background: rgba(255, 216, 77, 0.25); color: #8A6F00; font-size: 10.5px;
                         font-family: inherit; display: inline-flex;
                         align-items: center; gap: 3px;">
              <app-icon name="zap" [size]="10" /> suggéré
            </span>
          }
        </div>

        @if (hasMeta()) {
          <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-top: 4px;">
            @if (dueDate(); as due) {
              <span class="mono"
                    [style.display]="'inline-flex'"
                    [style.align-items]="'center'"
                    [style.gap.px]="4"
                    [style.color]="overdue() ? 'var(--p1)' : 'var(--mute)'"
                    [style.font-size.px]="11">
                <app-icon name="clock" [size]="11" />
                {{ dueLabel() }}
              </span>
            }
            @if (task().estimateMinutes) {
              <span class="mono" style="color: var(--mute); font-size: 11px;">
                {{ estimateLabel() }}
              </span>
            }
            @if (task().recurrenceRule || task().isRecurring) {
              <span class="mono"
                    style="display: inline-flex; align-items: center; gap: 3px; color: var(--mute); font-size: 11px;">
                <app-icon name="repeat" [size]="10" />
                {{ recurrenceLabel() }}
              </span>
            }
            @if (project(); as p) {
              <span style="display: inline-flex; align-items: center; gap: 4px; font-size: 11.5px; color: var(--ink-2);">
                <app-project-dot [color]="getColor(p.color)" [size]="7" />
                <span style="font-weight: 700; font-size: 14px;">{{ p.name }}</span>
              </span>
            }
            @if (task().mentionContext) {
              <span class="chip"
                    style="background: rgba(255, 94, 125, 0.15); color: #B22F4E;
                           font-family: inherit; font-size: 10.5px;">
                &#64;{{ task().mentionContext }}
              </span>
            }
            @for (lab of task().labels; track lab) {
              <app-tag-chip [name]="lab" [color]="labelColor(lab)" />
            }
          </div>
        }
      </div>
    </div>
  `,
})
export class TaskRowComponent {
  task = input.required<Task>();
  project = input<Project | null>(null);
  selected = input<boolean>(false);
  suggested = input<boolean>(false);

  toggled = output<Task>();
  selectTask = output<Task>();
  updated = output<{ id: string; patch: Partial<Task> }>();

  private labelService = inject(LabelService);
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  editing = signal(false);
  completing = signal(false);
  draft = signal('');

  @ViewChild('editInput') editInput?: ElementRef<HTMLInputElement>;

  dueDate = computed(() => getTaskDueDate(this.task()));
  overdue = computed(() => isOverdue(this.task()));

  dueLabel = computed<string>(() => {
    const d = this.dueDate();
    if (!d) return '';
    const t = this.task();
    const time = !isTaskAllDay(t) ? fmtTime(d) : '';
    const rel = fmtRel(d);
    return time && rel !== 'auj.' ? `${time} · ${rel}` : (time || rel);
  });

  estimateLabel = computed(() => fmtEstimate(this.task().estimateMinutes ?? null));
  recurrenceLabel = computed(() => rruleToLabel(this.task().recurrenceRule));

  hasMeta = computed(() => {
    const t = this.task();
    return !!(t.dueAt || t.estimateMinutes || t.recurrenceRule || t.isRecurring || this.project() ||
              t.mentionContext || (t.labels && t.labels.length > 0));
  });

  constructor() {
    effect(() => {
      if (this.editing() && this.editInput) {
        queueMicrotask(() => {
          this.editInput?.nativeElement.focus();
          this.editInput?.nativeElement.select();
        });
      }
    });
  }

  getColor = getColor;

  labelColor(name: string): string {
    const l = this.allLabels().find(x => x.name === name);
    return getColor(l?.color ?? 'charcoal');
  }

  onSelect(): void {
    this.selectTask.emit(this.task());
  }

  onToggle(e: MouseEvent): void {
    if (!this.task().isCompleted) {
      this.completing.set(true);
      setTimeout(() => {
        this.completing.set(false);
        this.toggled.emit(this.task());
      }, 600);
    } else {
      this.toggled.emit(this.task());
    }
  }

  startEdit(e: Event): void {
    e.stopPropagation();
    this.draft.set(this.task().content);
    this.editing.set(true);
  }

  commitEdit(): void {
    const next = this.draft().trim();
    if (next && next !== this.task().content) {
      this.updated.emit({ id: this.task().id, patch: { content: next } });
    }
    this.editing.set(false);
  }

  cancelEdit(): void {
    this.draft.set(this.task().content);
    this.editing.set(false);
  }
}
