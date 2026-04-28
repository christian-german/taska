import { Component, inject, input, output } from '@angular/core';
import { UiStateService } from '../../../core/services/ui-state.service';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-page-header',
  imports: [IconComponent],
  template: `
    <header style="padding: 28px 28px 12px; border-bottom: 1px solid var(--line);">
      <div style="display: flex; align-items: baseline; gap: 14px; flex-wrap: wrap;">
        <h1 class="script" style="font-size: 38px; margin: 0; line-height: 1;">{{ title() }}</h1>
        @if (subtitle()) {
          <span class="mono" style="font-size: 12.5px; color: var(--mute);">{{ subtitle() }}</span>
        }
      </div>

      <ng-content select="[banner]"></ng-content>
      <ng-content select="[hero]"></ng-content>

      <div style="margin-top: 14px; display: flex; gap: 4px; align-items: center;">
        <button class="btn btn-primary" (click)="onAdd()">
          <app-icon name="plus" [size]="13" />
          Ajouter
          <span class="kbd"
                style="margin-left: 6px; background: rgba(255,255,255,0.18);
                       color: rgba(255,255,255,0.85); border: 0;">⌘N</span>
        </button>
        <div style="flex: 1;"></div>
        <ng-content select="[actions]"></ng-content>
      </div>
    </header>
  `,
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input<string>('');

  add = output<void>();

  private ui = inject(UiStateService);

  onAdd(): void {
    this.add.emit();
    this.ui.openQuickAdd();
  }
}
