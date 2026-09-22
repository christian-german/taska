import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment } from '../models';

interface CommentCreateRequest {
  taskId: string;
  content: string;
}

interface CommentUpdateRequest {
  content: string;
}

@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/comments`;

  getComments(taskId?: string): Observable<Comment[]> {
    let params = new HttpParams();
    if (taskId) params = params.set('taskId', taskId);
    return this.http.get<Comment[]>(this.base, { params });
  }

  createComment(data: CommentCreateRequest): Observable<Comment> {
    return this.http.post<Comment>(this.base, data);
  }

  updateComment(commentId: string, data: CommentUpdateRequest): Observable<Comment> {
    return this.http.put<Comment>(`${this.base}/${commentId}`, data);
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${commentId}`);
  }
}
