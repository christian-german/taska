## MODIFIED Requirements

### Requirement: Tasks use scheduled-at terminology for planned time
The system SHALL name the existing optional planned task timestamp `scheduled_at` in persisted storage and `scheduledAt` in task-domain and client representations. The distinct timestamp used to identify a recurring occurrence in task mutations SHALL be named `occurrenceScheduledAt`. Task creation, retrieval, update, recurrence occurrences, date filtering, and notification scheduling SHALL retain the existing behavior of the planned timestamp under the new name. The system SHALL NOT expose or accept `due_at` or `dueAt` for this planned-time field. Date-based filtering and calendar occurrence queries SHALL determine local-day boundaries using the configured application calendar time zone. When an explicit non-null `scheduledAt` is created or changed, the system SHALL require it to be authorized by the associated project's planning calendar; an invalid value SHALL be rejected without persisting the write. The Android, web, and Tauri task-detail interfaces SHALL allow a user to remove an assigned schedule completely. Complete schedule removal SHALL persist `scheduledAt` as `null`, preserve the independent `dueAt` deadline and unrelated task values, and show the task as unscheduled after the update succeeds. Complete schedule removal SHALL remain distinct from clearing only a schedule's time, which SHALL retain its date as an all-day schedule.

#### Scenario: Create and retrieve a scheduled task
- **WHEN** a client creates a task with `scheduledAt`
- **THEN** retrieval SHALL return the same timestamp as `scheduledAt`

#### Scenario: Query scheduled tasks
- **WHEN** a client requests tasks using an existing date-based filter
- **THEN** the system SHALL apply the filter using each task's `scheduledAt` value and the configured calendar time zone's local-day boundaries

#### Scenario: Calendar query includes a task on its configured local date
- **WHEN** a task's `scheduledAt` falls after the configured time zone's start of a requested calendar date and before the next local-day start
- **THEN** the calendar occurrence query SHALL include the task for that requested date

#### Scenario: Calendar query excludes a task on an adjacent configured local date
- **WHEN** a task's `scheduledAt` falls outside the configured time zone's requested local-day boundaries
- **THEN** the calendar occurrence query SHALL exclude the task from that requested date

#### Scenario: Reject a scheduled time outside project availability
- **WHEN** a client creates or updates a task with a `scheduledAt` outside the associated project's planning-calendar availability
- **THEN** the system SHALL reject the request without changing the task's scheduled time

#### Scenario: Accept a scheduled time inside project availability
- **WHEN** a client creates or updates a task with a `scheduledAt` inside the associated project's planning-calendar availability
- **THEN** the system SHALL persist the scheduled time

#### Scenario: Preserve scheduled times during migration
- **WHEN** an existing task or recurring occurrence has a persisted `due_at` timestamp before migration
- **THEN** it SHALL have the identical instant persisted as `scheduled_at` after migration

#### Scenario: Reject legacy planned-time property
- **WHEN** a client sends or relies on `due_at` or `dueAt` for the planned-time field after this change
- **THEN** the task contract SHALL not treat that property as the task's scheduled time

#### Scenario: Remove a complete schedule from task detail
- **GIVEN** a task has an assigned `scheduledAt` and an independent `dueAt`
- **WHEN** the user removes the schedule from the Android, web, or Tauri task-detail interface
- **THEN** the client SHALL persist `scheduledAt` as `null`
- **AND** it SHALL preserve `dueAt` and unrelated task values
- **AND** task detail SHALL show the task as unscheduled after the update succeeds

#### Scenario: Remove the schedule from a task without a deadline
- **GIVEN** a task has an assigned `scheduledAt` and no `dueAt`
- **WHEN** the user removes the schedule from the Android, web, or Tauri task-detail interface
- **THEN** the client SHALL persist `scheduledAt` as `null`
- **AND** the task SHALL continue to have no deadline

#### Scenario: Clear only the scheduled time on web or Tauri
- **GIVEN** a task has a scheduled date and time
- **WHEN** the user chooses the time-clear action in the shared date-time picker
- **THEN** the client SHALL retain the scheduled date
- **AND** it SHALL represent the schedule as all-day

#### Scenario: Complete schedule removal fails
- **GIVEN** a task has an assigned `scheduledAt`
- **WHEN** a complete-removal request from task detail fails
- **THEN** task detail SHALL NOT represent the task as successfully unscheduled
