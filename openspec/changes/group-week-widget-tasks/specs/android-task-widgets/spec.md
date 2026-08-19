## MODIFIED Requirements

### Requirement: Calendar-week widget displays scheduled incomplete tasks
The Android application SHALL provide a resizable home-screen widget for the current calendar week, defined as Monday through Sunday in the device's local time zone. The widget SHALL display only incomplete task representations whose `scheduledAt` is non-null and falls within that week. It SHALL include expanded recurring occurrences returned for the range and SHALL NOT display unscheduled tasks, deadline-only tasks, completed tasks, or tasks outside the current week. The widget SHALL display every qualifying task rather than truncate the week to a fixed task count. When all date headers and tasks do not fit within the available widget height, the task area SHALL scroll vertically so the user can reach every qualifying task.

#### Scenario: Scheduled task appears in the current week
- **WHEN** an incomplete task has a `scheduledAt` within the current Monday-through-Sunday range
- **THEN** the widget SHALL display the task

#### Scenario: Unscheduled task is excluded
- **WHEN** a task has no `scheduledAt` value
- **THEN** the widget SHALL NOT display the task

#### Scenario: Deadline-only task is excluded
- **WHEN** a task has `dueAt` but no `scheduledAt`
- **THEN** the widget SHALL NOT display the task

#### Scenario: Task outside the current week is excluded
- **WHEN** an incomplete task's `scheduledAt` is before Monday or after Sunday of the current calendar week
- **THEN** the widget SHALL NOT display the task

#### Scenario: Recurring occurrence appears in the week
- **WHEN** the task API returns an incomplete recurring occurrence with a `scheduledAt` in the current week
- **THEN** the widget SHALL display that occurrence as an individual actionable row

#### Scenario: Weekly content exceeds the widget height
- **WHEN** the qualifying tasks and their date headers require more vertical space than the resized widget provides
- **THEN** the widget SHALL retain every qualifying task in chronological order and SHALL allow the user to reach them by vertically scrolling the task area

### Requirement: Calendar-week widget groups tasks by scheduled date
The calendar-week widget SHALL group its chronologically ordered tasks by the device-local date derived from `scheduledAt`. Immediately before the first task for each represented date, the widget SHALL display exactly one visually centered date header containing the locale-aware abbreviated weekday and two-digit day and month, equivalent to `Wed 17/06`. A date with no qualifying task SHALL NOT have a header. Each task row under a date header SHALL display its scheduled time and title without repeating the weekday or date. These grouping requirements SHALL NOT alter the Today widget.

#### Scenario: Multiple tasks share a date
- **WHEN** two or more qualifying tasks have the same device-local scheduled date
- **THEN** the widget SHALL display one date header followed by those task rows in scheduled-time order
- **AND** each task row SHALL show its time and title without a weekday or date prefix

#### Scenario: Tasks span multiple dates
- **WHEN** qualifying tasks occur on more than one device-local date
- **THEN** the widget SHALL display one centered header immediately before each date's first task
- **AND** the date groups SHALL appear in chronological order

#### Scenario: A day has no qualifying task
- **WHEN** a date in the current week has no qualifying task
- **THEN** the widget SHALL NOT display a header for that date

#### Scenario: Today widget is displayed
- **WHEN** the launcher renders the Today widget
- **THEN** its existing presentation and task behavior SHALL remain unchanged
