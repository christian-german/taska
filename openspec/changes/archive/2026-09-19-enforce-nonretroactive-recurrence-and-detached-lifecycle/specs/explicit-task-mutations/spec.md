## MODIFIED Requirements

### Requirement: Operation names describe persisted effects

The application task service SHALL name a single-occurrence deletion as a skip operation and a following-occurrence deletion as a series truncation operation. Deleting an attached generated occurrence SHALL persist a skip. Deleting an open detached occurrence SHALL remove its standalone persisted state because it overlays no generated occurrence to skip.

#### Scenario: Delete one generated occurrence
- **WHEN** deletion targets one generated recurring occurrence
- **THEN** the application records that occurrence as skipped through `skipOccurrence`

#### Scenario: Delete one detached occurrence
- **WHEN** deletion targets existing open detached occurrence state
- **THEN** the application removes that state
- **AND** it SHALL NOT persist a detached skipped state

#### Scenario: Delete a recurring tail
- **WHEN** deletion targets a recurring series from an identified occurrence onward
- **THEN** the application truncates the series through `truncateSeriesFrom`
