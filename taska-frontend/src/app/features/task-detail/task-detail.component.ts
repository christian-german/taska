import {Component, OnChanges, computed, inject, input, output, signal, ChangeDetectionStrategy} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  Comment,
  Label,
  PRIORITY_LABELS,
  Project,
  Task,
  fmtDateShort,
  fmtEstimate,
  fmtRel,
  fmtTime,
  getColor,
  getTaskDueDateTime,

  taskHasTime,
} from '../../core/models';

const FR_MONTHS = [
  'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
  'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre',
];

interface CalCell { date: string; day: number; inMonth: boolean; isToday: boolean; }
function pad(n: number): string { return n.toString().padStart(2, '0'); }

const ESTIMATE_PRESETS = [
  { minutes: 15, label: '15 min' }, { minutes: 30, label: '30 min' },
  { minutes: 45, label: '45 min' }, { minutes: 60, label: '1h' },
  { minutes: 90, label: '1h30' },  { minutes: 120, label: '2h' },
  { minutes: 180, label: '3h' },   { minutes: 240, label: '4h' },
];

const RECURRENCE_OPTIONS = [
  { value: '', label: 'Ne se répète pas' },
  { value: 'daily', label: 'Quotidien' },
  { value: 'weekly', label: 'Hebdomadaire' },
  { value: 'monthly', label: 'Mensuel' },
] as const;
import { TaskService } from '../../core/services/task.service';
import { CommentService } from '../../core/services/comment.service';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  CheckboxComponent,
  PriorityFlagComponent,
  ProjectDotComponent,
  TagChipComponent,
} from '../../shared/components/atoms/atoms.component';

type DetailPicker = 'date' | 'tags' | 'estimate' | 'recurrence' | null;

