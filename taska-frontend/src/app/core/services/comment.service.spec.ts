import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CommentService } from './comment.service';

describe('CommentService', () => {
  let http: HttpTestingController;
  let service: CommentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(CommentService);
  });

  afterEach(() => http.verify());

  it('uses the camelCase comment query contract', () => {
    service.getComments('task-1').subscribe();

    const request = http.expectOne((candidate) => candidate.url.endsWith('/comments'));
    expect(request.request.params.keys()).toEqual(['taskId']);
    expect(request.request.params.get('taskId')).toBe('task-1');
    request.flush([]);
  });
});
