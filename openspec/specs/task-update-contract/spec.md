## Purpose

Define complete-replacement contracts for base tasks, following recurring series, and single recurring occurrences.
## Requirements
### Requirement: Base task updates replace the complete mutable task representation

The system SHALL expose `PUT /tasks/{taskId}` as a full replacement operation for a non-occurrence task. The request body SHALL be a typed `TaskUpdateRequest` containing every mutable base-task property: content, type, description, projectId, parentId, order, priority, labels, scheduledAt, dueAt, allDay, isRecurring, estimateMinutes, mentionContext, and recurrenceRule. The request SHALL NOT contain a section identifier or output-only task fields such as ID, completion state, timestamps, or occurrence metadata. The system SHALL reject incomplete or invalid replacement requests without changing the task. When `isRecurring` is true, `dueAt` MUST be null. A successful replacement SHALL return `NonRecurringTaskDto` when the resulting task is non-recurring or `RecurringTaskSeriesDto` when it is recurring. Once a recurring series has persisted occurrence state, this operation SHALL reject a change to its recurring status, normalized recurrence rule, or `scheduledAt`; such generator changes require an operation with an explicit occurrence boundary. A recurring series without persisted occurrence state MAY replace those fields in place.

#### Scenario: Replace a non-recurring task with complete mutable values
- **WHEN** a client sends a valid complete non-recurring `TaskUpdateRequest` to `PUT /tasks/{taskId}`
- **THEN** the system SHALL replace every mutable base-task property with the supplied value
- **AND** it SHALL return the resulting discriminated task representation without a section identifier

#### Scenario: Replace a task as a recurring series without a deadline
- **WHEN** a client sends a complete `TaskUpdateRequest` with `isRecurring: true` and `dueAt: null`
- **THEN** the system SHALL persist the recurring-series definition with no deadline
- **AND** it SHALL return a `RecurringTaskSeriesDto` without a `dueAt` property

#### Scenario: Correct a stateless recurring series in place
- **WHEN** a client changes the schedule or recurrence rule of a recurring series that has no persisted occurrence state
- **THEN** the system SHALL replace the series definition in place

#### Scenario: Reject a retroactive generator change
- **WHEN** a client changes the recurring status, schedule, or normalized recurrence rule of a recurring series that has persisted occurrence state
- **THEN** the system SHALL reject the request without changing the series or occurrence state
- **AND** it SHALL require a following-series or truncation operation with an explicit occurrence boundary

#### Scenario: Recurring base-task replacement with a deadline is rejected
- **WHEN** a client sends a complete `TaskUpdateRequest` with `isRecurring: true` and a non-null `dueAt`
- **THEN** the system SHALL reject the request
- **AND** it SHALL leave the existing task unchanged

#### Scenario: Partial base-task update is rejected
- **WHEN** a client omits a required mutable property from `PUT /tasks/{taskId}`
- **THEN** the system SHALL reject the request
- **AND** it SHALL leave the existing task unchanged

#### Scenario: Output-only and retired task state is not client-controlled
- **WHEN** a client sends a base-task replacement request
- **THEN** the request SHALL NOT be able to replace a section identifier, task ID, completion state, created timestamp, updated timestamp, completed timestamp, or occurrence metadata

### Requirement: Following-series updates split and replace a complete series

The system SHALL expose a dedicated `PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following` endpoint for a recurring task. It SHALL accept a complete `TaskUpdateRequest` with `isRecurring: true` and `dueAt: null`, end the original series immediately before the identified occurrence, and create a new recurring task from every supported supplied mutable value. The new series SHALL begin from the supplied replacement schedule and SHALL be returned as a `RecurringTaskSeriesDto` representation without a `dueAt` property.

#### Scenario: Replace the following series
- **WHEN** a client sends a valid complete `TaskUpdateRequest` with `dueAt: null` to the following-series endpoint for a valid recurring occurrence
- **THEN** the system SHALL end the original series before that occurrence
- **AND** it SHALL create a new recurring series from the supplied representation
- **AND** it SHALL return `kind: RECURRING_SERIES` without a `dueAt` property

