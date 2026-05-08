import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {UpdateInfo, UpdateService} from '../../core/services/update.service';

@Component({
  standalone: true,
  selector: 'app-update-dialog',
  template: `
    @if (visible) {
      <div class="modal-veil" (click)="dismiss()">
        <div class="modal" (click)="$event.stopPropagation()" style="padding: 28px 28px 22px; width: min(400px, 92vw);">

          <div class="script" style="font-size: 22px; margin-bottom: 4px;">
            Mise à jour disponible
          </div>
          <div class="mono" style="font-size: 12px; color: var(--mute); margin-bottom: 16px;">
            version {{ updateInfo?.version }}
          </div>

          @if (updateInfo?.notes) {
            <div style="font-size: 13px; color: var(--ink-2); line-height: 1.6; margin-bottom: 20px;">
              {{ updateInfo?.notes }}
            </div>
          }

          @if (downloading) {
            <div class="mono" style="font-size: 11px; color: var(--mute); margin-bottom: 8px;">
              Téléchargement… {{ progress }}%
            </div>
            <div class="progress">
              <div [style.width.%]="progress"></div>
            </div>
          } @else {
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px;">
              <button class="btn btn-ghost" (click)="dismiss()">Plus tard</button>
              <button class="btn btn-primary" (click)="install()">Installer</button>
            </div>
          }

        </div>
      </div>
    }
  `
})
export class UpdateDialogComponent implements OnInit {
  private updateService = inject(UpdateService);
  private cdr = inject(ChangeDetectorRef);
  visible = false;
  downloading = false;
  progress = 0;
  updateInfo: UpdateInfo | null = null;

  ngOnInit() {
    this.updateService.updateAvailable.subscribe((info) => {
      this.updateInfo = info;
      this.visible = true;
      this.cdr.markForCheck();
    });
  }

  dismiss() { this.visible = false; this.cdr.markForCheck(); }

  async install() {
    this.downloading = true;
    this.cdr.markForCheck();
    await this.updateService.downloadAndInstall((downloaded, total) => {
      if (total) this.progress = Math.round((downloaded / total) * 100);
      this.cdr.markForCheck();
    });
  }
}
