import { Component, OnInit, OnChanges, input, output, signal, inject, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PriorityPickerComponent } from '../../shared/components/priority-picker/priority-picker.component';
import { DueDatePickerComponent } from '../../shared/components/due-date-picker/due-date-picker.component';
import { LabelPickerComponent } from '../../shared/components/label-picker/label-picker.component';
import { AddTaskFormComponent } from '../../shared/components/add-task-form/add-task-form.component';
import { TaskService } from '../../core/services/task.service';
import { CommentService } from '../../core/services/comment.service';
import { ProjectService } from '../../core/services/project.service';
import { LabelService } from '../../core/services/label.service';
import { Task, Comment, Project, Label, PRIORITY_LABELS, getColor } from '../../core/models';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-task-detail',
  imports: [FormsModule, PriorityPickerComponent, DueDatePickerComponent, LabelPickerComponent, AddTaskFormComponent],
  templateUrl: './task-detail.component.html'
})
export class TaskDetailComponent implements OnInit, OnChanges {
  task = input.required<Task>();
  close = output<void>();
  taskUpdated = output<Task>();

  private taskService = inject(TaskService);
  private commentService = inject(CommentService);
  private projectService = inject(ProjectService);
  private labelService = inject(LabelService);

  editedContent = signal('');
  editedDescription = signal('');
  subtasks = signal<Task[]>([]);
  comments = signal<Comment[]>([]);
  newComment = signal('');
  projects = signal<Project[]>([]);
  priorityLabels = PRIORITY_LABELS;
  allLabels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });
  getColor = getColor;

  ngOnInit(): void {
    this.projectService.projects$.subscribe(p => this.projects.set(p));
    this.loadSubtasks();
    this.loadComments();
  }

  ngOnChanges(): void {
    this.editedContent.set(this.task().content);
    this.editedDescription.set(this.task().description ?? '');
    this.loadSubtasks();
    this.loadComments();
  }

  private loadSubtasks(): void {
    this.taskService.getSubtasks(this.task().id).subscribe(t => this.subtasks.set(t));
  }

  private loadComments(): void {
    this.commentService.getComments(this.task().id).subscribe(c => this.comments.set(c));
  }

  saveContent(): void {
    if (this.editedContent().trim() === this.task().content) return;
    this.save({ content: this.editedContent().trim() });
  }

  saveDescription(): void {
    this.save({ description: this.editedDescription() });
  }

  onPriorityChange(priority: 1 | 2 | 3 | 4): void {
    this.save({ priority });
  }

  onDueDateChange(dueDate: string | undefined): void {
    this.save({ dueDate });
  }

  onProjectChange(projectId: string): void {
    this.save({ projectId, sectionId: undefined });
  }

  onLabelsChange(labels: string[]): void {
    this.save({ labels });
  }

  getLabelColor(name: string): string {
    const label = this.allLabels().find(l => l.name === name);
    return getColor(label?.color ?? 'charcoal');
  }

  private save(patch: Partial<Task>): void {
    this.taskService.updateTask(this.task().id, patch).subscribe(updated => {
      this.taskUpdated.emit(updated);
    });
  }

  addComment(): void {
    const content = this.newComment().trim();
    if (!content) return;
    this.commentService.createComment({ taskId: this.task().id, content }).subscribe(c => {
      this.comments.update(comments => [...comments, c]);
      this.newComment.set('');
    });
  }

  deleteComment(commentId: string): void {
    this.commentService.deleteComment(commentId).subscribe(() => {
      this.comments.update(c => c.filter(c2 => c2.id !== commentId));
    });
  }

  completeSubtask(subtask: Task): void {
    this.taskService.closeTask(subtask.id).subscribe(() => {
      this.subtasks.update(t => t.filter(t2 => t2.id !== subtask.id));
    });
  }

  onSubtaskCreated(subtask: Task): void {
    this.subtasks.update(t => [...t, subtask]);
  }

  getProjectName(projectId?: string): string {
    if (!projectId) return 'No project';
    return this.projects().find(p => p.id === projectId)?.name ?? 'Unknown';
  }

  formatCommentDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' });
  }
}
