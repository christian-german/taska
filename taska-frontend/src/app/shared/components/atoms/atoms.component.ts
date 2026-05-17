import {ChangeDetectionStrategy, Component, computed, input, output, signal} from '@angular/core';
import { hexToRgba } from '../../../core/models';

@Component({
  selector: 'app-checkbox',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="cb"
          [class.checked]="checked()"
          [class.just-checked]="justChecked()"
          [style.width.px]="size()"
          [style.height.px]="size()"
          (click)="onClick($event)"
          role="checkbox"
          [attr.aria-checked]="checked()">
      <svg [attr.width]="size() - 6" [attr.height]="size() - 6" viewBox="0 0 14 14" fill="none">
        <path class="check-svg" d="M2.5 7.5 L6 11 L11.5 3.5"
              stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </span>
  `,
})
export class CheckboxComponent {
  checked = input<boolean>(false);
  /** internal priority 1=lowest..4=highest */
  priority = input<number>(1);
  size = input<number>(20);
  toggled = output<MouseEvent>();

  justChecked = signal(false);

  onClick(e: MouseEvent): void {
    e.stopPropagation();
    if (!this.checked()) {
      this.justChecked.set(true);
      setTimeout(() => this.justChecked.set(false), 300);
    }
    this.toggled.emit(e);
  }
}

@Component({
  selector: 'app-priority-flag',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (display() && display() <= 4) {
      <span [style.color]="color()" [style.display]="'inline-flex'" [style.align-items]="'center'" [title]="'P' + display()">
        <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 16 16" fill="none">
          <path d="M3 1.5 V14.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
          <path d="M3.4 2 L13 3 L11 6.2 L13 9.4 L3.4 8.4" fill="currentColor" />
        </svg>
      </span>
    }
  `,
})
export class PriorityFlagComponent {
  /** internal priority 1=highest..4=lowest */
  priority = input<number>(4);
  size = input<number>(13);

  display = computed(() => this.priority() || 4);
  color = computed(() => {
    const p = this.display();
    return p === 1 ? 'var(--p1)' : p === 2 ? 'var(--p2)' : p === 3 ? 'var(--p3)' : 'var(--p4)';
  });
}

@Component({
  selector: 'app-project-dot',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="dot" [style.background]="color()" [style.width.px]="size()" [style.height.px]="size()"></span>`,
})
export class ProjectDotComponent {
  color = input<string>('#8A847A');
  size = input<number>(9);
}

@Component({
  selector: 'app-tag-chip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="chip tag" [style.background]="bg()" [style.color]="color()">{{ prefix() }}{{ name() }}</span>`,
})
export class TagChipComponent {
  name = input.required<string>();
  color = input<string>('#8A847A');
  prefix = input<string>('#');

  bg = computed(() => hexToRgba(this.color(), 0.15));
}

@Component({
  selector: 'app-empty-state',
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="empty">
      <div [style.width.px]="64" [style.height.px]="64" [style.border-radius.px]="16" [style.background]="'var(--bg-2)'"
           [style.display]="'flex'" [style.align-items]="'center'" [style.justify-content]="'center'" [style.margin-bottom.px]="12">
        <ng-content select="[icon]"></ng-content>
      </div>
      <div class="script" [style.font-size.px]="22" [style.color]="'var(--ink)'">{{ title() }}</div>
      @if (hint()) {
        <div [style.margin-top.px]="4" [style.font-size.px]="13">{{ hint() }}</div>
      }
    </div>
  `,
})
export class EmptyStateComponent {
  title = input.required<string>();
  hint = input<string>('');
}

export function fireConfetti(x: number, y: number): void {
  const colors = ['#FF8A3D', '#3AA3FF', '#7AD36B', '#FF5E7D', '#FFD84D', '#1A1814'];
  for (let i = 0; i < 26; i++) {
    const el = document.createElement('div');
    el.className = 'confetti';
    el.style.left = (x + (Math.random() - 0.5) * 100) + 'px';
    el.style.top = (y - 10) + 'px';
    el.style.background = colors[i % colors.length];
    el.style.transform = `rotate(${Math.random() * 360}deg)`;
    el.style.animation = `fall ${0.9 + Math.random() * 0.7}s ease-in forwards`;
    el.style.animationDelay = (Math.random() * 0.15) + 's';
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2000);
  }
}
