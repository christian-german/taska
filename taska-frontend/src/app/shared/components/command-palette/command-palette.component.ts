import { Component, ElementRef, OnInit, ViewChild, computed, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Project, Task, getColor } from '../../../core/models';
import { ProjectService } from '../../../core/services/project.service';
import { TaskService } from '../../../core/services/task.service';
import { UiStateService } from '../../../core/services/ui-state.service';
import { IconComponent } from '../icon/icon.component';
import { ProjectDotComponent } from '../atoms/atoms.component';

interface PaletteItem {
  id: string;
  kind: 'nav' | 'action' | 'project' | 'task';
  label: string;
  icon?: string;
  dot?: string;
  kbd?: string;
  run: () => void;
}

@Component({
  selector: 'app-command-palette',
  imports: [FormsModule, IconComponent, ProjectDotComponent],
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()" style="width: min(560px, 92vw);">
        <div style="padding: 14px 16px; border-bottom: 1px solid var(--line);
                    display: flex; align-items: center; gap: 10px;">
          <app-icon name="search" [size]="15" color="var(--mute)" />
          <input #inputEl
                 placeholder="Rechercher, naviguer, créer…"
                 [ngModel]="query()"
                 (ngModelChange)="onQueryChange($event)"
                 (keydown)="onKey($event)"
                 style="flex: 1; border: 0; outline: 0; background: transparent; font-size: 15px; color: var(--ink);" />
          <span class="kbd">esc</span>
        </div>
        <div class="scroll" style="max-height: 380px; overflow-y: auto;">
          @if (items().length === 0) {
            <div style="padding: 18px; color: var(--mute); font-size: 13px;">
              Aucun résultat.
              <span style="color: var(--ink); cursor: pointer; text-decoration: underline;"
                    (click)="createTask()">Créer "{{ query() }}"</span>
            </div>
          } @else {
            @for (it of items(); track it.id; let i = $index) {
              <div (mouseenter)="idx.set(i)"
                   (click)="run(it)"
                   [style.background]="i === idx() ? 'var(--bg-2)' : 'transparent'"
                   style="padding: 9px 16px; display: flex; align-items: center; gap: 10px;
                          cursor: pointer; font-size: 13.5px;">
                @if (it.dot) {
                  <app-project-dot [color]="it.dot" />
                } @else {
                  <app-icon [name]="it.icon || 'corner-down-right'" [size]="14" color="var(--mute)" />
                }
                <span style="flex: 1; color: var(--ink);">{{ it.label }}</span>
                @if (it.kbd) {
                  <span class="kbd">{{ it.kbd }}</span>
                }
                <span class="mono"
                      style="font-size: 10.5px; color: var(--mute); text-transform: uppercase;">
                  {{ it.kind }}
                </span>
              </div>
            }
          }
        </div>
        <div style="padding: 8px 16px; border-top: 1px solid var(--line);
                    display: flex; gap: 14px; font-size: 11px; color: var(--mute);">
          <span><span class="kbd">↑↓</span> naviguer</span>
          <span><span class="kbd">↵</span> ouvrir</span>
          <span><span class="kbd">esc</span> fermer</span>
        </div>
      </div>
    </div>
  `,
})
export class CommandPaletteComponent implements OnInit {
  close = output<void>();

  @ViewChild('inputEl') inputEl?: ElementRef<HTMLInputElement>;

  private router = inject(Router);
  private projectService = inject(ProjectService);
  private taskService = inject(TaskService);
  private ui = inject(UiStateService);

  private projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });
  private allTasks = signal<Task[]>([]);

  query = signal('');
  idx = signal(0);

  items = computed<PaletteItem[]>(() => {
    const base: PaletteItem[] = [
      { id: 'n_today', kind: 'nav', label: "Aller à Aujourd'hui", icon: 'star', run: () => this.router.navigateByUrl('/today') },
      { id: 'n_inbox', kind: 'nav', label: 'Aller à Inbox', icon: 'inbox', run: () => this.router.navigateByUrl('/inbox') },
      { id: 'n_week', kind: 'nav', label: 'Aller à Semaine', icon: 'calendar', run: () => this.router.navigateByUrl('/week') },
      { id: 'n_done', kind: 'nav', label: 'Voir les terminées', icon: 'check', run: () => this.router.navigateByUrl('/done') },
      { id: 'n_stats', kind: 'nav', label: 'Voir les stats', icon: 'chart', run: () => this.router.navigateByUrl('/stats') },
      { id: 'n_projects', kind: 'nav', label: 'Voir les projets', icon: 'folder', run: () => this.router.navigateByUrl('/projects') },
      { id: 'a_quick', kind: 'action', label: 'Ajout rapide…', icon: 'plus', kbd: '⌘N', run: () => this.ui.openQuickAdd() },
      { id: 'a_help', kind: 'action', label: 'Raccourcis clavier', icon: 'settings', kbd: '?', run: () => this.ui.showHelp.set(true) },
    ];
    for (const p of this.projects()) {
      if (p.isInboxProject) continue;
      base.push({
        id: 'p_' + p.id,
        kind: 'project',
        label: 'Projet · ' + p.name,
        dot: getColor(p.color),
        run: () => this.router.navigateByUrl('/project/' + p.id),
      });
    }
    for (const t of this.allTasks()) {
      if (t.isCompleted) continue;
      base.push({
        id: 't_' + t.id,
        kind: 'task',
        label: t.content,
        icon: 'check',
        run: () => this.ui.openTaskDetail(t),
      });
    }
    const q = this.query().trim().toLowerCase();
    if (!q) return base.slice(0, 12);
    return base.filter(x => x.label.toLowerCase().includes(q)).slice(0, 20);
  });

  ngOnInit(): void {
    this.taskService.getTasks({ showCompleted: false }).subscribe(t => this.allTasks.set(t));
    setTimeout(() => this.inputEl?.nativeElement.focus(), 0);
  }

  onQueryChange(v: string): void {
    this.query.set(v);
    this.idx.set(0);
  }

  run(it: PaletteItem): void {
    it.run();
    this.close.emit();
  }

  createTask(): void {
    const q = this.query().trim();
    if (!q) return;
    this.taskService.createTask({ content: q, priority: 1, labels: [] }).subscribe(() => this.close.emit());
  }

  onKey(event: KeyboardEvent): void {
    const len = this.items().length;
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.idx.update(i => Math.min(len - 1, i + 1));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.idx.update(i => Math.max(0, i - 1));
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const list = this.items();
      const it = list[this.idx()];
      if (it) this.run(it);
      else if (this.query().trim()) this.createTask();
    } else if (event.key === 'Escape') {
      this.close.emit();
    }
  }
}
