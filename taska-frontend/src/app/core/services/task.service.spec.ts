import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TaskService } from './task.service';
import { TaskCreationFeedbackService } from './task-creation-feedback.service';
import { NonRecurringTask, RecurringTaskSeries, Task } from '../models';

describe('TaskService task creation feedback', () => {
  let http: HttpTestingController;
  let service: TaskService;
  let feedback: TaskCreationFeedbackService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(TaskService);
    feedback = TestBed.inject(TaskCreationFeedbackService);
  });

  afterEach(() => http.verify());

  it('shows feedback only after creation succeeds', () => {
    service.createTask({ content: 'New task' }).subscribe();
    expect(feedback.visible()).toBe(false);

    http
      .expectOne((request) => request.method === 'POST' && request.url.endsWith('/tasks'))
      .flush({ id: '1', content: 'New task' });

    expect(feedback.visible()).toBe(true);
  });

  it('does not show feedback when creation fails', () => {
    service.createTask({ content: 'New task' }).subscribe({ error: () => undefined });
    http
      .expectOne((request) => request.method === 'POST' && request.url.endsWith('/tasks'))
      .flush('failed', { status: 500, statusText: 'Server error' });

    expect(feedback.visible()).toBe(false);
  });

  it('uses the camelCase task query contract', () => {
    service
      .getTasks({
        projectId: 'project-1',
        label: 'work',
        showCompleted: false,
        date: '2026-09-08',
        from: '2026-09-08',
        to: '2026-09-14',
      })
      .subscribe();

    const request = http.expectOne((candidate) => candidate.url.endsWith('/tasks'));
    expect(request.request.params.keys().sort()).toEqual([
      'date',
      'from',
      'label',
      'projectId',
      'showCompleted',
      'to',
    ]);
    expect(request.request.params.get('projectId')).toBe('project-1');
    expect(request.request.params.get('showCompleted')).toBe('false');
    request.flush([]);
  });

  it('builds a complete replacement payload and preserves an explicit schedule clear', () => {
    const task = taskFixture();
    service.updateTask(task.id, { scheduledAt: null }).subscribe();

    http
      .expectOne((request) => request.method === 'GET' && request.url.endsWith(`/tasks/${task.id}`))
      .flush(task);
    const request = http.expectOne(
      (request) => request.method === 'PUT' && request.url.endsWith(`/tasks/${task.id}`),
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      content: task.content,
      type: 'TODO',
      description: task.description,
      projectId: task.projectId,
      parentId: null,
      order: task.order,
      priority: task.priority,
      labels: task.labels,
      scheduledAt: null,
      dueAt: task.dueAt,
      allDay: task.allDay,
      isRecurring: false,
      estimateMinutes: task.estimateMinutes,
      mentionContext: task.mentionContext,
      recurrenceRule: null,
    });
    request.flush({ ...task, scheduledAt: null });
  });

  it('uses the following-series endpoint with a complete replacement payload', () => {
    const task = recurringTaskFixture({ recurrenceRule: 'FREQ=DAILY' });
    const occurrence = '2026-08-24T09:00:00Z';
    service
      .updateTask(task.id, {
        scope: 'FROM_THIS',
        occurrenceScheduledAt: occurrence,
        content: 'Future title',
      })
      .subscribe();

    http
      .expectOne((request) => request.method === 'GET' && request.url.endsWith(`/tasks/${task.id}`))
      .flush(task);
    const request = http.expectOne(
      (request) =>
        request.method === 'PUT' &&
        request.url.endsWith(
          `/tasks/${task.id}/occurrences/${encodeURIComponent(occurrence)}/following`,
        ),
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toMatchObject({
      content: 'Future title',
      type: 'TODO',
      dueAt: null,
      isRecurring: true,
      recurrenceRule: 'FREQ=DAILY',
    });
    expect(Object.keys(request.request.body)).toHaveLength(15);
    request.flush(task);
  });

  it('uses the narrow occurrence endpoint for a single recurring occurrence', () => {
    const task = recurringTaskFixture({ recurrenceRule: 'FREQ=DAILY' });
    const occurrence = '2026-08-24T09:00:00Z';
    service
      .updateTask(task.id, {
        scope: 'THIS_ONLY',
        occurrenceScheduledAt: occurrence,
        scheduledAt: null,
      })
      .subscribe();

    http
      .expectOne(
        (request) =>
          request.method === 'GET' &&
          request.url.endsWith(`/tasks/${task.id}/occurrences/${encodeURIComponent(occurrence)}`),
      )
      .flush({
        ...task,
        kind: 'RECURRING_OCCURRENCE',
        dueAt: '2026-08-25T09:00:00Z',
        isCompleted: false,
        completedAt: null,
        instanceId: null,
        occurrenceScheduledAt: occurrence,
        isVirtual: true,
        isDetached: false,
      });
    const request = http.expectOne(
      (request) =>
        request.method === 'PUT' &&
        request.url.endsWith(`/tasks/${task.id}/occurrences/${encodeURIComponent(occurrence)}`),
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      title: task.content,
      priority: task.priority,
      scheduledAt: null,
      dueAt: '2026-08-25T09:00:00Z',
    });
    request.flush(task);
  });

  it('retrieves one occurrence by its stable identity', () => {
    const occurrence = '2026-08-24T09:00:00Z';

    service.getOccurrence('task-1', occurrence).subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.method === 'GET' &&
        candidate.url.endsWith(`/tasks/task-1/occurrences/${encodeURIComponent(occurrence)}`),
    );
    request.flush({});
  });
});

function taskFixture(overrides: Partial<NonRecurringTask> = {}): NonRecurringTask {
  return {
    kind: 'NON_RECURRING',
    id: 'task-1',
    content: 'Plan launch',
    type: 'TODO',
    description: 'Keep this',
    projectId: 'project-1',
    order: 3,
    priority: 2,
    labels: ['work'],
    isCompleted: false,
    scheduledAt: '2026-08-24T09:00:00Z',
    dueAt: '2026-08-25T09:00:00Z',
    allDay: false,
    completedAt: null,
    estimateMinutes: 30,
    mentionContext: 'context',
    createdAt: '2026-08-01T00:00:00Z',
    updatedAt: '2026-08-01T00:00:00Z',
    ...overrides,
  };
}

function recurringTaskFixture(overrides: Partial<RecurringTaskSeries> = {}): RecurringTaskSeries {
  const {
    dueAt: _dueAt,
    isCompleted: _isCompleted,
    completedAt: _completedAt,
    ...base
  } = taskFixture();
  return {
    ...base,
    kind: 'RECURRING_SERIES',
    recurrenceRule: 'FREQ=DAILY',
    rruleEndsAt: null,
    ...overrides,
  };
}
