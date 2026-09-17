## Context

Recurring occurrences are generated from a series and remain virtual until an action requires sparse persisted state. The current `TaskInstance` and `taskId` names imply a standalone task instance rather than state attached to a recurring series. That terminology now appears across JPA, repositories, services, notification scheduling, tests, and the `task_instances` schema.

The REST and MCP contracts still address a task resource through `taskId` and expose the established occurrence representation. Those external names are not part of this persistence refactor.

## Goals / Non-Goals

**Goals:**

- Make the Java model state-oriented with `TaskOccurrenceState`, `TaskOccurrenceStatus`, and `seriesId` terminology.
- Rename the database table and association column without losing existing occurrence state.
- Apply the same series vocabulary to notification delivery markers that reference recurring series.
- Keep virtual occurrence generation, override resolution, close/reopen, and notification behavior unchanged.

**Non-Goals:**

- Rename `Task`, REST path variables, JSON properties, MCP parameters, or Firebase's existing `task_id` payload.
- Change occurrence identity, lifecycle rules, or supported overrides.
- Rewrite earlier migrations that may already have been applied.

## Decisions

### Rename the complete persistence vocabulary

Rename the entity, enum, repository, repository methods, result component, local variables, and tests together. `TaskOccurrenceState` represents the optional persisted state, while the occurrence itself remains the combination of a series and `occurrenceScheduledAt`.

Alternative considered: rename only the entity. This would leave `TaskInstanceRepository`, `TaskInstanceStatus`, and `taskId` carrying the old ambiguity into every call site.

### Preserve external task resource names

Keep `taskId` at HTTP, MCP, and Firebase boundaries because those identifiers address the task resource and are part of existing contracts. Map them to `seriesId` only inside recurring-occurrence persistence and notification-marker components.

Alternative considered: rename every `taskId` to `seriesId`. This would introduce a breaking API change unrelated to the persistence clarification.

### Rename deployed schema objects through a forward migration

Add a new migration that renames `task_instances` to `task_occurrence_states`, `task_id` to `series_id`, and associated constraints and indexes. Apply the same column terminology to `task_occurrence_notifications`. Do not edit historical migrations.

Alternative considered: create a new table and copy rows. A direct PostgreSQL rename is atomic, preserves data and foreign keys, and avoids maintaining two representations.

## Risks / Trade-offs

- **A missed Java reference leaves mixed vocabulary** → search production code and tests for every old type, accessor, repository method, and table name.
- **A generated PostgreSQL constraint name differs from expectations** → exercise all migrations from an empty PostgreSQL database in an integration test.
- **The refactor accidentally changes API fields** → keep controller, DTO, MCP, and Firebase payload names unchanged and run contract tests.

## Migration Plan

1. Deploy the forward migration before Hibernate validates or queries the renamed entity mapping.
2. Start the application with Java code mapped to `task_occurrence_states.series_id`.
3. Roll back application code only together with a reverse schema rename; no data transformation is required.

## Open Questions

None.
