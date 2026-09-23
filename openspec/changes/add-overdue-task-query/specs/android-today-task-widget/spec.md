## ADDED Requirements

### Requirement: Today widget retrieves overdue content from the authoritative query
The Today widget SHALL retrieve its historical overdue content from `GET /tasks/overdue` and keep that result distinct from its current-day range response until it constructs its overdue and current-day groups. It SHALL place the returned overdue representations before current-day tasks, subject to its existing row capacity, and SHALL preserve recurring occurrence identity for task actions.

#### Scenario: A historical recurring occurrence is returned
- **WHEN** the overdue query returns an incomplete recurring occurrence
- **THEN** the Today widget SHALL display it as an overdue actionable row, subject to row capacity
