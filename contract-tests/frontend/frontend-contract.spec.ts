import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  provideHttpClient,
  withFetch,
  withInterceptors,
} from '../../taska-frontend/node_modules/@angular/common/fesm2022/http.mjs';
import { TestBed } from '../../taska-frontend/node_modules/@angular/core/fesm2022/testing.mjs';
import {
  Observable,
  catchError,
  firstValueFrom,
  throwError,
} from '../../taska-frontend/node_modules/rxjs/dist/esm/index.js';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { CommentService } from '../../taska-frontend/src/app/core/services/comment.service';
import { LabelService } from '../../taska-frontend/src/app/core/services/label.service';
import { PlanningCalendarService } from '../../taska-frontend/src/app/core/services/planning-calendar.service';
import { ProjectService } from '../../taska-frontend/src/app/core/services/project.service';
import {
  TaskCreateRequest,
  TaskService,
} from '../../taska-frontend/src/app/core/services/task.service';
import { VersionService } from '../../taska-frontend/src/app/core/services/version.service';
import { Task } from '../../taska-frontend/src/app/core/models';
import { environment } from '../../taska-frontend/src/environments/environment';

const PROJECT_ID = '10000000-0000-4000-8000-000000000001';
const TASK_ID = '20000000-0000-4000-8000-000000000002';
const LABEL_ID = '30000000-0000-4000-8000-000000000003';
const COMMENT_ID = '40000000-0000-4000-8000-000000000004';
const CALENDAR_ID = '50000000-0000-4000-8000-000000000005';
const OCCURRENCE = '2026-09-08T09:00:00Z';
const CONTRACT_FAILURE_MARKER = 'FRONTEND_CONTRACT_VIOLATION:';

interface CapturedRequest {
  method: string;
  url: string;
  headers: Record<string, string>;
  body?: unknown;
}

interface ContractViolation {
  endpoint: string;
  request: CapturedRequest;
  prismValidationMessage: string;
  recommendedFix: string;
}

const contractViolations: ContractViolation[] = [];

function stable(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(stable);
  if (value && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>)
        .sort(([left], [right]) => left.localeCompare(right))
        .map(([key, nested]) => [key, stable(nested)]),
    );
  }
  return value;
}

function capture(request: HttpRequest<unknown>): CapturedRequest {
  const headers = Object.fromEntries(
    request.headers
      .keys()
      .sort()
      .map(name => [
        name,
        name.toLowerCase() === 'authorization' ? '[REDACTED]' : request.headers.get(name) ?? '',
      ]),
  );
  const value: CapturedRequest = {
    method: request.method,
    url: request.urlWithParams.replace(environment.apiUrl, '<PRISM_BASE_URL>'),
    headers,
  };
  if (request.body !== null) value.body = stable(request.body);
  return value;
}

function prismMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return String(error);
  if (typeof error.error === 'string') return error.error;
  if (error.error !== undefined && error.error !== null) {
    return JSON.stringify(stable(error.error));
  }
  return `${error.status} ${error.statusText}`.trim();
}

const contractInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const outgoing = request.clone({
    setHeaders: {
      Authorization: 'Bearer frontend-contract-test-token',
      Prefer: 'dynamic=true',
    },
  });
  return next(outgoing).pipe(
    catchError(error => {
      const requestDetails = capture(outgoing);
      const violation: ContractViolation = {
        endpoint: `${requestDetails.method} ${new URL(outgoing.urlWithParams).pathname}`,
        request: requestDetails,
        prismValidationMessage: prismMessage(error),
        recommendedFix:
          'Align the Angular service request with docs/openapi/taska.openapi.yaml; update the contract first only when the intended API behavior has changed.',
      };
      contractViolations.push(violation);
      return throwError(
        () => new Error(`${CONTRACT_FAILURE_MARKER}${JSON.stringify(stable(violation))}`),
      );
    }),
  );
};

async function exercise<T>(request: Observable<T>): Promise<void> {
  await firstValueFrom(request);
  if (contractViolations.length) {
    throw new Error(
      `${CONTRACT_FAILURE_MARKER}${JSON.stringify(stable(contractViolations[0]))}`,
    );
  }
  expect(contractViolations).toEqual([]);
}

