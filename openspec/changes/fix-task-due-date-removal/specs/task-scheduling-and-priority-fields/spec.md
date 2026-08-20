## MODIFIED Requirements

### Requirement: Tasks expose an optional due date distinct from scheduled time
The system SHALL persist an optional task deadline as `due_at` and expose it as `dueAt` in task-domain, REST, MCP, and client representations. `dueAt` SHALL represent the instant by which a task is expected to be completed, while `scheduledAt` SHALL continue to represent only the task's calendar-placement time. The two values SHALL be independently writable and retrievable, and an absent due date SHALL be represented as `null`. The Android, web, and Tauri task-detail interfaces SHALL allow a user to remove an assigned due date. Removing a due date SHALL persist `dueAt` as `null` without changing `scheduledAt` or `allDay`, and the task-detail interface SHALL show the deadline as absent after the update succeeds.

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

#### Scenario: Remove a deadline from a scheduled task in a graphical client
- **GIVEN** a task has an assigned `dueAt` and has existing `scheduledAt` and `allDay` values
- **WHEN** the user removes the deadline from the Android, web, or Tauri task-detail interface
- **THEN** the client SHALL persist `dueAt` as `null`
- **AND** it SHALL preserve `scheduledAt` and `allDay` unchanged
- **AND** the task-detail interface SHALL show no assigned deadline after the update succeeds

#### Scenario: Remove a deadline from an unscheduled task in a graphical client
- **GIVEN** a task has an assigned `dueAt` and has no `scheduledAt`
- **WHEN** the user removes the deadline from the Android, web, or Tauri task-detail interface
- **THEN** the client SHALL persist `dueAt` as `null`
- **AND** the task SHALL remain unscheduled

#### Scenario: Deadline removal fails
- **GIVEN** a task has an assigned `dueAt`
- **WHEN** a deadline-removal request from a task-detail interface fails
- **THEN** the interface SHALL NOT represent the deadline as successfully removed
