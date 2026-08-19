## MODIFIED Requirements

### Requirement: Today widget displays all tasks planned for the local current day
The Android application SHALL provide a separate, resizable Today home-screen widget. It SHALL request task representations whose `scheduledAt` has a device-local date up to and including the current local date. It SHALL display incomplete overdue tasks before tasks planned for today, ordering each set by `scheduledAt` ascending and applying the widget's existing row capacity to that combined order. For the current local date it SHALL retain the existing inclusion of both incomplete and completed tasks and expanded recurring occurrences. It SHALL NOT display completed historical tasks, unscheduled tasks, deadline-only tasks, or tasks planned after today.

#### Scenario: Overdue task precedes today's task
- **WHEN** an incomplete task is scheduled before the current local date and another task is scheduled today
- **THEN** the Today widget SHALL place the overdue task before the task scheduled today

#### Scenario: Incomplete planned task is displayed
- **WHEN** an incomplete task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it as an incomplete task row

#### Scenario: Capacity is consumed in overdue-first order
- **WHEN** qualifying overdue and today tasks exceed the Today widget's existing row capacity
- **THEN** the widget SHALL select its displayed rows from the combined overdue-first chronological order

#### Scenario: Completed historical task is excluded
- **WHEN** a completed task has a `scheduledAt` earlier than the current local date
- **THEN** the Today widget SHALL NOT display it

#### Scenario: Completed planned task is displayed
- **WHEN** a completed task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it with its existing completed-task presentation and action

#### Scenario: Earlier time today is not overdue
- **WHEN** a task's `scheduledAt` is earlier than the current time but on the current local date
- **THEN** the Today widget SHALL include it in the today set rather than classify it as overdue

#### Scenario: Unscheduled or deadline-only task is excluded
- **WHEN** a task has no `scheduledAt`, whether or not it has `dueAt`
- **THEN** the Today widget SHALL NOT display it

#### Scenario: Recurring overdue occurrence appears
- **WHEN** the task API returns an incomplete recurring occurrence scheduled before the current local date
- **THEN** the Today widget SHALL display that occurrence as an individual actionable overdue row, subject to the existing row capacity

#### Scenario: Recurring occurrence planned today is displayed
- **WHEN** the task API returns a recurring occurrence with a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display that occurrence as an individual row

#### Scenario: Task outside today is excluded
- **WHEN** a task's `scheduledAt` is after the device's current local date
- **THEN** the Today widget SHALL NOT display it
