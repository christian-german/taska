import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PlanningCalendar } from '../models';
@Injectable({providedIn:'root'}) export class PlanningCalendarService {
 private http=inject(HttpClient); private base=`${environment.apiUrl}/planning-calendars`;
 list():Observable<PlanningCalendar[]>{return this.http.get<PlanningCalendar[]>(this.base);}
 create(data:Pick<PlanningCalendar,'name'|'rules'>):Observable<PlanningCalendar>{return this.http.post<PlanningCalendar>(this.base,data);}
 update(id:string,data:Pick<PlanningCalendar,'name'|'rules'>):Observable<PlanningCalendar>{return this.http.put<PlanningCalendar>(`${this.base}/${id}`,data);}
}
