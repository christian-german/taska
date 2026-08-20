import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TaskCreationFeedbackService} from '../../../core/services/task-creation-feedback.service';
import {TaskCreatedToastComponent} from './task-created-toast.component';

describe('TaskCreatedToastComponent', () => {
  let fixture: ComponentFixture<TaskCreatedToastComponent>;
  let feedback: TaskCreationFeedbackService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [TaskCreatedToastComponent]}).compileComponents();
    fixture = TestBed.createComponent(TaskCreatedToastComponent);
    feedback = TestBed.inject(TaskCreationFeedbackService);
  });

  it('renders an accessible non-interactive status while visible', () => {
    feedback.show();
    fixture.detectChanges();

    const status = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    expect(status.textContent).toContain('Tâche créée');
    expect(status.getAttribute('aria-live')).toBe('polite');
    feedback.dismiss();
  });

  it('dismisses automatically', () => {
    vi.useFakeTimers();
    feedback.show();
    expect(feedback.visible()).toBe(true);

    vi.advanceTimersByTime(3000);

    expect(feedback.visible()).toBe(false);
    vi.useRealTimers();
  });
});
