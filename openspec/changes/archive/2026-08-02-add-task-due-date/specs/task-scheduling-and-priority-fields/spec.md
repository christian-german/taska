## ADDED Requirements

### Requirement: Tasks expose an optional due date distinct from scheduled time
The system SHALL persist an optional task deadline as `due_at` and expose it as `dueAt` in task-domain, REST, MCP, and client representations. `dueAt` SHALL represent the instant by which a task is expected to be completed, while `scheduledAt` SHALL continue to represent only the task's calendar-placement time. The two values SHALL be independently writable and retrievable, and an absent due date SHALL be represented as `null`.

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

### Requirement: Recurring task occurrences preserve due-date semantics
The system SHALL expose a recurring occurrence's effective `dueAt`. A virtual occurrence SHALL inherit its recurring task's `dueAt`; a persisted occurrence with a due-date override SHALL expose the override. A scoped update that changes an individual occurrence's `dueAt` SHALL persist an occurrence-specific override without changing the parent recurring task's deadline.

#### Scenario: Virtual occurrence inherits the recurring task deadline
- **WHEN** a recurring task has a `dueAt` and a generated occurrence has no persisted override
- **THEN** the generated occurrence representation SHALL contain the recurring task's `dueAt`

#### Scenario: Update one recurring occurrence deadline
- **WHEN** a client performs a `THIS_ONLY` update with `dueAt` for a recurring occurrence
- **THEN** that occurrence SHALL return the new deadline and other occurrences SHALL retain the recurring task's deadline
