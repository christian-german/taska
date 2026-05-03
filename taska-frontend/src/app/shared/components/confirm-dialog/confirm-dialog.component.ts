import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="cancelled.emit()">
      <div class="modal" (click)="$event.stopPropagation()"
           style="padding: 28px 28px 22px; width: min(380px, 92vw);">

        <div style="font-size: 17px; font-weight: 600; color: var(--ink); margin-bottom: 8px;">
          {{ title() }}
        </div>

        @if (message()) {
          <div class="mono" style="font-size: 12px; color: var(--mute); line-height: 1.6;">
            {{ message() }}
          </div>
        }

        <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 22px;">
          <button class="btn btn-ghost" (click)="cancelled.emit()">Annuler</button>
          <button class="btn"
                  (click)="confirmed.emit()"
                  [style.background]="danger() ? 'var(--p1)' : 'var(--ink)'"
                  style="color: #fff; border-color: transparent;">
            {{ confirmLabel() }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDialogComponent {
  title = input.required<string>();
  message = input('');
  confirmLabel = input('Supprimer');
  danger = input(true);

  confirmed = output<void>();
  cancelled = output<void>();
}
