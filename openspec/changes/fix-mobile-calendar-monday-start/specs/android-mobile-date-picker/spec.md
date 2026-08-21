## ADDED Requirements

### Requirement: Task date-picker week ordering
The Android mobile application SHALL display every task date-picker calendar with Monday as the first day of each week and Sunday as the last day, regardless of the device locale.

#### Scenario: Calendar opened during task creation
- **WHEN** a user opens the calendar while selecting a task date in the Android task-creation flow
- **THEN** the calendar displays Monday in the first weekday column
- **AND** the calendar displays Sunday in the last weekday column

#### Scenario: Scheduled-date calendar opened from task details
- **WHEN** a user opens the scheduled-date calendar from Android task details
- **THEN** the calendar displays Monday in the first weekday column
- **AND** the calendar displays Sunday in the last weekday column

#### Scenario: Due-date calendar opened from task details
- **WHEN** a user opens the due-date calendar from Android task details
- **THEN** the calendar displays Monday in the first weekday column
- **AND** the calendar displays Sunday in the last weekday column

#### Scenario: Device locale normally starts weeks on Sunday
- **WHEN** any affected task calendar is opened on a device whose locale normally uses Sunday as the first weekday
- **THEN** the task calendar still displays Monday first and Sunday last
