import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {RecurrenceScope} from '../../../core/models';

@Component({
  selector: 'app-recurrence-scope-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="cancelled.emit()">
      <div class="modal" (click)="$event.stopPropagation()"
           style="padding: 24px 24px 18px; width: min(360px, 92vw);">

        <div style="font-size: 16px; font-weight: 600; color: var(--ink); margin-bottom: 16px;">
          {{ title() }}
        </div>

        <div style="display: flex; flex-direction: column; gap: 8px;">
          <button class="btn btn-ghost"
                  style="justify-content: flex-start; text-align: left; padding: 12px 14px;"
                  (click)="selected.emit('THIS_ONLY')">
            <span style="font-weight: 500;">Cette occurrence seulement</span>
          </button>
          <button class="btn btn-ghost"
                  style="justify-content: flex-start; text-align: left; padding: 12px 14px;"
                  (click)="selected.emit('FROM_THIS')">
            <span style="font-weight: 500;">Cette occurrence et les suivantes</span>
          </button>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
          <button class="btn btn-ghost" (click)="cancelled.emit()">Annuler</button>
        </div>
      </div>
    </div>
  `,
})
export class RecurrenceScopeDialogComponent {
  title = input('Modifier la récurrence');
  selected = output<RecurrenceScope>();
  cancelled = output<void>();
}
