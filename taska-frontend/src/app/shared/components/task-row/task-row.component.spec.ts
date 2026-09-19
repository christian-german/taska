import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { RecurringTaskOccurrence } from '../../../core/models';
import { LabelService } from '../../../core/services/label.service';
import { TaskRowComponent } from './task-row.component';

describe('TaskRowComponent detached occurrence', () => {
  let fixture: ComponentFixture<TaskRowComponent>;
  let component: TaskRowComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskRowComponent],
      providers: [{ provide: LabelService, useValue: { labels$: of([]) } }],
    }).compileComponents();
    fixture = TestBed.createComponent(TaskRowComponent);
    component = fixture.componentInstance;
  });

  it('shows the detached badge and targets inline edits to this occurrence', () => {
    const occurrence = detachedOccurrence();
    fixture.componentRef.setInput('task', occurrence);
    fixture.detectChanges();
    const updates: unknown[] = [];
    component.updated.subscribe((update) => updates.push(update));

    component.draft.set('Updated occurrence');
    component.commitEdit();

    expect(fixture.nativeElement.textContent).toContain('Hors série');
    expect(updates).toEqual([
      {
        id: occurrence.id,
        patch: {
          content: 'Updated occurrence',
          scope: 'THIS_ONLY',
          occurrenceScheduledAt: occurrence.occurrenceScheduledAt,
        },
      },
    ]);
  });
});

function detachedOccurrence(): RecurringTaskOccurrence {
  return {
    kind: 'RECURRING_OCCURRENCE',
    id: 'task-1',
    content: 'Detached occurrence',
    type: 'TODO',
    description: null,
    projectId: null,
    parentId: null,
    order: 0,
    priority: null,
    labels: [],
    scheduledAt: '2026-08-24T09:00:00Z',
    dueAt: null,
    allDay: false,
    estimateMinutes: null,
    mentionContext: null,
    createdAt: '2026-08-01T00:00:00Z',
    updatedAt: '2026-08-01T00:00:00Z',
    recurrenceRule: 'FREQ=DAILY',
    rruleEndsAt: '2026-08-23T09:00:00Z',
    isCompleted: false,
    completedAt: null,
    instanceId: 'instance-1',
    occurrenceScheduledAt: '2026-08-24T09:00:00Z',
    isVirtual: false,
    isDetached: true,
  };
}
