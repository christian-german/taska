import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Section } from '../models';
import { environment } from '../../../environments/environment';
import {ConfigService} from './config.service';

@Injectable({ providedIn: 'root' })
export class SectionService {
  private http = inject(HttpClient);
  private config = inject(ConfigService);
  private readonly base = `${this.config.apiUrl}/sections`;

  getSections(projectId?: string): Observable<Section[]> {
    let params = new HttpParams();
    if (projectId) params = params.set('project_id', projectId);
    return this.http.get<Section[]>(this.base, { params });
  }

  createSection(data: Partial<Section>): Observable<Section> {
    return this.http.post<Section>(this.base, data);
  }

  updateSection(id: string, data: Partial<Section>): Observable<Section> {
    return this.http.put<Section>(`${this.base}/${id}`, data);
  }

  deleteSection(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
