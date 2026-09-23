# web-today-overdue-tasks Specification

## Purpose

Display the server-authoritative overdue backlog in the web Today view.

## Requirements

### Requirement: Web Today displays the authoritative overdue backlog
The web Today view SHALL request the overdue task query together with its current today-and-tomorrow task range. It SHALL display every response from the overdue query in its `En retard` group before the today and tomorrow groups, and SHALL use the returned representations' existing identities for task actions.

#### Scenario: A recurring overdue occurrence is displayed
- **WHEN** the overdue query returns an open recurring occurrence
- **THEN** the Today view SHALL display that occurrence in `En retard` and SHALL retain its occurrence-scoped actions

#### Scenario: The overdue backlog is empty
- **WHEN** the overdue query returns no task
- **THEN** the Today view SHALL omit the `En retard` group
