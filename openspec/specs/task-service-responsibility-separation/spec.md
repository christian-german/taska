# Task Service Responsibility Separation Specification

## Purpose

Define explicit application-service ownership for stored tasks, recurring occurrences, and recurring-series restructuring while preserving transport behavior.

## Requirements

### Requirement: Stored task operations have a dedicated owner

`TaskDefinitionService` in `com.taska.domain.task.definition.service` SHALL own stored task and recurring-series definition queries and mutations that do not target one occurrence or restructure the following portion of a series. The persisted `Task` entity and `TaskRepository` SHALL reside in `com.taska.domain.task.definition.repository`. `TaskDefinitionService` SHALL NOT depend on occurrence-state persistence or recurrence expansion services.

#### Scenario: Mutate a non-recurring task
- **WHEN** the mutation boundary updates, deletes, closes, or reopens a non-recurring task
- **THEN** it SHALL delegate the operation to `TaskDefinitionService`

### Requirement: Recurring occurrence behavior has a dedicated owner

`TaskOccurrenceService` SHALL reside in `com.taska.domain.task.occurrence.service` and own recurrence expansion, occurrence identity validation, occurrence overrides, skipping, completion, reopening, and sparse occurrence-state persistence. It SHALL be the application-service owner of `TaskOccurrenceStateRepository` and `TaskRecurrenceService`. The JPA `TaskOccurrenceState` entity and its repository SHALL reside in `com.taska.domain.task.occurrence.repository`.

#### Scenario: Mutate one recurring occurrence
- **WHEN** the mutation boundary updates, replaces, skips, closes, or reopens one recurring occurrence
- **THEN** it SHALL delegate the operation to `TaskOccurrenceService` with the series identifier and occurrence identity

#### Scenario: Query calendar occurrences
- **WHEN** a caller requests tasks for a date or date range
- **THEN** `TaskOccurrenceService` SHALL expand recurring series and combine them with applicable non-recurring tasks using the existing result semantics

### Requirement: Recurring-series restructuring has a dedicated owner

`RecurringTaskSeriesService` SHALL reside in `com.taska.domain.task.series.service` and own operations that truncate a recurring series or split it by creating a successor series. These operations SHALL preserve the existing occurrence validation, replacement, priority invalidation, and persistence behavior.

#### Scenario: Update a series from one occurrence onward
- **WHEN** the mutation boundary receives a `FROM_THIS` update for a recurring series
- **THEN** it SHALL delegate the split operation to `RecurringTaskSeriesService`

#### Scenario: Delete the following portion of a series
- **WHEN** the mutation boundary receives a `FROM_THIS` deletion for a recurring series
- **THEN** it SHALL delegate series truncation to `RecurringTaskSeriesService`

### Requirement: Transport behavior remains unchanged

The service extraction SHALL preserve HTTP and MCP paths, payloads, outputs, validation behavior, task result variants, and successful-mutation change publication.

#### Scenario: Existing transport invokes a recurring mutation
- **WHEN** an existing HTTP or MCP adapter invokes a recurring occurrence or series mutation
- **THEN** the request SHALL produce the same observable result as before the service extraction
- **AND** a successful mutation SHALL publish exactly one account change signal

### Requirement: Task package structure exposes responsibility ownership

The task feature SHALL keep its HTTP adapter in `com.taska.domain.task.controller`, its MCP adapter in `com.taska.domain.task.mcp`, and its cross-subfeature mutation boundary and shared application contracts in `com.taska.domain.task.service`. Stored task definitions SHALL reside under `com.taska.domain.task.definition`, recurring occurrence behavior SHALL reside under `com.taska.domain.task.occurrence`, and recurring-series restructuring SHALL reside under `com.taska.domain.task.series`, with services and persistence types in the corresponding technical subpackages.

#### Scenario: Architecture verification inspects the task feature
- **WHEN** automated architecture checks inspect task production classes
- **THEN** definition, occurrence, series, adapter, and shared mutation types SHALL reside in their specified owning packages

### Requirement: Task subfeature dependencies follow ownership direction

Task adapters MAY depend on the task-level application boundary and read-owning subfeature services. `TaskMutationService` MAY depend on definition, occurrence, and series services. The series service MAY coordinate definition and occurrence services. The occurrence service MAY read stored task definitions but SHALL NOT depend on the series service, and the definition service SHALL NOT depend on occurrence persistence or recurrence expansion.

#### Scenario: Architecture verification inspects task dependencies
- **WHEN** automated architecture checks inspect dependencies among task subfeatures
- **THEN** no dependency SHALL point against the defined ownership direction
