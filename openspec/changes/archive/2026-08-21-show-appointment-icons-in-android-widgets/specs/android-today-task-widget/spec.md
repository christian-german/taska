## ADDED Requirements

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
