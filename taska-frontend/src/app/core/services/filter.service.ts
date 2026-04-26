import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Filter, Task } from '../models';
import { ConfigService } from './config.service';

@Injectable({ providedIn: 'root' })
export class FilterService {
  private http = inject(HttpClient);
  private config = inject(ConfigService);
  private readonly base = `${this.config.apiUrl}/filters`;

  private filtersSubject = new BehaviorSubject<Filter[]>([]);
  filters$ = this.filtersSubject.asObservable();

  loadFilters(): Observable<Filter[]> {
    return this.http.get<Filter[]>(this.base).pipe(
      tap(f => this.filtersSubject.next(f))
    );
  }

  createFilter(data: Partial<Filter>): Observable<Filter> {
    return this.http.post<Filter>(this.base, data).pipe(
      tap(created => this.filtersSubject.next([...this.filtersSubject.value, created]))
    );
  }

  updateFilter(id: string, data: Partial<Filter> & { clearProject?: boolean }): Observable<Filter> {
    return this.http.put<Filter>(`${this.base}/${id}`, data).pipe(
      tap(updated => {
        this.filtersSubject.next(this.filtersSubject.value.map(f => f.id === id ? updated : f));
      })
    );
  }

  deleteFilter(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      tap(() => {
        this.filtersSubject.next(this.filtersSubject.value.filter(f => f.id !== id));
      })
    );
  }

  getFilter(id: string): Observable<Filter> {
    return this.http.get<Filter>(`${this.base}/${id}`);
  }

  getFilterTasks(id: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.base}/${id}/tasks`);
  }
}
