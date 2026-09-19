## Purpose

Define the permanent absence of project sections and elapsed-time tracking while preserving task planning features.

## Requirements

### Requirement: Project sections are absent from Taska

The system SHALL NOT expose project-section resources, section-based task grouping or filtering, section identifiers in task representations, or section controls in first-party clients. The maintained database schema SHALL NOT contain a `sections` table or a task-to-section relationship.

#### Scenario: Client uses the remaining task and project interfaces
- **WHEN** a client lists, creates, retrieves, or updates projects or tasks
- **THEN** no section resource, field, filter, grouping, or control SHALL be offered or returned

#### Scenario: Caller requests a removed section endpoint
- **WHEN** a caller requests a former `/sections` or `/projects/{projectId}/sections` endpoint
- **THEN** the system SHALL NOT route the request to section application behavior

### Requirement: Elapsed-time tracking is absent from Taska

The system SHALL NOT expose time-entry resources, elapsed-time tracking endpoints, timer actions, time-tracker routes or navigation, or time-tracking models and services in first-party clients. The maintained database schema SHALL NOT contain a `time_entries` table.

#### Scenario: User navigates the maintained clients
- **WHEN** a user views web or Android task-management interfaces
- **THEN** no time tracker, time-entry editor, or start/stop timer control SHALL be available

#### Scenario: Caller requests a removed time-entry endpoint
- **WHEN** a caller requests a former `/time-entries` endpoint
- **THEN** the system SHALL NOT route the request to time-entry application behavior

### Requirement: Upgrade removes retired feature data

The database upgrade SHALL remove all persisted section assignments, section records, and time-entry records. Historical immutable migration files MAY remain as deployment history, but the post-upgrade schema and live application SHALL contain no section or time-entry capability.

#### Scenario: Existing installation is upgraded
- **WHEN** Flyway applies the removal migration to a database containing sections, task section assignments, and time entries
- **THEN** it SHALL remove the task-to-section relationship and both retired feature tables

### Requirement: Time-related task management remains available

Removing elapsed-time tracking SHALL NOT remove task scheduling, deadlines, recurrence, notifications, calendar behavior, or effort estimates.

#### Scenario: User manages task planning data after removal
- **WHEN** a user creates or edits a task after the removal
- **THEN** the user SHALL retain the existing scheduled time, due time, recurrence, notification, calendar, and estimate behavior
