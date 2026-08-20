## ADDED Requirements

### Requirement: Android calendars refresh after successful task creation
When task creation initiated from an open Android day or week calendar succeeds, the application SHALL reload the task data for the calendar range that is currently displayed. The calendar SHALL remain on the same selected day or week. The refresh SHALL occur without requiring the user to navigate to another view and return. Dismissing the creation interface or receiving a failed creation result SHALL NOT trigger this success refresh.

#### Scenario: Task is created from the day calendar
- **WHEN** a user successfully creates a task from the Android day calendar while a day is displayed
- **THEN** the application SHALL close the task-creation interface
- **AND** reload the displayed day's task data
- **AND** keep the same day selected

#### Scenario: Task is created from the week calendar
- **WHEN** a user successfully creates a task from the Android week calendar while a week is displayed
- **THEN** the application SHALL close the task-creation interface
- **AND** reload the displayed week's task data
- **AND** keep the same week selected

#### Scenario: Task creation does not succeed
- **WHEN** a user dismisses task creation or the creation request fails from an Android calendar
- **THEN** the application SHALL NOT perform the successful-creation calendar refresh
