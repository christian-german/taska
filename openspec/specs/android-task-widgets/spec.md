## Purpose

Define the Android home-screen widget behavior for the current calendar week.

## Requirements

### Requirement: Calendar-week widget displays scheduled incomplete tasks
The Android application SHALL provide a resizable home-screen widget for the current calendar week, defined as Monday through Sunday in the device's local time zone. The widget SHALL display only incomplete task representations whose `scheduledAt` is non-null and falls within that week. It SHALL include expanded recurring occurrences returned for the range and SHALL NOT display unscheduled tasks, deadline-only tasks, completed tasks, or tasks outside the current week.

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

### Requirement: Widget supports one-tap task completion
The widget SHALL provide an immediately accessible completion action for every displayed task. Invoking the action SHALL use the task ID and, for a recurring occurrence, its `occurrenceScheduledAt` to complete precisely that task or occurrence through the authenticated task API. The widget SHALL refresh after successful completion and SHALL NOT present a failed completion as complete.

#### Scenario: Complete a non-recurring task
- **WHEN** the user invokes the completion action for a displayed non-recurring task
- **THEN** the system SHALL complete that task and remove it from the refreshed widget

#### Scenario: Complete one recurring occurrence
- **WHEN** the user invokes the completion action for a displayed recurring occurrence
- **THEN** the system SHALL complete only the occurrence identified by its `occurrenceScheduledAt`

#### Scenario: Completion request fails
- **WHEN** the completion action cannot be authenticated or the task API rejects the request
- **THEN** the widget SHALL retain the task as incomplete and make a subsequent refresh possible

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
The Android task widget SHALL use the Taska web application's semantic visual language: navy primary ink, mint completion accent, neutral surface and divider colors, muted secondary text, and a sans-serif type hierarchy aligned to the web application. The widget SHALL provide corresponding light and dark color resources and SHALL NOT use hard-coded, generic black, gray, or white styling in its rendered task surface.

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
