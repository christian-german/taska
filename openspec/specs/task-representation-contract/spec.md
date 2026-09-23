# task-representation-contract Specification

## Purpose
TBD - created by archiving change separate-task-representations. Update Purpose after archive.
## Requirements
### Requirement: Task responses use explicit representation variants

The HTTP API SHALL represent every task response as exactly one of `NonRecurringTaskDto`, `RecurringTaskSeriesDto`, or `RecurringTaskOccurrenceDto`. Every representation SHALL contain a required `kind` discriminator with the corresponding value `NON_RECURRING`, `RECURRING_SERIES`, or `RECURRING_OCCURRENCE`. The API SHALL NOT expose `isRecurring` in task responses. `dueAt` SHALL be present only on non-recurring-task and recurring-occurrence representations and SHALL NOT be a property of `RecurringTaskSeriesDto`.

#### Scenario: Non-recurring task is returned
- **WHEN** an endpoint returns a persisted task whose recurrence flag is false
- **THEN** the response SHALL use `NonRecurringTaskDto` with `kind: NON_RECURRING`
- **AND** it SHALL contain a nullable `dueAt`
- **AND** it SHALL NOT contain recurring-series or occurrence metadata

#### Scenario: Recurring-series definition is returned
- **WHEN** an endpoint returns a persisted recurring-series definition rather than an expanded occurrence
- **THEN** the response SHALL use `RecurringTaskSeriesDto` with `kind: RECURRING_SERIES`
- **AND** it SHALL contain the series recurrence metadata
- **AND** it SHALL NOT contain `dueAt`, occurrence identity, instance identity, virtual-state, or completion fields

#### Scenario: Recurring occurrence is returned
- **WHEN** an endpoint returns an expanded recurring occurrence
- **THEN** the response SHALL use `RecurringTaskOccurrenceDto` with `kind: RECURRING_OCCURRENCE`
- **AND** it SHALL contain required `occurrenceScheduledAt`, `isVirtual`, and nullable `dueAt` fields
- **AND** its `id` SHALL identify the backing recurring series

### Requirement: Recurring occurrence identity remains distinct from effective scheduling

A `RecurringTaskOccurrenceDto` SHALL use `(id, occurrenceScheduledAt)` as its stable occurrence identity. Its `scheduledAt` SHALL contain the occurrence's effective planned time after applying an instance override. Its `instanceId` SHALL be nullable only because a virtual occurrence has no persisted instance.

#### Scenario: Virtual occurrence is represented
- **WHEN** recurrence expansion produces an occurrence with no persisted `TaskInstance`
- **THEN** the API SHALL return `isVirtual: true`, `instanceId: null`, and the generated instant as both `occurrenceScheduledAt` and effective `scheduledAt`

#### Scenario: Materialized occurrence is moved
- **WHEN** a persisted occurrence overrides its planned time
- **THEN** the API SHALL preserve the generated instant in `occurrenceScheduledAt`
- **AND** it SHALL return the overridden time in `scheduledAt`
- **AND** it SHALL return `isVirtual: false` and the persisted `instanceId`

### Requirement: Task endpoints constrain allowed representation variants

Undated task listings, task retrieval, task creation, base-task replacement, following-series replacement, and subtask listings SHALL return only non-recurring-task or recurring-series representations. Date and date-range task listings SHALL return only non-recurring-task or recurring-occurrence representations. A single-occurrence replacement, completion, or reopening SHALL return a recurring-occurrence representation when the target is recurring.

#### Scenario: Date-range list contains mixed displayable tasks
- **WHEN** a date-range query matches a non-recurring task and occurrences from a recurring series
- **THEN** the response SHALL contain a `NonRecurringTaskDto` and one `RecurringTaskOccurrenceDto` for each included occurrence
- **AND** it SHALL NOT contain a `RecurringTaskSeriesDto`

#### Scenario: Undated list contains resource definitions
- **WHEN** an undated task list matches a non-recurring task and a recurring series
- **THEN** the response SHALL contain a `NonRecurringTaskDto` and a `RecurringTaskSeriesDto`
- **AND** it SHALL NOT expand the recurring series into occurrence representations

