## ADDED Requirements

### Requirement: Invalid recurring definitions are isolated during notification selection

The notification candidate sweep SHALL process recurrence expansion independently for each recurring series. A series with a missing or unusable recurrence rule SHALL produce no recurring notification candidates for that sweep and SHALL NOT prevent other eligible tasks or series from being processed.

#### Scenario: Candidate row has no recurrence rule
- **WHEN** notification selection encounters a recurring task without a recurrence rule
- **THEN** the system SHALL exclude it from recurring notification candidates
- **AND** it SHALL continue selecting candidates from other tasks

#### Scenario: One recurrence rule cannot be expanded
- **WHEN** recurrence expansion throws for one recurring series during a notification sweep
- **THEN** the system SHALL skip that series for the current sweep
- **AND** it SHALL continue processing every other series
- **AND** it SHALL record diagnostic information without exposing it to clients

