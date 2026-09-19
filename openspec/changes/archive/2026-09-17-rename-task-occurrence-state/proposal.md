## Why

`TaskInstance` suggests that a persisted row is the occurrence itself, while the row actually stores only sparse state for an occurrence generated from a recurring series. The `taskId` name also obscures that the foreign identifier always points to the recurring series definition.

## What Changes

- Rename the persistence model from `TaskInstance` to `TaskOccurrenceState` and its repository and status types consistently.
- Rename the internal `taskId` association to `seriesId` wherever it identifies the recurring series backing occurrence state.
- Rename the database table from `task_instances` to `task_occurrence_states` and its foreign-key column from `task_id` to `series_id` through a forward migration.
- Preserve all REST, MCP, notification payload, and observable occurrence behavior.

## Capabilities

### New Capabilities

- `task-occurrence-state-persistence`: Defines the persistence identity and migration guarantees for materialized recurring-occurrence state.

### Modified Capabilities

None. This is an implementation vocabulary and persistence-schema refactor with no product requirement change.

## Impact

- Backend occurrence entity, enum, repository, services, mappers, notification scheduling, and tests.
- A database migration that renames the existing occurrence-state table, column, constraints, and indexes without losing data.
- No client API or transport contract changes; task resource identifiers remain named `taskId` at external boundaries.
