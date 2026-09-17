## Why

Recurring series are currently scheduled for notification as if the series definition were one task, so only the first series timestamp can trigger and later occurrences are never notified. Reopening a recurring occurrence can also erase occurrence overrides or mutate the series definition when occurrence identity is missing.

## What Changes

- Notify each eligible recurring occurrence once, using its effective scheduled time and content as if it were an independent task.
- Persist notification-delivery identity separately from occurrence business state so notifying a virtual occurrence does not materialize it as a modified occurrence.
- Preserve occurrence overrides when a completed occurrence is reopened, while restoring an unmodified occurrence to its virtual state.
- Reject recurring completion or reopening requests that do not identify a valid occurrence, without mutating the series definition.
- Preserve existing notification and completion behavior for non-recurring tasks.

## Capabilities

### New Capabilities

- `task-notification-scheduling`: Defines one-time notification scheduling for non-recurring tasks and individual recurring occurrences.
- `task-occurrence-lifecycle`: Defines completion and reopening invariants for recurring occurrences.

### Modified Capabilities

None.

## Impact

- Backend notification scheduling, recurrence expansion, occurrence state handling, persistence, and tests.
- A database migration for recurring-occurrence notification delivery markers.
- Existing REST and MCP endpoint shapes remain unchanged.
