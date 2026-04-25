import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CommentService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/comments`;

  getComments(taskId?: string, projectId?: string): Observable<Comment[]> {
    let params = new HttpParams();
    if (taskId) params = params.set('task_id', taskId);
    if (projectId) params = params.set('project_id', projectId);
    return this.http.get<Comment[]>(this.base, { params });
  }

  createComment(data: Partial<Comment>): Observable<Comment> {
    return this.http.post<Comment>(this.base, data);
  }

  updateComment(id: string, data: Partial<Comment>): Observable<Comment> {
    return this.http.put<Comment>(`${this.base}/${id}`, data);
  }

  deleteComment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
