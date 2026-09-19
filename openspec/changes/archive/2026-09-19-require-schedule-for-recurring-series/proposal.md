## Why

A recurring task without `scheduledAt` has no recurrence anchor and therefore cannot generate meaningful occurrences. The backend currently accepts this invalid state even though it already requires a recurrence rule and forbids a series-level deadline.

## What Changes

- **BREAKING**: Reject creation or mutation of a recurring-series definition when its resulting `scheduledAt` is null.
- Apply the invariant consistently to REST and MCP mutations, including creation, complete replacement, partial base updates, and following-series replacement.
- Add a database constraint preventing recurring task rows without `scheduled_at`.
- Convert existing recurring rows with no `scheduled_at` into non-recurring tasks and clear their recurrence metadata before enabling the constraint.
- Preserve nullable schedules for non-recurring tasks and occurrence-level schedule overrides.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Require every recurring-series definition to have a non-null planned timestamp while preserving optional schedules for non-recurring tasks and occurrence overrides.
- `task-update-contract`: Reject complete or partial mutations whose resulting task definition is recurring without a schedule.

## Impact

- Backend task-definition validation and recurring-series mutation services.
- Task mutation unit tests and persistence invariant tests.
- A forward-only Flyway migration for data repair and the database constraint.
- REST and MCP request shapes remain unchanged; previously accepted invalid recurring mutations now return a client error.
