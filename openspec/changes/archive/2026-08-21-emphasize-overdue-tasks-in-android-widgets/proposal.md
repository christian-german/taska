## Why

Overdue tasks now appear in both Android home-screen widgets, but they use the same text treatment as current tasks. This makes overdue work and the Week widget's `Overdue` section difficult to identify while scanning a widget.

## What Changes

- Render the Week widget's `Overdue` header in bold overdue-red text.
- Render every overdue task's visible text in bold overdue red in both the Week and Today widgets.
- Keep non-overdue headers and task text in their existing styles, and preserve all existing widget content and behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: The calendar-week widget visually emphasizes its overdue header and overdue task text.
- `android-today-task-widget`: The Today widget visually emphasizes overdue task text.

## Impact

- Android widget text styling, color resources for light and dark themes, row binding, and focused widget tests.
- No changes to overdue classification, task selection, ordering, grouping, capacity, actions, appointment indicators, refresh behavior, or non-widget screens.
