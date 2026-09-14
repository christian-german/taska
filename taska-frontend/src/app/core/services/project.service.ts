import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Project, Task } from '../models';

export interface ReorderItem {
  id: string;
  order: number;
}

export interface ProjectCreateRequest {
  name: string;
  color?: string | null;
  parentId?: string | null;
  order?: number | null;
  isFavorite?: boolean | null;
  viewStyle?: Project['viewStyle'] | null;
  planningCalendarId?: string | null;
}

export interface ProjectUpdateRequest {
  name: string;
  color: string;
  parentId: string | null;
  order: number;
  isFavorite: boolean;
  viewStyle: Project['viewStyle'];
  planningCalendarId: string;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/projects`;

  private readonly projectsSubject = new BehaviorSubject<Project[]>([]);
  readonly projects$ = this.projectsSubject.asObservable();

  loadProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.base).pipe(
      tap(projects => this.projectsSubject.next(projects)),
    );
  }

  getProject(projectId: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${projectId}`);
  }

  createProject(data: ProjectCreateRequest): Observable<Project> {
    return this.http.post<Project>(this.base, data).pipe(
      tap(() => this.loadProjects().subscribe()),
    );
  }

  updateProject(projectId: string, data: ProjectUpdateRequest): Observable<Project> {
    return this.http.put<Project>(`${this.base}/${projectId}`, data).pipe(
      tap(updated => {
        const current = this.projectsSubject.value;
        this.projectsSubject.next(current.map(project => (project.id === projectId ? updated : project)));
      }),
    );
  }

  deleteProject(projectId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${projectId}`).pipe(
      tap(() => {
        this.projectsSubject.next(this.projectsSubject.value.filter(project => project.id !== projectId));
      }),
    );
  }

  reorderProjects(items: ReorderItem[]): Observable<void> {
    return this.http.patch<void>(`${this.base}/reorder`, items).pipe(
      tap(() => {
        const updated = this.projectsSubject.value.map(project => {
          const item = items.find(reorderItem => reorderItem.id === project.id);
          return item ? { ...project, order: item.order } : project;
        });
        this.projectsSubject.next([...updated].sort((a, b) => a.order - b.order));
      }),
    );
  }

  getProjectTasks(projectId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${projectId}/tasks`);
  }

}
