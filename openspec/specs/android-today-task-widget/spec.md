## Purpose

Provide a Today Android home-screen widget for tasks planned on the device's current local date.

## Requirements

### Requirement: Today widget displays all tasks planned for the local current day
The Android application SHALL provide a separate, resizable Today home-screen widget. It SHALL request task representations whose `scheduledAt` has a device-local date up to and including the current local date. It SHALL display incomplete overdue tasks before tasks planned for today, ordering each set by `scheduledAt` ascending and applying the widget's existing row capacity to that combined order. For the current local date it SHALL retain the existing inclusion of both incomplete and completed tasks and expanded recurring occurrences. It SHALL NOT display completed historical tasks, unscheduled tasks, deadline-only tasks, or tasks planned after today.

#### Scenario: Incomplete planned task is displayed
- **WHEN** an incomplete task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it as an incomplete task row

#### Scenario: Completed planned task is displayed
- **WHEN** a completed task has a `scheduledAt` on the device's current local date
- **THEN** the Today widget SHALL display it as a completed task row

#### Scenario: Task outside today is excluded
- **WHEN** a task's `scheduledAt` is after the device's current local date
- **THEN** the Today widget SHALL NOT display it

#### Scenario: Overdue task precedes today's task
- **WHEN** an incomplete task is scheduled before the current local date and another task is scheduled today
- **THEN** the Today widget SHALL place the overdue task before the task scheduled today

#### Scenario: Capacity is consumed in overdue-first order
- **WHEN** qualifying overdue and today tasks exceed the Today widget's existing row capacity
- **THEN** the widget SHALL select its displayed rows from the combined overdue-first chronological order

#### Scenario: Completed historical task is excluded
- **WHEN** a completed task has a `scheduledAt` earlier than the current local date
- **THEN** the Today widget SHALL NOT display it

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

### Requirement: Today widget distinguishes and toggles completed tasks
The Today widget SHALL use the existing Taska widget visual design. It SHALL render a completed task with struck-through primary text and a checked circular completion control. Tapping that completed task's control SHALL reopen precisely that task or recurring occurrence through the authenticated task API, then refresh the widget. It SHALL render an incomplete task with the existing unchecked circular completion control and completion action. Invoking an incomplete task's completion action SHALL immediately render only its circular completion control as checked before the request finishes, while leaving the row's other presentation unchanged. The widget SHALL refresh after successful completion. If initialization, authentication, transport, or API completion fails, the invoked control SHALL return to unchecked without requiring a successful network refresh. All displayed task rows SHALL retain navigation to their matching task detail, and completed-task reopening behavior SHALL remain response-driven.

#### Scenario: Completed task row is rendered
- **WHEN** the Today widget renders a completed task
- **THEN** its title SHALL be struck through and its circular completion control SHALL appear checked

#### Scenario: Incomplete task completion shows immediate feedback
- **WHEN** the user invokes the completion control for an incomplete Today task
- **THEN** that task's circular completion control SHALL appear checked before the completion request succeeds or fails
- **AND** its row visibility and title presentation SHALL remain unchanged while the request is pending

#### Scenario: Incomplete task can be completed
- **WHEN** the completion request for an incomplete Today task or recurring occurrence succeeds
- **THEN** the system SHALL refresh the Today widget from authoritative task data

#### Scenario: Incomplete task completion fails
- **WHEN** initialization, authentication, transport, or the task API prevents completion from succeeding
- **THEN** the invoked task's circular completion control SHALL return to unchecked
- **AND** the task SHALL remain available for a subsequent completion attempt

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

### Requirement: Today widget groups overdue tasks visibly
The Today widget SHALL show the established `Overdue` label before its overdue task rows when at least one overdue row is displayed. When both overdue and current-day rows are displayed, it SHALL render exactly one divider between those groups. It SHALL NOT render dividers between individual tasks within either group or show an overdue label or group divider when no overdue row is displayed.

#### Scenario: Overdue and current-day tasks are displayed
- **WHEN** the Today widget displays one or more overdue tasks followed by one or more tasks planned for the current local date
- **THEN** it SHALL show `Overdue` before the overdue rows and exactly one divider between the overdue and current-day groups
- **AND** it SHALL NOT show dividers between tasks within either group

#### Scenario: Only overdue tasks are displayed
- **WHEN** every displayed Today-widget task is overdue
- **THEN** it SHALL show `Overdue` before those rows without a trailing group divider

#### Scenario: No overdue task is displayed
- **WHEN** the Today widget displays only tasks planned for the current local date or displays no tasks
- **THEN** it SHALL hide the overdue label and SHALL NOT show task-row dividers

### Requirement: Today widget identifies appointments
The Android Today widget SHALL display the same outlined calendar appointment icon used by the rest of the Android application in every task row whose task type is `APPOINTMENT`, whether that task is complete or incomplete. The icon SHALL have an accessible Appointment label. The widget SHALL NOT display or expose that appointment indicator for `TODO` tasks. Adding the indicator SHALL NOT change the row's task-detail navigation or completion control behavior.

#### Scenario: Incomplete appointment appears in the Today widget
- **WHEN** the Today widget displays an incomplete task whose type is `APPOINTMENT`
- **THEN** the task row SHALL show the application's outlined calendar appointment icon
- **AND** the icon SHALL expose an accessible Appointment label

#### Scenario: Completed appointment appears in the Today widget
- **WHEN** the Today widget displays a completed task whose type is `APPOINTMENT`
- **THEN** the task row SHALL show the same appointment icon and accessible Appointment label while retaining its completed presentation

#### Scenario: To-do appears in the Today widget
- **WHEN** the Today widget displays a task whose type is `TODO`
- **THEN** the task row SHALL retain the standard to-do presentation without an appointment icon or Appointment accessibility label

#### Scenario: Appointment row actions remain available
- **WHEN** the Today widget displays the appointment indicator in a task row
- **THEN** the row SHALL retain its existing task-detail navigation and completion or reopen control for that task or recurring occurrence
