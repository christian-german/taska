import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PlanningCalendar } from '../models';

type PlanningCalendarCreateRequest = Pick<PlanningCalendar, 'name'> &
  Partial<Pick<PlanningCalendar, 'rules'>>;
type PlanningCalendarUpdateRequest = Pick<PlanningCalendar, 'name' | 'rules'>;

@Injectable({ providedIn: 'root' })
export class PlanningCalendarService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/planning-calendars`;

  list(): Observable<PlanningCalendar[]> {
    return this.http.get<PlanningCalendar[]>(this.base);
  }

  create(data: PlanningCalendarCreateRequest): Observable<PlanningCalendar> {
    return this.http.post<PlanningCalendar>(this.base, data);
  }

  update(planningCalendarId: string, data: PlanningCalendarUpdateRequest): Observable<PlanningCalendar> {
    return this.http.put<PlanningCalendar>(`${this.base}/${planningCalendarId}`, data);
  }
}
