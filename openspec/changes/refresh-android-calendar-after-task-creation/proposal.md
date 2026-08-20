## Why

Creating a task from an open Android day or week calendar closes the creation sheet but leaves the calendar's already-loaded task data unchanged. The new task therefore remains invisible until the user navigates away and returns, even when it belongs in the currently displayed calendar range.

## What Changes

- Refresh the currently displayed Android day calendar after task creation succeeds.
- Refresh the currently displayed Android week calendar after task creation succeeds.
- Keep the selected day or week unchanged while refreshing, and do not refresh when creation is dismissed or fails.

## Capabilities

### New Capabilities

- `android-calendar-task-refresh`: Defines how Android calendar screens synchronize their visible task lists after in-app task creation.

### Modified Capabilities

None.

## Impact

- Android day and week activity task-creation callbacks.
- Focused Android tests for successful task-creation refresh behavior.
- No API, persistence, navigation, or non-calendar screen behavior changes.
