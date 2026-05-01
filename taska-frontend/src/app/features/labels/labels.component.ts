import { Component, OnInit, inject, signal } from '@angular/core';
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
  template: `
    @if (deletingLabel()) {
      <app-confirm-dialog
        [title]="'Supprimer le tag « ' + deletingLabel()!.name + ' » ?'"
        message="Les tâches associées à ce tag ne seront pas supprimées."
        (confirmed)="confirmDeleteLabel()"
        (cancelled)="deletingLabel.set(null)" />
    }
    <div style="padding: 32px 28px;">
      <div style="display: flex; align-items: baseline; gap: 14px;">
        <h1 class="script" style="font-size: 38px; margin: 0; line-height: 1;">Tags</h1>
        <span class="mono" style="font-size: 12.5px; color: var(--mute);">{{ labels().length }} tags</span>
      </div>

      <div style="margin-top: 24px; display: grid; gap: 6px; max-width: 600px;">
        @for (l of labels(); track l.id) {
          @if (editingId() === l.id) {
            <div style="background: var(--bg-2); border: 1px solid var(--line-2); border-radius: 12px; padding: 14px;">
              <input [ngModel]="editName()" (ngModelChange)="editName.set($event)"
                     (keydown.enter)="saveEdit(l)" (keydown.escape)="cancelEdit()"
                     placeholder="Nom du tag"
                     style="width: 100%; padding: 6px 10px; background: var(--bg);
                            border: 1px solid var(--line); border-radius: 6px; outline: none; color: var(--ink);" />
              <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px;">
                @for (key of colorKeys; track key) {
                  <button (click)="editColor.set(key)"
                          [style.background]="getColor(key)"
                          [style.outline]="editColor() === key ? '2px solid var(--ink)' : 'none'"
                          [style.outline-offset.px]="2"
                          style="width: 22px; height: 22px; border-radius: 50%; border: 0; cursor: pointer;"></button>
                }
              </div>
              <div style="display: flex; gap: 6px; margin-top: 12px;">
                <button class="btn btn-primary" (click)="saveEdit(l)" [disabled]="!editName().trim()">Enregistrer</button>
                <button class="btn btn-ghost" (click)="cancelEdit()">Annuler</button>
              </div>
            </div>
          } @else {
            <div style="display: flex; align-items: center; gap: 12px; padding: 10px 14px;
                        background: var(--bg-2); border-radius: 10px;">
              <app-project-dot [color]="getColor(l.color)" [size]="11" />
              <span style="flex: 1; font-size: 14px;">{{ l.name }}</span>
              <button class="btn btn-ghost btn-icon" (click)="startEdit(l)" title="Éditer">
                <app-icon name="edit" [size]="13" />
              </button>
              <button class="btn btn-ghost btn-icon" (click)="deleteLabel(l)" title="Supprimer">
                <app-icon name="trash" [size]="13" />
              </button>
            </div>
          }
        }
      </div>

      @if (showAdd()) {
        <div style="background: var(--bg-2); border: 1px solid var(--line-2); border-radius: 12px;
                    padding: 14px; margin-top: 16px; max-width: 600px;">
          <input [ngModel]="newName()" (ngModelChange)="newName.set($event)"
                 (keydown.enter)="createLabel()" (keydown.escape)="closeAdd()"
                 placeholder="Nouveau tag…"
                 autofocus
                 style="width: 100%; padding: 6px 10px; background: var(--bg);
                        border: 1px solid var(--line); border-radius: 6px; outline: none; color: var(--ink);" />
          <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px;">
            @for (key of colorKeys; track key) {
              <button (click)="newColor.set(key)"
                      [style.background]="getColor(key)"
                      [style.outline]="newColor() === key ? '2px solid var(--ink)' : 'none'"
                      [style.outline-offset.px]="2"
                      style="width: 22px; height: 22px; border-radius: 50%; border: 0; cursor: pointer;"></button>
            }
          </div>
          <div style="display: flex; gap: 6px; margin-top: 12px;">
            <button class="btn btn-primary" (click)="createLabel()" [disabled]="!newName().trim()">Créer</button>
            <button class="btn btn-ghost" (click)="closeAdd()">Annuler</button>
          </div>
        </div>
      } @else {
        <button class="btn btn-ghost" (click)="openAdd()" style="margin-top: 14px;">
          <app-icon name="plus" [size]="13" /> Ajouter un tag
        </button>
      }
    </div>
  `,
})
export class LabelsComponent implements OnInit {
  private labelService = inject(LabelService);

  labels = toSignal(this.labelService.labels$, { initialValue: [] as Label[] });

  readonly colorKeys = Object.keys(PROJECT_COLORS);
  readonly getColor = getColor;

  showAdd = signal(false);
  deletingLabel = signal<Label | null>(null);
  newName = signal('');
  newColor = signal('charcoal');

  editingId = signal<string | null>(null);
  editName = signal('');
  editColor = signal('charcoal');

  ngOnInit(): void {}

  openAdd(): void {
    this.newName.set('');
    this.newColor.set('charcoal');
    this.showAdd.set(true);
  }

  closeAdd(): void {
    this.showAdd.set(false);
  }

  createLabel(): void {
    const name = this.newName().trim();
    if (!name) return;
    this.labelService.createLabel({ name, color: this.newColor(), isFavorite: false }).subscribe();
    this.closeAdd();
  }

  startEdit(l: Label): void {
    this.editingId.set(l.id);
    this.editName.set(l.name);
    this.editColor.set(l.color);
  }

  saveEdit(l: Label): void {
    const name = this.editName().trim();
    if (!name) return;
    this.labelService.updateLabel(l.id, { name, color: this.editColor(), isFavorite: l.isFavorite } as any).subscribe();
    this.editingId.set(null);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  deleteLabel(l: Label): void {
    this.deletingLabel.set(l);
  }

  confirmDeleteLabel(): void {
    const l = this.deletingLabel();
    if (!l) return;
    this.deletingLabel.set(null);
    this.labelService.deleteLabel(l.id).subscribe();
  }
}
