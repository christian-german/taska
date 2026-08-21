## Purpose

Provide clear, accessible confirmation when a user successfully creates a task from a graphical client.

## Requirements

### Requirement: Graphical clients confirm successful task creation

The Android, web, and Tauri desktop clients SHALL show a transient, non-modal success toast after a user-initiated task-creation operation receives a successful response. The toast SHALL communicate that the task was created, SHALL be exposed to accessibility services as a status update, SHALL dismiss without requiring user action, and SHALL NOT prevent continued interaction while visible.

#### Scenario: Android task creation succeeds

- **WHEN** a user submits a valid task from an Android task-creation entry point
- **AND** the create request succeeds
- **THEN** the Android client SHALL show one success toast communicating that the task was created

#### Scenario: Web task creation succeeds

- **WHEN** a user submits a valid task from a web task-creation entry point
- **AND** the create request succeeds
- **THEN** the web client SHALL show one success toast communicating that the task was created

#### Scenario: Desktop task creation succeeds

- **WHEN** a user submits a valid task from a Tauri desktop task-creation entry point
- **AND** the create request succeeds
- **THEN** the desktop client SHALL show one success toast communicating that the task was created

#### Scenario: Alternate creation entry point succeeds

- **WHEN** a user initiates task creation from a supported graphical-client entry point other than its primary add-task control
- **AND** the create request succeeds
- **THEN** that client SHALL show the same task-created success feedback

### Requirement: Success feedback reflects the create result

The graphical clients SHALL show task-created success feedback only in response to a successful user-initiated create operation. They SHALL NOT show that success feedback before the request completes, after the request fails, for a task received through synchronization, or for a task mutation other than creation. Existing failure feedback and successful-create side effects SHALL remain unchanged.

#### Scenario: Task creation remains pending

- **WHEN** a user submits a task and the create request has not completed
- **THEN** the client SHALL NOT show the task-created success toast

#### Scenario: Task creation fails

- **WHEN** a user submits a task and the create request fails
- **THEN** the client SHALL NOT show the task-created success toast
- **AND** the client SHALL retain its existing failure behavior

#### Scenario: A task arrives without local user creation

- **WHEN** a client receives or refreshes a task created outside its user-facing creation flow
- **THEN** that client SHALL NOT show the task-created success toast for that task

#### Scenario: Successful creation retains existing side effects

- **WHEN** a user-initiated task creation succeeds
- **THEN** the client SHALL retain its existing refresh, navigation, form-closing, and local-content update behavior in addition to the toast
