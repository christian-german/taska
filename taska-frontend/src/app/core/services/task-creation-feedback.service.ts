import {Injectable, signal} from '@angular/core';

@Injectable({providedIn: 'root'})
export class TaskCreationFeedbackService {
  readonly visible = signal(false);
  private dismissal?: ReturnType<typeof setTimeout>;

  show(): void {
    this.visible.set(true);
    clearTimeout(this.dismissal);
    this.dismissal = setTimeout(() => this.visible.set(false), 3000);
  }

  dismiss(): void {
    clearTimeout(this.dismissal);
    this.visible.set(false);
  }
}
