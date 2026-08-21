## Purpose

Define the Android home-screen widget behavior for the current calendar week.

## Requirements

### Requirement: Calendar-week widget displays scheduled incomplete tasks
The Android application SHALL provide a resizable home-screen widget for the current calendar week, defined as Monday through Sunday in the device's local time zone. The widget SHALL display every incomplete task representation whose `scheduledAt` is non-null and either falls within that week or has a device-local scheduled date earlier than the current local date. It SHALL include expanded recurring occurrences returned for those ranges and SHALL NOT display unscheduled tasks, deadline-only tasks, completed tasks, or tasks scheduled after the current week. Overdue tasks SHALL appear before every non-overdue current-week task. When all group headers and tasks do not fit within the available widget height, the task area SHALL scroll vertically so the user can reach every qualifying task.

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
- **WHEN** an incomplete task's `scheduledAt` is after Sunday of the current calendar week
- **THEN** the widget SHALL NOT display the task

#### Scenario: Recurring occurrence appears in the week
- **WHEN** the task API returns an incomplete recurring occurrence with a `scheduledAt` in the current week
- **THEN** the widget SHALL display that occurrence as an individual actionable row

#### Scenario: American locale uses a 24-hour time format
- **WHEN** a task scheduled for 1:05 PM in the device time zone is rendered in the calendar-week widget while the device uses an American locale
- **THEN** the task row displays `13:05` before the task title
- **AND** the task row does not display an AM or PM marker

#### Scenario: European locale uses the same time format
- **WHEN** the same task is rendered while the device uses a European locale
- **THEN** the task row displays `13:05` before the task title

#### Scenario: Scheduled instant is converted to device time zone
- **WHEN** the widget renders a scheduled task
- **THEN** the displayed `HH:mm` value represents that instant in the device time zone

#### Scenario: Overdue scheduled task appears before current-week tasks
- **WHEN** an incomplete task has a non-null `scheduledAt` whose device-local date is earlier than today
- **THEN** the widget SHALL display the task before every task scheduled today or later in the current week

#### Scenario: Task earlier today is not overdue
- **WHEN** an incomplete task's `scheduledAt` is earlier than the current time but remains on the device's current local date
- **THEN** the widget SHALL treat it as a current-week task rather than an overdue task

#### Scenario: Completed historical task is excluded
- **WHEN** a completed task has a `scheduledAt` earlier than the device's current local date
- **THEN** the widget SHALL NOT display the task

#### Scenario: Recurring overdue occurrence appears
- **WHEN** the task API returns an incomplete recurring occurrence with a `scheduledAt` earlier than the device's current local date
- **THEN** the widget SHALL display that occurrence as an individual actionable overdue row

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

### Requirement: Widget supports one-tap task completion
The widget SHALL provide an immediately accessible completion action for every displayed task. Invoking the action SHALL immediately render that task's circular completion control as checked, before the authenticated completion request finishes, while leaving the task row otherwise unchanged. The action SHALL use the task ID and, for a recurring occurrence, its `occurrenceScheduledAt` to complete precisely that task or occurrence through the authenticated task API. The widget SHALL refresh after successful completion. If initialization, authentication, transport, or API completion fails, the invoked control SHALL return to its unchecked presentation without requiring a successful network refresh, and the widget SHALL NOT present the rejected completion as complete.

#### Scenario: Completion shows immediate feedback
- **WHEN** the user invokes the completion action for a displayed incomplete task
- **THEN** that task's circular completion control SHALL appear checked before the completion request succeeds or fails
- **AND** the row's visibility, text presentation, grouping, ordering, and task identity SHALL remain unchanged while the request is pending

#### Scenario: Complete a non-recurring task
- **WHEN** the completion request for a non-recurring task succeeds
- **THEN** the system SHALL refresh the widget and remove the completed task from its authoritative contents

#### Scenario: Complete one recurring occurrence
- **WHEN** the user invokes the completion action for a displayed recurring occurrence
- **THEN** the system SHALL optimistically check that occurrence's control and complete only the occurrence identified by its `occurrenceScheduledAt`

#### Scenario: Completion request fails
- **WHEN** initialization, authentication, transport, or the task API prevents the completion request from succeeding
- **THEN** the invoked task's completion control SHALL return to unchecked
- **AND** the widget SHALL retain the task as incomplete and make a subsequent completion or refresh possible

### Requirement: Widget refreshes without user interaction
The widget SHALL refresh its data after a successful completion from the widget, after relevant local task mutations, when the current calendar week changes, and on a periodic fallback schedule. It SHALL also refresh after receiving an account-targeted task-change push event.

#### Scenario: New calendar week
- **WHEN** the device enters a new local calendar week
- **THEN** the widget SHALL refresh to show that week's scheduled tasks

#### Scenario: Task changed in another client
- **WHEN** the Android device receives an account-targeted task-change push event
- **THEN** the widget SHALL refresh its current calendar-week data without user interaction

#### Scenario: Push delivery is missed
- **WHEN** a task-change push event is not delivered
- **THEN** the widget SHALL refresh on its next periodic fallback or local refresh trigger

### Requirement: Widget uses the Taska visual design system
The Android task widget SHALL use the Taska web application's semantic visual language: navy primary ink, mint completion accent, neutral surface and divider colors, muted secondary text, and a sans-serif type hierarchy aligned to the web application. The widget SHALL provide corresponding light and dark color resources and SHALL NOT use hard-coded, generic black, gray, or white styling in its rendered task surface. The widget SHALL NOT display a scheduled-task count after a successful refresh, but SHALL retain visible refresh error feedback.

