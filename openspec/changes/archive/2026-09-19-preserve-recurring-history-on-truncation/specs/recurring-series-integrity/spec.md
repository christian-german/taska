## ADDED Requirements

### Requirement: Persisted recurring definitions require a recurrence rule

Every write that creates or mutates a task with `isRecurring: true` SHALL require a non-null, non-blank recurrence rule. The persistence layer SHALL enforce the same invariant. A rejected write SHALL NOT persist a partially updated task or successor series.

#### Scenario: Create a recurring series without a rule
- **WHEN** a client attempts to create a task with `isRecurring: true` and a null or blank recurrence rule
- **THEN** the system SHALL reject the write
- **AND** it SHALL NOT persist the invalid recurring definition

#### Scenario: Convert a task into a recurring series without a rule
- **WHEN** a client attempts to update or replace a task as recurring without a non-blank recurrence rule
- **THEN** the system SHALL reject the write without changing the stored task

#### Scenario: Create a successor without a rule
- **WHEN** a partial or complete following-series operation would create a recurring successor without a non-blank recurrence rule
- **THEN** the system SHALL reject the operation without persisting an invalid successor

#### Scenario: Persistence bypass attempts an invalid series
- **WHEN** a database write attempts to store `is_recurring = true` with a null or blank `recurrence_rule`
- **THEN** the database SHALL reject the write

### Requirement: Existing ruleless recurring rows are repaired

The recurrence-integrity migration SHALL convert every existing task that claims to recur with a null or blank rule into a non-recurring task and SHALL clear its recurrence end.

#### Scenario: Upgrade a database containing a ruleless series
- **WHEN** the migration encounters a task with `is_recurring = true` and a null or blank `recurrence_rule`
- **THEN** it SHALL persist that task with `is_recurring = false`
- **AND** it SHALL set `rrule_ends_at` to null

#### Scenario: Upgrade a valid recurring series
- **WHEN** the migration encounters a recurring task with a non-blank recurrence rule
- **THEN** it SHALL preserve the task as recurring

