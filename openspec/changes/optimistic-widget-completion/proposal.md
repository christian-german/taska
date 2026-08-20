## Why

Completing a task from an Android home-screen widget currently leaves its completion control unchecked until the authenticated API request finishes and the widget refreshes. Network latency therefore makes a successful tap appear to have been ignored.

## What Changes

- Check an incomplete task's widget completion control immediately when the user invokes it, before the completion request finishes.
- Keep the optimistic checked state when completion succeeds and let the normal widget refresh reconcile the row with server data.
- Restore the unchecked control when completion fails so the widget never leaves a rejected completion presented as successful.
- Apply the behavior to incomplete task completion in both the calendar-week and Today Android widgets, including recurring occurrences.
- Preserve the existing behavior for reopening already-completed Today tasks.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Calendar-week completion controls provide immediate optimistic feedback and roll back failed requests.
- `android-today-task-widget`: Incomplete Today-task completion controls provide the same optimistic feedback and rollback.

## Impact

- Android widget completion-action dispatch, `RemoteViews` updates, failure recovery, and focused widget tests.
- No task API, persistence, task-selection, scheduling, recurrence, or non-widget behavior changes.
