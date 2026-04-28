import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';
import { StatsOverview } from '../models';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private http = inject(HttpClient);
  private config = inject(ConfigService);
  private readonly base = `${this.config.apiUrl}/stats`;

  getOverview(): Observable<StatsOverview> {
    return this.http.get<StatsOverview>(`${this.base}/overview`);
  }
}
