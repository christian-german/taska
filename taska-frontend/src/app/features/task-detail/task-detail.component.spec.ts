import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, Subject} from 'rxjs';
import {Task} from '../../core/models';
import {CommentService} from '../../core/services/comment.service';
import {LabelService} from '../../core/services/label.service';
import {ProjectService} from '../../core/services/project.service';
import {TaskService} from '../../core/services/task.service';
import {TaskDetailComponent} from './task-detail.component';

describe('TaskDetailComponent schedule removal', () => {
  let fixture: ComponentFixture<TaskDetailComponent>;
  let component: TaskDetailComponent;
  let updateResult: Subject<Task>;
  let updateTask: ReturnType<typeof vi.fn>;

  const task = (changes: Partial<Task> = {}): Task => ({
    id: 'task-1', content: 'Plan launch', order: 1, priority: 2, labels: ['work'],
    isCompleted: false, scheduledAt: '2026-08-24T09:00:00Z', dueAt: '2026-09-01T00:00:00Z',
    allDay: false, isRecurring: false, createdAt: '', updatedAt: '', ...changes,
  });

  beforeEach(async () => {
    updateResult = new Subject<Task>();
    updateTask = vi.fn(() => updateResult.asObservable());
    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        {provide: TaskService, useValue: {getSubtasks: () => of([]), updateTask}},
        {provide: CommentService, useValue: {getComments: () => of([])}},
        {provide: ProjectService, useValue: {projects$: of([])}},
        {provide: LabelService, useValue: {labels$: of([])}},
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('task', task());
    fixture.detectChanges();
  });

  it('sends a null schedule, preserves the deadline, and adopts only the server response', () => {
    const emitted: Task[] = [];
    component.taskUpdated.subscribe(value => emitted.push(value));

    component.clearDate(new Event('click'));

    expect(updateTask).toHaveBeenCalledWith('task-1', {scheduledAt: null, allDay: false});
    expect(component.task().dueAt).toBe('2026-09-01T00:00:00Z');
    expect(emitted).toEqual([]);

    const serverTask = task({scheduledAt: null});
    updateResult.next(serverTask);
    expect(emitted).toEqual([serverTask]);
  });

  it('shows complete schedule removal even when there is no deadline', () => {
    fixture.componentRef.setInput('task', task({dueAt: null}));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[aria-label="Supprimer la date planifiée"]')).not.toBeNull();
  });

  it('does not emit an unscheduled task when removal fails', () => {
    const emitted: Task[] = [];
    component.taskUpdated.subscribe(value => emitted.push(value));

    component.clearDate(new Event('click'));
    updateResult.error(new Error('failed'));

    expect(emitted).toEqual([]);
    expect(component.task().scheduledAt).toBe('2026-08-24T09:00:00Z');
  });

  it('retains recurrence targeting for complete removal', () => {
    fixture.componentRef.setInput('task', task({isRecurring: true, occurrenceScheduledAt: '2026-08-24T09:00:00Z'}));
    fixture.detectChanges();

    component.clearDate(new Event('click'));
    component.onModifyScope('FROM_THIS');

    expect(updateTask).toHaveBeenCalledWith('task-1', {
      scheduledAt: null, allDay: false, scope: 'FROM_THIS', occurrenceScheduledAt: '2026-08-24T09:00:00Z',
    });
  });
});
