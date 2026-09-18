import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { isRecurringTask, RecurrenceScope, Task, TaskPatch } from '../models';
import { TaskCreationFeedbackService } from './task-creation-feedback.service';

export interface TaskQueryParams {
  projectId?: string;
  label?: string;
  showCompleted?: boolean;
  date?: string;
  from?: string;
  to?: string;
}

interface TaskUpdateRequest {
  content: string;
  type: NonNullable<Task['type']>;
  description: string | null;
  projectId: string | null;
  parentId: string | null;
  order: number;
  priority: Task['priority'];
  labels: string[];
  scheduledAt: string | null;
  dueAt: string | null;
  allDay: boolean;
  isRecurring: boolean;
  estimateMinutes: number | null;
  mentionContext: string | null;
  recurrenceRule: string | null;
}

export interface TaskCreateRequest {
  content: string;
  type?: Task['type'] | null;
  description?: string | null;
  projectId?: string | null;
  parentId?: string | null;
  order?: number | null;
  priority?: Task['priority'];
  labels?: string[] | null;
  scheduledAt?: string | null;
  dueAt?: string | null;
  allDay?: boolean | null;
  isRecurring?: boolean | null;
  estimateMinutes?: number | null;
  mentionContext?: string | null;
  recurrenceRule?: string | null;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly taskCreationFeedback = inject(TaskCreationFeedbackService);
  private readonly base = `${environment.apiUrl}/tasks`;

  getTasks(query?: TaskQueryParams): Observable<Task[]> {
    let params = new HttpParams();
    if (query?.projectId) params = params.set('projectId', query.projectId);
    if (query?.label) params = params.set('label', query.label);
    if (query?.showCompleted !== undefined) {
      params = params.set('showCompleted', String(query.showCompleted));
    }
    if (query?.date) params = params.set('date', query.date);
    if (query?.from) params = params.set('from', query.from);
    if (query?.to) params = params.set('to', query.to);
    return this.http.get<Task[]>(this.base, { params });
  }

  getTask(taskId: string): Observable<Task> {
    return this.http.get<Task>(`${this.base}/${taskId}`);
  }

  createTask(data: TaskCreateRequest): Observable<Task> {
    return this.http.post<Task>(this.base, data).pipe(tap(() => this.taskCreationFeedback.show()));
  }

  updateTask(
    taskId: string,
    patch: TaskPatch & {
      scope?: RecurrenceScope;
      occurrenceScheduledAt?: string | null;
    },
  ): Observable<Task> {
    const { scope, occurrenceScheduledAt, ...changes } = patch;
    return this.getTask(taskId).pipe(
      switchMap((task) => {
        if (scope && !occurrenceScheduledAt) {
          return throwError(() => new Error('A recurring update requires an occurrence identity'));
        }
        if (scope === 'THIS_ONLY') {
          const unsupported = Object.keys(changes).filter(
            (key) => !['content', 'priority', 'scheduledAt', 'dueAt'].includes(key),
          );
          if (unsupported.length) {
            return throwError(
              () => new Error(`Unsupported single-occurrence fields: ${unsupported.join(', ')}`),
            );
          }
          return this.http.put<Task>(
            `${this.base}/${taskId}/occurrences/${encodeURIComponent(occurrenceScheduledAt!)}`,
            {
              title: 'content' in changes ? changes.content : task.content,
              priority: 'priority' in changes ? changes.priority : task.priority,
              scheduledAt: 'scheduledAt' in changes ? changes.scheduledAt : task.scheduledAt,
              dueAt: 'dueAt' in changes ? changes.dueAt : null,
            },
          );
        }
        // A following-occurrences replacement always creates another recurring series.
        const replacementChanges =
          scope === 'FROM_THIS' ? { ...changes, isRecurring: true } : changes;
        const request = this.toUpdateRequest(task, replacementChanges);
        const url =
          scope === 'FROM_THIS'
            ? `${this.base}/${taskId}/occurrences/${encodeURIComponent(occurrenceScheduledAt!)}/following`
            : `${this.base}/${taskId}`;
        return this.http.put<Task>(url, request);
      }),
    );
  }

  private toUpdateRequest(task: Task, changes: TaskPatch): TaskUpdateRequest {
    const updated = { ...task, ...changes };
    const recurring = changes.isRecurring ?? isRecurringTask(task);
    const dueAt =
      'dueAt' in changes
        ? (changes.dueAt ?? null)
        : task.kind === 'RECURRING_SERIES'
          ? null
          : task.dueAt;
    return {
      content: updated.content,
      type: updated.type ?? 'TODO',
      description: updated.description ?? null,
      projectId: updated.projectId ?? null,
      parentId: updated.parentId ?? null,
      order: updated.order,
      priority: updated.priority,
      labels: updated.labels ?? [],
      scheduledAt: updated.scheduledAt,
      // Complete recurring-series replacements must carry an explicit null deadline.
      dueAt: recurring ? null : dueAt,
      allDay: updated.allDay,
      isRecurring: recurring,
      estimateMinutes: updated.estimateMinutes ?? null,
      mentionContext: updated.mentionContext ?? null,
      recurrenceRule: recurring
        ? 'recurrenceRule' in changes
          ? (changes.recurrenceRule ?? null)
          : isRecurringTask(task)
            ? task.recurrenceRule
            : null
        : null,
    };
  }

  deleteTask(
    taskId: string,
    scope?: RecurrenceScope,
    occurrenceScheduledAt?: string,
  ): Observable<void> {
    const body = scope ? { scope, occurrenceScheduledAt } : undefined;
    return this.http.delete<void>(`${this.base}/${taskId}`, { body });
  }

  closeTask(taskId: string, occurrenceScheduledAt?: string): Observable<Task> {
    return this.http.post<Task>(
      `${this.base}/${taskId}/close`,
      occurrenceScheduledAt ? { occurrenceScheduledAt } : {},
    );
  }

  reopenTask(taskId: string, occurrenceScheduledAt?: string): Observable<Task> {
    return this.http.post<Task>(
      `${this.base}/${taskId}/reopen`,
      occurrenceScheduledAt ? { occurrenceScheduledAt } : {},
    );
  }

  getSubtasks(parentId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${parentId}/subtasks`);
  }
}
