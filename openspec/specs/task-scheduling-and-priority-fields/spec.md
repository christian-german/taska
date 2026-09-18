## Purpose

Define task contracts for optional manual priority and planned scheduling timestamps.
## Requirements
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
The system SHALL name the existing optional planned task timestamp `scheduled_at` in persisted storage and `scheduledAt` in task-domain and client representations. The distinct timestamp used to identify a recurring occurrence in task mutations SHALL be named `occurrenceScheduledAt`. Task creation, retrieval, update, recurrence occurrences, date filtering, and notification scheduling SHALL retain the existing behavior of the planned timestamp under the new name. The system SHALL NOT expose or accept `due_at` or `dueAt` for this planned-time field. Date-based filtering and calendar occurrence queries SHALL determine local-day boundaries using the configured application calendar time zone. When an explicit non-null `scheduledAt` is created or changed, the system SHALL require it to be authorized by the associated project's planning calendar; an invalid value SHALL be rejected without persisting the write. A complete `TaskUpdateRequest` with `scheduledAt: null` SHALL remove the base task's schedule while preserving the independently supplied `dueAt` value. The Android, web, and Tauri task-detail interfaces SHALL allow a user to remove an assigned schedule completely. Complete schedule removal SHALL persist `scheduledAt` as `null`, preserve the independent `dueAt` deadline and unrelated task values, and show the task as unscheduled after the update succeeds. Complete schedule removal SHALL remain distinct from clearing only a schedule's time, which SHALL retain its date as an all-day schedule.

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

#### Scenario: Remove a complete schedule during full replacement
- **GIVEN** a task has an assigned `scheduledAt` and an independent `dueAt`
- **WHEN** a client sends a valid complete `TaskUpdateRequest` with `scheduledAt: null`
- **THEN** the system SHALL persist `scheduledAt` as `null`
- **AND** it SHALL persist the supplied `dueAt` value independently

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

### Requirement: Tasks expose an optional due date distinct from scheduled time
The system SHALL persist an optional absolute deadline as `due_at` and expose it as `dueAt` for non-recurring tasks and individual recurring-occurrence overrides. `dueAt` SHALL represent the instant by which that task or occurrence is expected to be completed, while `scheduledAt` SHALL continue to represent only calendar placement. A recurring-series definition SHALL NOT persist a non-null `due_at`, and a request that explicitly combines a recurring series with a non-null `dueAt` SHALL be rejected. Because the Android task-detail deadline control accepts a calendar date without a time, its assigned deadline value SHALL be presented as a localized calendar date without a clock time.

#### Scenario: Create a non-recurring task with distinct schedule and deadline
- **WHEN** a client creates a non-recurring task with different `scheduledAt` and `dueAt` values
- **THEN** retrieval SHALL return both values unchanged in their respective fields

#### Scenario: Create a non-recurring task without a due date
- **WHEN** a client creates a non-recurring task without `dueAt`
- **THEN** its task representation SHALL contain `dueAt: null`

#### Scenario: Reject an absolute deadline on a recurring series
- **WHEN** a client creates or replaces a recurring-series definition with a non-null `dueAt`
- **THEN** the system SHALL reject the request without persisting the series change

#### Scenario: Convert a task with a deadline into a recurring series
- **GIVEN** a non-recurring task has a persisted `dueAt`
- **WHEN** a partial update converts it into a recurring series without supplying a new deadline
- **THEN** the system SHALL clear the persisted `due_at`

#### Scenario: Update a non-recurring task deadline without rescheduling it
- **WHEN** a client updates a non-recurring task's `dueAt` without changing its `scheduledAt`
- **THEN** the task's deadline SHALL be updated and its scheduled time SHALL remain unchanged

#### Scenario: Deadline does not change calendar scheduling behavior
- **WHEN** a task or occurrence has a `dueAt` value that differs from its `scheduledAt` value
- **THEN** calendar display, schedule-based date filtering, recurrence scheduling, and notification timing SHALL continue to use `scheduledAt`

