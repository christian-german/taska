## ADDED Requirements

### Requirement: Tasks expose optional manual priority
The system SHALL allow a task and its applicable recurring-task occurrence representation to have no manual `priority`. A supplied manual priority SHALL continue to satisfy the existing priority-range validation. Task creation, update, persistence, and task representations SHALL preserve an absent priority as `null` rather than assigning priority `4`.

#### Scenario: Create a task without a priority
- **WHEN** a client creates a task without assigning a manual priority
- **THEN** the created task representation SHALL contain `priority: null`

#### Scenario: Clear an assigned priority
- **WHEN** a client updates a task to remove its manual priority
- **THEN** subsequent task retrieval SHALL represent its `priority` as `null`

#### Scenario: Reject an invalid assigned priority
- **WHEN** a client supplies a manual priority outside the supported range
- **THEN** the system SHALL reject the write without persisting the invalid value

### Requirement: Tasks use scheduled-at terminology for planned time
The system SHALL name the existing optional planned task timestamp `scheduled_at` in persisted storage and `scheduledAt` in task-domain and client representations. The distinct timestamp used to identify a recurring occurrence in task mutations SHALL be named `occurrenceScheduledAt`. Task creation, retrieval, update, recurrence occurrences, date filtering, and notification scheduling SHALL retain the existing behavior of the planned timestamp under the new name. The system SHALL NOT expose or accept `due_at` or `dueAt` for this planned-time field.

#### Scenario: Create and retrieve a scheduled task
- **WHEN** a client creates a task with `scheduledAt`
- **THEN** retrieval SHALL return the same timestamp as `scheduledAt`

#### Scenario: Query scheduled tasks
- **WHEN** a client requests tasks using an existing date-based filter
- **THEN** the system SHALL apply the filter using each task's `scheduledAt` value

#### Scenario: Preserve scheduled times during migration
- **WHEN** an existing task or recurring occurrence has a persisted `due_at` timestamp before migration
- **THEN** it SHALL have the identical instant persisted as `scheduled_at` after migration

#### Scenario: Reject legacy planned-time property
- **WHEN** a client sends or relies on `due_at` or `dueAt` for the planned-time field after this change
- **THEN** the task contract SHALL not treat that property as the task's scheduled time
