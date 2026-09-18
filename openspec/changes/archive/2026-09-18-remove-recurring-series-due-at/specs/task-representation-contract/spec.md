## MODIFIED Requirements

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
