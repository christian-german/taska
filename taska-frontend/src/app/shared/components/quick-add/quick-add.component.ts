import {
  ChangeDetectionStrategy, Component, ElementRef, OnInit,
  ViewChild, computed, inject, output, signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { LabelService } from '../../../core/services/label.service';
import { UiStateService } from '../../../core/services/ui-state.service';
import { NlParserService, NlParsed } from '../../../core/services/nl-parser.service';
import {
  Label, Project, TaskType, fmtRel, fmtTime, fmtEstimate, hexToRgba, getColor,
} from '../../../core/models';
import { IconComponent } from '../icon/icon.component';
import { DatetimePickerComponent } from '../datetime-picker/datetime-picker.component';

interface DetectedChip { k: string; v: string; color: string; }
type PickerName = 'date' | 'project' | 'tags' | 'estimate' | 'recurrence';

const ESTIMATE_PRESETS = [
  { minutes: 15, label: '15 min' }, { minutes: 30, label: '30 min' },
  { minutes: 45, label: '45 min' }, { minutes: 60, label: '1h' },
  { minutes: 90, label: '1h30' },  { minutes: 120, label: '2h' },
  { minutes: 180, label: '3h' },   { minutes: 240, label: '4h' },
];

const RRULE_LABELS: Record<string, string> = {
  'daily': 'quotidien', 'freq=daily': 'quotidien',
  'weekly': 'hebdomadaire', 'freq=weekly': 'hebdomadaire',
  'monthly': 'mensuel', 'freq=monthly': 'mensuel',
  'yearly': 'annuel', 'freq=yearly': 'annuel',
};
function rruleToLabel(rule: string): string | undefined {
  return RRULE_LABELS[rule.toLowerCase()];
}

const RECURRENCE_OPTIONS = [
  { value: '', label: 'Ne se répète pas', icon: 'x' },
  { value: 'daily', label: 'Quotidien', icon: 'repeat' },
  { value: 'weekly', label: 'Hebdomadaire', icon: 'repeat' },
  { value: 'monthly', label: 'Mensuel', icon: 'repeat' },
  { value: 'yearly', label: 'Annuel', icon: 'repeat' },
] as const;


@Component({
  selector: 'app-quick-add',
  imports: [FormsModule, IconComponent, DatetimePickerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()">
        <div style="padding: 20px 22px 12px;">
          <div class="script" style="font-size:22px;color:var(--mute);margin-bottom:10px;">
            nouvelle tâche
          </div>

          <!-- NL input with highlight mirror -->
          <div style="position:relative;">
            <div aria-hidden="true"
                 style="position:absolute;inset:0;padding:10px 12px;
                        font-family:inherit;font-size:22px;line-height:32px;font-weight:700;
                        color:var(--ink);white-space:pre-wrap;word-break:break-word;pointer-events:none;">
              @if (input(); as txt) {
                @for (seg of segments(); track $index) {
                  <span [class]="segClass(seg.type)">{{ seg.text }}</span>
                }
              } @else {
                <span style="color:var(--mute);">Finir slide deck Q2 demain 14h #boulot !!</span>
              }
            </div>
            <input #inputEl
                   [ngModel]="input()"
                   (ngModelChange)="input.set($event)"
                   (keydown.enter)="$event.preventDefault(); submit()"
                   (keydown.escape)="close.emit()"
                   style="width:100%;padding:10px 12px;
                          font-family:inherit;font-size:22px;line-height:32px;font-weight:700;
                          background:transparent;border:0;outline:none;
                          color:transparent;caret-color:var(--ink);position:relative;z-index:2;" />
          </div>

          <!-- Detected chips -->
          @if (detected().length > 0) {
            <div style="margin-top:14px;display:flex;flex-wrap:wrap;gap:6px;
                        align-items:center;font-size:12px;">
              <span class="mono" style="color:var(--mute);font-size:11px;">→ détecté:</span>
              @for (d of detected(); track d.k) {
                <span class="chip"
                      [style.background]="rgba(d.color, 0.15)"
                      [style.color]="d.color"
                      style="font-size:11.5px;">{{ d.v }}</span>
              }
            </div>
          }

          <hr class="dash-hr" style="margin:18px 0 12px;" />

          <!-- Single backdrop for all pickers -->
          @if (activePicker()) {
            <div style="position:fixed;inset:0;z-index:55;"
                 (click)="activePicker.set(null)"></div>
          }

          <div style="display:flex;flex-direction:column;gap:2px;color:var(--ink-2);">

            <div style="display:flex;gap:6px;padding:6px 8px;" role="group" aria-label="Type de tâche">
              @for (option of taskTypes; track option.value) {
                <button type="button" (click)="taskType.set(option.value)"
                        [attr.aria-pressed]="taskType() === option.value"
                        [style.background]="taskType() === option.value ? 'var(--orange)' : 'transparent'"
                        [style.color]="taskType() === option.value ? '#fff' : 'var(--ink-2)'"
                        style="border:1px solid var(--line);border-radius:14px;padding:3px 9px;cursor:pointer;font-size:12px;">
                  {{ option.label }}
                </button>
              }
            </div>

            <!-- ── DATE ─────────────────────────────────────────────── -->
            <div style="position:relative;">
              <button style="width:100%;display:flex;align-items:center;gap:10px;font-size:13px;background:transparent;border:0;padding:6px 8px;cursor:pointer;text-align:left;border-radius:7px;color:inherit;" (click)="togglePicker('date')">
                <app-icon name="calendar" [size]="14" color="#FF8A3D" />
                <span style="flex:1;"
                      [style.color]="effectiveDueDate() ? 'var(--ink)' : 'var(--ink-2)'">
                  {{ dateRowLabel() }}
                </span>
                @if (manualDatetime()) {
                  <span (click)="clearManual($event,'date')"
                        style="color:var(--mute);font-size:15px;line-height:1;cursor:pointer;">×</span>
                }
              </button>

              @if (activePicker() === 'date') {
                <div style="position:absolute;left:0;top:calc(100% + 4px);z-index:56;background:var(--bg);border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 24px rgba(0,0,0,.14);padding:12px;min-width:260px;"
                     (click)="$event.stopPropagation()">
                  <app-datetime-picker
                    [value]="datePickerValue()"
                    [withTime]="true"
                    (valueChange)="onDatetimeChange($event)" />
                </div>
              }
            </div>

            <!-- ── PROJET ───────────────────────────────────────────── -->
            <div style="position:relative;">
              <button style="width:100%;display:flex;align-items:center;gap:10px;font-size:13px;background:transparent;border:0;padding:6px 8px;cursor:pointer;text-align:left;border-radius:7px;color:inherit;" (click)="togglePicker('project')">
                <app-icon name="folder" [size]="14" color="#3AA3FF" />
                <span style="flex:1;display:flex;align-items:center;gap:6px;"
                      [style.color]="manualProjectId() ? 'var(--ink)' : 'var(--ink-2)'">
                  @if (effectiveProject() && !effectiveProject()!.isInboxProject) {
                    <span style="width:8px;height:8px;border-radius:50%;flex-shrink:0;display:inline-block;"
                          [style.background]="getColor(effectiveProject()!.color)"></span>
                  }
                  {{ effectiveProject()?.isInboxProject ? 'Inbox' : (effectiveProject()?.name ?? 'Inbox') }}
                </span>
                @if (manualProjectId()) {
                  <span (click)="clearManual($event,'project')"
                        style="color:var(--mute);font-size:15px;line-height:1;cursor:pointer;">×</span>
                }
              </button>

              @if (activePicker() === 'project') {
                <div style="position:absolute;left:0;top:calc(100% + 4px);z-index:56;background:var(--bg);border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 24px rgba(0,0,0,.14);padding:8px;min-width:220px;max-height:260px;"
                     (click)="$event.stopPropagation()">
                  <input [ngModel]="projectSearch()"
                         (ngModelChange)="projectSearch.set($event)"
                         placeholder="Rechercher un projet..."
                         style="width:100%;background:var(--bg);border:1px solid var(--line);
                                border-radius:6px;padding:6px 8px;font-size:12.5px;outline:none;
                                color:var(--ink);box-sizing:border-box;margin-bottom:6px;" />
                  <div style="overflow-y:auto;max-height:180px;">
                    <!-- Inbox -->
                    <button (click)="selectProject(null)"
                            style="width:100%;display:flex;align-items:center;gap:8px;padding:6px 8px;
                                   background:transparent;border:0;cursor:pointer;border-radius:6px;
                                   font-size:13px;text-align:left;"
                            [style.background]="!manualProjectId() ? 'rgba(255,138,61,0.08)' : 'transparent'"
                            [style.color]="!manualProjectId() ? 'var(--orange)' : 'var(--ink)'">
                      <span style="width:8px;height:8px;border-radius:50%;background:var(--mute);flex-shrink:0;"></span>
                      Inbox
                    </button>
                    @for (p of filteredProjects(); track p.id) {
                      <button (click)="selectProject(p.id)"
                              style="width:100%;display:flex;align-items:center;gap:8px;padding:6px 8px;
                                     background:transparent;border:0;cursor:pointer;border-radius:6px;
                                     font-size:13px;text-align:left;"
                              [style.background]="manualProjectId() === p.id ? 'rgba(255,138,61,0.08)' : 'transparent'"
                              [style.color]="manualProjectId() === p.id ? 'var(--orange)' : 'var(--ink)'">
                        <span style="width:8px;height:8px;border-radius:50%;flex-shrink:0;"
                              [style.background]="getColor(p.color)"></span>
                        {{ p.name }}
                      </button>
                    }
                  </div>
                </div>
              }
            </div>

            <!-- ── TAGS ────────────────────────────────────────────── -->
            <div style="position:relative;">
              <button style="width:100%;display:flex;align-items:center;gap:10px;font-size:13px;background:transparent;border:0;padding:6px 8px;cursor:pointer;text-align:left;border-radius:7px;color:inherit;" (click)="togglePicker('tags')">
                <app-icon name="grid" [size]="14" color="#FF5E7D" />
                <span style="flex:1;"
                      [style.color]="effectiveTags().length ? 'var(--ink)' : 'var(--ink-2)'">
                  {{ tagRowLabel() }}
                </span>
                @if (hasManualTags()) {
                  <span (click)="clearManual($event,'tags')"
                        style="color:var(--mute);font-size:15px;line-height:1;cursor:pointer;">×</span>
                }
              </button>

              @if (activePicker() === 'tags') {
                <div style="position:absolute;left:0;top:calc(100% + 4px);z-index:56;background:var(--bg);border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 24px rgba(0,0,0,.14);padding:10px;min-width:260px;"
                     (click)="$event.stopPropagation()">
                  <input [ngModel]="tagSearch()"
                         (ngModelChange)="tagSearch.set($event)"
                         (keydown.enter)="addOrToggleTag()"
                         placeholder="Rechercher ou créer un tag..."
                         style="width:100%;background:var(--bg);border:1px solid var(--line);
                                border-radius:6px;padding:6px 8px;font-size:12.5px;outline:none;
                                color:var(--ink);box-sizing:border-box;margin-bottom:8px;" />

                  <!-- Existing labels -->
                  <div style="display:flex;flex-wrap:wrap;gap:5px;max-height:130px;overflow-y:auto;">
                    @for (label of filteredLabels(); track label.id) {
                      <button (click)="toggleTag(label.name)"
                              style="display:flex;align-items:center;gap:4px;padding:3px 8px;
                                     border-radius:20px;border:1.5px solid;font-size:12px;cursor:pointer;
                                     transition:all .12s;"
                              [style.border-color]="rgba(getColor(label.color), effectiveTagSet().has(label.name) ? 1 : 0.3)"
                              [style.background]="effectiveTagSet().has(label.name) ? rgba(getColor(label.color), 0.15) : 'transparent'"
                              [style.color]="effectiveTagSet().has(label.name) ? getColor(label.color) : 'var(--ink-2)'">
                        @if (effectiveTagSet().has(label.name)) {
                          <span style="font-size:10px;">✓</span>
                        }
                        #{{ label.name }}
                      </button>
                    }
                    <!-- Add new tag if no exact match -->
                    @if (tagSearch().trim() && !hasExactLabelMatch()) {
                      <button (click)="addOrToggleTag()"
                              style="display:flex;align-items:center;gap:4px;padding:3px 8px;
                                     border-radius:20px;border:1.5px dashed var(--mute);
                                     font-size:12px;cursor:pointer;color:var(--mute);background:transparent;">
                        + #{{ tagSearch().trim().replace(/^#/, '') }}
                      </button>
                    }
                  </div>

                  <!-- Selected summary -->
                  @if (effectiveTags().length > 0) {
                    <div style="margin-top:8px;padding-top:8px;border-top:1px solid var(--line);
                                display:flex;flex-wrap:wrap;gap:4px;align-items:center;">
                      <span class="mono" style="color:var(--mute);font-size:10.5px;width:100%;margin-bottom:2px;">
                        sélectionnés :
                      </span>
                      @for (tag of effectiveTags(); track tag) {
                        <span style="display:flex;align-items:center;gap:3px;padding:2px 7px;
                                     border-radius:20px;background:rgba(255,94,125,0.1);
                                     color:#B22F4E;font-size:11.5px;">
                          #{{ tag }}
                          <span (click)="toggleTag(tag)"
                                style="cursor:pointer;font-size:13px;line-height:1;color:#B22F4E;">×</span>
                        </span>
                      }
                    </div>
                  }
                </div>
              }
            </div>

            <!-- ── ESTIMATION ──────────────────────────────────────── -->
            <div style="position:relative;">
              <button style="width:100%;display:flex;align-items:center;gap:10px;font-size:13px;background:transparent;border:0;padding:6px 8px;cursor:pointer;text-align:left;border-radius:7px;color:inherit;" (click)="togglePicker('estimate')">
                <app-icon name="clock" [size]="14" color="#7AD36B" />
                <span style="flex:1;"
                      [style.color]="effectiveEstimate() ? 'var(--ink)' : 'var(--ink-2)'">
                  {{ estimateRowLabel() }}
                </span>
                @if (manualEstimate() !== null) {
                  <span (click)="clearManual($event,'estimate')"
                        style="color:var(--mute);font-size:15px;line-height:1;cursor:pointer;">×</span>
                }
              </button>

              @if (activePicker() === 'estimate') {
                <div style="position:absolute;left:0;top:calc(100% + 4px);z-index:56;background:var(--bg);border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 24px rgba(0,0,0,.14);padding:10px;min-width:220px;"
                     (click)="$event.stopPropagation()">
                  <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:4px;">
                    @for (p of estimatePresets; track p.minutes) {
                      <button (click)="selectEstimate(p.minutes)"
                              style="padding:6px 4px;border-radius:7px;border:1.5px solid;
                                     font-size:12px;cursor:pointer;transition:all .12s;"
                              [style.border-color]="manualEstimate() === p.minutes ? 'var(--orange)' : 'var(--line)'"
                              [style.background]="manualEstimate() === p.minutes ? 'var(--orange)' : 'transparent'"
                              [style.color]="manualEstimate() === p.minutes ? '#fff' : 'var(--ink)'">
                        {{ p.label }}
                      </button>
                    }
                  </div>
                </div>
              }
            </div>

            <!-- ── RÉCURRENCE ──────────────────────────────────────── -->
            <div style="position:relative;">
              <button style="width:100%;display:flex;align-items:center;gap:10px;font-size:13px;background:transparent;border:0;padding:6px 8px;cursor:pointer;text-align:left;border-radius:7px;color:inherit;" (click)="togglePicker('recurrence')">
                <app-icon name="repeat" [size]="14" color="#3AA3FF" />
                <span style="flex:1;"
                      [style.color]="effectiveRecurrence() ? 'var(--ink)' : 'var(--ink-2)'">
                  {{ recurrenceRowLabel() }}
                </span>
                @if (manualRecurrence() !== null) {
                  <span (click)="clearManual($event,'recurrence')"
                        style="color:var(--mute);font-size:15px;line-height:1;cursor:pointer;">×</span>
                }
              </button>

              @if (activePicker() === 'recurrence') {
                <div style="position:absolute;left:0;top:calc(100% + 4px);z-index:56;background:var(--bg);border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 24px rgba(0,0,0,.14);padding:6px;min-width:180px;"
                     (click)="$event.stopPropagation()">
                  @for (opt of recurrenceOptions; track opt.value) {
                    <button (click)="selectRecurrence(opt.value)"
                            style="width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;
                                   background:transparent;border:0;cursor:pointer;border-radius:7px;
                                   font-size:13px;text-align:left;transition:background .1s;"
                            [style.background]="effectiveRecurrence() === opt.value || (opt.value === '' && !effectiveRecurrence())
                              ? 'rgba(255,138,61,0.08)' : 'transparent'"
                            [style.color]="effectiveRecurrence() === opt.value || (opt.value === '' && !effectiveRecurrence())
                              ? 'var(--orange)' : 'var(--ink)'">
                      {{ opt.label }}
                    </button>
                  }
                </div>
              }
            </div>

          </div>
        </div>

        <div style="padding:12px 22px;border-top:1px solid var(--line);
                    display:flex;justify-content:space-between;align-items:center;">
          <button class="btn btn-ghost" (click)="close.emit()">
            annuler <span class="kbd" style="margin-left:4px;">esc</span>
          </button>
          <button class="btn btn-primary" (click)="submit()" [disabled]="!parsed().title">
            + ajouter
            <span class="kbd"
                  style="margin-left:6px;background:rgba(255,255,255,0.15);
                         color:rgba(255,255,255,0.85);border:0;">↵</span>
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
  private labelService = inject(LabelService);
  private ui = inject(UiStateService);
  private parser = inject(NlParserService);

  private allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  input = signal('');
  parsed = computed<NlParsed>(() => this.parser.parse(this.input()));
  segments = computed(() => this.parser.segments(this.input(), this.parsed()));

  // Active picker (only one open at a time)
  activePicker = signal<PickerName | null>(null);

  // Manual overrides — null means "use NL-detected value"
  manualDatetime = signal<string | null>(null);
  manualProjectId = signal<string | null>(null);
  manualTags = signal<Set<string> | null>(null);
  manualEstimate = signal<number | null>(null);
  manualRecurrence = signal<string | null>(null); // '' = explicitly none
  taskType = signal<TaskType>('TODO');

  // Picker search inputs
  projectSearch = signal('');
  tagSearch = signal('');

  // Static data
  readonly estimatePresets = ESTIMATE_PRESETS;
  readonly recurrenceOptions = RECURRENCE_OPTIONS;
  readonly taskTypes: { value: TaskType; label: string }[] = [
    { value: 'TODO', label: 'À faire' },
    { value: 'APPOINTMENT', label: 'Rendez-vous' },
  ];

  // ── Derived ──────────────────────────────────────────────────────────

  inboxProject = computed(() => this.allProjects().find(p => p.isInboxProject));

  filteredProjects = computed(() => {
    const q = this.projectSearch().toLowerCase();
    return this.allProjects()
      .filter(p => !p.isInboxProject)
      .filter(p => !q || p.name.toLowerCase().includes(q))
      .sort((a, b) => a.order - b.order);
  });

  filteredLabels = computed(() => {
    const q = this.tagSearch().trim().toLowerCase().replace(/^#/, '');
    return q
      ? this.labels().filter(l => l.name.toLowerCase().includes(q))
      : this.labels();
  });

  hasExactLabelMatch = computed(() => {
    const q = this.tagSearch().trim().toLowerCase().replace(/^#/, '');
    return this.labels().some(l => l.name.toLowerCase() === q);
  });

  effectiveDueDate = computed(() => {
    const m = this.manualDatetime();
    return m ? m.slice(0, 10) : (this.parsed().scheduledAt?.slice(0, 10) ?? null);
  });

  datePickerValue = computed(() => {
    const m = this.manualDatetime();
    if (m) return m;
    const p = this.parsed();
    if (p.scheduledAt) return p.scheduledAt;
    return '';
  });

  effectiveProjectId = computed(() => this.manualProjectId() ?? this.ui.defaultProjectId() ?? this.inboxProject()?.id ?? null);

  effectiveProject = computed(() =>
    this.allProjects().find(p => p.id === this.effectiveProjectId()) ?? this.inboxProject()
  );

  effectiveTags = computed(() => {
    const manual = this.manualTags();
    return manual !== null ? [...manual] : this.parsed().tags;
  });

  effectiveTagSet = computed(() => new Set(this.effectiveTags()));

  effectiveEstimate = computed<number | null>(() =>
    this.manualEstimate() !== null ? this.manualEstimate() : (this.parsed().estimateMinutes ?? null)
  );

  effectiveRecurrence = computed<string | null>(() =>
    this.manualRecurrence() !== null ? this.manualRecurrence() : (this.parsed().recurrence ?? null)
  );

  hasManualTags = computed(() => this.manualTags() !== null);

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
    if (p.priority) list.push({ k: 'prio', v: 'P' + (5 - p.priority), color: '#E5484D' });
    if (p.estimateMinutes) list.push({ k: 'est', v: fmtEstimate(p.estimateMinutes), color: '#7AD36B' });
    if (p.recurrence) list.push({ k: 'recur', v: p.recurrence, color: '#3AA3FF' });
    return list;
  });

  // ── Lifecycle ─────────────────────────────────────────────────────────

  ngOnInit(): void {
    setTimeout(() => this.inputEl?.nativeElement.focus(), 0);
  }

  // ── Picker control ────────────────────────────────────────────────────

  togglePicker(name: PickerName): void {
    if (this.activePicker() === name) { this.activePicker.set(null); return; }

    if (name === 'date') { /* calendar navigation handled by DatetimePickerComponent */ }
    if (name === 'tags') {
      this.tagSearch.set('');
      if (this.manualTags() === null) {
        this.manualTags.set(new Set(this.parsed().tags));
      }
    }
    if (name === 'project') {
      this.projectSearch.set('');
    }

    this.activePicker.set(name);
  }

  clearManual(e: MouseEvent, field: PickerName): void {
    e.stopPropagation();
    switch (field) {
      case 'date':       this.manualDatetime.set(null); break;
      case 'project':    this.manualProjectId.set(null); break;
      case 'tags':       this.manualTags.set(null); break;
      case 'estimate':   this.manualEstimate.set(null); break;
      case 'recurrence': this.manualRecurrence.set(null); break;
    }
  }

  // ── Date ──────────────────────────────────────────────────────────────

  onDatetimeChange(value: string): void {
    this.manualDatetime.set(value);
  }

  // ── Project ───────────────────────────────────────────────────────────

  selectProject(id: string | null): void {
    this.manualProjectId.set(id);
    this.activePicker.set(null);
  }

  // ── Tags ──────────────────────────────────────────────────────────────

  toggleTag(name: string): void {
    const current = this.manualTags() ?? new Set(this.parsed().tags);
    const next = new Set(current);
    if (next.has(name)) next.delete(name); else next.add(name);
    this.manualTags.set(next);
  }

  addOrToggleTag(): void {
    const name = this.tagSearch().trim().toLowerCase().replace(/^#/, '');
    if (!name) return;
    this.toggleTag(name);
    this.tagSearch.set('');
  }

  // ── Estimate ──────────────────────────────────────────────────────────

  selectEstimate(minutes: number): void {
    const isSelected = this.manualEstimate() === minutes;
    this.manualEstimate.set(isSelected ? null : minutes);
    if (!isSelected) this.activePicker.set(null);
  }

  // ── Recurrence ────────────────────────────────────────────────────────

  selectRecurrence(value: string): void {
    this.manualRecurrence.set(value);
    this.activePicker.set(null);
  }

  // ── Row labels ────────────────────────────────────────────────────────

  segClass(type: string | null): string {
    return type ? 'nl-' + type : '';
  }

  rgba = (hex: string, a: number) => hexToRgba(hex, a);
  getColor = getColor;

  dateRowLabel(): string {
    const m = this.manualDatetime();
    if (m) {
      const hasTime = m.includes('T');
      const d = new Date(hasTime ? m : m + 'T00:00:00');
      return fmtRel(d) + (hasTime ? ' · ' + fmtTime(d) : '');
    }
    const p = this.parsed();
    if (!p.scheduledAt) return 'pas de date';
    const d = new Date(p.scheduledAt);
    return fmtRel(d) + (p.hasTime ? ' · ' + fmtTime(d) : '');
  }

  tagRowLabel(): string {
    const tags = this.effectiveTags();
    return tags.length ? tags.map(t => '#' + t).join(' ') : 'pas de tag';
  }

  estimateRowLabel(): string {
    const eff = this.effectiveEstimate();
    return eff ? fmtEstimate(eff) + ' estimées' : "pas d'estimation";
  }

  recurrenceRowLabel(): string {
    const r = this.effectiveRecurrence();
    return r ? (rruleToLabel(r) ?? r) : 'ne se répète pas';
  }

  // ── Submit ────────────────────────────────────────────────────────────

  submit(): void {
    const p = this.parsed();
    if (!p.title) return;
    const manual = this.manualDatetime();
    const hasTime = manual ? manual.includes('T') : false;

    let scheduledAt: string | null = null;
    let allDay = false;
    if (manual) {
      scheduledAt = hasTime ? manual : manual + 'T00:00:00Z';
      allDay = !hasTime;
    } else if (p.scheduledAt) {
      scheduledAt = p.scheduledAt;
      allDay = p.allDay;
    }

    this.taskService.createTask({
      content: p.title,
      projectId: this.effectiveProjectId() ?? undefined,
      labels: this.effectiveTags(),
      priority: p.priority,
      scheduledAt,
      allDay,
      mentionContext: p.context,
      estimateMinutes: this.effectiveEstimate() ?? undefined,
      recurrenceRule: this.effectiveRecurrence() || undefined,
      isRecurring: !!(this.effectiveRecurrence()),
      type: this.taskType(),
    } as any).subscribe(created => {
      this.ui.taskCreated$.next(created);
      this.close.emit();
    });
  }
}
