## ADDED Requirements

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
