import {
  ChangeDetectionStrategy, Component, computed,
  effect, input, output, signal,
} from '@angular/core';
import { IconComponent } from '../icon/icon.component';

const FR_MONTHS = [
  'janvier','février','mars','avril','mai','juin',
  'juillet','août','septembre','octobre','novembre','décembre',
];
const DAY_HEADERS = ['L','M','M','J','V','S','D'];

interface CalCell { date: string; day: number; inMonth: boolean; isToday: boolean; }

function pad(n: number) { return n.toString().padStart(2, '0'); }

function todayIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
}

@Component({
  selector: 'app-datetime-picker',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconComponent],
  template: `
    <!-- Month nav -->
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
      <button class="btn btn-ghost btn-icon" (click)="prevMonth()">
        <app-icon name="chevron-left" [size]="13"/>
      </button>
      <span class="mono" style="font-size:12px;font-weight:600;text-transform:capitalize;color:var(--ink);">
        {{ monthLabel() }}
      </span>
      <button class="btn btn-ghost btn-icon" (click)="nextMonth()">
        <app-icon name="chevron-right" [size]="13"/>
      </button>
    </div>

    <!-- Day headers -->
    <div style="display:grid;grid-template-columns:repeat(7,1fr);margin-bottom:4px;">
      @for (h of DAY_HEADERS; track $index) {
        <div class="mono" style="text-align:center;font-size:10px;color:var(--mute);padding:2px 0;">{{ h }}</div>
      }
    </div>

    <!-- Day grid -->
    <div style="display:grid;grid-template-columns:repeat(7,1fr);gap:2px;">
      @for (cell of calendarDays(); track cell.date) {
        <button
          style="aspect-ratio:1;border-radius:5px;border:none;cursor:pointer;font-size:12px;
                 font-family:inherit;transition:background .1s;"
          [style.opacity]="cell.inMonth ? '1' : '0.3'"
          [style.background]="cell.date === selectedDate() ? 'var(--orange)'
            : cell.isToday ? 'rgba(255,138,61,0.12)' : 'transparent'"
          [style.color]="cell.date === selectedDate() ? '#fff' : 'var(--ink)'"
          [style.font-weight]="cell.isToday || cell.date === selectedDate() ? '700' : '400'"
          [style.box-shadow]="cell.isToday && cell.date !== selectedDate()
            ? 'inset 0 0 0 1.5px var(--orange)' : 'none'"
          (click)="onDateClick(cell.date)">
          {{ cell.day }}
        </button>
      }
    </div>

    <!-- Time picker -->
    @if (withTime()) {
      <div style="margin-top:10px;padding-top:10px;border-top:1px solid var(--line-2);">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
          <div style="display:flex;align-items:center;gap:6px;">
            <app-icon name="clock" [size]="12" color="var(--mute)"/>
            <span class="mono" style="font-size:11px;color:var(--mute);">heure</span>
          </div>
          @if (selectedTime()) {
            <button class="btn btn-ghost" style="font-size:11px;padding:2px 8px;"
                    (click)="clearTime()">effacer</button>
          }
        </div>
        <div style="display:flex;align-items:center;gap:6px;">
          <select (change)="onHourChange($any($event.target).value)"
                  style="flex:1;padding:6px 8px;border:1px solid var(--line);border-radius:7px;
                         background:var(--bg);color:var(--ink);font-size:13px;font-family:monospace;
                         outline:none;cursor:pointer;appearance:auto;">
            <option value="">--</option>
            @for (h of HOURS; track h) {
              <option [value]="h" [selected]="h === selectedHour()">{{ h }}</option>
            }
          </select>
          <span class="mono" style="color:var(--mute);font-size:14px;font-weight:600;">:</span>
          <select (change)="onMinuteChange($any($event.target).value)"
                  style="flex:1;padding:6px 8px;border:1px solid var(--line);border-radius:7px;
                         background:var(--bg);color:var(--ink);font-size:13px;font-family:monospace;
                         outline:none;cursor:pointer;appearance:auto;">
            <option value="">--</option>
            @for (m of MINUTES; track m) {
              <option [value]="m" [selected]="m === selectedMinute()">{{ m }}</option>
            }
          </select>
        </div>
      </div>
    }
  `,
})
export class DatetimePickerComponent {
  value    = input<string>('');
  withTime = input<boolean>(false);
  valueChange = output<string>();

