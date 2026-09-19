## ADDED Requirements

### Requirement: Task mutations preserve the recurring schedule invariant

Every complete or partial base-task mutation SHALL validate the resulting combination of recurring status and `scheduledAt` before persisting it. A following-series replacement SHALL validate its successor definition before truncating the original series or detaching occurrence state.

#### Scenario: Convert an unscheduled task to recurring
- **WHEN** a complete or partial mutation makes an unscheduled non-recurring task recurring without assigning `scheduledAt`
- **THEN** the system SHALL reject the mutation without changing the task

#### Scenario: Remove the schedule from a recurring series
- **WHEN** a complete replacement keeps `isRecurring: true` and supplies `scheduledAt: null`
- **THEN** the system SHALL reject the replacement without changing the series

#### Scenario: Replace a following series without a schedule
- **WHEN** a following-series replacement supplies `isRecurring: true` and `scheduledAt: null`
- **THEN** the system SHALL reject the replacement before changing the original series or its occurrence state

#### Scenario: Remove the schedule while making a task non-recurring
- **WHEN** a complete replacement supplies `isRecurring: false` and `scheduledAt: null`
- **THEN** the system SHALL accept the resulting unscheduled non-recurring task when its other values are valid
