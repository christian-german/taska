import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DatetimePickerComponent} from './datetime-picker.component';

describe('DatetimePickerComponent time clearing', () => {
  let fixture: ComponentFixture<DatetimePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [DatetimePickerComponent]}).compileComponents();
    fixture = TestBed.createComponent(DatetimePickerComponent);
    fixture.componentRef.setInput('value', '2026-08-24T09:15:00Z');
    fixture.componentRef.setInput('withTime', true);
    fixture.detectChanges();
  });

  it('clears only the time and retains the selected date', () => {
    const emitted: string[] = [];
    fixture.componentInstance.valueChange.subscribe(value => emitted.push(value));

    fixture.componentInstance.clearTime();

    expect(emitted).toEqual(['2026-08-24']);
    expect(fixture.nativeElement.querySelector('[aria-label="Effacer uniquement l\'heure planifiée"]')).not.toBeNull();
  });
});