#### Scenario: Widget refresh succeeds
- **WHEN** either Android task widget successfully refreshes with any number of tasks
- **THEN** it SHALL NOT display a scheduled-task count

#### Scenario: Widget refresh fails
- **WHEN** either Android task widget cannot refresh its task data
- **THEN** it SHALL display refresh error feedback instead of silently hiding the failure

#### Scenario: Widget is rendered in light mode
- **WHEN** the launcher renders the widget in light system theme
- **THEN** the widget SHALL use the Taska light surface, navy primary text, muted slate secondary text, mint completion accent, and subtle neutral dividers

#### Scenario: Widget is rendered in dark mode
- **WHEN** the launcher renders the widget in dark system theme
- **THEN** the widget SHALL use the Taska dark canvas and surface colors with readable light primary text, muted secondary text, and a visible mint completion accent

#### Scenario: Task row is displayed
- **WHEN** the widget displays a task row
- **THEN** the row SHALL use Taska-aligned primary text, compact spacing, a restrained separator treatment, and a circular completion affordance rather than a font-dependent checkbox glyph

### Requirement: Widget is rendered as a rounded Taska card
The Android task widget SHALL render its outer surface as an opaque rounded card with a 10dp corner radius. Its background and child content SHALL be clipped or inset so that no task row, divider, or status area visibly reaches a square outer corner, regardless of the widget's supported size.

#### Scenario: Widget is placed on the home screen
- **WHEN** the launcher displays the widget at its default size
- **THEN** all four visible outer corners SHALL be rounded with the Taska card treatment

#### Scenario: Widget is resized
- **WHEN** the user resizes the widget horizontally or vertically within its supported bounds
- **THEN** its outer surface SHALL retain rounded corners and its content SHALL remain inside the rounded card boundary

### Requirement: Calendar-week widget identifies appointments
The Android calendar-week widget SHALL display the same outlined calendar appointment icon used by the rest of the Android application in every task row whose task type is `APPOINTMENT`. The icon SHALL have an accessible Appointment label. The widget SHALL NOT display or expose that appointment indicator for `TODO` tasks. Adding the indicator SHALL NOT change the row's task-detail navigation or completion action.

#### Scenario: Appointment appears in the calendar-week widget
- **WHEN** the calendar-week widget displays a task whose type is `APPOINTMENT`
- **THEN** the task row SHALL show the application's outlined calendar appointment icon
- **AND** the icon SHALL expose an accessible Appointment label

#### Scenario: To-do appears in the calendar-week widget
- **WHEN** the calendar-week widget displays a task whose type is `TODO`
- **THEN** the task row SHALL retain the standard to-do presentation without an appointment icon or Appointment accessibility label

#### Scenario: Appointment row actions remain available
- **WHEN** the calendar-week widget displays the appointment indicator in a task row
- **THEN** the row SHALL retain its existing task-detail navigation and completion action for that task or recurring occurrence

### Requirement: Calendar-week widget visually emphasizes overdue content
The calendar-week widget SHALL render the visible text of its `Overdue` header and every overdue task row in a legible overdue-red color and bold font weight. This treatment SHALL apply to the task row's time and title text and SHALL NOT recolor its completion control or appointment indicator. Ordinary date headers and non-overdue task text SHALL retain their existing styles.

#### Scenario: Overdue group is emphasized
- **WHEN** the calendar-week widget displays its `Overdue` group
- **THEN** the `Overdue` header text and every task's time/title text in that group SHALL be overdue red and bold

#### Scenario: Other Week content retains its style
- **WHEN** the calendar-week widget displays a current-week date header and non-overdue task row
- **THEN** neither the date header nor the task's time/title text SHALL receive the overdue color or bold treatment

#### Scenario: Non-text row controls retain their presentation
- **WHEN** an overdue Week row includes its completion control or an appointment indicator
- **THEN** those controls SHALL retain their existing presentation rather than receive the overdue text treatment

#### Scenario: Overdue emphasis works in each widget theme
- **WHEN** the device renders the calendar-week widget in either its light or dark theme
- **THEN** the overdue-red header and task text SHALL remain legible against the widget surface

### Requirement: Calendar-week widget omits time for all-day tasks
The calendar-week widget SHALL use each displayed task's `allDay` value to determine whether its row includes a clock time. A task whose `allDay` value is true SHALL display its title without a clock time or a time-specific separator. A task whose `allDay` value is false SHALL retain its localized scheduled-time text. This distinction SHALL NOT change task selection, date grouping, chronological ordering, completion actions, recurring-occurrence identity, or task-detail navigation.

#### Scenario: All-day task is displayed in the calendar-week widget
- **WHEN** the calendar-week widget renders a qualifying task whose `allDay` value is true
- **THEN** the task row SHALL display the task title without a clock time
- **AND** the row SHALL NOT reserve a blank time prefix or display a time-specific separator

#### Scenario: Timed task is displayed in the calendar-week widget
- **WHEN** the calendar-week widget renders a qualifying task whose `allDay` value is false
- **THEN** the task row SHALL display the task's localized scheduled time and title

#### Scenario: All-day task remains actionable
- **WHEN** the calendar-week widget omits the time from an all-day task row
- **THEN** the task SHALL retain its existing completion and task-detail actions, including recurring-occurrence identity when present
