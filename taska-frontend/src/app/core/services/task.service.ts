import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Task, RecurrenceScope} from '../models';
import {environment} from '../../../environments/environment';

export interface TaskFilters {
  projectId?: string;
  sectionId?: string;
  label?: string;
  filter?: 'today' | 'overdue' | 'upcoming';
  showCompleted?: boolean;
  date?: string;
  from?: string;
  to?: string;
}

@Injectable({providedIn: 'root'})
export class TaskService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/tasks`;

  getTasks(filters?: TaskFilters): Observable<Task[]> {
    let params = new HttpParams();
    if (filters?.projectId)    params = params.set('project_id', filters.projectId);
    if (filters?.sectionId)    params = params.set('section_id', filters.sectionId);
    if (filters?.label)        params = params.set('label', filters.label);
    if (filters?.filter)       params = params.set('filter', filters.filter);
    if (filters?.showCompleted) params = params.set('show_completed', 'true');
    if (filters?.date)         params = params.set('date', filters.date);
    if (filters?.from)         params = params.set('from', filters.from);
    if (filters?.to)           params = params.set('to', filters.to);
    return this.http.get<Task[]>(this.base, {params});
  }

  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.base}/${id}`);
  }

  createTask(data: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(this.base, data);
  }

  updateTask(id: string, data: Partial<Task> & { scope?: RecurrenceScope; scheduledAt?: string | null }): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}`, data);
  }

  deleteTask(id: string, scope?: RecurrenceScope, scheduledAt?: string): Observable<void> {
    const body = scope ? {scope, scheduledAt} : undefined;
    return this.http.delete<void>(`${this.base}/${id}`, {body});
  }

  closeTask(id: string, scheduledAt?: string): Observable<Task> {
    return this.http.post<Task>(`${this.base}/${id}/close`, scheduledAt ? {scheduledAt} : {});
  }

  reopenTask(id: string, scheduledAt?: string): Observable<Task> {
    return this.http.post<Task>(`${this.base}/${id}/reopen`, scheduledAt ? {scheduledAt} : {});
  }

  getSubtasks(parentId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${parentId}/subtasks`);
  }
}