### Requirement: Maintained clients consume task representations by discriminator

The Angular and Android clients SHALL model the three HTTP task representations as a discriminated union and SHALL use `kind`, rather than nullable occurrence fields or an `isRecurring` response property, to select recurrence-specific behavior. Their recurring-series concrete models SHALL NOT deserialize or serialize a `dueAt` property. Android SHALL deserialize each discriminator value into its corresponding concrete task type.

#### Scenario: Angular receives an occurrence
- **WHEN** Angular receives `kind: RECURRING_OCCURRENCE`
- **THEN** it SHALL treat `occurrenceScheduledAt` and nullable `dueAt` as occurrence properties
- **AND** it SHALL use `occurrenceScheduledAt` for occurrence-scoped operations

#### Scenario: Android receives a recurring series
- **WHEN** Android receives `kind: RECURRING_SERIES`
- **THEN** it SHALL deserialize the response as a recurring-series task without requiring occurrence fields or `dueAt`

#### Scenario: Android receives an unknown representation
- **WHEN** Android receives a task response with a missing or unsupported `kind`
- **THEN** deserialization SHALL fail instead of guessing a representation from nullable fields

### Requirement: Recurring occurrence responses expose detached status

Every `RecurringTaskOccurrenceDto` SHALL contain a required boolean `isDetached`. The value SHALL be true exactly when the represented materialized occurrence state is detached from the span generated by its backing series. Virtual and attached materialized occurrences SHALL return false.

#### Scenario: Return a detached occurrence
- **WHEN** an HTTP endpoint returns recurring-occurrence state classified as detached
- **THEN** the response SHALL contain `isDetached: true`
- **AND** it SHALL continue to expose the original series ID and stable occurrence identity

#### Scenario: Return a generated virtual occurrence
- **WHEN** an HTTP endpoint returns a virtual occurrence generated by its current series
- **THEN** the response SHALL contain `isDetached: false`

#### Scenario: Return attached materialized state
- **WHEN** an HTTP endpoint returns materialized occurrence state still attached to its generated occurrence
- **THEN** the response SHALL contain `isDetached: false`

### Requirement: One recurring occurrence can be retrieved by stable identity

The HTTP API SHALL expose `GET /tasks/{taskId}/occurrences/{occurrenceScheduledAt}` and return the addressed `RecurringTaskOccurrenceDto`. Existing detached state SHALL be retrievable by persisted identity even when the current recurrence rule no longer generates its anchor. An absent attached state SHALL be returned as a virtual occurrence only when the current rule generates the requested identity. Skipped or unavailable identities SHALL be rejected as not found.

#### Scenario: Retrieve a generated virtual occurrence
- **WHEN** a client retrieves an identity generated by the current recurring series with no persisted state
- **THEN** the API SHALL return a virtual `RecurringTaskOccurrenceDto`
- **AND** it SHALL contain `isDetached: false`

#### Scenario: Retrieve a detached occurrence
- **WHEN** a client retrieves an identity backed by detached occurrence state
- **THEN** the API SHALL return the materialized recurring occurrence
- **AND** it SHALL contain `isDetached: true`

#### Scenario: Retrieve a skipped occurrence
- **WHEN** a client retrieves an identity backed by skipped state
- **THEN** the API SHALL return not found

### Requirement: Overdue task listings contain only displayable task representations

The `GET /tasks/overdue` endpoint SHALL return only `NonRecurringTaskDto` and `RecurringTaskOccurrenceDto` representations. It SHALL NOT return a `RecurringTaskSeriesDto`.

#### Scenario: An overdue recurring series is listed
- **WHEN** one or more occurrences of a recurring series are overdue
- **THEN** the response SHALL contain one recurring-occurrence representation for each matching occurrence
- **AND** it SHALL NOT contain that series definition

#### Scenario: Retrieve an unavailable identity
- **WHEN** a client retrieves an identity that is neither generated by the current rule nor backed by detached state
- **THEN** the API SHALL return not found
