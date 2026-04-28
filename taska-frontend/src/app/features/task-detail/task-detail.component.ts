import { Component, OnChanges, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  Comment,
  Label,
  PRIORITY_LABELS,
  Project,
  Task,
  fmtDateLong,
  fmtDateShort,
  fmtTime,
  getColor,
  getTaskDueDateTime,
  taskHasTime,
} from '../../core/models';
import { TaskService } from '../../core/services/task.service';
import { CommentService } from '../../core/services/comment.service';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import {
  CheckboxComponent,
  PriorityFlagComponent,
  ProjectDotComponent,
  TagChipComponent,
} from '../../shared/components/atoms/atoms.component';

@Component({
  selector: 'app-task-detail',
  imports: [
    FormsModule,
    IconComponent,
    CheckboxComponent,
    PriorityFlagComponent,
    ProjectDotComponent,
    TagChipComponent,
  ],
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

  subtasks = signal<Task[]>([]);
  comments = signal<Comment[]>([]);
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  currentProject = computed(() =>
    this.projects().find(p => p.id === this.task().projectId) ?? null
  );
  dueDate = computed(() => getTaskDueDateTime(this.task()));

  dueLabel = computed(() => {
    const d = this.dueDate();
    if (!d) return '';
    const t = this.task();
    const time = taskHasTime(t) ? ' · ' + fmtTime(d) : '';
    return fmtDateLong(d) + time +
           (t.estimateMinutes ? ` → +${t.estimateMinutes}min` : '');
  });

  completedSubs = computed(() => this.subtasks().filter(s => s.isCompleted).length);

  priorityLabel = computed(() => {
    const p = this.task().priority;
    return `${PRIORITY_LABELS[p] ?? ''}`;
  });

  ngOnChanges(): void {
    const t = this.task();
    this.editedContent.set(t.content);
    this.editedDescription.set(t.description ?? '');
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

  labelColor(name: string): string {
    const l = this.allLabels().find(x => x.name === name);
    return getColor(l?.color ?? 'charcoal');
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

  cyclePriority(): void {
    const next = (this.task().priority % 4 + 1) as 1 | 2 | 3 | 4;
    this.save({ priority: next });
  }

  toggleProjectMenu(e: Event): void {
    e.stopPropagation();
    this.showProjectMenu.set(!this.showProjectMenu());
  }

  setProject(projectId: string): void {
    this.showProjectMenu.set(false);
    this.save({ projectId });
  }

  setQuickDate(kind: 'today' | 'tomorrow' | 'week'): void {
    const d = new Date();
    if (kind === 'tomorrow') d.setDate(d.getDate() + 1);
    if (kind === 'week') d.setDate(d.getDate() + 7);
    const iso = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
    this.save({ dueDate: iso });
  }

  clearDate(): void {
    this.taskService.updateTask(this.task().id, { dueDate: null as any, dueDateTime: null as any })
      .subscribe(t => this.taskUpdated.emit(t));
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
    if (!confirm('Supprimer cette tâche ?')) return;
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
