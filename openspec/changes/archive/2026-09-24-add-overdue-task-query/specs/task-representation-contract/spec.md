## ADDED Requirements

### Requirement: Overdue task listings contain only displayable task representations
The `GET /tasks/overdue` endpoint SHALL return only `NonRecurringTaskDto` and `RecurringTaskOccurrenceDto` representations. It SHALL NOT return a `RecurringTaskSeriesDto`.

#### Scenario: An overdue recurring series is listed
- **WHEN** one or more occurrences of a recurring series are overdue
- **THEN** the response SHALL contain one recurring-occurrence representation for each matching occurrence
- **AND** it SHALL NOT contain that series definition
