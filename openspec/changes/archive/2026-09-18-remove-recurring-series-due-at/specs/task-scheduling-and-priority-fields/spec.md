## MODIFIED Requirements

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
