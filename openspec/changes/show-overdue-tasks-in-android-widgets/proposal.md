## Why

Android widgets currently request only the current day or current week, so incomplete tasks planned before today disappear from both widgets even though they still need attention.

## What Changes

- Include incomplete tasks whose scheduled date is before the device-local current date in both Android task widgets.
- Place overdue tasks before tasks planned for today or later.
- Add a single `Overdue` group at the start of the calendar-week widget instead of grouping overdue tasks under their original dates.
- Preserve the existing completion, navigation, recurrence, refresh, and visual behavior for widget task rows.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: The calendar-week widget includes all incomplete overdue scheduled tasks in a leading `Overdue` group.
- `android-today-task-widget`: The Today widget includes incomplete overdue scheduled tasks before tasks planned for today.

## Impact

- Android widget task retrieval, filtering, ordering, status counts, and weekly collection grouping.
- Android widget tests for local-date boundaries, completed-task exclusion, ordering, grouping, and recurring occurrences.
- No task API contract, task mutation, widget action, or non-widget screen changes.
