## ADDED Requirements

### Requirement: Materialized occurrence state uses series-oriented persistence identity

The system SHALL persist non-virtual recurring-occurrence state separately from its recurring series, identified by `seriesId` and `occurrenceScheduledAt`, without changing occurrence lifecycle or override behavior.

#### Scenario: Persist state for one occurrence
- **WHEN** an occurrence is completed, skipped, or modified
- **THEN** its persisted state SHALL reference the backing recurring series through `seriesId`
- **AND** its `occurrenceScheduledAt` SHALL remain the stable occurrence identity

#### Scenario: Resolve a persisted occurrence
- **WHEN** the system expands a recurring series containing persisted occurrence state
- **THEN** it SHALL apply the same completion, skip, and override resolution behavior as before the persistence rename

### Requirement: Existing occurrence state survives the schema rename

The database migration SHALL rename the occurrence-state table, series association column, constraints, and indexes without recreating or discarding existing occurrence-state rows.

#### Scenario: Upgrade a database containing occurrence state
- **WHEN** the migration runs against a database containing rows in `task_instances`
- **THEN** those rows SHALL remain available in `task_occurrence_states`
- **AND** their former `task_id` values SHALL remain available as `series_id`

### Requirement: External task identifiers remain compatible

The persistence terminology change SHALL NOT rename task resource identifiers exposed by REST, MCP, or Firebase notification payloads.

#### Scenario: Use an existing external task contract
- **WHEN** a client addresses a task or receives a task notification after the refactor
- **THEN** the existing `taskId` or `task_id` contract SHALL remain unchanged
