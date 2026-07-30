## ADDED Requirements

### Requirement: Tasks have a persisted type
The system SHALL classify every task as either a `TODO` or a `MEETING`. The classification SHALL be stored with the task and included in task representations returned to supported clients and integrations.

#### Scenario: A meeting task is retrieved
- **WHEN** a client retrieves a task classified as `MEETING`
- **THEN** the returned task representation SHALL identify its type as `MEETING`

#### Scenario: A to-do task is retrieved
- **WHEN** a client retrieves a task classified as `TODO`
- **THEN** the returned task representation SHALL identify its type as `TODO`

### Requirement: Task creation defaults to a to-do
The system SHALL allow a client to specify `TODO` or `MEETING` when creating a task. When task creation does not specify a type, the system SHALL create the task as `TODO`.

#### Scenario: A meeting is created
- **WHEN** a client creates a task with type `MEETING`
- **THEN** the system SHALL persist and return the task with type `MEETING`

#### Scenario: A type is omitted during creation
- **WHEN** a client creates a task without specifying a type
- **THEN** the system SHALL persist and return the task with type `TODO`

### Requirement: Task type can be changed
The system SHALL allow a client to change an existing task's type between `TODO` and `MEETING` without changing the task's other fields unless the client requests those changes.

#### Scenario: A to-do becomes a meeting
- **WHEN** a client updates a `TODO` task with type `MEETING`
- **THEN** the system SHALL persist and return the task with type `MEETING`

#### Scenario: A meeting becomes a to-do
- **WHEN** a client updates a `MEETING` task with type `TODO`
- **THEN** the system SHALL persist and return the task with type `TODO`

### Requirement: Task type is selectable in task forms
The task creation and editing interfaces SHALL provide a control for selecting either To-do or Meeting. The creation interface SHALL preselect To-do when no type has been chosen, and editing SHALL preselect the task's current type.

#### Scenario: User creates a meeting from the task form
- **WHEN** a user selects Meeting in the task creation interface and saves a valid task
- **THEN** the system SHALL create the task with type `MEETING`

#### Scenario: User edits a meeting
- **WHEN** a user opens a task classified as `MEETING` for editing
- **THEN** the editing interface SHALL show Meeting as the selected type

### Requirement: Meeting tasks are distinguishable in task presentation
The system SHALL present each `MEETING` task with a visible, non-color-only meeting indicator and an accessible meeting label in task-list and task-detail views. Mobile daily and weekly views SHALL use the same meeting icon as the mobile Today view. The web tracker view SHALL use the same meeting identifier as the other web task presentations. The system SHALL retain the standard to-do presentation for `TODO` tasks.

#### Scenario: Meeting appears in a task list
- **WHEN** a task list contains a task classified as `MEETING`
- **THEN** the task row SHALL show a meeting indicator that distinguishes it from a to-do task

#### Scenario: Meeting appears in task details
- **WHEN** a user views the details of a task classified as `MEETING`
- **THEN** the detail view SHALL identify the task as a meeting

#### Scenario: Meeting appears in a mobile daily or weekly view
- **WHEN** a mobile daily or weekly view contains a task classified as `MEETING`
- **THEN** the view SHALL show the same meeting icon used by the mobile Today view

#### Scenario: Meeting appears in the web tracker
- **WHEN** the web tracker view contains a task classified as `MEETING`
- **THEN** the tracker SHALL show the same meeting identifier used by other web task presentations
