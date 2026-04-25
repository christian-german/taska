import {
  Component, ElementRef, ViewChild, inject, input, output,
  signal, computed, effect
} from '@angular/core';
import { ProjectService } from '../../../core/services/project.service';
import { LabelService } from '../../../core/services/label.service';
import { Project, Label, getColor } from '../../../core/models';
import { toSignal } from '@angular/core/rxjs-interop';

export interface SmartParsed {
  content: string;
  priority: 1 | 2 | 3 | 4;
  dueDate?: string;
  projectId?: string;
  projectName?: string;
  labels: string[];
}

type SegmentType = 'plain' | 'priority' | 'project' | 'label' | 'date';

interface Segment { text: string; type: SegmentType; }
interface TokenMatch { start: number; end: number; type: SegmentType; }

@Component({
  selector: 'app-smart-task-input',
  template: `
    <div class="relative">
      <!-- Mirror layer: absolutely overlays the input, shows colored highlights -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none select-none flex items-center" aria-hidden="true">
        <span #mirrorInner class="inline-block text-sm" style="white-space: pre;">@for (seg of segments(); track $index) {<span [class]="segClass(seg.type)">{{ seg.text }}</span>}</span>
      </div>

      <!-- Transparent-text input — cursor visible, text invisible (mirror shows it colored) -->
      <input #inputEl type="text"
        [value]="raw()"
        (input)="onInput($event)"
        (keydown)="onKeydown($event)"
        (click)="updateCursor($event)"
        (keyup)="updateCursor($event)"
        [placeholder]="placeholder()"
        style="color: transparent; padding: 0; caret-color: inherit;"
        class="relative w-full bg-transparent caret-gray-900 dark:caret-gray-100 text-sm outline-none placeholder-gray-400" />

      <!-- Project dropdown -->
      @if (dropdownType() === 'project' && filteredProjects().length > 0) {
        <ul class="absolute left-0 top-full mt-1 w-56 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50 py-1">
          @for (project of filteredProjects(); track project.id; let i = $index) {
            <li>
              <button type="button" (mousedown)="selectProject(project)"
                class="w-full text-left px-3 py-1.5 text-sm text-gray-700 dark:text-gray-300 flex items-center gap-2 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                [class.bg-gray-100]="i === dropdownIndex()"
                [class.dark:bg-gray-700]="i === dropdownIndex()">
                <span class="w-2 h-2 rounded-full flex-shrink-0" [style.background-color]="getColor(project.color)"></span>
                {{ project.name }}
              </button>
            </li>
          }
        </ul>
      }

      <!-- Label dropdown -->
      @if (dropdownType() === 'label' && filteredLabels().length > 0) {
        <ul class="absolute left-0 top-full mt-1 w-56 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50 py-1">
          @for (label of filteredLabels(); track label.id; let i = $index) {
            <li>
              <button type="button" (mousedown)="selectLabel(label)"
                class="w-full text-left px-3 py-1.5 text-sm text-gray-700 dark:text-gray-300 flex items-center gap-2 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                [class.bg-gray-100]="i === dropdownIndex()"
                [class.dark:bg-gray-700]="i === dropdownIndex()">
                <span class="w-2 h-2 rounded-full flex-shrink-0" [style.background-color]="getColor(label.color)"></span>
                {{ label.name }}
              </button>
            </li>
          }
        </ul>
      }
    </div>
  `
})
export class SmartTaskInputComponent {
  placeholder = input('Task name');

  parsedChange = output<SmartParsed>();
  enter = output<void>();
  escape = output<void>();

