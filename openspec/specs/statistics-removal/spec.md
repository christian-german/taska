# statistics-removal Specification

## Purpose

Define the permanent absence of aggregate statistics APIs and first-party statistics user interfaces while preserving core task and project workflows.

## Requirements

### Requirement: Aggregate statistics are absent from Taska

The system SHALL NOT expose aggregate statistics endpoints, statistics computation services or data-transfer types, dedicated statistics routes or screens, or statistics navigation and shortcut controls in maintained first-party clients.

#### Scenario: User navigates maintained clients
- **WHEN** a user opens or navigates the maintained web and Android applications
- **THEN** no dedicated statistics screen, statistics navigation action, statistics command, statistics shortcut, or Android project statistics summary SHALL be available

#### Scenario: Caller requests the former statistics endpoint
- **WHEN** a caller requests the former `/stats/overview` endpoint
- **THEN** the backend SHALL NOT route the request to statistics application behavior

### Requirement: Maintained contracts omit statistics

Maintained API contracts and current feature documentation SHALL NOT advertise aggregate statistics paths, schemas, DTOs, services, or supported behavior.

#### Scenario: Client inspects the maintained API contract
- **WHEN** a client inspects the current OpenAPI definition
- **THEN** no statistics tag, path, operation, or schema SHALL be present

### Requirement: Core task and project behavior remains available

Removing aggregate statistics SHALL NOT remove task completion, effort estimates, scheduling, overdue task presentation, project task listing, or project navigation behavior outside the retired statistics surfaces.

#### Scenario: User manages tasks and projects after statistics removal
- **WHEN** a user views or updates tasks and projects through a remaining supported workflow
- **THEN** the existing completion, estimate, scheduling, overdue, and project-list behavior SHALL remain available
