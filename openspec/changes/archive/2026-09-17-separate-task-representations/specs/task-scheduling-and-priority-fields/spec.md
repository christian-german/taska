## ADDED Requirements

### Requirement: Variant-specific task fields preserve scheduling and priority semantics

Every task representation SHALL expose its effective nullable `priority`, `scheduledAt`, and `dueAt` values. A recurring occurrence SHALL resolve supported instance overrides before series values. Recurring-series and recurring-occurrence representations SHALL expose recurrence-rule metadata, while a non-recurring representation SHALL not expose recurrence-rule metadata.

#### Scenario: Occurrence inherits nullable values
- **WHEN** a virtual recurring occurrence has no priority, schedule, or deadline override
- **THEN** its representation SHALL inherit the applicable values from recurrence expansion and the backing series according to existing scheduling and priority semantics

#### Scenario: Non-recurring task omits recurrence metadata
- **WHEN** a non-recurring task representation is returned
- **THEN** it SHALL expose its scheduling, deadline, and priority values
- **AND** it SHALL not expose `recurrenceRule` or `rruleEndsAt`