#### Scenario: Following-series update rejects a deadline
- **WHEN** a client supplies a non-null `dueAt` in a following-series replacement request
- **THEN** the system SHALL reject the request without splitting the original series

#### Scenario: Following-series update preserves supported explicit nulls
- **WHEN** a client supplies `priority` or `scheduledAt` as null in a valid following-series replacement request
- **THEN** the new series SHALL persist each supplied null value

#### Scenario: Following-series update requires a valid occurrence
- **WHEN** a client targets an instant that is not an occurrence generated by the recurring task
- **THEN** the system SHALL reject the request without splitting the series

### Requirement: Single-occurrence updates use an occurrence-specific replacement resource

The system SHALL expose `PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}` for a single recurring occurrence. The endpoint SHALL accept a typed `OccurrenceUpdateRequest` containing only title, priority, scheduledAt, and dueAt, because those are the properties supported by occurrence state. It SHALL create or replace the target occurrence's overrides without changing the parent task or other occurrences and SHALL return a `RecurringTaskOccurrenceDto`. The target MUST either be generated by the current series rule or identify existing detached occurrence state.

#### Scenario: Replace supported values for one occurrence
- **WHEN** a client sends a valid `OccurrenceUpdateRequest` for a valid recurring occurrence
- **THEN** the system SHALL persist that occurrence's supported override values
- **AND** it SHALL leave the parent task and every other occurrence unchanged
- **AND** it SHALL return `kind: RECURRING_OCCURRENCE` with the targeted `occurrenceScheduledAt`

#### Scenario: Replace a detached occurrence
- **WHEN** a client sends a valid `OccurrenceUpdateRequest` for existing detached occurrence state
- **THEN** the system SHALL replace its supported override values without requiring the current rule to generate its anchor
- **AND** the occurrence SHALL remain detached

#### Scenario: Unsupported occurrence property is rejected
- **WHEN** a client attempts to replace a base-task-only property through the single-occurrence endpoint
- **THEN** the system SHALL reject the request
- **AND** it SHALL not change the parent task or occurrence

#### Scenario: Single-occurrence update requires a valid occurrence
- **WHEN** a client targets an instant that is neither generated by the parent task's recurrence rule nor backed by detached occurrence state
- **THEN** the system SHALL reject the request without persisting an occurrence override

### Requirement: Partial base mutations preserve recurring history

An unscoped partial task mutation SHALL apply the same non-retroactive generator rule as full replacement. Once a recurring series has persisted occurrence state, it SHALL reject a change to recurring status, normalized recurrence rule, or `scheduledAt` without changing stored state.

#### Scenario: MCP partial update attempts a retroactive rule change
- **WHEN** an unscoped partial mutation changes the recurrence rule of a recurring series with persisted occurrence state
- **THEN** the system SHALL reject the mutation before changing the series

#### Scenario: MCP partial update changes a non-generator field
- **WHEN** an unscoped partial mutation changes only non-generator fields of a recurring series
- **THEN** the system SHALL apply the mutation without requiring a following-series boundary

### Requirement: Task mutations preserve the recurring schedule invariant

Every complete or partial base-task mutation SHALL validate the resulting combination of recurring status and `scheduledAt` before persisting it. A following-series replacement SHALL validate its successor definition before truncating the original series or detaching occurrence state.

#### Scenario: Convert an unscheduled task to recurring
- **WHEN** a complete or partial mutation makes an unscheduled non-recurring task recurring without assigning `scheduledAt`
- **THEN** the system SHALL reject the mutation without changing the task

#### Scenario: Remove the schedule from a recurring series
- **WHEN** a complete replacement keeps `isRecurring: true` and supplies `scheduledAt: null`
- **THEN** the system SHALL reject the replacement without changing the series

#### Scenario: Replace a following series without a schedule
- **WHEN** a following-series replacement supplies `isRecurring: true` and `scheduledAt: null`
- **THEN** the system SHALL reject the replacement before changing the original series or its occurrence state

#### Scenario: Remove the schedule while making a task non-recurring
- **WHEN** a complete replacement supplies `isRecurring: false` and `scheduledAt: null`
- **THEN** the system SHALL accept the resulting unscheduled non-recurring task when its other values are valid
