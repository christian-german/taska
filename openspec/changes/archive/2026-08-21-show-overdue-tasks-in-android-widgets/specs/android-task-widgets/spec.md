## MODIFIED Requirements

### Requirement: Calendar-week widget displays scheduled incomplete tasks
The Android application SHALL provide a resizable home-screen widget for the current calendar week, defined as Monday through Sunday in the device's local time zone. The widget SHALL display every incomplete task representation whose `scheduledAt` is non-null and either falls within that week or has a device-local scheduled date earlier than the current local date. It SHALL include expanded recurring occurrences returned for those ranges and SHALL NOT display unscheduled tasks, deadline-only tasks, completed tasks, or tasks scheduled after the current week. Overdue tasks SHALL appear before every non-overdue current-week task. When all group headers and tasks do not fit within the available widget height, the task area SHALL scroll vertically so the user can reach every qualifying task.

#### Scenario: Overdue scheduled task appears before current-week tasks
- **WHEN** an incomplete task has a non-null `scheduledAt` whose device-local date is earlier than today
- **THEN** the widget SHALL display the task before every task scheduled today or later in the current week

#### Scenario: Scheduled task appears in the current week
- **WHEN** an incomplete task has a `scheduledAt` within the current Monday-through-Sunday range
- **THEN** the widget SHALL display the task

#### Scenario: Task earlier today is not overdue
- **WHEN** an incomplete task's `scheduledAt` is earlier than the current time but remains on the device's current local date
- **THEN** the widget SHALL treat it as a current-week task rather than an overdue task

#### Scenario: Completed historical task is excluded
- **WHEN** a completed task has a `scheduledAt` earlier than the device's current local date
- **THEN** the widget SHALL NOT display the task

#### Scenario: Unscheduled task is excluded
- **WHEN** a task has no `scheduledAt` value and no `dueAt` value
- **THEN** the widget SHALL NOT display the task

#### Scenario: Deadline-only task is excluded
- **WHEN** a task has `dueAt` but no `scheduledAt`
- **THEN** the widget SHALL NOT display the task

#### Scenario: Recurring overdue occurrence appears
- **WHEN** the task API returns an incomplete recurring occurrence with a `scheduledAt` earlier than the device's current local date
- **THEN** the widget SHALL display that occurrence as an individual actionable overdue row

#### Scenario: Recurring occurrence appears in the week
- **WHEN** the task API returns an incomplete recurring occurrence with a `scheduledAt` in the current week
- **THEN** the widget SHALL display that occurrence as an individual actionable row

#### Scenario: Task after the current week is excluded
- **WHEN** an incomplete task's `scheduledAt` is after Sunday of the current calendar week
- **THEN** the widget SHALL NOT display the task

#### Scenario: Task outside the current week is excluded
- **WHEN** an incomplete task's `scheduledAt` is after Sunday of the current calendar week
- **THEN** the widget SHALL NOT display the task

#### Scenario: Weekly content exceeds the widget height
- **WHEN** the qualifying tasks and their group headers require more vertical space than the resized widget provides
- **THEN** the widget SHALL retain every qualifying task in the required order and SHALL allow the user to reach them by vertically scrolling the task area

### Requirement: Calendar-week widget groups tasks by overdue status and scheduled date
The calendar-week widget SHALL begin with exactly one visually centered `Overdue` header when at least one qualifying overdue task exists. It SHALL place all overdue tasks under that header in ascending `scheduledAt` order and SHALL NOT insert their original scheduled-date headers. After the overdue group, the widget SHALL group current-week tasks chronologically by the device-local date derived from `scheduledAt`, with exactly one locale-aware date header immediately before each represented date's first task. A date with no qualifying task SHALL NOT have a header. Current-week task rows SHALL display scheduled time and title without repeating the weekday or date. These grouping requirements SHALL NOT alter the Today widget's row styling.

#### Scenario: Multiple overdue tasks span original dates
- **WHEN** qualifying overdue tasks have scheduled dates on multiple past local dates
- **THEN** the widget SHALL display one `Overdue` header followed by all of those tasks in ascending scheduled order
- **AND** it SHALL NOT display a date header for any original overdue date

#### Scenario: No overdue task exists
- **WHEN** no qualifying overdue task exists
- **THEN** the widget SHALL NOT display an `Overdue` header
- **AND** its current-week date groups SHALL retain their existing order and formatting

#### Scenario: Overdue and current-week tasks both exist
- **WHEN** the widget has qualifying overdue tasks and current-week tasks
- **THEN** the complete overdue group SHALL appear before the first current-week date header

#### Scenario: Multiple current-week tasks share a date
- **WHEN** two or more qualifying current-week tasks have the same device-local scheduled date
- **THEN** the widget SHALL display one date header followed by those task rows in scheduled-time order

#### Scenario: A current-week date has no qualifying task
- **WHEN** a date in the current week has no qualifying task
- **THEN** the widget SHALL NOT display a header for that date
