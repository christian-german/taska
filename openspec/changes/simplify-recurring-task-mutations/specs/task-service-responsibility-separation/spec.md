## MODIFIED Requirements

### Requirement: Recurring-series restructuring has a dedicated owner
`RecurringTaskSeriesService` in `com.taska.domain.task.series.service` SHALL own stopping a series at a valid generated occurrence. It SHALL validate the cut, persist the series end and preserve detached occurrence history. It SHALL NOT split a series or create a successor.

#### Scenario: Stop the following portion of a series
- **WHEN** the mutation boundary receives a FROM_THIS deletion
- **THEN** it SHALL delegate stopping to `RecurringTaskSeriesService`
- **AND** no successor SHALL be persisted

### Requirement: Transport behavior remains unchanged
Task reads, representations, scheduling expansion, completion, reopening, skipping and successful-mutation publication SHALL retain their semantics. Following-series updates SHALL be removed, occurrence updates SHALL be scheduling-only, and existing series generators SHALL be immutable.

#### Scenario: Existing transport invokes a supported mutation
- **WHEN** REST or MCP moves or completes an occurrence
- **THEN** it SHALL preserve stable identity and publish exactly one successful-mutation signal
