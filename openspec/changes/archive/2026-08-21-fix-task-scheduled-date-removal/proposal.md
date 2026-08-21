## Why

Users cannot reliably remove a task's scheduled date from task detail. On Android, selecting the existing removal action does not clear the schedule. In the shared web and Tauri interface, the visible clear action in the date-time picker clears only the scheduled time and leaves the date assigned.

## What Changes

- Make the Android task-detail schedule-removal action persist an absent `scheduledAt` value.
- Provide a complete scheduled-date removal action in the shared web and Tauri task-detail interface, distinct from clearing only the scheduled time.
- Show the task as unscheduled after removal succeeds, without changing its independent due date or other task fields.
- Preserve the existing date and time editing behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Require Android, web, and Tauri task-detail interfaces to remove the complete planned schedule independently from the task deadline.

## Impact

- Android task-detail schedule removal and its tests.
- Shared Angular task-detail and date-time picker presentation used by web and Tauri, plus tests.
- No backend API, persistence, recurrence policy, calendar filtering, notification, or deadline behavior changes.
