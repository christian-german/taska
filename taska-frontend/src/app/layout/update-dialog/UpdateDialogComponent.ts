import {Component, inject, OnInit} from '@angular/core';
import {UpdateInfo, UpdateService} from '../../core/services/update.service';

@Component({
  standalone: true,
  selector: 'app-update-dialog',
  template: `
    @if (visible) {
      <div class="overlay">
        <div class="dialog">
          <h2>Mise à jour disponible — v{{ updateInfo?.version }}</h2>
          <p class="notes">{{ updateInfo?.notes }}</p>

          @if (downloading) {
            <div class="progress-bar">
              <div class="fill" [style.width.%]="progress"></div>
            </div>
            <span>{{ progress }}%</span>
          } @else {
            <div class="actions">
              <button (click)="dismiss()">Plus tard</button>
              <button class="primary" (click)="install()">Installer</button>
            </div>
          }
        </div>
      </div>
    }
  `
})
export class UpdateDialogComponent implements OnInit {
  private updateService = inject(UpdateService);
  visible = false;
  downloading = false;
  progress = 0;
  updateInfo: UpdateInfo | null = null;

  ngOnInit() {
    this.updateService.updateAvailable.subscribe((info) => {
      this.updateInfo = info;
      this.visible = true;
    });
  }

  dismiss() { this.visible = false; }

  async install() {
    this.downloading = true;
    await this.updateService.downloadAndInstall((downloaded, total) => {
      if (total) this.progress = Math.round((downloaded / total) * 100);
    });
  }
}
