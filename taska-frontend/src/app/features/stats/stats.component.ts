import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { StatsOverview, fmtEstimate, getColor, hexToRgba } from '../../core/models';
import { StatsService } from '../../core/services/stats.service';
import { ProjectDotComponent } from '../../shared/components/atoms/atoms.component';

@Component({
  selector: 'app-stats',
  imports: [ProjectDotComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (data(); as s) {
      <div class="scroll stats-page" style="flex: 1; overflow-y: auto; padding: 32px 40px;">
        <div class="script" style="font-size: 36px; line-height: 1;">Stats</div>
        <div class="mono" style="font-size: 12px; color: var(--mute); margin-top: 6px;">
          {{ s.totalCompleted }} terminées · streak {{ s.streakDays }}j ·
          {{ s.byProject.length }} projets actifs
        </div>

        <div class="stats-summary-grid" style="display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; margin-top: 28px;">
          <div style="background: var(--bg-2); border-radius: 14px; padding: 22px;">
            <div class="script" style="font-size: 22px;">Terminées (14 derniers jours)</div>
            <div style="display: flex; align-items: flex-end; gap: 6px; height: 180px; margin-top: 18px;">
              @for (d of s.last14Days; track d.date; let i = $index) {
                <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;">
                  <div style="flex: 1; display: flex; align-items: flex-end; width: 100%;">
                    <div [style.width.%]="100"
                         [style.height.%]="(d.count / barMax()) * 100"
                         [style.background]="i === s.last14Days.length - 1 ? 'var(--accent)' : 'var(--navy-light)'"
                         style="border-radius: 4px; min-height: 2px; transition: height 0.4s ease;"
                         [title]="d.count + ' tâches'"></div>
                  </div>
                  <div class="mono" style="font-size: 10px; color: var(--mute);">{{ formatDay(d.date) }}</div>
                </div>
              }
            </div>
          </div>

          <div style="background: var(--bg-2); border-radius: 14px; padding: 22px;">
            <div class="script" style="font-size: 22px;">Streak</div>
            <div style="display: flex; align-items: baseline; gap: 6px; margin-top: 14px;">
              <span class="script" style="font-size: 72px; line-height: 1; color: var(--accent-ink);">{{ s.streakDays }}</span>
              <span class="mono" style="font-size: 12px; color: var(--mute);">jours d'affilée</span>
            </div>
          </div>
        </div>

        <div style="margin-top: 24px; background: var(--bg-2); border-radius: 14px; padding: 22px;">
          <div class="script" style="font-size: 22px;">Répartition par projet</div>
          <div style="margin-top: 14px; display: flex; flex-direction: column; gap: 10px;">
            @for (p of s.byProject; track p.projectId) {
              <div>
                <div style="display: flex; align-items: center; gap: 10px; font-size: 13px; margin-bottom: 4px;">
                  <app-project-dot [color]="getColor(p.color)" />
                  <span style="flex: 1;">{{ p.name }}</span>
                  <span class="mono" style="font-size: 11px; color: var(--mute);">
                    {{ p.done }}/{{ p.total }} · {{ percent(p.done, p.total) }}%
                  </span>
                </div>
                <div style="height: 8px; border-radius: 4px; background: var(--bg-3); overflow: hidden; display: flex;">
                  <div [style.width.%]="(p.done / p.total) * 100" [style.background]="getColor(p.color)"></div>
                  <div [style.width.%]="((p.total - p.done) / p.total) * 100"
                       [style.background]="rgba(getColor(p.color), 0.25)"></div>
                </div>
              </div>
            }
          </div>
        </div>

        <div class="stats-metrics-grid" style="margin-top: 24px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
          <div style="background: var(--bg-2); border-radius: 14px; padding: 18px;">
            <div class="script" style="font-size: 36px; color: var(--accent-ink); line-height: 1;">{{ s.completedThisWeek }}</div>
            <div class="mono" style="font-size: 11px; color: var(--mute); margin-top: 6px;
                                     text-transform: uppercase; letter-spacing: .08em;">
              Terminées cette semaine
            </div>
          </div>
          <div style="background: var(--bg-2); border-radius: 14px; padding: 18px;">
            <div class="script" style="font-size: 36px; color: var(--navy-light); line-height: 1;">
              {{ formatEstimate(s.remainingMinutes) }}
            </div>
            <div class="mono" style="font-size: 11px; color: var(--mute); margin-top: 6px;
                                     text-transform: uppercase; letter-spacing: .08em;">
              Temps estimé restant
            </div>
          </div>
          <div style="background: var(--bg-2); border-radius: 14px; padding: 18px;">
            <div class="script" style="font-size: 36px; color: #E5484D; line-height: 1;">{{ s.overdue }}</div>
            <div class="mono" style="font-size: 11px; color: var(--mute); margin-top: 6px;
                                     text-transform: uppercase; letter-spacing: .08em;">
              En retard
            </div>
          </div>
        </div>
      </div>
    } @else {
      <div class="scroll" style="flex: 1; overflow-y: auto; padding: 32px 40px;">
        <div class="mono" style="color: var(--mute);">Chargement…</div>
      </div>
    }
  `,
  styles: [`
    @media (max-width: 700px) {
      .stats-page { padding: 20px 16px !important; }
      .stats-summary-grid, .stats-metrics-grid { grid-template-columns: 1fr !important; gap: 14px !important; }
    }
  `],
})
export class StatsComponent implements OnInit {
  private statsService = inject(StatsService);

  data = signal<StatsOverview | null>(null);

  barMax = computed(() => {
    const d = this.data();
    if (!d) return 1;
    return Math.max(4, ...d.last14Days.map(x => x.count));
  });

  ngOnInit(): void {
    this.statsService.getOverview().subscribe(s => this.data.set(s));
  }

  getColor = getColor;
  rgba = (hex: string, a: number) => hexToRgba(hex, a);

  percent(done: number, total: number): number {
    return total ? Math.round((done / total) * 100) : 0;
  }

  formatDay(iso: string): string {
    const d = new Date(iso + 'T00:00:00');
    return d.getDate().toString();
  }

  formatEstimate(min: number): string {
    if (!min) return '0h';
    const h = Math.round(min / 60);
    return h + 'h';
  }
}
