## Why

Android task detail can present a clock time for values that users selected as dates only. In particular, an all-day schedule is correct when initially created, but schedule-removal/update flows can leave metadata that causes a midnight timestamp to appear as a local time such as `02:00`. Deadlines selected through the date-only due-date control are also rendered with a clock time even though no due time was selected.

## What Changes

- Make Android task detail render an absent scheduled date as unassigned, regardless of the task's `allDay` value.
- Render an assigned all-day schedule as a calendar date without a clock time.
- Continue rendering both date and localized time for an assigned timed schedule.
- Render the Android task-detail deadline as a calendar date without a clock time because the mobile deadline control is date-only.
- Add regression coverage for scheduled-date and due-date presentation, including the state returned after schedule removal.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Define how Android task detail presents absent, all-day, and timed schedules, and date-only deadlines.

## Impact

- Android task-detail date formatting and presentation.
- Android task-detail unit/UI regression tests.
- No backend, database, REST, MCP, web/Tauri, scheduling, deadline persistence, recurrence, notification, or calendar-query behavior changes.
