## Context

The previous refactor split mixed mutation methods into explicit task, occurrence, and following-series operations. Those methods still live in one `TaskService`, which consequently depends on recurrence expansion, occurrence-state persistence, notification markers, task persistence, project persistence, priority evaluation, calendar configuration, and planning-calendar validation.

The service split must preserve the existing transaction boundaries, validation order, sparse occurrence-state model, result variants, and transport contracts.

## Goals / Non-Goals

**Goals:**

- Make one service authoritative for each mutation target.
- Remove occurrence-state and RRULE dependencies from `TaskService`.
- Keep transport dispatch and shared change publication in `TaskMutationService`.
- Preserve shared task-definition mapping and schedule validation without copying it into the new services.

**Non-Goals:**

- Creating separate REST or MCP APIs for the new services.
- Changing the `Task`, `TaskOccurrenceState`, or database schema.
- Changing recurrence expansion, completion, reopening, notification, or series-splitting semantics.
- Renaming application parameter or result types.

## Decisions

### Keep the three services in the task service package

`TaskService`, `TaskOccurrenceService`, and `RecurringTaskSeriesService` remain under `com.taska.domain.task.service`. They are application services for one feature, and package proximity allows narrowly scoped reuse of task-definition mapping and schedule validation without exposing those implementation helpers as public APIs.

The alternative of placing `TaskOccurrenceService` under the occurrence persistence package was rejected because the service returns task application results, participates in transport use cases, and owns more than persistence.

### Give occurrence behavior one owner

`TaskOccurrenceService` owns occurrence expansion and the complete occurrence lifecycle: partial and complete overrides, skip, close, reopen, occurrence validation, and sparse-state restoration. It becomes the only application service using `TaskOccurrenceStateRepository` and `TaskRecurrenceService`.

### Give recurring-series topology one owner

`RecurringTaskSeriesService` owns `updateSeriesFrom`, `replaceFollowing`, and `truncateSeriesFrom`. These operations change the topology of stored series by truncating or creating definitions; they do not mutate one occurrence state.

It reuses occurrence validation for complete following-series replacement and reuses `TaskService` task-definition field application so replacement semantics remain authoritative in one place.

### Keep routing and publication at the mutation boundary

`TaskMutationService` loads the target definition, selects the owning service, invokes exactly one operation, and publishes exactly one account change signal after success. HTTP and MCP adapters continue to call this boundary with their existing contracts.

Calendar occurrence queries move directly from `TaskService` to `TaskOccurrenceService` because their result is an expanded occurrence read model rather than a list of stored task definitions.

## Risks / Trade-offs

- [Service-to-service collaboration could become circular] → Dependencies point outward from `TaskMutationService`; `TaskService` does not depend on either extracted service, and `TaskOccurrenceService` does not depend on the series service.
- [Moving code could subtly reorder validation or persistence] → Move method bodies intact first, then update call sites and run the existing mutation, query, controller, notification, and contract suites.
- [Shared task-definition behavior could be duplicated] → Keep field application and schedule validation authoritative in `TaskService` and expose them only within the service package.