@Component({
  selector: 'app-task-detail',
  imports: [
    FormsModule,
    IconComponent,
    ConfirmDialogComponent,
    CheckboxComponent,
    PriorityFlagComponent,
    ProjectDotComponent,
    TagChipComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-detail.component.html',
})
export class TaskDetailComponent implements OnChanges {
  task = input.required<Task>();
  isMobile = input<boolean>(false);

  close = output<void>();
  taskUpdated = output<Task>();
  taskDeleted = output<string>();

  private taskService = inject(TaskService);
  private commentService = inject(CommentService);
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);

  editedContent = signal('');
  editedDescription = signal('');
  newSubtaskContent = signal('');
  newComment = signal('');
  showProjectMenu = signal(false);
  showPriorityMenu = signal(false);
  showDeleteConfirm = signal(false);
  activeDetailPicker = signal<DetailPicker>(null);
  calYear = signal(new Date().getFullYear());
  calMonth = signal(new Date().getMonth());
  tagSearch = signal('');

  readonly priorities: { value: 1 | 2 | 3 | 4; label: string }[] = [
    { value: 4, label: PRIORITY_LABELS[4] },
    { value: 3, label: PRIORITY_LABELS[3] },
    { value: 2, label: PRIORITY_LABELS[2] },
    { value: 1, label: PRIORITY_LABELS[1] },
  ];

  subtasks = signal<Task[]>([]);
  comments = signal<Comment[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  currentProject = computed(() =>
    this.projects().find(p => p.id === this.task().projectId) ?? null
  );
  dueDate = computed(() => getTaskDueDateTime(this.task()));

  completedSubs = computed(() => this.subtasks().filter(s => s.isCompleted).length);

  priorityLabel = computed(() => PRIORITY_LABELS[this.task().priority] ?? '');

  calMonthLabel = computed(() =>
    `${FR_MONTHS[this.calMonth()]} ${this.calYear()}`
  );

  calendarDays = computed<CalCell[]>(() => {
    const y = this.calYear(), m = this.calMonth();
    const today = new Date();
    const firstDow = new Date(y, m, 1).getDay();
    const offset = (firstDow + 6) % 7;
    const daysInMonth = new Date(y, m + 1, 0).getDate();
    const cells: CalCell[] = [];
    for (let i = 0; i < offset; i++) {
      const d = new Date(y, m, 1 - offset + i);
      cells.push({ date: `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`, day: d.getDate(), inMonth: false, isToday: false });
    }
    for (let d = 1; d <= daysInMonth; d++) {
      const dt = new Date(y, m, d);
      cells.push({ date: `${y}-${pad(m+1)}-${pad(d)}`, day: d, inMonth: true, isToday: dt.toDateString() === today.toDateString() });
    }
    while (cells.length < 42) {
      const d = new Date(y, m + 1, cells.length - offset - daysInMonth + 1);
      cells.push({ date: `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`, day: d.getDate(), inMonth: false, isToday: false });
    }
    return cells;
  });

  filteredLabels = computed(() => {
    const q = this.tagSearch().toLowerCase();
    return q ? this.allLabels().filter(l => l.name.toLowerCase().includes(q)) : this.allLabels();
  });

  hasExactLabelMatch = computed(() =>
    this.allLabels().some(l => l.name.toLowerCase() === this.tagSearch().toLowerCase().trim())
  );

  dateRowLabel = computed(() => {
    const d = this.dueDate();
    if (!d) return 'Ajouter une date';
    return fmtRel(d) + (taskHasTime(this.task()) ? ' · ' + fmtTime(d) : '');
  });

  estimateRowLabel = computed(() => {
    const m = this.task().estimateMinutes;
    return m ? fmtEstimate(m) : 'Estimer';
  });

  recurrenceRowLabel = computed(() => {
    const r = this.task().recurrenceRule;
    if (!r) return 'Ne se répète pas';
    return RECURRENCE_OPTIONS.find(o => (o.value as string) === r)?.label ?? r;
  });

  ngOnChanges(): void {
    const t = this.task();
    this.editedContent.set(t.content);
    this.editedDescription.set(t.description ?? '');
    this.activeDetailPicker.set(null);
    const now = new Date();
    const d = this.dueDate();
    this.calYear.set(d ? d.getFullYear() : now.getFullYear());
    this.calMonth.set(d ? d.getMonth() : now.getMonth());
    this.loadSubtasks();
    this.loadComments();
  }

  private loadSubtasks(): void {
    this.taskService.getSubtasks(this.task().id).subscribe(t => this.subtasks.set(t));
  }

  private loadComments(): void {
    this.commentService.getComments(this.task().id).subscribe(c => this.comments.set(c));
  }

  getColor = getColor;
  ESTIMATE_PRESETS = ESTIMATE_PRESETS;
  RECURRENCE_OPTIONS = RECURRENCE_OPTIONS;

  labelColor(name: string): string {
    const l = this.allLabels().find(x => x.name === name);
    return getColor(l?.color ?? 'charcoal');
  }

  hasLabel(name: string): boolean {
    return this.task().labels.includes(name);
  }

  saveContent(): void {
    const next = this.editedContent().trim();
    if (next && next !== this.task().content) this.save({ content: next });
  }

  saveDescription(): void {
    if (this.editedDescription() !== (this.task().description ?? '')) {
      this.save({ description: this.editedDescription() });
    }
  }

  toggleComplete(): void {
    if (this.task().isCompleted) {
      this.taskService.reopenTask(this.task().id).subscribe(t => this.taskUpdated.emit(t));
    } else {
      this.taskService.closeTask(this.task().id).subscribe(t => this.taskUpdated.emit(t));
    }
  }

  closeMenus(): void {
    this.showProjectMenu.set(false);
    this.showPriorityMenu.set(false);
    this.activeDetailPicker.set(null);
  }

  togglePriorityMenu(e: Event): void {
    e.stopPropagation();
    this.showPriorityMenu.set(!this.showPriorityMenu());
    this.activeDetailPicker.set(null);
  }

  setPriority(priority: 1 | 2 | 3 | 4): void {
    this.showPriorityMenu.set(false);
    this.save({ priority });
  }

  toggleProjectMenu(e: Event): void {
    e.stopPropagation();
    this.showProjectMenu.set(!this.showProjectMenu());
    this.activeDetailPicker.set(null);
  }

  setProject(projectId: string): void {
    this.showProjectMenu.set(false);
    this.save({ projectId });
  }

  openPicker(name: DetailPicker, e: Event): void {
    e.stopPropagation();
    this.showProjectMenu.set(false);
    this.showPriorityMenu.set(false);
    this.activeDetailPicker.set(this.activeDetailPicker() === name ? null : name);
    if (name === 'tags') this.tagSearch.set('');
  }

  prevMonth(e: Event): void {
    e.stopPropagation();
    let m = this.calMonth() - 1, y = this.calYear();
    if (m < 0) { m = 11; y--; }
    this.calMonth.set(m); this.calYear.set(y);
  }

  nextMonth(e: Event): void {
    e.stopPropagation();
    let m = this.calMonth() + 1, y = this.calYear();
    if (m > 11) { m = 0; y++; }
    this.calMonth.set(m); this.calYear.set(y);
  }

  isDateSelected(date: string): boolean {
    const t = this.task();
    if (t.dueDateTime) return t.dueDateTime.startsWith(date);
    return t.dueDate === date;
  }

  timeValue = computed(() => this.task().dueDateTime?.slice(11, 16) ?? '');

  selectDate(cell: CalCell, e: Event): void {
    e.stopPropagation();
    const currentTime = this.task().dueDateTime?.slice(11, 16);
    if (currentTime) {
      this.save({ dueDate: null as any, dueDateTime: `${cell.date}T${currentTime}:00` });
    } else {
      this.save({ dueDate: cell.date, dueDateTime: null as any });
    }
    // Keep picker open so user can optionally set/change time
  }

  onTimeChange(e: Event): void {
    const time = (e.target as HTMLInputElement).value;
    const t = this.task();
    const date = t.dueDate ?? t.dueDateTime?.slice(0, 10);
    if (!date) return;
    if (!time) {
      this.save({ dueDate: date, dueDateTime: null as any });
    } else {
      this.save({ dueDate: null as any, dueDateTime: `${date}T${time}:00` });
    }
  }

  clearTime(e: Event): void {
    e.stopPropagation();
    const t = this.task();
    const date = t.dueDate ?? t.dueDateTime?.slice(0, 10);
    if (date) this.save({ dueDate: date, dueDateTime: null as any });
  }

  clearDate(e: Event): void {
    e.stopPropagation();
    this.activeDetailPicker.set(null);
    this.taskService.updateTask(this.task().id, { dueDate: null as any, dueDateTime: null as any })
      .subscribe(t => this.taskUpdated.emit({ ...t, dueDate: undefined, dueDateTime: undefined }));
  }

  toggleTag(name: string, e: Event): void {
    e.stopPropagation();
    const labels = this.task().labels.includes(name)
      ? this.task().labels.filter(l => l !== name)
      : [...this.task().labels, name];
    this.save({ labels });
  }

  selectEstimate(minutes: number, e: Event): void {
    e.stopPropagation();
    this.activeDetailPicker.set(null);
    this.save({ estimateMinutes: minutes });
  }

  clearEstimate(e: Event): void {
    e.stopPropagation();
    this.save({ estimateMinutes: undefined });
  }

  selectRecurrence(value: string, e: Event): void {
    e.stopPropagation();
    this.activeDetailPicker.set(null);
    this.save({ recurrenceRule: value || (null as any) });
  }

  toggleSubtask(s: Task): void {
    const op = s.isCompleted ? this.taskService.reopenTask(s.id) : this.taskService.closeTask(s.id);
    op.subscribe(updated => {
      this.subtasks.update(list => list.map(x => x.id === updated.id ? updated : x));
    });
  }

  addSubtask(): void {
    const content = this.newSubtaskContent().trim();
    if (!content) return;
    this.taskService.createTask({
      content,
      parentId: this.task().id,
      projectId: this.task().projectId,
      priority: 1,
      labels: [],
    }).subscribe(sub => {
      this.subtasks.update(list => [...list, sub]);
      this.newSubtaskContent.set('');
    });
  }

  addComment(): void {
    const content = this.newComment().trim();
    if (!content) return;
    this.commentService.createComment({ taskId: this.task().id, content }).subscribe(c => {
      this.comments.update(arr => [...arr, c]);
      this.newComment.set('');
    });
  }

  deleteTask(): void {
    this.showDeleteConfirm.set(true);
  }

  confirmDelete(): void {
    this.showDeleteConfirm.set(false);
    this.taskService.deleteTask(this.task().id).subscribe(() => {
      this.taskDeleted.emit(this.task().id);
    });
  }

  onClose(): void {
    this.close.emit();
  }

  formatCommentDate(dateStr: string): string {
    const d = new Date(dateStr);
    return `${fmtDateShort(d)} · ${fmtTime(d)}`;
  }

  formatShort(dateStr: string): string {
    return fmtDateShort(new Date(dateStr));
  }

  private save(patch: Partial<Task>): void {
    this.taskService.updateTask(this.task().id, patch).subscribe(updated => this.taskUpdated.emit(updated));
  }
}
