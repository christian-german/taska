import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  private http = inject(HttpClient);
  private configService = inject(ConfigService);

  private version$ = this.http
    .get<{ version: string }>(`${this.configService.apiUrl}/version`)
    .pipe(
      map(response => response.version),
      catchError(() => of('unknown')),
      shareReplay(1)
    );

  getVersion(): Observable<string> {
    return this.version$;
  }
}
