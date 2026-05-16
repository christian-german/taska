import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { ProjectService } from '../../../core/services/project.service';
import { Project, PROJECT_COLORS, ViewStyle, getColor } from '../../../core/models';
import { IconComponent } from '../icon/icon.component';

const COLOR_KEYS = Object.keys(PROJECT_COLORS) as (keyof typeof PROJECT_COLORS)[];

@Component({
  selector: 'app-add-project-modal',
  imports: [FormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" style="width: 480px; max-width: 95vw;" (click)="$event.stopPropagation()">

        <div style="padding: 20px 22px 16px;">
          <div class="script" style="font-size: 22px; color: var(--mute); margin-bottom: 18px;">
            {{ project() ? 'modifier le projet' : 'nouveau projet' }}
          </div>

          <!-- Nom -->
          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; font-weight: 500; color: var(--mute);
                           text-transform: uppercase; letter-spacing: .04em; margin-bottom: 5px;">
              Nom
            </label>
            <input #nameInput
                   [ngModel]="name()"
                   (ngModelChange)="name.set($event)"
                   (keydown.enter)="submit()"
                   (keydown.escape)="close.emit()"
                   placeholder="Nom du projet"
                   style="width: 100%; background: var(--bg); border: 1px solid var(--line);
                          padding: 8px 10px; border-radius: 7px; font-size: 14px;
                          outline: none; color: var(--ink); box-sizing: border-box;" />
          </div>

          <!-- Couleur -->
          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; font-weight: 500; color: var(--mute);
                           text-transform: uppercase; letter-spacing: .04em; margin-bottom: 8px;">
              Couleur
            </label>
            <div style="display: flex; flex-wrap: wrap; gap: 7px;">
              @for (key of colorKeys; track key) {
                <button
                  (click)="color.set(PROJECT_COLORS[key])"
                  [title]="key"
                  style="width: 22px; height: 22px; border-radius: 50%; border: 2px solid transparent;
                         cursor: pointer; padding: 0; flex-shrink: 0; transition: transform .1s;"
                  [style.background]="PROJECT_COLORS[key]"
                  [style.border-color]="color() === PROJECT_COLORS[key] ? 'var(--ink)' : 'transparent'"
                  [style.transform]="color() === PROJECT_COLORS[key] ? 'scale(1.25)' : 'scale(1)'">
                </button>
              }
            </div>
          </div>

          <!-- Projet parent -->
          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; font-weight: 500; color: var(--mute);
                           text-transform: uppercase; letter-spacing: .04em; margin-bottom: 5px;">
              Projet parent
            </label>
            <div style="position: relative;">
              <app-icon name="folder" [size]="14"
                        style="position: absolute; left: 9px; top: 50%; transform: translateY(-50%);
                               color: var(--mute); pointer-events: none;" />
              <select
                [ngModel]="parentId()"
                (ngModelChange)="parentId.set($event)"
                style="width: 100%; background: var(--bg); border: 1px solid var(--line);
                       padding: 8px 10px 8px 30px; border-radius: 7px; font-size: 14px;
                       outline: none; color: var(--ink); appearance: none; cursor: pointer;
                       box-sizing: border-box;">
                <option value="">Aucun</option>
                @for (p of parentOptions(); track p.id) {
                  <option [value]="p.id">{{ p.name }}</option>
                }
              </select>
            </div>
          </div>

          <!-- Vue -->
          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; font-weight: 500; color: var(--mute);
                           text-transform: uppercase; letter-spacing: .04em; margin-bottom: 8px;">
              Vue par défaut
            </label>
            <div style="display: flex; gap: 6px;">
              @for (vs of viewStyles; track vs.value) {
                <button
                  (click)="viewStyle.set(vs.value)"
                  style="flex: 1; display: flex; align-items: center; justify-content: center;
                         gap: 5px; padding: 7px 10px; border-radius: 7px; font-size: 13px;
                         cursor: pointer; border: 1px solid var(--line); transition: all .15s;"
                  [style.background]="viewStyle() === vs.value ? 'var(--orange)' : 'var(--bg)'"
                  [style.color]="viewStyle() === vs.value ? '#fff' : 'var(--ink-2)'"
                  [style.border-color]="viewStyle() === vs.value ? 'var(--orange)' : 'var(--line)'">
                  <app-icon [name]="vs.icon" [size]="13" />
                  {{ vs.label }}
                </button>
              }
            </div>
          </div>

          <!-- Favori -->
          <div style="display: flex; align-items: center; justify-content: space-between;
                      padding: 8px 10px; border-radius: 7px; background: var(--bg);
                      border: 1px solid var(--line);">
            <div style="display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--ink-2);">
              <app-icon name="star" [size]="14" />
              <span>Ajouter aux favoris</span>
            </div>
            <button
              (click)="isFavorite.set(!isFavorite())"
              style="width: 38px; height: 22px; border-radius: 11px; border: 0; cursor: pointer;
                     transition: background .2s; position: relative;"
              [style.background]="isFavorite() ? 'var(--orange)' : 'var(--line)'">
              <span style="position: absolute; top: 3px; width: 16px; height: 16px; border-radius: 50%;
                            background: #fff; transition: left .2s; box-shadow: 0 1px 3px rgba(0,0,0,.2);"
                    [style.left]="isFavorite() ? '19px' : '3px'">
              </span>
            </button>
          </div>
        </div>

        <div style="padding: 12px 22px; border-top: 1px solid var(--line);
                    display: flex; justify-content: space-between; align-items: center;">
          <button class="btn btn-ghost" (click)="close.emit()">
            annuler <span class="kbd" style="margin-left: 4px;">esc</span>
          </button>
          <button class="btn btn-primary" (click)="submit()" [disabled]="!name().trim()">
            {{ project() ? '✓ enregistrer' : '+ créer le projet' }}
          </button>
        </div>

      </div>
    </div>
  `,
})
export class AddProjectModalComponent implements OnInit {
  project = input<Project | null>(null);
  close = output<void>();

  @ViewChild('nameInput') nameInput?: ElementRef<HTMLInputElement>;

  private projectService = inject(ProjectService);
  private allProjects = toSignal(this.projectService.projects$, { initialValue: [] as Project[] });

  name = signal('');
  color = signal('#808080');
  parentId = signal('');
  viewStyle = signal<ViewStyle>('LIST');
  isFavorite = signal(false);

  colorKeys = COLOR_KEYS;
  PROJECT_COLORS = PROJECT_COLORS;
  getColor = getColor;

  viewStyles: { value: ViewStyle; label: string; icon: string }[] = [
    { value: 'LIST', label: 'Liste', icon: 'list' },
    { value: 'BOARD', label: 'Board', icon: 'grid' },
    { value: 'CALENDAR', label: 'Calendrier', icon: 'calendar' },
  ];

  parentOptions = computed(() => {
    const editingId = this.project()?.id;
    return this.allProjects().filter(p => !p.isInboxProject && p.id !== editingId);
  });

  ngOnInit(): void {
    const p = this.project();
    if (p) {
      this.name.set(p.name);
      this.color.set(p.color);
      this.parentId.set(p.parentId ?? '');
      this.viewStyle.set(p.viewStyle ?? 'LIST');
      this.isFavorite.set(p.isFavorite ?? false);
    }
    setTimeout(() => this.nameInput?.nativeElement.focus(), 0);
  }

  submit(): void {
    const name = this.name().trim();
    if (!name) return;
    const p = this.project();
    if (p) {
      this.projectService.updateProject(p.id, {
        name,
        color: this.color(),
        parentId: this.parentId() || undefined,
        viewStyle: this.viewStyle(),
        isFavorite: this.isFavorite(),
      }).subscribe(() => this.close.emit());
    } else {
      this.projectService.createProject({
        name,
        color: this.color(),
        parentId: this.parentId() || undefined,
        viewStyle: this.viewStyle(),
        isFavorite: this.isFavorite(),
      }).subscribe(() => this.close.emit());
    }
  }
}
