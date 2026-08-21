## MODIFIED Requirements

### Requirement: Tasks expose an optional due date distinct from scheduled time
The system SHALL persist an optional task deadline as `due_at` and expose it as `dueAt` in task-domain, REST, MCP, and client representations. `dueAt` SHALL represent the instant by which a task is expected to be completed, while `scheduledAt` SHALL continue to represent only the task's calendar-placement time. The two values SHALL be independently writable and retrievable, and an absent due date SHALL be represented as `null`. Because the Android task-detail deadline control accepts a calendar date without a time, its assigned deadline value SHALL be presented as a localized calendar date without a clock time.

#### Scenario: Create a task with distinct schedule and deadline
- **WHEN** a client creates a task with different `scheduledAt` and `dueAt` values
- **THEN** retrieval SHALL return both values unchanged in their respective fields

#### Scenario: Create a task without a due date
- **WHEN** a client creates a task without `dueAt`
- **THEN** its task representation SHALL contain `dueAt: null`

#### Scenario: Update a task deadline without rescheduling it
- **WHEN** a client updates a task's `dueAt` without changing its `scheduledAt`
- **THEN** the task's deadline SHALL be updated and its scheduled time SHALL remain unchanged

#### Scenario: Deadline does not change calendar scheduling behavior
- **WHEN** a task has a `dueAt` value that differs from its `scheduledAt` value
- **THEN** calendar display, schedule-based date filtering, recurrence scheduling, and notification timing SHALL continue to use `scheduledAt`

#### Scenario: Android task detail displays a deadline selected as a date
- **GIVEN** a task has an assigned `dueAt`
- **WHEN** Android task detail presents the deadline selected through its date-only deadline control
- **THEN** it SHALL display the localized calendar date
- **AND** it SHALL NOT display a clock time
- **AND** it SHALL NOT change the persisted `dueAt` instant

## ADDED Requirements

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
