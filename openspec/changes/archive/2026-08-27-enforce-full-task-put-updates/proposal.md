## Why

The task update endpoint is labelled `PUT` but currently applies partial patches. This makes an omitted nullable field indistinguishable from a requested clear, so web clients cannot reliably remove a task schedule by sending `scheduledAt: null`.

## What Changes

- **BREAKING** Require ordinary task updates to send a complete mutable task representation in a typed `TaskUpdateRequest`.
- **BREAKING** Make `PUT /tasks/{id}` replace every mutable base-task property, including nullable properties such as `priority`, `scheduledAt`, and `dueAt`.
- Add distinct recurring-update endpoints: one that splits and replaces the following series with a complete `TaskUpdateRequest`, and one that updates a single occurrence through a limited `OccurrenceUpdateRequest`.
- Update Android and web clients to construct and send the required full representations for base-task and following-series updates.

## Capabilities

### New Capabilities

- `task-update-contract`: Define full-replacement task updates and explicit scoped recurring-update endpoints.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Define reliable explicit clearing of nullable schedule, deadline, and priority values during full task replacement.
- `task-type-classification`: Require a replacement update to preserve or deliberately replace the task type.

## Impact

- Backend task controller, typed request DTOs, recurrence service logic, and API tests.
- Android Retrofit API, request models, task-detail/day/week/snooze update flows, and tests.
- Web task service, task editing callers, and tests.
- Existing clients using partial `PUT /tasks/{id}` payloads must migrate before the endpoint contract changes.
