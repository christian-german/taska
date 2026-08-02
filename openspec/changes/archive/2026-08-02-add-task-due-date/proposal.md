## Why

`scheduled_at` now describes when a task is displayed in the calendar, but it cannot express the separate deadline by which the task should be completed. A dedicated due date is needed to communicate that expectation and to provide input to future priority evaluation.

## What Changes

- Add an optional `due_at` task field, represented as `dueAt` in domain and client contracts, for the deadline by which a task should be done.
- Keep `scheduled_at`/`scheduledAt` as the independent calendar-placement timestamp.
- Persist, expose, and accept the two timestamps independently across task interfaces.
- Do not alter the existing priority algorithm in this change; it will consume `due_at` in a later change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Distinguish the new optional due-date field from the existing scheduled-time field and require independent persistence and task-contract handling.

## Impact

Affected task persistence and migrations, domain and recurring-task representations, REST and MCP task contracts, web and Android task models/forms, documentation, and corresponding tests. The existing `scheduled_at` behavior for calendar display, date filtering, and notifications remains unchanged.
