import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PlanningCalendarService } from './planning-calendar.service';

describe('PlanningCalendarService', () => {
  let service: PlanningCalendarService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PlanningCalendarService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads planning calendars and their weekly rules', () => {
    service.list().subscribe(calendars => {
      expect(calendars[0].name).toBe('Work');
      expect(calendars[0].rules[0].startMinute).toBe(540);
    });

    const request = http.expectOne(request => request.url.endsWith('/planning-calendars'));
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 'calendar-1', name: 'Work', rules: [{ dayOfWeek: 1, startMinute: 540, endMinute: 1020 }] }]);
  });
});
