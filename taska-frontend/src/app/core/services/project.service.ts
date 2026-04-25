import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Project, Section, Task } from '../models';
import { environment } from '../../../environments/environment';

export interface ReorderItem { id: string; order: number; }

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/projects`;

  private projectsSubject = new BehaviorSubject<Project[]>([]);
  projects$ = this.projectsSubject.asObservable();

  loadProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.base).pipe(
      tap(p => this.projectsSubject.next(p))
    );
  }

  getProject(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${id}`);
  }

  createProject(data: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(this.base, data).pipe(
      tap(() => this.loadProjects().subscribe())
    );
  }

  updateProject(id: string, data: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.base}/${id}`, data).pipe(
      tap(updated => {
        const current = this.projectsSubject.value;
        this.projectsSubject.next(current.map(p => p.id === id ? updated : p));
      })
    );
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      tap(() => {
        this.projectsSubject.next(this.projectsSubject.value.filter(p => p.id !== id));
      })
    );
  }

  reorderProjects(items: ReorderItem[]): Observable<void> {
    return this.http.patch<void>(`${this.base}/reorder`, items).pipe(
      tap(() => {
        const updated = this.projectsSubject.value.map(p => {
          const item = items.find(i => i.id === p.id);
          return item ? { ...p, order: item.order } : p;
        });
        this.projectsSubject.next([...updated].sort((a, b) => a.order - b.order));
      })
    );
  }

  getProjectTasks(id: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${id}/tasks`);
  }

  getProjectSections(id: string): Observable<Section[]> {
    return this.http.get<Section[]>(`${this.base}/${id}/sections`);
  }
}
