## MODIFIED Requirements

### Requirement: Tasks expose optional manual priority
The system SHALL allow a task and its applicable recurring-task occurrence representation to have no manual `priority`. A supplied manual priority SHALL continue to satisfy the existing priority-range validation. Task creation, update, persistence, and task representations SHALL preserve an absent priority as `null` rather than assigning priority `4`. A complete `TaskUpdateRequest` with `priority: null` SHALL clear a base task's manual priority; an `OccurrenceUpdateRequest` with `priority: null` SHALL replace the occurrence's supported priority value according to the occurrence-update contract.

#### Scenario: Create a task without a priority
- **WHEN** a client creates a task without assigning a manual priority
- **THEN** the created task representation SHALL contain `priority: null`

#### Scenario: Clear an assigned priority during full replacement
- **WHEN** a client sends a valid complete `TaskUpdateRequest` with `priority: null`
- **THEN** the updated base task representation SHALL contain `priority: null`

#### Scenario: Reject an invalid assigned priority
- **WHEN** a client supplies a manual priority outside the supported range
- **THEN** the system SHALL reject the write without persisting the invalid value

### Requirement: Tasks use scheduled-at terminology for planned time
The system SHALL name the existing optional planned task timestamp `scheduled_at` in persisted storage and `scheduledAt` in task-domain and client representations. The distinct timestamp used to identify a recurring occurrence in task operations SHALL be named `occurrenceScheduledAt`. Task creation, retrieval, update, recurrence occurrences, date filtering, and notification scheduling SHALL retain the existing behavior of the planned timestamp under the new name. Date-based filtering and calendar occurrence queries SHALL determine local-day boundaries using the configured application calendar time zone. When a complete task replacement supplies a non-null `scheduledAt`, the system SHALL require it to be authorized by the associated project's planning calendar; an invalid value SHALL be rejected without persisting the replacement. A complete `TaskUpdateRequest` with `scheduledAt: null` SHALL remove the base task's schedule while preserving the independently supplied `dueAt` value.

#### Scenario: Create and retrieve a scheduled task
- **WHEN** a client creates a task with `scheduledAt`
- **THEN** retrieval SHALL return the same timestamp as `scheduledAt`

#### Scenario: Query scheduled tasks
- **WHEN** a client requests tasks using an existing date-based filter
- **THEN** the system SHALL apply the filter using each task's `scheduledAt` value and the configured calendar time zone's local-day boundaries

#### Scenario: Reject a scheduled time outside project availability
- **WHEN** a client replaces a task with a non-null `scheduledAt` outside the associated project's planning-calendar availability
- **THEN** the system SHALL reject the request without changing the task's scheduled time

#### Scenario: Remove a complete schedule during full replacement
- **GIVEN** a task has an assigned `scheduledAt` and an independent `dueAt`
- **WHEN** a client sends a valid complete `TaskUpdateRequest` with `scheduledAt: null`
- **THEN** the system SHALL persist `scheduledAt` as `null`
- **AND** it SHALL persist the supplied `dueAt` value independently

#### Scenario: Clear only the scheduled time
- **GIVEN** a task has a scheduled date and time
- **WHEN** a client replaces it with the same date and an all-day value
- **THEN** the system SHALL retain the scheduled date
- **AND** it SHALL represent the schedule as all-day
