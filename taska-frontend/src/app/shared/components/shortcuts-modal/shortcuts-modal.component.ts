import { ChangeDetectionStrategy, Component, output } from '@angular/core';

interface Group {
  h: string;
  items: [string, string][];
}

@Component({
  selector: 'app-shortcuts-modal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-veil" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()" style="padding: 22px 26px;">
        <div class="script" style="font-size: 28px;">Raccourcis</div>
        <div class="mono" style="font-size: 11px; color: var(--mute); margin-bottom: 16px;">
          tout est plus rapide au clavier
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 22px;">
          @for (g of groups; track g.h) {
            <div>
              <div class="mono"
                   style="font-size: 10.5px; text-transform: uppercase; letter-spacing: .1em;
                          color: var(--mute); margin-bottom: 8px;">
                {{ g.h }}
              </div>
              @for (it of g.items; track it[0]) {
                <div style="display: flex; justify-content: space-between; padding: 5px 0;
                            border-bottom: 1px dashed var(--line);">
                  <span style="font-size: 13px;">{{ it[1] }}</span>
                  <span class="kbd">{{ it[0] }}</span>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class ShortcutsModalComponent {
  close = output<void>();

  groups: Group[] = [
    {
      h: 'Navigation',
      items: [
        ['⌘K', 'palette / recherche'],
        ['⌘N', 'ajout rapide'],
        ['Q', 'ajout rapide'],
        ['g puis t', "aujourd'hui"],
        ['g puis i', 'inbox'],
        ['g puis w', 'semaine'],
        ['g puis s', 'stats'],
        ['?', 'aide'],
      ],
    },
    {
      h: 'Liste',
      items: [
        ['x', 'cocher / décocher (depuis le détail)'],
        ['e', 'éditer le titre (double-clic)'],
        ['⏎', 'ouvrir détail'],
        ['esc', 'fermer'],
      ],
    },
    {
      h: 'Composition',
      items: [
        ['#', 'ajouter un tag'],
        ['@', 'ajouter un contexte'],
        ['!', 'priorité (! = P3, !! = P2, !!! = P1)'],
        ['~', 'durée estimée (~30min, ~2h)'],
      ],
    },
  ];
}
