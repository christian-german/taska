import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { APP_VERSION } from '../../core/constants/app-version';
import { VersionService } from '../../core/services/version.service';

@Component({
  selector: 'app-about-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()"
           style="padding: 32px 28px 24px; width: min(340px, 92vw); text-align: center;">
        <div class="script" style="font-size: 36px; line-height: 1; color: var(--ink);">taska</div>
        <svg width="56" height="8" viewBox="0 0 56 8" style="display: block; margin: 4px auto 0;">
          <path d="M2 5 Q 8 1, 14 5 T 26 5 T 38 5 T 50 5"
                stroke="var(--orange)" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
        <div style="height: 1px; background: var(--line); margin: 20px 0;"></div>
        <div style="display: flex; flex-direction: column; gap: 6px; text-align: left;">
          <div style="display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--ink-2);">
            <span style="color: var(--mute);">Frontend</span>
            <span class="mono" style="font-size: 12px;">v{{ frontendVersion }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--ink-2);">
            <span style="color: var(--mute);">API</span>
            <span class="mono" style="font-size: 12px;">v{{ apiVersion() }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class AboutDialogComponent {
  private versionService = inject(VersionService);

  close = output<void>();
  readonly frontendVersion = APP_VERSION;
  readonly apiVersion = toSignal(this.versionService.getVersion(), { initialValue: '...' });
}
