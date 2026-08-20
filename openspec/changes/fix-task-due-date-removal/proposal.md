## Why

Task deadlines can be assigned independently from calendar scheduling, but users cannot reliably remove an assigned deadline from the task-detail interfaces. Android currently clears the task's scheduled date instead, while the web deadline picker only offers an action that removes the deadline's time component and leaves its date assigned.

## What Changes

- Allow a user to remove an assigned due date from the Android task-detail screen.
- Allow a user to remove an assigned due date from the shared web and Tauri task-detail screen.
- Persist removal as an absent `dueAt` value without changing the task's independent `scheduledAt` value or its all-day scheduling state.
- Keep the existing ability to edit a deadline's date and time.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Require the Android, web, and Tauri task-detail interfaces to remove an assigned deadline independently from calendar scheduling.

## Impact

- Android task-detail presentation, deadline update behavior, and tests.
- Shared Angular task-detail presentation used by the web and Tauri clients, plus tests.
- No backend API, persistence, MCP, recurrence, calendar-placement, notification, or priority-policy changes.