  calYear  = signal(new Date().getFullYear());
  calMonth = signal(new Date().getMonth());

  readonly DAY_HEADERS = DAY_HEADERS;
  readonly HOURS   = Array.from({ length: 24 }, (_, i) => pad(i));
  readonly MINUTES = Array.from({ length: 60 }, (_, i) => pad(i));

  constructor() {
    effect(() => {
      const v = this.value();
      if (v && v.length >= 10) {
        if (v.includes('T')) {
          const d = new Date(v);
          if (!isNaN(d.getTime())) { this.calYear.set(d.getFullYear()); this.calMonth.set(d.getMonth()); }
        } else {
          const [y, mo] = v.split('-').map(Number);
          if (!isNaN(y) && !isNaN(mo)) { this.calYear.set(y); this.calMonth.set(mo - 1); }
        }
      }
    });
  }

  selectedDate = computed(() => {
    const v = this.value();
    if (!v || v.length < 10) return '';
    if (v.includes('T')) {
      const d = new Date(v);
      if (!isNaN(d.getTime())) return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
    }
    return v.slice(0, 10);
  });
  selectedTime = computed(() => {
    const v = this.value();
    if (!v || !v.includes('T')) return '';
    const d = new Date(v);
    if (isNaN(d.getTime())) return '';
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
  });
  selectedHour   = computed(() => this.selectedTime() ? this.selectedTime().slice(0, 2) : '');
  selectedMinute = computed(() => this.selectedTime() ? this.selectedTime().slice(3, 5) : '');
  monthLabel     = computed(() => `${FR_MONTHS[this.calMonth()]} ${this.calYear()}`);

  calendarDays = computed<CalCell[]>(() => {
    const y = this.calYear(), m = this.calMonth();
    const today = todayIso();
    const firstDow = (new Date(y, m, 1).getDay() + 6) % 7;
    const daysInMonth = new Date(y, m + 1, 0).getDate();
    const cells: CalCell[] = [];

    const prevM = m === 0 ? 11 : m - 1, prevY = m === 0 ? y - 1 : y;
    const dInPrev = new Date(y, m, 0).getDate();
    for (let i = firstDow; i > 0; i--) {
      const d = dInPrev - i + 1;
      cells.push({ date: `${prevY}-${pad(prevM+1)}-${pad(d)}`, day: d, inMonth: false, isToday: false });
    }
    for (let d = 1; d <= daysInMonth; d++) {
      const date = `${y}-${pad(m+1)}-${pad(d)}`;
      cells.push({ date, day: d, inMonth: true, isToday: date === today });
    }
    const nextM = m === 11 ? 0 : m + 1, nextY = m === 11 ? y + 1 : y;
    let nd = 1;
    while (cells.length < 42)
      cells.push({ date: `${nextY}-${pad(nextM+1)}-${pad(nd++)}`, day: nd - 1, inMonth: false, isToday: false });

    return cells;
  });

  prevMonth(): void {
    let m = this.calMonth() - 1, y = this.calYear();
    if (m < 0) { m = 11; y--; }
    this.calMonth.set(m); this.calYear.set(y);
  }

  nextMonth(): void {
    let m = this.calMonth() + 1, y = this.calYear();
    if (m > 11) { m = 0; y++; }
    this.calMonth.set(m); this.calYear.set(y);
  }

  onDateClick(date: string): void {
    const time = this.selectedTime();
    if (time && this.withTime()) {
      this.valueChange.emit(new Date(`${date}T${time}:00`).toISOString());
    } else {
      this.valueChange.emit(date);
    }
  }

  onHourChange(h: string): void {
    if (!h) { this.clearTime(); return; }
    const date = this.selectedDate() || todayIso();
    const min  = this.selectedMinute() || '00';
    this.valueChange.emit(new Date(`${date}T${h}:${min}:00`).toISOString());
  }

  onMinuteChange(m: string): void {
    if (!m) { this.clearTime(); return; }
    const date = this.selectedDate() || todayIso();
    const hour = this.selectedHour() || '00';
    this.valueChange.emit(new Date(`${date}T${hour}:${m}:00`).toISOString());
  }

  clearTime(): void {
    const date = this.selectedDate();
    if (date) this.valueChange.emit(date);
  }
}