  @ViewChild('inputEl') inputEl!: ElementRef<HTMLInputElement>;
  @ViewChild('mirrorInner') mirrorInner!: ElementRef<HTMLSpanElement>;

  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);

  private allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  raw = signal('');
  private cursorPos = signal(0);
  dropdownIndex = signal(0);

  readonly getColor = getColor;

  private activeToken = computed(() => {
    const before = this.raw().slice(0, this.cursorPos());
    const projMatch = before.match(/#([^\s#@]*)$/);
    if (projMatch) return { type: 'project' as const, query: projMatch[1], start: before.length - projMatch[0].length };
    const labelMatch = before.match(/@([^\s#@]*)$/);
    if (labelMatch) return { type: 'label' as const, query: labelMatch[1], start: before.length - labelMatch[0].length };
    return null;
  });

  dropdownType = computed(() => this.activeToken()?.type ?? null);

  filteredProjects = computed(() => {
    const token = this.activeToken();
    if (token?.type !== 'project') return [];
    const q = token.query.toLowerCase();
    return this.allProjects()
      .filter(p => !p.isInboxProject && p.name.toLowerCase().includes(q))
      .slice(0, 8);
  });

  filteredLabels = computed(() => {
    const token = this.activeToken();
    if (token?.type !== 'label') return [];
    const q = token.query.toLowerCase();
    return this.allLabels().filter(l => l.name.toLowerCase().includes(q)).slice(0, 8);
  });

  segments = computed<Segment[]>(() => this.buildSegments(this.raw()));

  private parsedValue = computed<SmartParsed>(() => this.buildParsed(this.raw()));

  constructor() {
    effect(() => { this.parsedChange.emit(this.parsedValue()); });
  }

  segClass(type: SegmentType): string {
    switch (type) {
      case 'priority': return 'bg-orange-100 text-orange-600 rounded px-0.5';
      case 'project':  return 'bg-blue-100 text-blue-600 rounded px-0.5';
      case 'label':    return 'bg-purple-100 text-purple-600 rounded px-0.5';
      case 'date':     return 'bg-green-100 text-green-600 rounded px-0.5';
      default:         return 'text-gray-900 dark:text-gray-100';
    }
  }

  private buildSegments(raw: string): Segment[] {
    const tokens = this.findTokens(raw);
    if (!tokens.length) return raw ? [{ text: raw, type: 'plain' }] : [];
    const segs: Segment[] = [];
    let pos = 0;
    for (const t of tokens) {
      if (t.start > pos) segs.push({ text: raw.slice(pos, t.start), type: 'plain' });
      segs.push({ text: raw.slice(t.start, t.end), type: t.type });
      pos = t.end;
    }
    if (pos < raw.length) segs.push({ text: raw.slice(pos), type: 'plain' });
    return segs;
  }

  private findTokens(raw: string): TokenMatch[] {
    const result: TokenMatch[] = [];
    const occupied = new Uint8Array(raw.length);

    const tryAdd = (start: number, end: number, type: SegmentType) => {
      for (let i = start; i < end; i++) if (occupied[i]) return;
      result.push({ start, end, type });
      for (let i = start; i < end; i++) occupied[i] = 1;
    };

    for (const m of raw.matchAll(/\bp([1-4])\b/gi))
      if (m.index !== undefined) tryAdd(m.index, m.index + m[0].length, 'priority');

    for (const m of raw.matchAll(/\b(tod|tom)\b/gi))
      if (m.index !== undefined) tryAdd(m.index, m.index + m[0].length, 'date');

    for (const m of raw.matchAll(/\b(\d{1,2})\/(\d{1,2})\b/g))
      if (m.index !== undefined) tryAdd(m.index, m.index + m[0].length, 'date');

    for (const m of raw.matchAll(/#([^\s#@]+)/g))
      if (m.index !== undefined) tryAdd(m.index, m.index + m[0].length, 'project');

    for (const m of raw.matchAll(/@([^\s#@]+)/g))
      if (m.index !== undefined) tryAdd(m.index, m.index + m[0].length, 'label');

    return result.sort((a, b) => a.start - b.start);
  }

  private buildParsed(raw: string): SmartParsed {
    let content = raw;
    let priority: 1 | 2 | 3 | 4 = 1;
    let dueDate: string | undefined;
    let projectId: string | undefined;
    let projectName: string | undefined;
    const labels: string[] = [];

    content = content.replace(/\bp([1-4])\b/gi, (_, n) => {
      priority = +n as 1 | 2 | 3 | 4;
      return '';
    });

    const today = new Date();
    content = content.replace(/\btod\b/gi, () => {
      dueDate = today.toISOString().split('T')[0];
      return '';
    });
    content = content.replace(/\btom\b/gi, () => {
      const d = new Date(today);
      d.setDate(d.getDate() + 1);
      dueDate = d.toISOString().split('T')[0];
      return '';
    });
    content = content.replace(/\b(\d{1,2})\/(\d{1,2})\b/g, (_, day, mon) => {
      const year = today.getFullYear();
      const d = new Date(year, +mon - 1, +day);
      if (d < today) d.setFullYear(year + 1);
      dueDate = d.toISOString().split('T')[0];
      return '';
    });

    content = content.replace(/#([^\s#@]+)/g, (_, name) => {
      const match = this.allProjects().find(p => p.name.toLowerCase() === name.toLowerCase());
      projectId = match?.id;
      projectName = name;
      return '';
    });

    content = content.replace(/@([^\s#@]+)/g, (_, name) => {
      labels.push(name);
      return '';
    });

    return {
      content: content.replace(/\s+/g, ' ').trim(),
      priority,
      dueDate,
      projectId,
      projectName,
      labels,
    };
  }

  onInput(event: Event): void {
    const el = event.target as HTMLInputElement;
    this.raw.set(el.value);
    this.cursorPos.set(el.selectionStart ?? el.value.length);
    this.dropdownIndex.set(0);
    this.syncScroll();
  }

  updateCursor(event: Event): void {
    const el = event.target as HTMLInputElement;
    this.cursorPos.set(el.selectionStart ?? 0);
  }

  onKeydown(event: KeyboardEvent): void {
    const type = this.dropdownType();
    const list = type === 'project' ? this.filteredProjects() : type === 'label' ? this.filteredLabels() : [];

    if (type && list.length > 0) {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.dropdownIndex.update(i => Math.min(i + 1, list.length - 1));
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.dropdownIndex.update(i => Math.max(i - 1, 0));
        return;
      }
      if (event.key === 'Enter' || event.key === 'Tab') {
        const item = list[this.dropdownIndex()];
        if (item) {
          event.preventDefault();
          type === 'project' ? this.selectProject(item as Project) : this.selectLabel(item as Label);
          return;
        }
      }
    }

    if (event.key === 'Enter') this.enter.emit();
    if (event.key === 'Escape') this.escape.emit();
  }

  selectProject(project: Project): void {
    this.replaceActiveToken('#' + project.name + ' ');
  }

  selectLabel(label: Label): void {
    this.replaceActiveToken('@' + label.name + ' ');
  }

  private replaceActiveToken(replacement: string): void {
    const token = this.activeToken();
    if (!token) return;
    const cur = this.raw();
    const newRaw = cur.slice(0, token.start) + replacement + cur.slice(this.cursorPos());
    const newPos = token.start + replacement.length;
    this.raw.set(newRaw);
    this.cursorPos.set(newPos);
    this.dropdownIndex.set(0);
    setTimeout(() => {
      if (!this.inputEl) return;
      this.inputEl.nativeElement.value = newRaw;
      this.inputEl.nativeElement.setSelectionRange(newPos, newPos);
      this.inputEl.nativeElement.focus();
      this.syncScroll();
    });
  }

  focus(): void {
    this.inputEl?.nativeElement.focus();
  }

  reset(): void {
    this.raw.set('');
    this.cursorPos.set(0);
    this.dropdownIndex.set(0);
    if (this.inputEl) this.inputEl.nativeElement.value = '';
  }

  private syncScroll(): void {
    if (!this.mirrorInner?.nativeElement || !this.inputEl?.nativeElement) return;
    this.mirrorInner.nativeElement.style.transform =
      `translateX(-${this.inputEl.nativeElement.scrollLeft}px)`;
  }
}
