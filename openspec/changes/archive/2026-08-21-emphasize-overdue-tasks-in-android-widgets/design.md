## Context

The Android Week widget represents overdue work as a leading `Overdue` collection header followed by overdue task items. The Today widget places overdue tasks first in its fixed task rows but has no separate overdue header. Both widget renderers currently apply their normal header or task-title styles regardless of overdue status.

The product request is limited to visible emphasis: the Week header and overdue task text must be red and bold, while the Today widget applies that treatment only to overdue task text. Overdue classification remains an incomplete task whose `scheduledAt` falls before the device-local current date.

## Goals / Non-Goals

**Goals:**

- Make overdue content immediately distinguishable in both Android widgets.
- Apply both color and font weight so the distinction does not rely on color alone.
- Keep the Week widget's ordinary date headers and both widgets' non-overdue task text unchanged.
- Provide an overdue-red widget color with suitable light- and dark-theme variants.

**Non-Goals:**

- Change which tasks are overdue, visible, or actionable.
- Add an `Overdue` label to the Today widget.
- Recolor completion controls, appointment icons, dividers, titles, or status text.
- Change any non-widget task presentation.

## Decisions

### Style only the text that communicates overdue status

The Week widget will apply overdue red and bold weight to the `Overdue` header and to the time/title text of each task in that group. The Today widget will apply the same semantic text treatment to each overdue task's time/title text. Other row elements retain their current presentation.

This keeps the emphasis aligned with the requested label and tasks without suggesting that completion controls or appointment indicators have changed meaning.

### Derive row styling from the established overdue classification

Each renderer will determine whether a task is overdue using the existing device-local scheduled-date rule rather than its list position. This makes the presentation testable and prevents an ordinary task from receiving overdue styling merely because ordering or capacity logic changes.

### Use theme-aware widget resources

Define a semantic widget overdue-red color for the default and night resource sets, then bind that color and bold weight through `RemoteViews`-compatible text styling. The exact resource and binding structure is an internal implementation detail, but both theme variants must keep the emphasized text legible against the widget surface.

## Risks / Trade-offs

- [Red text can lose contrast in one theme] → Provide and verify separate light- and dark-theme overdue colors against the widget surfaces.
- [`RemoteViews` supports less styling than in-app Compose text] → Use supported text color and typeface/span mechanisms and cover the rendered properties with focused tests.
- [The two widgets use separate row renderers] → Test Week header, Week rows, and Today rows independently, including non-overdue controls.

## Migration Plan

1. Add semantic overdue colors for light and dark widget themes.
2. Bind bold overdue-red text to the Week overdue header and overdue task rows.
3. Bind bold overdue-red text to Today overdue task rows.
4. Verify that ordinary date headers and non-overdue task rows retain their existing styles.

No data migration is required. Rollback restores the existing normal text styles.

## Open Questions

- None.
