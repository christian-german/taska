## Why

Android widget status lines report the number of scheduled tasks even though that count consumes limited launcher space without helping users act on their tasks. The Today widget also mixes overdue and current-day rows without the labeled grouping already used by the Week widget.

## What Changes

- Remove scheduled-task count text from both Android widgets while retaining refresh error feedback.
- Add an `Overdue` label before overdue rows in the Today widget, matching the Week widget's label.
- Separate the overdue and current-day groups with one divider in the Today widget.
- Remove dividers between individual Today-widget task rows.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Widget chrome no longer displays scheduled-task counts.
- `android-today-task-widget`: The Today widget labels overdue work and uses a single divider between overdue and current-day groups instead of per-task dividers.

## Impact

- Android Week and Today widget status binding.
- Android Today widget layout, row binding, and focused tests.
- No changes to task retrieval, filtering, ordering, capacity, actions, recurrence, refresh behavior, or non-widget screens.
