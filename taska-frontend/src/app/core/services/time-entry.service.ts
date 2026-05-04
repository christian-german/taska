import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TimeEntry } from '../models';
import { environment } from '../../../environments/environment';

export interface TimeEntryFilters {
  projectId?: string;
  start?: string;  // ISO datetime "2024-05-01T00:00:00"
  end?: string;    // ISO datetime "2024-05-07T23:59:59"
}

@Injectable({ providedIn: 'root' })
export class TimeEntryService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/time-entries`;

  getEntries(filters?: TimeEntryFilters): Observable<TimeEntry[]> {
    let params = new HttpParams();
    if (filters?.projectId) params = params.set('project_id', filters.projectId);
    if (filters?.start)     params = params.set('start', filters.start);
    if (filters?.end)       params = params.set('end', filters.end);
    return this.http.get<TimeEntry[]>(this.base, { params });
  }

  createEntry(data: Pick<TimeEntry, 'startAt' | 'endAt' | 'projectId' | 'description' | 'notes'>): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(this.base, data);
  }

  updateEntry(id: string, data: Partial<Pick<TimeEntry, 'startAt' | 'endAt' | 'projectId' | 'description' | 'notes'>>): Observable<TimeEntry> {
    return this.http.put<TimeEntry>(`${this.base}/${id}`, data);
  }

  deleteEntry(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
