import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { LabelService } from '../../core/services/label.service';
import { Label, PROJECT_COLORS, getColor } from '../../core/models';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { ProjectDotComponent } from '../../shared/components/atoms/atoms.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-labels',
  imports: [FormsModule, IconComponent, ProjectDotComponent, ConfirmDialogComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (deletingLabel()) {
      <app-confirm-dialog
        [title]="'Supprimer le tag « ' + deletingLabel()!.name + ' » ?'"
        message="Les tâches associées à ce tag ne seront pas supprimées."
        (confirmed)="confirmDeleteLabel()"
        (cancelled)="deletingLabel.set(null)" />
    }

    @if (showModal()) {
      <div class="modal-veil" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()"
             style="width: min(420px, 92vw); padding: 24px 24px 20px;">
          <div style="font-size: 17px; font-weight: 600; color: var(--ink); margin-bottom: 18px;">
            {{ editingLabel() ? 'Modifier le tag' : 'Nouveau tag' }}
          </div>
          <input [ngModel]="modalName()" (ngModelChange)="modalName.set($event)"
                 (keydown.enter)="saveModal()" (keydown.escape)="closeModal()"
                 placeholder="Nom du tag"
                 autofocus
                 style="width: 100%; padding: 7px 10px; background: var(--bg-2);
                        border: 1px solid var(--line); border-radius: 7px;
                        outline: none; color: var(--ink); font-size: 14px;" />
          <div style="display: flex; flex-wrap: wrap; gap: 7px; margin-top: 14px;">
            @for (key of colorKeys; track key) {
              <button (click)="modalColor.set(key)"
                      [style.background]="getColor(key)"
                      [style.outline]="modalColor() === key ? '2px solid var(--ink)' : 'none'"
                      [style.outline-offset.px]="2"
                      style="width: 22px; height: 22px; border-radius: 50%; border: 0; cursor: pointer;"></button>
            }
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px;">
            <button class="btn btn-ghost" (click)="closeModal()">Annuler</button>
            <button class="btn btn-primary" (click)="saveModal()" [disabled]="!modalName().trim()">
              {{ editingLabel() ? 'Enregistrer' : 'Créer' }}
            </button>
          </div>
        </div>
      </div>
    }

    <div style="padding: 32px 28px;">
      <div style="display: flex; align-items: baseline; gap: 14px;">
        <h1 class="script" style="font-size: 38px; margin: 0; line-height: 1;">Tags</h1>
        <span class="mono" style="font-size: 12.5px; color: var(--mute);">{{ labels().length }} tags</span>
      </div>

      <div style="margin-top: 24px; display: grid; gap: 6px; max-width: 600px;">
        @for (l of labels(); track l.id) {
          <div style="display: flex; align-items: center; gap: 12px; padding: 10px 14px;
                      background: var(--bg-2); border-radius: 10px;">
            <app-project-dot [color]="getColor(l.color)" [size]="11" />
            <span style="flex: 1; font-size: 14px;">{{ l.name }}</span>
            <button class="btn btn-ghost btn-icon" (click)="openEdit(l)" title="Éditer">
              <app-icon name="edit" [size]="13" />
            </button>
            <button class="btn btn-ghost btn-icon" (click)="deletingLabel.set(l)" title="Supprimer">
              <app-icon name="trash" [size]="13" />
            </button>
          </div>
        }
      </div>

      <button class="btn btn-ghost" (click)="openCreate()" style="margin-top: 14px;">
        <app-icon name="plus" [size]="13" /> Ajouter un tag
      </button>
    </div>
  `,
})
export class LabelsComponent implements OnInit {
  private labelService = inject(LabelService);

  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  readonly colorKeys = Object.keys(PROJECT_COLORS);
  readonly getColor = getColor;

  showModal = signal(false);
  editingLabel = signal<Label | null>(null);
  modalName = signal('');
  modalColor = signal('charcoal');
  deletingLabel = signal<Label | null>(null);

  ngOnInit(): void {}

  openCreate(): void {
    this.editingLabel.set(null);
    this.modalName.set('');
    this.modalColor.set('charcoal');
    this.showModal.set(true);
  }

  openEdit(l: Label): void {
    this.editingLabel.set(l);
    this.modalName.set(l.name);
    this.modalColor.set(l.color);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  saveModal(): void {
    const name = this.modalName().trim();
    if (!name) return;
    const editing = this.editingLabel();
    if (editing) {
      this.labelService.updateLabel(editing.id, { name, color: this.modalColor(), isFavorite: editing.isFavorite } as any).subscribe();
    } else {
      this.labelService.createLabel({ name, color: this.modalColor(), isFavorite: false }).subscribe();
    }
    this.closeModal();
  }

  confirmDeleteLabel(): void {
    const l = this.deletingLabel();
    if (!l) return;
    this.deletingLabel.set(null);
    this.labelService.deleteLabel(l.id).subscribe();
  }
}