#### Scenario: Android task detail displays a deadline selected as a date
- **GIVEN** a non-recurring task or recurring occurrence has an assigned `dueAt`
- **WHEN** Android task detail presents the deadline selected through its date-only deadline control
- **THEN** it SHALL display the localized calendar date
- **AND** it SHALL NOT display a clock time
- **AND** it SHALL NOT change the persisted `dueAt` instant

### Requirement: Android task detail distinguishes absent, all-day, and timed schedules
Android task detail SHALL derive scheduled-date presentation from both the presence of `scheduledAt` and, when present, the task's `allDay` value. An absent `scheduledAt` SHALL have no displayed scheduled value regardless of `allDay`. An assigned all-day schedule SHALL display its localized calendar date without a clock time. An assigned timed schedule SHALL display its localized calendar date and localized clock time.

#### Scenario: Removed schedule has no displayed value
- **GIVEN** a schedule-removal update returns a task with `scheduledAt: null`
- **WHEN** Android task detail presents the returned task
- **THEN** it SHALL show no assigned scheduled date or time
- **AND** the value of `allDay` SHALL NOT cause a date or clock time to be displayed

#### Scenario: All-day schedule displays only its date
- **GIVEN** a task has an assigned `scheduledAt` and `allDay: true`
- **WHEN** Android task detail presents its schedule
- **THEN** it SHALL display the localized calendar date
- **AND** it SHALL NOT display a clock time

#### Scenario: Timed schedule displays its date and time
- **GIVEN** a task has an assigned `scheduledAt` and `allDay: false`
- **WHEN** Android task detail presents its schedule
- **THEN** it SHALL display the localized calendar date and localized clock time

#### Scenario: Schedule and deadline presentation remain independent
- **GIVEN** a task has both an assigned schedule and an independent deadline
- **WHEN** Android task detail presents both properties
- **THEN** the schedule SHALL follow its absent, all-day, or timed presentation state
- **AND** the deadline SHALL remain a date-only presentation

### Requirement: Recurring task occurrences preserve due-date semantics
The system SHALL expose a recurring occurrence's effective `dueAt` only when its persisted occurrence state contains an explicit due-date override. A virtual occurrence and a materialized occurrence without that override SHALL expose `dueAt: null`. A scoped update that changes one occurrence's `dueAt` SHALL persist the override without changing the parent recurring series or other occurrences.

#### Scenario: Virtual occurrence has no inherited deadline
- **WHEN** recurrence expansion generates an occurrence with no persisted due-date override
- **THEN** the generated occurrence representation SHALL contain `dueAt: null`

#### Scenario: Update one recurring occurrence deadline
- **WHEN** a client performs a `THIS_ONLY` update with `dueAt` for a recurring occurrence
- **THEN** that occurrence SHALL return the new deadline
- **AND** other occurrences without their own override SHALL return `dueAt: null`

### Requirement: Variant-specific task fields preserve scheduling and priority semantics

Non-recurring-task and recurring-occurrence representations SHALL expose nullable `priority`, `scheduledAt`, and `dueAt` values. A recurring-series representation SHALL expose nullable `priority` and `scheduledAt` but SHALL NOT expose `dueAt`. A recurring occurrence SHALL resolve supported occurrence-state overrides before series values, except that `dueAt` SHALL resolve only from occurrence state. Recurring-series and recurring-occurrence representations SHALL expose recurrence-rule metadata, while a non-recurring representation SHALL not expose recurrence-rule metadata.

#### Scenario: Occurrence inherits only supported series values
- **WHEN** a virtual recurring occurrence has no priority, schedule, or deadline override
- **THEN** its representation SHALL inherit priority and recurrence-expanded scheduling according to existing semantics
- **AND** it SHALL expose `dueAt: null`

#### Scenario: Recurring series omits deadline
- **WHEN** a recurring-series definition is returned
- **THEN** its representation SHALL NOT contain a `dueAt` property

#### Scenario: Non-recurring task omits recurrence metadata
- **WHEN** a non-recurring task representation is returned
- **THEN** it SHALL expose its scheduling, deadline, and priority values
- **AND** it SHALL not expose `recurrenceRule` or `rruleEndsAt`
