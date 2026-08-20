import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {TaskService} from './task.service';
import {TaskCreationFeedbackService} from './task-creation-feedback.service';

describe('TaskService task creation feedback', () => {
  let http: HttpTestingController;
  let service: TaskService;
  let feedback: TaskCreationFeedbackService;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(TaskService);
    feedback = TestBed.inject(TaskCreationFeedbackService);
  });

  afterEach(() => http.verify());

  it('shows feedback only after creation succeeds', () => {
    service.createTask({content: 'New task'}).subscribe();
    expect(feedback.visible()).toBe(false);

    http.expectOne(request => request.method === 'POST' && request.url.endsWith('/tasks'))
      .flush({id: '1', content: 'New task'});

    expect(feedback.visible()).toBe(true);
  });

  it('does not show feedback when creation fails', () => {
    service.createTask({content: 'New task'}).subscribe({error: () => undefined});
    http.expectOne(request => request.method === 'POST' && request.url.endsWith('/tasks'))
      .flush('failed', {status: 500, statusText: 'Server error'});

    expect(feedback.visible()).toBe(false);
  });
});
