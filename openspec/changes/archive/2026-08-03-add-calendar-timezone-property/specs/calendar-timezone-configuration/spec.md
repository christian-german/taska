## ADDED Requirements

### Requirement: Calendar availability has a configured application time zone
The system SHALL define `taska.calendar.time-zone` in the backend `application.properties` as an IANA time-zone identifier. The configured value SHALL be bound as a `ZoneId` and the application SHALL fail to start when it is invalid. The default configured value SHALL be `Europe/Paris`.

#### Scenario: Default calendar time zone is available
- **WHEN** the backend starts with its standard application properties
- **THEN** calendar availability SHALL use the `Europe/Paris` time zone

#### Scenario: Deployment overrides the calendar time zone
- **WHEN** a deployment supplies a valid override for `taska.calendar.time-zone`
- **THEN** calendar availability SHALL use the overridden IANA time zone

#### Scenario: Invalid calendar time zone is rejected
- **WHEN** the backend is started with an invalid `taska.calendar.time-zone` value
- **THEN** startup SHALL fail rather than using a system-default or fallback time zone
