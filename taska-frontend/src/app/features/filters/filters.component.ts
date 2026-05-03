import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { FilterService } from '../../core/services/filter.service';
import { ProjectService } from '../../core/services/project.service';
import { Filter, Project, PROJECT_COLORS, getColor } from '../../core/models';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ProjectDotComponent } from '../../shared/components/atoms/atoms.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

const SELECT_STYLE = 'width: 100%; padding: 5px 8px; background: var(--bg-2); border: 1px solid var(--line); border-radius: 6px; color: var(--ink); font-size: 13px; outline: none;';

@Component({
  selector: 'app-filters',
  imports: [FormsModule, RouterLink, IconComponent, ProjectDotComponent, ConfirmDialogComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (deletingId()) {
      <app-confirm-dialog
        title="Supprimer ce filtre ?"
        message="Cette action est définitive et ne peut pas être annulée."
        (confirmed)="confirmDelete()"
        (cancelled)="deletingId.set(null)" />
    }

    @if (showModal()) {
      <div class="modal-veil" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()"
             style="width: min(460px, 92vw); padding: 24px 24px 20px;">
          <div style="font-size: 17px; font-weight: 600; color: var(--ink); margin-bottom: 18px;">
            {{ editingFilter() ? 'Modifier le filtre' : 'Nouveau filtre' }}
          </div>

          <input [ngModel]="modalName()" (ngModelChange)="modalName.set($event)"
                 (keydown.enter)="saveModal()" (keydown.escape)="closeModal()"
                 placeholder="Nom du filtre"
                 autofocus
                 style="width: 100%; padding: 7px 10px; background: var(--bg-2);
                        border: 1px solid var(--line); border-radius: 7px;
                        outline: none; color: var(--ink); font-size: 14px;" />

          <div style="display: flex; flex-wrap: wrap; gap: 7px; margin-top: 14px;">
            @for (entry of colorEntries; track entry.key) {
              <button (click)="modalColor.set(entry.key)"
                      [style.background]="entry.value"
                      [style.outline]="modalColor() === entry.key ? '2px solid var(--ink)' : 'none'"
                      [style.outline-offset.px]="2"
                      style="width: 22px; height: 22px; border-radius: 50%; border: 0; cursor: pointer;"></button>
            }
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 14px;">
            <div>
              <div class="mono" style="font-size: 10.5px; color: var(--mute); text-transform: uppercase;
                                       letter-spacing: .06em; margin-bottom: 5px;">Projet</div>
              <select [ngModel]="modalProjectId()" (ngModelChange)="modalProjectId.set($event)" [style]="SELECT_STYLE">
                <option value="">Tous les projets</option>
                @for (p of projects(); track p.id) {
                  @if (!p.isInboxProject) {
                    <option [value]="p.id">{{ p.name }}</option>
                  }
                }
              </select>
            </div>
            <div>
              <div class="mono" style="font-size: 10.5px; color: var(--mute); text-transform: uppercase;
                                       letter-spacing: .06em; margin-bottom: 5px;">Date d'échéance</div>
              <select [ngModel]="modalHasDate()" (ngModelChange)="modalHasDate.set($event)" [style]="SELECT_STYLE">
                <option value="any">Toute date</option>
                <option value="true">Avec date</option>
                <option value="false">Sans date</option>
              </select>
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 8px; margin-top: 14px;">
            <input type="checkbox"
                   [ngModel]="modalFavorite()" (ngModelChange)="modalFavorite.set($event)"
                   id="modal-fav" style="cursor: pointer; accent-color: var(--accent);" />
            <label for="modal-fav" style="font-size: 13px; color: var(--ink); cursor: pointer;">Favori</label>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px;">
            <button class="btn btn-ghost" (click)="closeModal()">Annuler</button>
            <button class="btn btn-primary" (click)="saveModal()" [disabled]="!modalName().trim()">
              {{ editingFilter() ? 'Enregistrer' : 'Créer' }}
            </button>
          </div>
        </div>
      </div>
    }

    <div style="padding: 32px 28px;">
      <div style="display: flex; align-items: baseline; gap: 14px;">
        <h1 class="script" style="font-size: 38px; margin: 0; line-height: 1;">Filtres</h1>
        <span class="mono" style="font-size: 12.5px; color: var(--mute);">{{ filters().length }} filtres</span>
      </div>

      <div style="margin-top: 24px; display: grid; gap: 6px; max-width: 600px;">
        @for (filter of filters(); track filter.id) {
          <div style="display: flex; align-items: center; gap: 12px; padding: 10px 14px;
                      background: var(--bg-2); border-radius: 10px;">
            <app-project-dot [color]="getColor(filter.color)" [size]="11" />
            <div style="flex: 1; min-width: 0;">
              <a [routerLink]="['/filter', filter.id]"
                 style="font-size: 14px; color: var(--ink); text-decoration: none;">
                {{ filter.name }}
              </a>
              @if (filter.projectId || filter.hasDate != null) {
                <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-top: 2px;">
                  @if (filter.projectId) {
                    <span class="mono" style="font-size: 11px; color: var(--mute);">{{ getProjectName(filter.projectId) }}</span>
                  }
                  @if (filter.hasDate === true) {
                    <span class="mono" style="font-size: 11px; color: var(--mute);">Avec date</span>
                  }
                  @if (filter.hasDate === false) {
                    <span class="mono" style="font-size: 11px; color: var(--mute);">Sans date</span>
                  }
                </div>
              }
            </div>
            <button class="btn btn-ghost btn-icon" (click)="toggleFavorite(filter)" title="Favori"
                    [style.color]="filter.isFavorite ? 'var(--yellow)' : 'var(--mute)'">
              <app-icon name="star" [size]="13" />
            </button>
            <button class="btn btn-ghost btn-icon" (click)="openEdit(filter)" title="Éditer">
              <app-icon name="edit" [size]="13" />
            </button>
            <button class="btn btn-ghost btn-icon" (click)="deletingId.set(filter.id)" title="Supprimer">
              <app-icon name="trash" [size]="13" />
            </button>
          </div>
        }
      </div>

      <button class="btn btn-ghost" (click)="openCreate()" style="margin-top: 14px;">
        <app-icon name="plus" [size]="13" /> Ajouter un filtre
      </button>
    </div>
  `
})
export class FiltersComponent implements OnInit {
  private filterService = inject(FilterService);
  private projectService = inject(ProjectService);

  filters = toSignal(this.filterService.filters$, { initialValue: [] as Filter[] });
  projects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  showModal = signal(false);
  editingFilter = signal<Filter | null>(null);
  modalName = signal('');
  modalColor = signal('charcoal');
  modalProjectId = signal('');
  modalHasDate = signal('any');
  modalFavorite = signal(false);
  deletingId = signal<string | null>(null);

  readonly colorEntries = Object.entries(PROJECT_COLORS).map(([key, value]) => ({ key, value }));
  readonly getColor = getColor;
  readonly SELECT_STYLE = SELECT_STYLE;

  ngOnInit(): void {
    this.filterService.loadFilters().subscribe();
  }

  getProjectName(projectId: string): string {
    return this.projects().find(p => p.id === projectId)?.name ?? '';
  }

  openCreate(): void {
    this.editingFilter.set(null);
    this.modalName.set('');
    this.modalColor.set('charcoal');
    this.modalProjectId.set('');
    this.modalHasDate.set('any');
    this.modalFavorite.set(false);
    this.showModal.set(true);
  }

  openEdit(filter: Filter): void {
    this.editingFilter.set(filter);
    this.modalName.set(filter.name);
    this.modalColor.set(filter.color);
    this.modalProjectId.set(filter.projectId ?? '');
    this.modalHasDate.set(filter.hasDate == null ? 'any' : String(filter.hasDate));
    this.modalFavorite.set(filter.isFavorite);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  saveModal(): void {
    const name = this.modalName().trim();
    if (!name) return;
    const editing = this.editingFilter();
    if (editing) {
      const hasProjectChanged = this.modalProjectId() !== (editing.projectId ?? '');
      this.filterService.updateFilter(editing.id, {
        name,
        color: this.modalColor(),
        isFavorite: this.modalFavorite(),
        projectId: this.modalProjectId() || undefined,
        clearProject: hasProjectChanged && !this.modalProjectId() ? true : undefined,
        hasDate: this.modalHasDate() === 'any' ? undefined : this.modalHasDate() === 'true',
      }).subscribe();
    } else {
      this.filterService.createFilter({
        name,
        color: this.modalColor(),
        projectId: this.modalProjectId() || undefined,
        hasDate: this.modalHasDate() === 'any' ? undefined : this.modalHasDate() === 'true',
        isFavorite: this.modalFavorite(),
        order: this.filters().length,
      }).subscribe();
    }
    this.closeModal();
  }

  toggleFavorite(filter: Filter): void {
    this.filterService.updateFilter(filter.id, { isFavorite: !filter.isFavorite }).subscribe();
  }

  confirmDelete(): void {
    const id = this.deletingId();
    if (!id) return;
    this.deletingId.set(null);
    this.filterService.deleteFilter(id).subscribe();
  }
}
