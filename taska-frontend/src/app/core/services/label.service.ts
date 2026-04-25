import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Label } from '../models';
import { environment } from '../../../environments/environment';
import {ConfigService} from './config.service';

@Injectable({ providedIn: 'root' })
export class LabelService {
  private http = inject(HttpClient);
  private config = inject(ConfigService);
  private readonly base = `${this.config.apiUrl}/labels`;

  private labelsSubject = new BehaviorSubject<Label[]>([]);
  labels$ = this.labelsSubject.asObservable();

  loadLabels(): Observable<Label[]> {
    return this.http.get<Label[]>(this.base).pipe(
      tap(l => this.labelsSubject.next(l))
    );
  }

  createLabel(data: Partial<Label>): Observable<Label> {
    return this.http.post<Label>(this.base, data).pipe(
      tap(created => this.labelsSubject.next([...this.labelsSubject.value, created]))
    );
  }

  updateLabel(id: string, data: Partial<Label>): Observable<Label> {
    return this.http.put<Label>(`${this.base}/${id}`, data).pipe(
      tap(updated => {
        this.labelsSubject.next(this.labelsSubject.value.map(l => l.id === id ? updated : l));
      })
    );
  }

  deleteLabel(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      tap(() => {
        this.labelsSubject.next(this.labelsSubject.value.filter(l => l.id !== id));
      })
    );
  }
}