describe('Angular outgoing OpenAPI contract', () => {
  beforeEach(() => {
    const apiUrl = process.env['TASKA_CONTRACT_API_URL'];
    if (!apiUrl) throw new Error('TASKA_CONTRACT_API_URL is required');
    environment.apiUrl = apiUrl;
    contractViolations.length = 0;
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withFetch(), withInterceptors([contractInterceptor]))],
    });
  });

  afterEach(() => TestBed.resetTestingModule());

  describe('CommentService', () => {
    it('GET /comments', () =>
      exercise(TestBed.inject(CommentService).getComments(TASK_ID, PROJECT_ID)));
    it('POST /comments', () =>
      exercise(TestBed.inject(CommentService).createComment({ taskId: TASK_ID, content: 'Note' })));
    it('PUT /comments/{commentId}', () =>
      exercise(TestBed.inject(CommentService).updateComment(COMMENT_ID, { content: 'Updated' })));
    it('DELETE /comments/{commentId}', () =>
      exercise(TestBed.inject(CommentService).deleteComment(COMMENT_ID)));
  });

  describe('LabelService', () => {
    it('GET /labels', () => exercise(TestBed.inject(LabelService).loadLabels()));
    it('POST /labels', () =>
      exercise(
        TestBed.inject(LabelService).createLabel({
          name: 'work',
          color: 'blue',
          order: 1,
          isFavorite: true,
        }),
      ));
    it('PUT /labels/{labelId}', () =>
      exercise(
        TestBed.inject(LabelService).updateLabel(LABEL_ID, {
          name: 'work',
          color: 'blue',
          order: 2,
          isFavorite: false,
        }),
      ));
    it('DELETE /labels/{labelId}', () =>
      exercise(TestBed.inject(LabelService).deleteLabel(LABEL_ID)));
  });

  describe('PlanningCalendarService', () => {
    const calendar = {
      name: 'Work week',
      rules: [{ dayOfWeek: 1, startMinute: 540, endMinute: 1020 }],
    };

    it('GET /planning-calendars', () =>
      exercise(TestBed.inject(PlanningCalendarService).list()));
    it('POST /planning-calendars', () =>
      exercise(TestBed.inject(PlanningCalendarService).create(calendar)));
    it('PUT /planning-calendars/{planningCalendarId}', () =>
      exercise(TestBed.inject(PlanningCalendarService).update(CALENDAR_ID, calendar)));
  });

  describe('ProjectService', () => {
    const project = {
      name: 'Launch',
      color: '#4073ff',
      parentId: null,
      order: 1,
      isFavorite: true,
      viewStyle: 'LIST' as const,
      planningCalendarId: CALENDAR_ID,
    };

    it('GET /projects', () => exercise(TestBed.inject(ProjectService).loadProjects()));
    it('GET /projects/{projectId}', () =>
      exercise(TestBed.inject(ProjectService).getProject(PROJECT_ID)));
    it('POST /projects', () =>
      exercise(TestBed.inject(ProjectService).createProject(project)));
    it('PUT /projects/{projectId}', () =>
      exercise(TestBed.inject(ProjectService).updateProject(PROJECT_ID, project)));
    it('DELETE /projects/{projectId}', () =>
      exercise(TestBed.inject(ProjectService).deleteProject(PROJECT_ID)));
    it('PATCH /projects/reorder', () =>
      exercise(
        TestBed.inject(ProjectService).reorderProjects([{ id: PROJECT_ID, order: 2 }]),
      ));
    it('GET /projects/{projectId}/tasks', () =>
      exercise(TestBed.inject(ProjectService).getProjectTasks(PROJECT_ID)));
  });

  describe('TaskService', () => {
    const createRequest: TaskCreateRequest = {
      content: 'Prepare launch',
      type: 'TODO',
      description: 'Review checklist',
      projectId: PROJECT_ID,
      parentId: null,
      order: 1,
      priority: 2,
      labels: ['work'],
      scheduledAt: OCCURRENCE,
      dueAt: null,
      allDay: false,
      isRecurring: true,
      estimateMinutes: 30,
      mentionContext: 'launch',
      recurrenceRule: 'FREQ=DAILY',
    };

    it('GET /tasks with every supported query parameter', () =>
      exercise(
        TestBed.inject(TaskService).getTasks({
          projectId: PROJECT_ID,
          label: 'work',
          showCompleted: false,
          date: '2026-09-08',
          from: '2026-09-08',
          to: '2026-09-14',
        }),
      ));
    it('GET /tasks/{taskId}', () => exercise(TestBed.inject(TaskService).getTask(TASK_ID)));
    it('POST /tasks', () =>
      exercise(TestBed.inject(TaskService).createTask(createRequest)));
    it('PUT /tasks/{taskId}', () =>
      exercise(TestBed.inject(TaskService).updateTask(TASK_ID, { content: 'Updated task' })));
    it('PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}', () =>
      exercise(
        TestBed.inject(TaskService).updateTask(TASK_ID, {
          scope: 'THIS_ONLY',
          occurrenceScheduledAt: OCCURRENCE,
          content: 'Updated occurrence',
        }),
      ));
    it('PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following', () =>
      exercise(
        TestBed.inject(TaskService).updateTask(TASK_ID, {
          scope: 'FROM_THIS',
          occurrenceScheduledAt: OCCURRENCE,
          content: 'Updated series',
        }),
      ));
    it('DELETE /tasks/{taskId} without a body', () =>
      exercise(TestBed.inject(TaskService).deleteTask(TASK_ID)));
    it('DELETE /tasks/{taskId} with recurrence scope', () =>
      exercise(TestBed.inject(TaskService).deleteTask(TASK_ID, 'THIS_ONLY', OCCURRENCE)));
    it('POST /tasks/{taskId}/close without occurrence identity', () =>
      exercise(TestBed.inject(TaskService).closeTask(TASK_ID)));
    it('POST /tasks/{taskId}/close with occurrence identity', () =>
      exercise(TestBed.inject(TaskService).closeTask(TASK_ID, OCCURRENCE)));
    it('POST /tasks/{taskId}/reopen without occurrence identity', () =>
      exercise(TestBed.inject(TaskService).reopenTask(TASK_ID)));
    it('POST /tasks/{taskId}/reopen with occurrence identity', () =>
      exercise(TestBed.inject(TaskService).reopenTask(TASK_ID, OCCURRENCE)));
    it('GET /tasks/{taskId}/subtasks', () =>
      exercise(TestBed.inject(TaskService).getSubtasks(TASK_ID)));
  });

  describe('VersionService', () => {
    it('GET /version', () => exercise(TestBed.inject(VersionService).getVersion()));
  });
});
