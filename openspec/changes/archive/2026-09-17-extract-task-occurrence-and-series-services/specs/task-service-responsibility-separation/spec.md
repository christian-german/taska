## ADDED Requirements

### Requirement: Stored task operations have a dedicated owner

`TaskService` SHALL own stored task and recurring-series definition queries and mutations that do not target one occurrence or restructure the following portion of a series. It SHALL NOT depend on occurrence-state persistence or recurrence expansion services.

#### Scenario: Mutate a non-recurring task
- **WHEN** the mutation boundary updates, deletes, closes, or reopens a non-recurring task
- **THEN** it SHALL delegate the operation to `TaskService`

### Requirement: Recurring occurrence behavior has a dedicated owner

`TaskOccurrenceService` SHALL own recurrence expansion, occurrence identity validation, occurrence overrides, skipping, completion, reopening, and sparse occurrence-state persistence. It SHALL be the application-service owner of `TaskOccurrenceStateRepository` and `TaskRecurrenceService`.

#### Scenario: Mutate one recurring occurrence
- **WHEN** the mutation boundary updates, replaces, skips, closes, or reopens one recurring occurrence
- **THEN** it SHALL delegate the operation to `TaskOccurrenceService` with the series identifier and occurrence identity

#### Scenario: Query calendar occurrences
- **WHEN** a caller requests tasks for a date or date range
- **THEN** `TaskOccurrenceService` SHALL expand recurring series and combine them with applicable non-recurring tasks using the existing result semantics

### Requirement: Recurring-series restructuring has a dedicated owner

`RecurringTaskSeriesService` SHALL own operations that truncate a recurring series or split it by creating a successor series. These operations SHALL preserve the existing occurrence validation, replacement, priority invalidation, and persistence behavior.

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
