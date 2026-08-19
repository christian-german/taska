## Why

Android home-screen widgets currently format every scheduled task with a clock time. All-day tasks have a scheduled date but no meaningful time, so displaying the timestamp's placeholder time misrepresents them as time-specific tasks.

## What Changes

- Omit the time from all-day task rows in both the calendar-week and Today Android widgets.
- Continue showing the scheduled time for non-all-day task rows.
- Preserve each task's title, ordering, completion action, and task-detail navigation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Calendar-week task rows will distinguish all-day tasks from timed tasks when rendering their text.
- `android-today-task-widget`: Today task rows will distinguish all-day tasks from timed tasks when rendering their text.

## Impact

- Android widget row formatting and focused widget tests.
- No changes to task storage, API representations, widget task selection, sorting, actions, or non-widget Android screens.
