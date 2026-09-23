## ADDED Requirements

### Requirement: Calendar-week widget retrieves overdue content from the authoritative query
The calendar-week widget SHALL retrieve its historical overdue content from `GET /tasks/overdue` and keep that result distinct from its current-week range response until it constructs widget groups. It SHALL display every returned overdue representation before current-week content and SHALL preserve recurring occurrence identity for its task actions.

#### Scenario: A historical recurring occurrence is returned
- **WHEN** the overdue query returns an incomplete recurring occurrence
- **THEN** the calendar-week widget SHALL display it as an overdue actionable row
