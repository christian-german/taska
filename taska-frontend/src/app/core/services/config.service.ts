import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppConfiguration } from '../models/config.model';
import { tap, shareReplay, map } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private config: AppConfiguration | undefined;
  private config$: Observable<AppConfiguration> | undefined;

  constructor(private http: HttpClient) {}

  loadConfig(): Observable<AppConfiguration> {
    if (!this.config$) {
      this.config$ = this.http.get<AppConfiguration>('/config.json')
        .pipe(
          tap(config => this.config = config),
          shareReplay(1)
        );
    }
    return this.config$;
  }

  get configObservable(): Observable<AppConfiguration> {
    return this.loadConfig();
  }

  get apiUrl(): string {
    if (!this.config) {
      throw new Error('Configuration not loaded');
    }
    return this.config.apiUrl;
  }
}
