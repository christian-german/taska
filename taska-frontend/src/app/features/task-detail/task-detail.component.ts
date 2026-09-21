import {
  Component,
  OnChanges,
  computed,
  inject,
  input,
  output,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { DatetimePickerComponent } from '../../shared/components/datetime-picker/datetime-picker.component';
import {
  Comment,
  Label,
  PRIORITY_LABELS,
  Project,
  RecurrenceScope,
  Task,
  TaskPatch,
  TaskType,
  fmtDateShort,
  fmtEstimate,
  fmtRel,
  fmtTime,
  getColor,
  isTaskAllDay,
} from '../../core/models';

const ESTIMATE_PRESETS = [
  { minutes: 15, label: '15 min' },
  { minutes: 30, label: '30 min' },
  { minutes: 45, label: '45 min' },
  { minutes: 60, label: '1h' },
  { minutes: 90, label: '1h30' },
  { minutes: 120, label: '2h' },
  { minutes: 180, label: '3h' },
  { minutes: 240, label: '4h' },
];

const RECURRENCE_OPTIONS = [
  { value: '', label: 'Ne se répète pas' },
  { value: 'daily', label: 'Quotidien' },
  { value: 'weekly', label: 'Hebdomadaire' },
  { value: 'monthly', label: 'Mensuel' },
  { value: 'yearly', label: 'Annuel' },
] as const;

const RRULE_TO_KEY: Record<string, string> = {
  'freq=daily': 'daily',
  'freq=weekly': 'weekly',
  'freq=monthly': 'monthly',
  'freq=yearly': 'yearly',
};
function normalizeRRuleKey(rule: string | null | undefined): string {
  if (!rule) return '';
  return RRULE_TO_KEY[rule.toLowerCase()] ?? rule.toLowerCase();
}
import { TaskService } from '../../core/services/task.service';
import { CommentService } from '../../core/services/comment.service';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { RecurrenceScopeDialogComponent } from '../../shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component';
import {
  CheckboxComponent,
  PriorityFlagComponent,
  ProjectDotComponent,
  TagChipComponent,
} from '../../shared/components/atoms/atoms.component';

type DetailPicker = 'date' | 'due' | 'tags' | 'estimate' | 'recurrence' | null;

@Component({
  selector: 'app-task-detail',
  imports: [
    FormsModule,
    IconComponent,
    ConfirmDialogComponent,
    RecurrenceScopeDialogComponent,
    CheckboxComponent,
    PriorityFlagComponent,
    ProjectDotComponent,
    TagChipComponent,
    DatetimePickerComponent,
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
  showDeleteScopeDialog = signal(false);
  activeDetailPicker = signal<DetailPicker>(null);
  tagSearch = signal('');

  readonly priorities: { value: 1 | 2 | 3 | 4; label: string }[] = [
    { value: 4, label: PRIORITY_LABELS[4] },
    { value: 3, label: PRIORITY_LABELS[3] },
    { value: 2, label: PRIORITY_LABELS[2] },
    { value: 1, label: PRIORITY_LABELS[1] },
  ];
  readonly taskTypeOptions: { value: TaskType; label: string }[] = [
    { value: 'TODO', label: 'À faire' },
    { value: 'APPOINTMENT', label: 'Rendez-vous' },
  ];

  subtasks = signal<Task[]>([]);
  comments = signal<Comment[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  currentProject = computed(
    () => this.projects().find((p) => p.id === this.task().projectId) ?? null,
  );
  isOccurrence = computed(() => this.task().kind === 'RECURRING_OCCURRENCE');
  isSeries = computed(() => this.task().kind === 'RECURRING_SERIES');
  isDetached = computed(
    () => this.task().kind === 'RECURRING_OCCURRENCE' && this.task().isDetached === true,
  );
  scheduledDate = computed(() => {
    const scheduledAt = this.task().scheduledAt;
    return scheduledAt ? new Date(scheduledAt) : null;
  });
  dueAt = computed(() => {
    const task = this.task();
    return task.kind === 'RECURRING_SERIES' ? null : task.dueAt;
  });
  dueDate = computed(() => {
    const dueAt = this.dueAt();
    return dueAt ? new Date(dueAt) : null;
  });

  completedSubs = computed(() => this.subtasks().filter((s) => s.isCompleted).length);

  priorityLabel = computed(() => {
    const priority = this.task().priority;
    return priority == null ? 'Non définie' : (PRIORITY_LABELS[priority] ?? '');
  });
  taskTypeLabel = computed(() => (this.task().type === 'APPOINTMENT' ? 'Rendez-vous' : 'À faire'));

  filteredLabels = computed(() => {
    const q = this.tagSearch().toLowerCase();
    return q ? this.allLabels().filter((l) => l.name.toLowerCase().includes(q)) : this.allLabels();
  });

  hasExactLabelMatch = computed(() =>
    this.allLabels().some((l) => l.name.toLowerCase() === this.tagSearch().toLowerCase().trim()),
  );

  dateRowLabel = computed(() => {
    const d = this.scheduledDate();
    if (!d) return 'Planifier';
    return fmtRel(d) + (!isTaskAllDay(this.task()) ? ' · ' + fmtTime(d) : '');
  });

  estimateRowLabel = computed(() => {
    const m = this.task().estimateMinutes;
    return m ? fmtEstimate(m) : 'Estimer';
  });

  activeRecurrenceKey = computed(() => normalizeRRuleKey(this.task().recurrenceRule));

  recurrenceRowLabel = computed(() => {
    const key = this.activeRecurrenceKey();
    if (!key) return 'Ne se répète pas';
    return (
      RECURRENCE_OPTIONS.find((o) => o.value === key)?.label ?? this.task().recurrenceRule ?? key
    );
  });

  ngOnChanges(): void {
    const t = this.task();
    this.editedContent.set(t.content);
    this.editedDescription.set(t.description ?? '');
    this.activeDetailPicker.set(null);
    this.loadSubtasks();
    this.loadComments();
  }

  private loadSubtasks(): void {
    this.taskService.getSubtasks(this.task().id).subscribe((t) => this.subtasks.set(t));
  }

  private loadComments(): void {
    this.commentService.getComments(this.task().id).subscribe((c) => this.comments.set(c));
  }

  getColor = getColor;
  ESTIMATE_PRESETS = ESTIMATE_PRESETS;
  RECURRENCE_OPTIONS = RECURRENCE_OPTIONS;

  labelColor(name: string): string {
    const l = this.allLabels().find((x) => x.name === name);
    return getColor(l?.color ?? 'charcoal');
  }

  hasLabel(name: string): boolean {
    return this.task().labels.includes(name);
  }

  saveContent(): void {
    if (this.isOccurrence()) return;
    const next = this.editedContent().trim();
    if (next && next !== this.task().content) this.save({ content: next });
  }

  saveDescription(): void {
    if (this.isOccurrence()) return;
    if (this.editedDescription() !== (this.task().description ?? '')) {
      this.save({ description: this.editedDescription() });
    }
  }

  toggleComplete(): void {
    const occurrenceScheduledAt = this.task().occurrenceScheduledAt ?? undefined;
    if (this.task().isCompleted) {
      this.taskService
        .reopenTask(this.task().id, occurrenceScheduledAt)
        .subscribe((t) => this.taskUpdated.emit(t));
    } else {
      this.taskService
        .closeTask(this.task().id, occurrenceScheduledAt)
        .subscribe((t) => this.taskUpdated.emit(t));
    }
  }

  closeMenus(): void {
    this.showProjectMenu.set(false);
    this.showPriorityMenu.set(false);
    this.activeDetailPicker.set(null);
  }

  togglePriorityMenu(e: Event): void {
    if (this.isOccurrence()) return;
    e.stopPropagation();
    this.showPriorityMenu.set(!this.showPriorityMenu());
    this.activeDetailPicker.set(null);
  }

  setPriority(priority: 1 | 2 | 3 | 4): void {
    this.showPriorityMenu.set(false);
    this.save({ priority });
  }

  setTaskType(type: TaskType): void {
    if (this.isOccurrence()) return;
    if (this.task().type !== type) this.save({ type });
  }

  toggleProjectMenu(e: Event): void {
    e.stopPropagation();
    this.showProjectMenu.set(!this.showProjectMenu());
    this.activeDetailPicker.set(null);
  }

  setProject(projectId: string): void {
    if (this.isOccurrence()) return;
    this.showProjectMenu.set(false);
    this.save({ projectId });
  }

  openPicker(name: DetailPicker, e: Event): void {
    if (
      (this.isOccurrence() && name !== 'date') ||
      (this.isSeries() && (name === 'date' || name === 'recurrence'))
    )
      return;
    e.stopPropagation();
    this.showProjectMenu.set(false);
    this.showPriorityMenu.set(false);
    this.activeDetailPicker.set(this.activeDetailPicker() === name ? null : name);
    if (name === 'tags') this.tagSearch.set('');
  }

  datePickerValue = computed(() => this.task().scheduledAt ?? '');
  dueDatePickerValue = computed(() => this.dueAt() ?? '');

  onDatetimeChange(value: string): void {
    const hasTime = value.includes('T');
    const scheduledAt = hasTime ? value : value + 'T00:00:00Z';
    this.save(this.isOccurrence() ? { scheduledAt } : { scheduledAt, allDay: !hasTime });
  }

  clearDate(e: Event): void {
    if (this.isSeries()) return;
    e.stopPropagation();
    this.activeDetailPicker.set(null);
    this.save(this.isOccurrence() ? { scheduledAt: null } : { scheduledAt: null, allDay: false });
  }

  onDueDateChange(value: string): void {
    const dueAt = value.includes('T') ? value : value + 'T00:00:00Z';
    this.save({ dueAt });
  }

  toggleTag(name: string, e: Event): void {
    e.stopPropagation();
    if (this.isOccurrence()) return;
    const labels = this.task().labels.includes(name)
      ? this.task().labels.filter((l) => l !== name)
      : [...this.task().labels, name];
    this.save({ labels });
  }

  selectEstimate(minutes: number, e: Event): void {
    e.stopPropagation();
    if (this.isOccurrence()) return;
    this.activeDetailPicker.set(null);
    this.save({ estimateMinutes: minutes });
  }

  clearEstimate(e: Event): void {
    e.stopPropagation();
    if (this.isOccurrence()) return;
    this.save({ estimateMinutes: undefined });
  }

  selectRecurrence(value: string, e: Event): void {
    if (this.isSeries()) return;
    e.stopPropagation();
    if (this.isOccurrence()) return;
    this.activeDetailPicker.set(null);
    this.save({ recurrenceRule: value || null, isRecurring: !!value });
  }

  toggleSubtask(s: Task): void {
    const op = s.isCompleted ? this.taskService.reopenTask(s.id) : this.taskService.closeTask(s.id);
    op.subscribe((updated) => {
      this.subtasks.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
    });
  }

  addSubtask(): void {
    const content = this.newSubtaskContent().trim();
    if (!content) return;
    this.taskService
      .createTask({
        content,
        parentId: this.task().id,
        projectId: this.task().projectId,
        priority: 1,
        labels: [],
      })
      .subscribe((sub) => {
        this.subtasks.update((list) => [...list, sub]);
        this.newSubtaskContent.set('');
      });
  }

  addComment(): void {
    const content = this.newComment().trim();
    if (!content) return;
    this.commentService.createComment({ taskId: this.task().id, content }).subscribe((c) => {
      this.comments.update((arr) => [...arr, c]);
      this.newComment.set('');
    });
  }

  deleteTask(): void {
    if (this.task().kind === 'RECURRING_OCCURRENCE' && !this.task().isDetached) {
      this.showDeleteScopeDialog.set(true);
    } else {
      this.showDeleteConfirm.set(true);
    }
  }

  confirmDelete(): void {
    this.showDeleteConfirm.set(false);
    const task = this.task();
    const deletion =
      task.kind === 'RECURRING_OCCURRENCE' && task.isDetached
        ? this.taskService.deleteTask(task.id, 'THIS_ONLY', task.occurrenceScheduledAt)
        : this.taskService.deleteTask(task.id);
    deletion.subscribe(() => {
      this.taskDeleted.emit(this.task().id);
    });
  }

  onDeleteScope(scope: RecurrenceScope): void {
    this.showDeleteScopeDialog.set(false);
    const occurrenceScheduledAt = this.task().occurrenceScheduledAt ?? undefined;
    this.taskService.deleteTask(this.task().id, scope, occurrenceScheduledAt).subscribe(() => {
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

  private save(patch: TaskPatch): void {
    const task = this.task();
    if (task.kind === 'RECURRING_OCCURRENCE') {
      if (Object.keys(patch).some((key) => key !== 'scheduledAt')) return;
      this.taskService
        .updateTask(task.id, {
          ...patch,
          scope: 'THIS_ONLY',
          occurrenceScheduledAt: task.occurrenceScheduledAt,
        })
        .subscribe({
          next: (updated) => this.taskUpdated.emit(updated),
          error: () => undefined,
        });
    } else {
      this.taskService.updateTask(task.id, patch).subscribe({
        next: (updated) => this.taskUpdated.emit(updated),
        error: () => undefined,
      });
    }
  }
}
