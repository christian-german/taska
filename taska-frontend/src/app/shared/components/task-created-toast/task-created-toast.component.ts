import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {TaskCreationFeedbackService} from '../../../core/services/task-creation-feedback.service';

@Component({
  selector: 'app-task-created-toast',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (feedback.visible()) {
      <div class="task-created-toast" role="status" aria-live="polite" aria-atomic="true">
        Tâche créée
      </div>
    }
  `,
})
export class TaskCreatedToastComponent {
  protected readonly feedback = inject(TaskCreationFeedbackService);
}
