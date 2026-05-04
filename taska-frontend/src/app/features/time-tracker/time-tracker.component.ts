import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { fromEvent, interval } from 'rxjs';
import {
  Project,
  TimeEntry,
  fmtEstimate,
  getColor,
  hexToRgba,
  timeEntryDuration,
} from '../../core/models';
import { ProjectService } from '../../core/services/project.service';
import { TimeEntryService } from '../../core/services/time-entry.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ProjectDotComponent } from '../../shared/components/atoms/atoms.component';

// ── constants ─────────────────────────────────────────────────────────────────

const PX_H   = 60; // pixels per hour → 1 px = 1 min
const HOURS  = Array.from({ length: 24 }, (_, i) => i);
const DRAG_THRESHOLD = 5; // px movement to switch from click-pending to actual drag

const FR_DAYS_SH   = ['dim.','lun.','mar.','mer.','jeu.','ven.','sam.'];
const FR_MONTHS    = ['janvier','février','mars','avril','mai','juin',
                      'juillet','août','septembre','octobre','novembre','décembre'];
const FR_MONTHS_SH = ['janv.','févr.','mars','avr.','mai','juin',
                      'juil.','août','sept.','oct.','nov.','déc.'];

// ── helpers ───────────────────────────────────────────────────────────────────

function isoDate(d: Date): string { return d.toISOString().split('T')[0]; }

function getMonday(d: Date): Date {
  const date = new Date(d);
  const dow  = date.getDay();
  date.setDate(date.getDate() - (dow === 0 ? 6 : dow - 1));
  date.setHours(0, 0, 0, 0);
  return date;
}

function snap15(min: number): number { return Math.round(min / 15) * 15; }

function minToHHMM(min: number): string {
  const h = Math.floor(min / 60) % 24;
  const m = min % 60;
  return `${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}`;
}

function dtToMin(isoStr: string): number {
  const t = isoStr.slice(11, 16);
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}
function toInputDt(iso: string):   string { return iso.slice(0, 16); }
function fromInputDt(val: string): string { return val.length === 16 ? val + ':00' : val; }

// ── interfaces ────────────────────────────────────────────────────────────────

/** Drag on empty column to create a new entry. */
interface CreateDragState {
  colIndex: number;
  startMin: number;
  endMin: number;
  date: string;
}

/** Interaction on an existing entry (move or resize). */
interface EntryInteraction {
  mode: 'pending' | 'move' | 'resize-top' | 'resize-bottom';
  entry: TimeEntry;
  // pointer position at mousedown (viewport px)
  startX: number;
  startY: number;
  // derived from entry times at drag start
  origStartMin: number;
  origEndMin: number;
  origDate: string;
  origColIndex: number;
  // current state (updated on mousemove)
  curStartMin: number;
  curEndMin: number;
  curDate: string;
  curColIndex: number;
}

interface GhostEntryInfo {
  id: string;
  startMin: number;
  endMin: number;
  colIndex: number;
  color: string;
  label: string;
}

interface DayInfo {
  iso: string;
  label: string;
  dayNum: number;
  monthLabel: string;
  isToday: boolean;
}

