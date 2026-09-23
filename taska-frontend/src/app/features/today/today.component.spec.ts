import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { ProjectService } from '../../core/services/project.service';
import { TaskService } from '../../core/services/task.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { TodayComponent } from './today.component';

describe('TodayComponent overdue tasks', () => {
  let fixture: ComponentFixture<TodayComponent>;
  let getOverdueTasks: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getOverdueTasks = vi.fn(() => of([]));
    await TestBed.configureTestingModule({
      imports: [TodayComponent],
      providers: [
        {
          provide: TaskService,
          useValue: {
            getOverdueTasks,
            getTasks: () => of([]),
            closeTask: () => of({}),
            reopenTask: () => of({}),
            updateTask: () => of({}),
          },
        },
        { provide: ProjectService, useValue: { projects$: of([]) } },
        {
          provide: UiStateService,
          useValue: {
            selectedTask: () => null,
            taskCreated$: new Subject(),
            taskDeleted$: new Subject(),
            taskUpdated$: new Subject(),
            openTaskDetail: () => undefined,
          },
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(TodayComponent);
  });

  it('shows a returned recurring occurrence in the overdue group', () => {
    getOverdueTasks.mockReturnValue(
      of([
        {
          kind: 'RECURRING_OCCURRENCE',
          id: 'series-1',
          content: 'Daily task',
          type: 'TODO',
          order: 0,
          priority: null,
          labels: [],
          isCompleted: false,
          scheduledAt: '2026-09-19T14:00:00Z',
          dueAt: null,
          allDay: false,
          createdAt: null,
          updatedAt: null,
          completedAt: null,
          recurrenceRule: 'FREQ=DAILY',
          rruleEndsAt: null,
          instanceId: null,
          occurrenceScheduledAt: '2026-09-19T14:00:00Z',
          isVirtual: true,
          isDetached: false,
        },
      ]),
    );

    fixture.detectChanges();

    expect(fixture.componentInstance.groups()[0]).toMatchObject({
      key: 'overdue',
      tasks: [{ id: 'series-1', occurrenceScheduledAt: '2026-09-19T14:00:00Z' }],
    });
  });

  it('omits the overdue group when the overdue query is empty', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.groups().map((group) => group.key)).not.toContain('overdue');
  });
});
