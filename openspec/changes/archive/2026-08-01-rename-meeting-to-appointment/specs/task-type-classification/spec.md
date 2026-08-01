## MODIFIED Requirements

### Requirement: Tasks have a persisted type
The system SHALL classify every task as either a `TODO` or an `APPOINTMENT`. The classification SHALL be stored with the task and included in task representations returned to supported clients and integrations. Existing persisted `MEETING` classifications SHALL be migrated to `APPOINTMENT` without changing the task's other data.

#### Scenario: An appointment task is retrieved
- **WHEN** a client retrieves a task classified as `APPOINTMENT`
- **THEN** the returned task representation SHALL identify its type as `APPOINTMENT`

#### Scenario: A legacy meeting task is migrated
- **WHEN** a persisted task has type `MEETING` before the terminology migration
- **THEN** it SHALL have type `APPOINTMENT` after the migration without changes to its other task data

#### Scenario: A to-do task is retrieved
- **WHEN** a client retrieves a task classified as `TODO`
- **THEN** the returned task representation SHALL identify its type as `TODO`

### Requirement: Task creation defaults to a to-do
The system SHALL allow a client to specify `TODO` or `APPOINTMENT` when creating a task. When task creation does not specify a type, the system SHALL create the task as `TODO`.

#### Scenario: An appointment is created
- **WHEN** a client creates a task with type `APPOINTMENT`
- **THEN** the system SHALL persist and return the task with type `APPOINTMENT`

#### Scenario: A type is omitted during creation
- **WHEN** a client creates a task without specifying a type
- **THEN** the system SHALL persist and return the task with type `TODO`

### Requirement: Task type can be changed
The system SHALL allow a client to change an existing task's type between `TODO` and `APPOINTMENT` without changing the task's other fields unless the client requests those changes.

#### Scenario: A to-do becomes an appointment
- **WHEN** a client updates a `TODO` task with type `APPOINTMENT`
- **THEN** the system SHALL persist and return the task with type `APPOINTMENT`

#### Scenario: An appointment becomes a to-do
- **WHEN** a client updates an `APPOINTMENT` task with type `TODO`
- **THEN** the system SHALL persist and return the task with type `TODO`

### Requirement: Task type is selectable in task forms
The task creation and editing interfaces SHALL provide a control for selecting either To-do or Appointment. The creation interface SHALL preselect To-do when no type has been chosen, and editing SHALL preselect the task's current type.

#### Scenario: User creates an appointment from the task form
- **WHEN** a user selects Appointment in the task creation interface and saves a valid task
- **THEN** the system SHALL create the task with type `APPOINTMENT`

#### Scenario: User edits an appointment
- **WHEN** a user opens a task classified as `APPOINTMENT` for editing
- **THEN** the editing interface SHALL show Appointment as the selected type

### Requirement: Appointment tasks are distinguishable in task presentation
The system SHALL present each `APPOINTMENT` task with a visible, non-color-only appointment indicator and an accessible appointment label in task-list and task-detail views. Mobile daily and weekly views SHALL use the same appointment icon as the mobile Today view. The web tracker view SHALL use the same appointment identifier as the other web task presentations. The system SHALL retain the standard to-do presentation for `TODO` tasks.

#### Scenario: Appointment appears in a task list
- **WHEN** a task list contains a task classified as `APPOINTMENT`
- **THEN** the task row SHALL show an appointment indicator that distinguishes it from a to-do task

#### Scenario: Appointment appears in task details
- **WHEN** a user views the details of a task classified as `APPOINTMENT`
- **THEN** the detail view SHALL identify the task as an appointment

#### Scenario: Appointment appears in a mobile daily or weekly view
- **WHEN** a mobile daily or weekly view contains a task classified as `APPOINTMENT`
- **THEN** the view SHALL show the same appointment icon used by the mobile Today view

#### Scenario: Appointment appears in the web tracker
- **WHEN** the web tracker view contains a task classified as `APPOINTMENT`
- **THEN** the tracker SHALL show the same appointment identifier used by other web task presentations
