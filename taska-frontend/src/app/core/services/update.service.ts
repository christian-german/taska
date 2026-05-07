import { Injectable } from '@angular/core';
import { check } from '@tauri-apps/plugin-updater';
import { relaunch } from '@tauri-apps/plugin-process';
import { Subject } from 'rxjs';

export interface UpdateInfo {
  version: string;
  notes: string;
}

@Injectable({ providedIn: 'root' })
export class UpdateService {
  private updateAvailable$ = new Subject<UpdateInfo>();
  updateAvailable = this.updateAvailable$.asObservable();

  async checkForUpdates(): Promise<void> {
    try {
      console.log('Checking for updates...');
      const update = await check();
      if (update?.available) {
        this.updateAvailable$.next({
          version: update.version,
          notes: update.body ?? '',
        });
      }
    } catch (e) {
      console.warn('Update check failed:', e);
    }
  }

  async downloadAndInstall(
    onProgress?: (downloaded: number, total: number | null) => void
  ): Promise<void> {
    const update = await check();
    if (!update?.available) return;

    let totalSize: number | null = null;
    let downloaded = 0;

    await update.downloadAndInstall((event) => {
      switch (event.event) {
        case 'Started':
          totalSize = event.data.contentLength ?? null;
          break;
        case 'Progress':
          downloaded += event.data.chunkLength;
          onProgress?.(downloaded, totalSize);
          break;
        case 'Finished':
          onProgress?.(downloaded, totalSize);
          break;
      }
    });

    await relaunch();
  }
}
