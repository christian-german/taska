import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {Project, Task} from '../../../core/models';
import {LabelService} from '../../../core/services/label.service';
import {TaskRowComponent} from './task-row.component';

describe('TaskRowComponent presentation', () => {
  let fixture: ComponentFixture<TaskRowComponent>;

  const task: Task = {
    id: 'task-1',
    content: 'Prepare launch',
    order: 1,
    priority: 4,
    labels: ['work'],
    isCompleted: false,
    scheduledAt: '2026-08-24T09:00:00Z',
    dueAt: null,
    allDay: false,
    estimateMinutes: 45,
    isRecurring: true,
    recurrenceRule: 'weekly',
    mentionContext: 'alex',
    type: 'APPOINTMENT',
    createdAt: '',
    updatedAt: '',
  };

  const project: Project = {
    id: 'project-1',
    name: 'Launch',
    color: 'blue',
    order: 1,
    isFavorite: false,
    viewStyle: 'LIST',
    isInboxProject: false,
    planningCalendarId: 'calendar-1',
    createdAt: '',
    updatedAt: '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskRowComponent],
      providers: [{provide: LabelService, useValue: {labels$: of([])}}],
    }).compileComponents();
    fixture = TestBed.createComponent(TaskRowComponent);
    fixture.componentRef.setInput('task', task);
    fixture.componentRef.setInput('project', project);
    fixture.detectChanges();
  });

  it('omits suggestion markers while retaining unrelated task metadata', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).not.toContain('suggéré');
    expect(fixture.nativeElement.querySelector('app-icon[name="zap"]')).toBeNull();
    expect(text).toContain('rendez-vous');
    expect(text).toContain('45min');
    expect(text).toContain('hebdomadaire');
    expect(text).toContain('Launch');
    expect(text).toContain('@alex');
    expect(text).toContain('work');
  });
});
