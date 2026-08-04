## Purpose

Provide a Today Android home-screen widget for tasks planned on the device's current local date.

## Requirements

### Requirement: Today widget displays all tasks planned for the local current day
The Android application SHALL provide a separate, resizable Today home-screen widget. It SHALL request and display task representations whose `scheduledAt` falls on the device's current local calendar date, including both incomplete and completed tasks and expanded recurring occurrences. It SHALL NOT display unscheduled tasks, deadline-only tasks, or tasks planned outside that local date.

#### Scenario: Incomplete planned task is displayed
- **WHEN** an incomplete task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it as an incomplete task row

#### Scenario: Completed planned task is displayed
- **WHEN** a completed task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it as a completed task row

#### Scenario: Task outside today is excluded
- **WHEN** a task's `scheduledAt` is before or after the device's current local date
- **THEN** the Today widget SHALL NOT display it

#### Scenario: Recurring occurrence planned today is displayed
- **WHEN** the task API returns a recurring occurrence with a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display that occurrence as an individual row

### Requirement: Today widget distinguishes and toggles completed tasks
The Today widget SHALL use the existing Taska widget visual design. It SHALL render a completed task with struck-through primary text and a checked circular completion control. Tapping that completed task's control SHALL reopen precisely that task or recurring occurrence through the authenticated task API, then refresh the widget. It SHALL render an incomplete task with the existing unchecked circular completion control and completion action. All displayed task rows SHALL retain navigation to their matching task detail.

#### Scenario: Completed task row is rendered
- **WHEN** the Today widget renders a completed task
- **THEN** its title SHALL be struck through and its circular completion control SHALL appear checked

#### Scenario: Incomplete task can be completed
- **WHEN** the user invokes the completion control for an incomplete Today task
- **THEN** the system SHALL complete the identified task or recurring occurrence and refresh the Today widget

#### Scenario: Completed task can be reopened
- **WHEN** the user invokes the checked completion control for a completed Today task
- **THEN** the system SHALL reopen the identified task or recurring occurrence and refresh the Today widget

#### Scenario: Reopening a completed task fails
- **WHEN** the completed task's reopen action cannot be authenticated or the task API rejects it
- **THEN** the widget SHALL retain the task as completed and make a subsequent refresh possible

#### Scenario: Completed task title is tapped
- **WHEN** the user taps a completed task's row or title outside its completion control
- **THEN** the application SHALL open that task's detail without changing its completion state

### Requirement: Today widget refreshes with shared task changes and day transitions
The Today widget SHALL refresh after relevant successful local task mutations, a successful in-widget completion, an account-targeted task-change push event, periodic fallback work, and the next device-local calendar-day transition.

#### Scenario: The local date changes
- **WHEN** the device enters a new local calendar date
- **THEN** the Today widget SHALL refresh to display tasks planned for the new date

#### Scenario: A task change is received
- **WHEN** the Android device receives an account-targeted task-change push event
- **THEN** the Today widget SHALL refresh its current local-date task data without user interaction

#### Scenario: Today widget is not installed
- **WHEN** a shared refresh trigger occurs with no Today widget instances installed
- **THEN** the application SHALL complete the refresh processing without attempting to render a Today widget