// ── component ─────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-time-tracker',
  imports: [FormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
<!-- ── outer shell ── -->
<div style="display:flex; flex-direction:column; height:100%; overflow:hidden; background:var(--bg);">

  <!-- control bar -->
  <div style="display:flex; align-items:center; gap:12px; padding:14px 20px 10px;
              border-bottom:1px solid var(--line); flex-shrink:0;">
    <button class="btn btn-ghost btn-icon" (click)="prevPeriod()" title="Précédent">
      <app-icon name="chevron-left" [size]="15" />
    </button>
    <button class="btn btn-ghost" (click)="goToToday()" style="font-size:12px;">Aujourd'hui</button>
    <button class="btn btn-ghost btn-icon" (click)="nextPeriod()" title="Suivant">
      <app-icon name="chevron-right" [size]="15" />
    </button>

    <span class="script" style="font-size:20px; color:var(--ink); min-width:0;">{{ periodLabel() }}</span>

    @if (weekTotal() > 0) {
      <span class="mono" style="font-size:11px; color:var(--mute); margin-left:4px;">
        {{ fmtEst(weekTotal()) }} total
      </span>
    }

    <div style="flex:1;"></div>

    <!-- view mode toggle -->
    <div style="display:flex; gap:4px; border:1px solid var(--line); border-radius:7px; padding:2px;">
      @for (m of VIEW_MODES; track m.id) {
        <button (click)="viewMode.set(m.id)"
                style="padding:4px 10px; border-radius:5px; border:0; cursor:pointer; font-size:12px;
                       transition:background .15s;"
                [style.background]="viewMode() === m.id ? 'var(--orange)' : 'transparent'"
                [style.color]="viewMode() === m.id ? '#fff' : 'var(--ink-2)'">{{ m.label }}</button>
      }
    </div>

    <!-- project filter -->
    <select [ngModel]="projectFilter()"
            (ngModelChange)="projectFilter.set($event || null)"
            style="padding:5px 8px; border:1px solid var(--line); border-radius:7px; font-size:12px;
                   background:var(--bg); color:var(--ink); cursor:pointer; outline:none;">
      <option value="">Tous les projets</option>
      @for (p of activeProjects(); track p.id) {
        <option [value]="p.id">{{ p.name }}</option>
      }
    </select>
  </div>

  <!-- day-header row -->
  <div style="display:flex; border-bottom:1px solid var(--line); flex-shrink:0; background:var(--bg);">
    <div style="width:50px; flex-shrink:0; border-right:1px solid var(--line);"></div>
    @for (day of viewDays(); track day.iso) {
      <div style="flex:1; padding:6px 0; text-align:center; min-width:0;"
           [style.background]="day.isToday ? 'rgba(255,138,61,.06)' : 'transparent'">
        <div class="mono" style="font-size:10px; color:var(--mute); text-transform:uppercase; letter-spacing:.06em;">
          {{ day.label }}
        </div>
        <div style="font-size:18px; font-weight:600; line-height:1.2;"
             [style.color]="day.isToday ? 'var(--orange)' : 'var(--ink)'">{{ day.dayNum }}</div>
        @if (totalPerDay()[day.iso]) {
          <div class="mono" style="font-size:10px; color:var(--mute);">{{ fmtEst(totalPerDay()[day.iso]) }}</div>
        }
      </div>
    }
  </div>

  <!-- scrollable grid -->
  <div #gridScroll
       style="flex:1; overflow-y:auto; overflow-x:hidden; position:relative;"
       [style.cursor]="gridCursor()"
       [style.user-select]="entryInteraction() ? 'none' : 'auto'"
       (mousemove)="onGridMove($event)"
       (mouseleave)="onGridLeave()">

    <!-- full-screen interaction overlay (prevents hover side-effects on child elements during drag) -->
    @if (entryInteraction() && entryInteraction()!.mode !== 'pending') {
      <div style="position:absolute; inset:0; z-index:50; cursor:inherit;"></div>
    }

    <div style="display:flex; position:relative; min-height:1440px;" #gridInner>

      <!-- time axis -->
      <div style="width:50px; flex-shrink:0; position:relative; border-right:1px solid var(--line); pointer-events:none;">
        @for (h of HOURS; track h) {
          @if (h > 0) {
            <div class="mono"
                 style="position:absolute; right:6px; font-size:10px; color:var(--mute);
                        line-height:1; transform:translateY(-50%); white-space:nowrap;"
                 [style.top.px]="h * PX_H">{{ h }}h</div>
          }
        }
      </div>

      <!-- day columns -->
      @for (day of viewDays(); track day.iso; let ci = $index) {
        <div style="flex:1; position:relative; border-right:1px solid var(--line-2);
                    cursor:crosshair; min-width:0;"
             [style.background]="day.isToday ? 'rgba(255,138,61,.03)' : 'transparent'"
             (mousedown)="onColDown($event, ci, day.iso)">

          <!-- hour / half-hour grid lines -->
          @for (h of HOURS; track h) {
            <div style="position:absolute; left:0; right:0; pointer-events:none;"
                 [style.top.px]="h * PX_H"
                 [style.border-top]="h === 0 ? 'none' : '1px solid var(--line-2)'"></div>
            @if (h < 23) {
              <div style="position:absolute; left:0; right:0; border-top:1px dashed var(--line);
                          pointer-events:none; opacity:.4;"
                   [style.top.px]="h * PX_H + 30"></div>
            }
          }

          <!-- existing entries -->
          @for (e of entriesForDay(day.iso); track e.id) {
            <div style="position:absolute; left:2px; right:2px; border-radius:5px; overflow:visible;
                        font-size:11px; color:#fff; z-index:1; box-sizing:border-box;
                        transition:opacity .1s;"
                 [style.top.px]="entryTop(e)"
                 [style.height.px]="entryHeightPx(e)"
                 [style.background]="entryBg(e)"
                 [style.opacity]="isBeingDragged(e) ? '0.25' : '1'"
                 [style.cursor]="activeDragCursor()"
                 [title]="e.description"
                 (mousedown)="onEntryDown($event, e, ci, day.iso)">

              <!-- top resize handle -->
              @if (entryHeightPx(e) >= 18) {
                <div style="position:absolute; top:0; left:0; right:0; height:5px;
                            cursor:ns-resize; z-index:3;"
                     (mousedown)="$event.stopPropagation(); onResizeDown($event, e, ci, day.iso, 'resize-top')"></div>
              }

              <!-- content -->
              <div style="padding:4px 6px; overflow:hidden; height:100%; box-sizing:border-box;"
                   [style.padding-top.px]="entryHeightPx(e) >= 18 ? 7 : 3">
                <div style="font-weight:600; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; line-height:1.3;">
                  {{ e.description || projectName(e.projectId) }}
                </div>
                @if (entryHeightPx(e) >= 36) {
                  <div style="font-size:10px; opacity:.85; white-space:nowrap; overflow:hidden; line-height:1.2;">
                    {{ entryTimeRange(e) }} · {{ fmtEst(timeEntryDuration(e)) }}
                  </div>
                }
              </div>

              <!-- bottom resize handle -->
              @if (entryHeightPx(e) >= 18) {
                <div style="position:absolute; bottom:0; left:0; right:0; height:5px;
                            cursor:ns-resize; z-index:3;"
                     (mousedown)="$event.stopPropagation(); onResizeDown($event, e, ci, day.iso, 'resize-bottom')"></div>
              }
            </div>
          }

          <!-- ghost for moved/resized entry (in target column) -->
          @if (ghostEntry()?.colIndex === ci) {
            <div style="position:absolute; left:2px; right:2px; border-radius:5px; z-index:4;
                        pointer-events:none; opacity:.85;
                        box-shadow:0 4px 14px rgba(0,0,0,.25); box-sizing:border-box;
                        display:flex; flex-direction:column; padding:4px 6px; gap:1px;"
                 [style.top.px]="ghostEntry()!.startMin"
                 [style.height.px]="ghostEntry()!.endMin - ghostEntry()!.startMin"
                 [style.background]="ghostEntry()!.color">
              <div style="font-weight:600; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
                          font-size:11px; color:#fff; line-height:1.3;">
                {{ ghostEntry()!.label }}
              </div>
              @if (ghostEntry()!.endMin - ghostEntry()!.startMin >= 28) {
                <div class="mono" style="font-size:10px; color:rgba(255,255,255,.85); white-space:nowrap;">
                  {{ minToHHMM(ghostEntry()!.startMin) }}–{{ minToHHMM(ghostEntry()!.endMin) }}
                  · {{ fmtEst(ghostEntry()!.endMin - ghostEntry()!.startMin) }}
                </div>
              }
            </div>
          }

          <!-- create-drag ghost -->
          @if (createDrag() && createDrag()!.colIndex === ci && createGhostRange()) {
            <div style="position:absolute; left:2px; right:2px; border-radius:5px; z-index:3;
                        pointer-events:none; background:rgba(255,138,61,.12);
                        border:2px dashed var(--orange); display:flex; align-items:center;
                        justify-content:center; box-sizing:border-box;"
                 [style.top.px]="createGhostRange()!.top"
                 [style.height.px]="createGhostRange()!.height">
              @if (createGhostRange()!.height >= 20) {
                <span class="mono" style="font-size:10px; color:var(--orange); white-space:nowrap;">
                  {{ createGhostLabel() }}
                </span>
              }
            </div>
          }

          <!-- current time line -->
          @if (day.isToday) {
            <div style="position:absolute; left:0; right:0; height:2px; background:var(--p1);
                        z-index:3; pointer-events:none;"
                 [style.top.px]="nowMin()">
              <div style="position:absolute; left:-4px; top:-3px; width:8px; height:8px;
                          border-radius:50%; background:var(--p1);"></div>
            </div>
          }
        </div>
      }
    </div>
  </div>
</div>

<!-- ── create / edit modal ── -->
@if (showModal()) {
  <div class="modal-veil" (click)="closeModal()">
    <div class="modal" (click)="$event.stopPropagation()"
         style="width:min(480px,95vw); padding:24px 24px 20px;">

      <div style="font-size:17px; font-weight:600; color:var(--ink); margin-bottom:20px;">
        {{ editingId() ? 'Modifier le créneau' : 'Nouveau créneau' }}
      </div>

      <!-- project -->
      <div style="margin-bottom:14px;">
        <label style="display:block; font-size:11px; font-weight:500; color:var(--mute);
                       text-transform:uppercase; letter-spacing:.05em; margin-bottom:5px;">Projet *</label>
        <select [ngModel]="modalProjectId()" (ngModelChange)="modalProjectId.set($event)"
                style="width:100%; padding:8px 10px; border:1px solid var(--line); border-radius:7px;
                       background:var(--bg); color:var(--ink); font-size:14px; outline:none;
                       cursor:pointer; box-sizing:border-box;">
          <option value="">Choisir un projet…</option>
          @for (p of activeProjects(); track p.id) {
            <option [value]="p.id">{{ p.name }}</option>
          }
        </select>
      </div>

      <!-- description -->
      <div style="margin-bottom:14px;">
        <label style="display:block; font-size:11px; font-weight:500; color:var(--mute);
                       text-transform:uppercase; letter-spacing:.05em; margin-bottom:5px;">Description</label>
        <input [ngModel]="modalDescription()" (ngModelChange)="modalDescription.set($event)"
               placeholder="Ex: revue de PR, réunion d'équipe…"
               style="width:100%; padding:8px 10px; border:1px solid var(--line); border-radius:7px;
                      background:var(--bg); color:var(--ink); font-size:14px; outline:none;
                      box-sizing:border-box;" />
      </div>

      <!-- start / end -->
      <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:10px;">
        <div>
          <label style="display:block; font-size:11px; font-weight:500; color:var(--mute);
                         text-transform:uppercase; letter-spacing:.05em; margin-bottom:5px;">Début</label>
          <input type="datetime-local" [ngModel]="modalStartAt()" (ngModelChange)="modalStartAt.set($event)"
                 style="width:100%; padding:8px 10px; border:1px solid var(--line); border-radius:7px;
                        background:var(--bg); color:var(--ink); font-size:13px; outline:none;
                        box-sizing:border-box; cursor:pointer;" />
        </div>
        <div>
          <label style="display:block; font-size:11px; font-weight:500; color:var(--mute);
                         text-transform:uppercase; letter-spacing:.05em; margin-bottom:5px;">Fin</label>
          <input type="datetime-local" [ngModel]="modalEndAt()" (ngModelChange)="modalEndAt.set($event)"
                 style="width:100%; padding:8px 10px; border:1px solid var(--line); border-radius:7px;
                        background:var(--bg); color:var(--ink); font-size:13px; outline:none;
                        box-sizing:border-box; cursor:pointer;" />
        </div>
      </div>

      @if (modalDurationLabel()) {
        <div class="mono" style="font-size:11px; color:var(--mute); margin-bottom:14px;">
          Durée : {{ modalDurationLabel() }}
        </div>
      }

      <!-- notes -->
      <div style="margin-bottom:18px;">
        <label style="display:block; font-size:11px; font-weight:500; color:var(--mute);
                       text-transform:uppercase; letter-spacing:.05em; margin-bottom:5px;">Notes</label>
        <textarea [ngModel]="modalNotes()" (ngModelChange)="modalNotes.set($event)"
                  rows="3" placeholder="Notes optionnelles…"
                  style="width:100%; padding:8px 10px; border:1px solid var(--line); border-radius:7px;
                         background:var(--bg); color:var(--ink); font-size:13px; outline:none;
                         resize:vertical; box-sizing:border-box; font-family:inherit;"></textarea>
      </div>

      <!-- actions -->
      <div style="display:flex; align-items:center; justify-content:space-between; gap:8px;">
        @if (editingId()) {
          <button class="btn btn-ghost" style="color:var(--p1);" (click)="deleteEntry()">
            <app-icon name="trash" [size]="13" /> Supprimer
          </button>
        } @else {
          <span></span>
        }
        <div style="display:flex; gap:8px;">
          <button class="btn btn-ghost" (click)="closeModal()">Annuler</button>
          <button class="btn btn-primary" (click)="saveModal()" [disabled]="!canSave()">
            {{ editingId() ? 'Enregistrer' : 'Créer' }}
          </button>
        </div>
      </div>

    </div>
  </div>
}
  `,
})
export class TimeTrackerComponent implements OnInit, AfterViewInit {
  private projectService   = inject(ProjectService);
  private timeEntryService = inject(TimeEntryService);
  private destroyRef       = inject(DestroyRef);

  @ViewChild('gridScroll') gridScrollRef?: ElementRef<HTMLElement>;
  @ViewChild('gridInner')  gridInnerRef?:  ElementRef<HTMLElement>;

  // ── constants exposed to template ──
  readonly HOURS      = HOURS;
  readonly PX_H       = PX_H;
  readonly minToHHMM  = minToHHMM;
  readonly VIEW_MODES = [
    { id: 'week' as const, label: 'Semaine' },
    { id: 'day'  as const, label: 'Jour'    },
  ];

  // ── reactive state ──
  allProjects    = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  activeProjects = computed(() =>
    this.allProjects().filter(p => !p.isInboxProject).sort((a, b) => a.order - b.order)
  );

  viewMode      = signal<'week' | 'day'>('week');
  refDate       = signal<Date>(new Date());
  projectFilter = signal<string | null>(null);
  entries       = signal<TimeEntry[]>([]);
  nowMin        = signal(this.currentMin());

  // drag to create
  createDrag = signal<CreateDragState | null>(null);

  // move / resize existing entries
  entryInteraction = signal<EntryInteraction | null>(null);

  // modal
  showModal        = signal(false);
  editingId        = signal<string | null>(null);
  modalStartAt     = signal('');
  modalEndAt       = signal('');
  modalProjectId   = signal('');
  modalDescription = signal('');
  modalNotes       = signal('');

  // ── computed ──

  viewDays = computed<DayInfo[]>(() => {
    const ref   = this.refDate();
    const today = isoDate(new Date());
    if (this.viewMode() === 'day') {
      const iso = isoDate(ref);
      return [{ iso, label: FR_DAYS_SH[ref.getDay()], dayNum: ref.getDate(),
                monthLabel: FR_MONTHS_SH[ref.getMonth()], isToday: iso === today }];
    }
    const monday = getMonday(ref);
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(monday);
      d.setDate(d.getDate() + i);
      const iso = isoDate(d);
      return { iso, label: FR_DAYS_SH[d.getDay()], dayNum: d.getDate(),
               monthLabel: FR_MONTHS_SH[d.getMonth()], isToday: iso === today };
    });
  });

  periodLabel = computed(() => {
    const days = this.viewDays();
    if (this.viewMode() === 'day') {
      const d = new Date(days[0].iso + 'T00:00:00');
      return `${FR_DAYS_SH[d.getDay()].replace('.', '')} ${d.getDate()} ${FR_MONTHS[d.getMonth()]} ${d.getFullYear()}`;
    }
    const first = new Date(days[0].iso + 'T00:00:00');
    const last  = new Date(days[6].iso + 'T00:00:00');
    if (first.getMonth() === last.getMonth()) {
      return `${first.getDate()}–${last.getDate()} ${FR_MONTHS[first.getMonth()]} ${first.getFullYear()}`;
    }
    return `${first.getDate()} ${FR_MONTHS_SH[first.getMonth()]} – ${last.getDate()} ${FR_MONTHS_SH[last.getMonth()]} ${last.getFullYear()}`;
  });

  totalPerDay = computed<Record<string, number>>(() => {
    const totals: Record<string, number> = {};
    for (const e of this.entries()) {
      const day = e.startAt.slice(0, 10);
      totals[day] = (totals[day] ?? 0) + timeEntryDuration(e);
    }
    return totals;
  });

  weekTotal = computed(() =>
    this.entries().reduce((sum, e) => sum + timeEntryDuration(e), 0)
  );

  // Ghost for existing entry being moved/resized
  ghostEntry = computed<GhostEntryInfo | null>(() => {
    const ia = this.entryInteraction();
    if (!ia || ia.mode === 'pending') return null;
    const proj  = this.allProjects().find(p => p.id === ia.entry.projectId);
    const color = proj ? hexToRgba(getColor(proj.color), 0.88) : 'rgba(128,128,128,.88)';
    return {
      id: ia.entry.id,
      startMin: ia.curStartMin,
      endMin: ia.curEndMin,
      colIndex: ia.curColIndex,
      color,
      label: ia.entry.description || proj?.name || '',
    };
  });

  // Ghost for create-by-drag
  createGhostRange = computed(() => {
    const d = this.createDrag();
    if (!d) return null;
    const lo = Math.min(d.startMin, d.endMin);
    const hi = Math.max(d.startMin, d.endMin);
    return { top: lo, height: Math.max(15, hi - lo) };
  });

  createGhostLabel = computed(() => {
    const d = this.createDrag();
    if (!d) return '';
    const lo = Math.min(d.startMin, d.endMin);
    const hi = Math.max(d.startMin, d.endMin);
    return `${minToHHMM(lo)}–${minToHHMM(hi)} (${fmtEstimate(hi - lo)})`;
  });

  canSave = computed(() =>
    this.modalProjectId() !== '' &&
    this.modalStartAt()   !== '' &&
    this.modalEndAt()     !== '' &&
    this.modalStartAt() < this.modalEndAt()
  );

  modalDurationLabel = computed(() => {
    const s = this.modalStartAt(), e = this.modalEndAt();
    if (!s || !e || s >= e) return '';
    const dur = (new Date(fromInputDt(e)).getTime() - new Date(fromInputDt(s)).getTime()) / 60000;
    return fmtEstimate(Math.round(dur));
  });

  gridCursor = computed(() => {
    const ia = this.entryInteraction();
    if (!ia) return 'auto';
    switch (ia.mode) {
      case 'pending':      return 'grab';
      case 'move':         return 'grabbing';
      case 'resize-top':
      case 'resize-bottom': return 'ns-resize';
    }
  });

  // Cursor to show on entry divs during an active interaction (prevent override)
  activeDragCursor(): string {
    const ia = this.entryInteraction();
    return ia && ia.mode !== 'pending' ? 'inherit' : 'grab';
  }

  // ── lifecycle ──

  constructor() {
    // Reload when period or filter changes
    effect(() => {
      const days = this.viewDays();
      const pid  = this.projectFilter();
      untracked(() => {
        const start = days[0].iso + 'T00:00:00';
        const end   = days[days.length - 1].iso + 'T23:59:59';
        this.timeEntryService.getEntries({ start, end, projectId: pid ?? undefined })
          .subscribe(es => this.entries.set(es));
      });
    });
  }

  ngOnInit(): void {
    interval(60000).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.nowMin.set(this.currentMin()));

    fromEvent<MouseEvent>(document, 'mouseup')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.finishCreateDrag();
        this.finishEntryInteraction();
      });
  }

  ngAfterViewInit(): void {
    const target = Math.max(0, this.nowMin() - 120);
    this.gridScrollRef?.nativeElement.scrollTo({ top: target, behavior: 'instant' });
  }

  // ── navigation ──

  prevPeriod():  void { this.shiftDate(this.viewMode() === 'week' ? -7 : -1); }
  nextPeriod():  void { this.shiftDate(this.viewMode() === 'week' ?  7 :  1); }
  goToToday():   void { this.refDate.set(new Date()); }

  private shiftDate(days: number): void {
    const d = new Date(this.refDate());
    d.setDate(d.getDate() + days);
    this.refDate.set(d);
  }

  // ── create-by-drag ──

  onColDown(event: MouseEvent, colIndex: number, date: string): void {
    if (this.entryInteraction()) return; // entry interaction has priority
    const y        = this.gridY(event.clientY);
    const startMin = snap15(Math.max(0, Math.min(1380, y)));
    this.createDrag.set({ colIndex, startMin, endMin: startMin + 60, date });
    event.preventDefault();
  }

  private finishCreateDrag(): void {
    const d = this.createDrag();
    this.createDrag.set(null);
    if (!d) return;
    const lo = Math.min(d.startMin, d.endMin);
    const hi = Math.max(d.startMin, d.endMin);
    if (hi - lo < 15) return;
    this.openCreate(d.date, lo, hi);
  }

  // ── entry interaction (move / resize) ──

  onEntryDown(event: MouseEvent, entry: TimeEntry, colIndex: number, date: string): void {
    event.stopPropagation();
    event.preventDefault();
    this.entryInteraction.set({
      mode: 'pending',
      entry,
      startX: event.clientX,
      startY: event.clientY,
      origStartMin: dtToMin(entry.startAt),
      origEndMin:   dtToMin(entry.endAt),
      origDate:     date,
      origColIndex: colIndex,
      curStartMin:  dtToMin(entry.startAt),
      curEndMin:    dtToMin(entry.endAt),
      curDate:      date,
      curColIndex:  colIndex,
    });
  }

  onResizeDown(event: MouseEvent, entry: TimeEntry, colIndex: number, date: string,
               mode: 'resize-top' | 'resize-bottom'): void {
    event.stopPropagation();
    event.preventDefault();
    this.entryInteraction.set({
      mode,
      entry,
      startX: event.clientX,
      startY: event.clientY,
      origStartMin: dtToMin(entry.startAt),
      origEndMin:   dtToMin(entry.endAt),
      origDate:     date,
      origColIndex: colIndex,
      curStartMin:  dtToMin(entry.startAt),
      curEndMin:    dtToMin(entry.endAt),
      curDate:      date,
      curColIndex:  colIndex,
    });
  }

  private finishEntryInteraction(): void {
    const ia = this.entryInteraction();
    this.entryInteraction.set(null);
    if (!ia) return;

    if (ia.mode === 'pending') {
      // No drag occurred → open edit modal
      this.openEdit(ia.entry);
      return;
    }

    // Drag occurred → save new times
    const startAt = `${ia.curDate}T${minToHHMM(ia.curStartMin)}:00`;
    const endAt   = `${ia.curDate}T${minToHHMM(ia.curEndMin)}:00`;

    // Optimistic update
    const optimistic: TimeEntry = { ...ia.entry, startAt, endAt };
    this.entries.update(list => list.map(e => e.id === ia.entry.id ? optimistic : e));

    this.timeEntryService.updateEntry(ia.entry.id, { startAt, endAt }).subscribe({
      next:  saved  => this.entries.update(list => list.map(e => e.id === saved.id ? saved : e)),
      error: ()     => this.entries.update(list => list.map(e => e.id === ia.entry.id ? ia.entry : e)),
    });
  }

  // ── unified mousemove ──

  onGridMove(event: MouseEvent): void {
    const y = this.gridY(event.clientY); // absolute px from top of the 1440px grid

    // Create drag
    const cd = this.createDrag();
    if (cd) {
      const endMin = snap15(Math.max(0, Math.min(1440, y)));
      this.createDrag.update(s => s ? { ...s, endMin } : null);
      return;
    }

    // Entry interaction
    const ia = this.entryInteraction();
    if (!ia) return;

    if (ia.mode === 'pending' || ia.mode === 'move') {
      const dx = Math.abs(event.clientX - ia.startX);
      const dy = Math.abs(event.clientY - ia.startY);
      if (ia.mode === 'pending' && dx < DRAG_THRESHOLD && dy < DRAG_THRESHOLD) return;

      const duration    = ia.origEndMin - ia.origStartMin;
      const rawDelta    = event.clientY - ia.startY;
      const newStartMin = Math.max(0, Math.min(1440 - duration,
                                               snap15(ia.origStartMin + rawDelta)));
      const newEndMin   = newStartMin + duration;
      const col         = this.colAtX(event.clientX);

      this.entryInteraction.update(s => s ? {
        ...s,
        mode: 'move',
        curStartMin: newStartMin,
        curEndMin:   newEndMin,
        curDate:     col?.date     ?? s.curDate,
        curColIndex: col?.colIndex ?? s.curColIndex,
      } : null);
      return;
    }

    if (ia.mode === 'resize-top') {
      const newStart = snap15(Math.max(0, Math.min(ia.origEndMin - 15, y)));
      this.entryInteraction.update(s => s ? { ...s, curStartMin: newStart } : null);
      return;
    }

    if (ia.mode === 'resize-bottom') {
      const newEnd = snap15(Math.max(ia.origStartMin + 15, Math.min(1440, y)));
      this.entryInteraction.update(s => s ? { ...s, curEndMin: newEnd } : null);
    }
  }

  onGridLeave(): void {
    // Only cancel create-drag on mouse-leave; entry interaction continues
    // (global mouseup will clean it up if the user releases outside)
    this.createDrag.set(null);
  }

  // ── helpers ──

  /** Y coordinate in px relative to the top of the 1440px grid inner div. */
  private gridY(clientY: number): number {
    const top = this.gridInnerRef?.nativeElement.getBoundingClientRect().top ?? 0;
    return Math.max(0, clientY - top);
  }

  /** Returns the column at a given viewport X, accounting for the 50px time axis. */
  private colAtX(clientX: number): { colIndex: number; date: string } | null {
    const grid = this.gridScrollRef?.nativeElement;
    if (!grid) return null;
    const rect      = grid.getBoundingClientRect();
    const x         = clientX - rect.left - 50; // subtract time axis
    if (x < 0) return null;
    const numCols   = this.viewDays().length;
    const colWidth  = (rect.width - 50) / numCols;
    if (colWidth <= 0) return null;
    const colIndex  = Math.floor(x / colWidth);
    if (colIndex < 0 || colIndex >= numCols) return null;
    return { colIndex, date: this.viewDays()[colIndex].iso };
  }

  // ── modal ──

  openCreate(date: string, startMin: number, endMin: number): void {
    this.editingId.set(null);
    this.modalStartAt.set(`${date}T${minToHHMM(startMin)}`);
    this.modalEndAt.set(`${date}T${minToHHMM(endMin)}`);
    this.modalProjectId.set(this.projectFilter() ?? '');
    this.modalDescription.set('');
    this.modalNotes.set('');
    this.showModal.set(true);
  }

  openEdit(e: TimeEntry): void {
    this.editingId.set(e.id);
    this.modalStartAt.set(toInputDt(e.startAt));
    this.modalEndAt.set(toInputDt(e.endAt));
    this.modalProjectId.set(e.projectId);
    this.modalDescription.set(e.description);
    this.modalNotes.set(e.notes ?? '');
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  saveModal(): void {
    if (!this.canSave()) return;
    const payload = {
      startAt:     fromInputDt(this.modalStartAt()),
      endAt:       fromInputDt(this.modalEndAt()),
      projectId:   this.modalProjectId(),
      description: this.modalDescription(),
      notes:       this.modalNotes() || undefined,
    };
    const id = this.editingId();
    const op = id
      ? this.timeEntryService.updateEntry(id, payload)
      : this.timeEntryService.createEntry(payload as any);

    op.subscribe(saved => {
      this.entries.update(list =>
        id ? list.map(e => e.id === id ? saved : e) : [...list, saved]
      );
      this.closeModal();
    });
  }

  deleteEntry(): void {
    const id = this.editingId();
    if (!id) return;
    this.timeEntryService.deleteEntry(id).subscribe(() => {
      this.entries.update(list => list.filter(e => e.id !== id));
      this.closeModal();
    });
  }

  // ── entry display helpers ──

  entriesForDay(iso: string): TimeEntry[] {
    return this.entries().filter(e => e.startAt.startsWith(iso));
  }

  isBeingDragged(e: TimeEntry): boolean {
    const ia = this.entryInteraction();
    return !!ia && ia.mode !== 'pending' && ia.entry.id === e.id;
  }

  entryTop(e: TimeEntry):      number { const d = new Date(e.startAt); return d.getHours() * 60 + d.getMinutes(); }
  entryHeightPx(e: TimeEntry): number { return Math.max(15, timeEntryDuration(e)); }

  entryBg(e: TimeEntry): string {
    const color = getColor(this.allProjects().find(p => p.id === e.projectId)?.color ?? 'charcoal');
    return hexToRgba(color, 0.85);
  }

  entryTimeRange(e: TimeEntry): string {
    return `${e.startAt.slice(11,16)}–${e.endAt.slice(11,16)}`;
  }

  projectName(id: string): string {
    return this.allProjects().find(p => p.id === id)?.name ?? '';
  }

  // ── misc ──

  fmtEst           = fmtEstimate;
  timeEntryDuration = timeEntryDuration;

  private currentMin(): number {
    const now = new Date();
    return now.getHours() * 60 + now.getMinutes();
  }
}
