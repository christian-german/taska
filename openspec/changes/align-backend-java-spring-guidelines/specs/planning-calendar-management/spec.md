## MODIFIED Requirements

### Requirement: Planning calendars can be created and maintained
The system SHALL allow users to list and retrieve planning calendars, create a calendar, and replace a calendar's mutable name and weekly availability rules. Planning-calendar creation and replacement SHALL use separate request contracts. A replacement SHALL require both `name` and `rules`; an empty rules array SHALL clear the calendar's availability, and omitted replacement properties SHALL be rejected. This change SHALL NOT provide calendar deletion.

#### Scenario: Rename a calendar
- **WHEN** a user sends a complete planning-calendar replacement with a changed name and retained rules
- **THEN** subsequent calendar and project representations SHALL show the new name while retaining its rules and project associations

#### Scenario: Edit calendar availability
- **WHEN** a user sends a complete planning-calendar replacement with a changed rules collection
- **THEN** future scheduling validation SHALL use exactly the replacement rules

#### Scenario: Clear calendar availability
- **WHEN** a user sends a complete planning-calendar replacement with an empty rules collection
- **THEN** the calendar SHALL retain no availability rules

### Requirement: Every project has one planning calendar
The system SHALL associate every project with exactly one planning calendar. Project create and complete-replacement representations SHALL expose the associated calendar identifier. A project created without an explicit calendar SHALL be associated with the Default Calendar. A complete project replacement SHALL require the current or newly selected planning calendar identifier. The system SHALL reject changing a project's calendar when any of its explicitly scheduled tasks would be outside the target calendar's availability.

#### Scenario: Create a project with a selected calendar
- **WHEN** a user creates a project and supplies a planning calendar identifier
- **THEN** the created project SHALL be associated with that calendar

#### Scenario: Create a project without selecting a calendar
- **WHEN** a user creates a project without supplying a planning calendar identifier
- **THEN** the system SHALL associate the project with the Default Calendar

#### Scenario: Retain a project calendar during replacement
- **WHEN** a user sends a complete project replacement without selecting a different planning calendar
- **THEN** the request SHALL contain the project's current planning calendar identifier
- **AND** the project SHALL remain associated with that calendar

#### Scenario: Reject an incompatible project calendar change
- **WHEN** a user assigns a project with an existing scheduled task to a calendar that does not permit that task's scheduled time
- **THEN** the system SHALL reject the assignment and preserve the project's current calendar
