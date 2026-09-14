import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {Label} from '../models';
import {environment} from '../../../environments/environment';

interface LabelCreateRequest {
  name: string;
  color?: string | null;
  order?: number | null;
  isFavorite?: boolean | null;
}

interface LabelUpdateRequest {
  name: string;
  color: string;
  order: number;
  isFavorite: boolean;
}

@Injectable({ providedIn: 'root' })
export class LabelService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/labels`;

  private labelsSubject = new BehaviorSubject<Label[]>([]);
  labels$ = this.labelsSubject.asObservable();

  loadLabels(): Observable<Label[]> {
    return this.http.get<Label[]>(this.base).pipe(
      tap(l => this.labelsSubject.next(l))
    );
  }

  createLabel(data: LabelCreateRequest): Observable<Label> {
    return this.http.post<Label>(this.base, data).pipe(
      tap(created => this.labelsSubject.next([...this.labelsSubject.value, created]))
    );
  }

  updateLabel(labelId: string, data: LabelUpdateRequest): Observable<Label> {
    return this.http.put<Label>(`${this.base}/${labelId}`, data).pipe(
      tap(updated => {
        this.labelsSubject.next(this.labelsSubject.value.map(l => l.id === labelId ? updated : l));
      })
    );
  }

  deleteLabel(labelId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${labelId}`).pipe(
      tap(() => {
        this.labelsSubject.next(this.labelsSubject.value.filter(l => l.id !== labelId));
      })
    );
  }
}
