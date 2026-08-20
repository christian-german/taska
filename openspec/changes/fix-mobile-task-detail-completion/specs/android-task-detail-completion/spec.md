## ADDED Requirements

### Requirement: Task detail toggles task completion

The Android task-detail interface SHALL provide an interactive completion control beside the task title. The control SHALL complete the displayed task when it is active and reopen the displayed task when it is completed. After the action succeeds, task detail SHALL remain open and SHALL represent the task state returned by the server.

#### Scenario: Complete an active task from task detail

- **GIVEN** Android task detail displays an active task
- **WHEN** the user activates the completion control beside its title
- **THEN** the client SHALL request completion of that task
- **AND** after the request succeeds, task detail SHALL remain open and display the returned task as completed

#### Scenario: Reopen a completed task from task detail

- **GIVEN** Android task detail displays a completed task
- **WHEN** the user activates the completion control beside its title
- **THEN** the client SHALL request reopening of that task
- **AND** after the request succeeds, task detail SHALL remain open and display the returned task as active

#### Scenario: Toggle a recurring occurrence

- **GIVEN** Android task detail was opened for a recurring occurrence
- **WHEN** the user activates the completion control
- **THEN** the completion or reopening request SHALL identify that displayed occurrence

#### Scenario: Completion toggle fails

- **GIVEN** Android task detail displays a task
- **WHEN** its completion or reopening request fails
- **THEN** task detail SHALL retain the task's previously confirmed completion state

#### Scenario: Completion toggle is already pending

- **GIVEN** a completion or reopening request from Android task detail has not finished
- **WHEN** the user attempts to activate the completion control again
- **THEN** the client SHALL NOT submit another completion or reopening request for that task

### Requirement: Task detail communicates completion state and action

The Android task-detail completion control SHALL visually distinguish active and completed tasks and SHALL expose an accessible action appropriate to the displayed state. The title of a completed task SHALL use the established Android completed-task presentation. Completion presentation SHALL remain derived from the last server-confirmed task state.

#### Scenario: Active task completion presentation

- **WHEN** Android task detail displays an active task
- **THEN** the title completion control SHALL appear unchecked
- **AND** assistive technology SHALL identify that activating it completes the task

#### Scenario: Completed task presentation

- **WHEN** Android task detail displays a completed task
- **THEN** the title completion control SHALL appear checked
- **AND** the title SHALL use the established completed-task treatment
- **AND** assistive technology SHALL identify that activating the control reopens the task
