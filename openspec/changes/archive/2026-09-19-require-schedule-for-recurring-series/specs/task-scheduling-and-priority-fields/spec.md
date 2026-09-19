## ADDED Requirements

### Requirement: Recurring-series definitions require a schedule anchor

Every recurring-series definition SHALL have a non-null `scheduledAt` value that anchors recurrence expansion. The invariant SHALL apply independently at the application-service and persistence boundaries. A non-recurring task MAY remain unscheduled, and an occurrence's persisted schedule override MAY remain null.

#### Scenario: Create a recurring series without a schedule
- **WHEN** a caller attempts to create a task with `isRecurring: true` and `scheduledAt: null` or omitted
- **THEN** the system SHALL reject the creation without persisting a task

#### Scenario: Persist a recurring series without a schedule
- **WHEN** a persistence caller attempts to save a task row with `is_recurring = TRUE` and `scheduled_at = NULL`
- **THEN** the database SHALL reject the row

#### Scenario: Keep a non-recurring task unscheduled
- **WHEN** a caller creates or updates a non-recurring task with `scheduledAt: null`
- **THEN** the system SHALL preserve the task as unscheduled

#### Scenario: Upgrade an invalid stored series
- **GIVEN** an existing task row is recurring and has no `scheduled_at`
- **WHEN** the invariant migration runs
- **THEN** the migration SHALL convert the row to a non-recurring task
- **AND** it SHALL clear its recurrence rule and recurrence end metadata without deleting the task
