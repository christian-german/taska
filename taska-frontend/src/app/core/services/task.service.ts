import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Task} from '../models';
import {environment} from '../../../environments/environment';

export interface TaskFilters {
  projectId?: string;
  sectionId?: string;
  label?: string;
  filter?: 'today' | 'overdue' | 'upcoming';
  showCompleted?: boolean;
}

@Injectable({providedIn: 'root'})
export class TaskService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/tasks`;

  getTasks(filters?: TaskFilters): Observable<Task[]> {
    let params = new HttpParams();
    if (filters?.projectId) params = params.set('project_id', filters.projectId);
    if (filters?.sectionId) params = params.set('section_id', filters.sectionId);
    if (filters?.label) params = params.set('label', filters.label);
    if (filters?.filter) params = params.set('filter', filters.filter);
    if (filters?.showCompleted) params = params.set('show_completed', 'true');
    return this.http.get<Task[]>(this.base, {params});
  }

  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.base}/${id}`);
  }

  createTask(data: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(this.base, data);
  }

  updateTask(id: string, data: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}`, data);
  }

  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  closeTask(id: string): Observable<Task> {
    return this.http.post<Task>(`${this.base}/${id}/close`, {});
  }

  reopenTask(id: string): Observable<Task> {
    return this.http.post<Task>(`${this.base}/${id}/reopen`, {});
  }

  getSubtasks(parentId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${parentId}/subtasks`);
  }
}
