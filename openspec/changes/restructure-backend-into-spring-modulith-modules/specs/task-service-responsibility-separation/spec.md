## MODIFIED Requirements

### Requirement: Stored task operations have a dedicated owner

`TaskDefinitionService` in `com.taska.task.application.definition` SHALL own stored task and recurring-series definition queries and mutations that do not target one occurrence or stop a series. The persisted `Task` entity SHALL reside in `com.taska.task.model` and `TaskRepository` in `com.taska.task.persistence`. `TaskDefinitionService` SHALL NOT depend on occurrence-state persistence or recurrence expansion services.

#### Scenario: Mutate a non-recurring task
- **WHEN** the mutation boundary updates, deletes, closes, or reopens a non-recurring task
- **THEN** it SHALL delegate the operation to `TaskDefinitionService`

### Requirement: Recurring occurrence behavior has a dedicated owner

`TaskOccurrenceService` SHALL reside in `com.taska.task.application.occurrence` and own occurrence identity validation, occurrence overrides, skipping, completion, reopening, calendar expansion, and sparse occurrence-state persistence. It SHALL be the application-service owner of `TaskOccurrenceStateRepository`. RRULE expansion SHALL reside in `com.taska.task.application.recurrence`. The JPA `TaskOccurrenceState` entity SHALL reside in `com.taska.task.model` and its repository in `com.taska.task.persistence`.

#### Scenario: Mutate one recurring occurrence
- **WHEN** the mutation boundary updates, replaces, skips, closes, or reopens one recurring occurrence
- **THEN** it SHALL delegate the operation to `TaskOccurrenceService` with the series identifier and occurrence identity

#### Scenario: Query calendar occurrences
- **WHEN** a caller requests tasks for a date or date range
- **THEN** `TaskOccurrenceService` SHALL expand recurring series and combine them with applicable non-recurring tasks using the existing result semantics

### Requirement: Task package structure exposes responsibility ownership

The task module SHALL keep its HTTP adapter in `com.taska.task.adapter.http`, its MCP adapter in `com.taska.task.adapter.mcp`, its event listeners in `com.taska.task.adapter.events`, its cross-sub-domain mutation boundary in `com.taska.task.application`, and its shared application contracts in `com.taska.task.model`. Stored task definitions SHALL reside under `com.taska.task.application.definition`, recurring occurrence behavior under `com.taska.task.application.occurrence`, and RRULE expansion under `com.taska.task.application.recurrence`. These are sub-domains of the application layer, not siblings of it.

#### Scenario: Architecture verification inspects the task feature
- **WHEN** automated architecture checks inspect task production classes
- **THEN** definition, occurrence, recurrence, adapter, model, persistence and mutation-boundary types SHALL reside in their specified owning packages

### Requirement: Task subfeature dependencies follow ownership direction

Task adapters MAY depend on the module's mutation boundary and on its sub-domain services. `TaskMutationService` MAY depend on the definition, occurrence and series services. The occurrence service MAY read stored task definitions. `com.taska.task.model` SHALL NOT depend on any other package of the task module, `com.taska.task.application.recurrence` SHALL NOT depend on definition or occurrence, and the definition service SHALL NOT depend on occurrence persistence or recurrence expansion.

#### Scenario: Architecture verification inspects task dependencies
- **WHEN** automated architecture checks inspect dependencies among task sub-domains
- **THEN** no dependency SHALL point against the defined ownership direction
