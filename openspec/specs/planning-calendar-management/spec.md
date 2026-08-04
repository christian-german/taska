## Purpose

Define named planning calendars, their recurring weekly availability, and their project and GUI integration.

## Requirements

### Requirement: Planning calendars define weekly availability
The system SHALL support named planning calendars. Each planning calendar SHALL own zero or more recurring weekly availability rules. A rule SHALL contain one weekday and a same-day local-time interval with an inclusive start and exclusive end. Rule times SHALL be interpreted in the configured application calendar time zone. An interval end of `24:00` SHALL represent the end of that local day. Rules for the same calendar and weekday SHALL NOT overlap, and a rule SHALL NOT span midnight.

#### Scenario: Create a calendar with working-hour rules
- **WHEN** a user creates a calendar named `Work` with non-overlapping Monday through Friday rules from `09:00` to `17:00`
- **THEN** the system SHALL persist the calendar and its rules

#### Scenario: Reject an overlapping rule
- **WHEN** a user adds a rule that overlaps another rule on the same calendar and weekday
- **THEN** the system SHALL reject the change without altering the calendar's existing rules

#### Scenario: Represent a full-day rule
- **WHEN** a calendar rule has `00:00` as its start and `24:00` as its end
- **THEN** the rule SHALL make the entire corresponding local day available

### Requirement: Planning calendars can be created and maintained
The system SHALL allow users to list and retrieve planning calendars, create a calendar, rename a calendar, and replace or modify its weekly availability rules. This change SHALL NOT provide calendar deletion.

#### Scenario: Rename a calendar
- **WHEN** a user changes the name of an existing planning calendar
- **THEN** subsequent calendar and project representations SHALL show the new name while retaining its rules and project associations

#### Scenario: Edit calendar availability
- **WHEN** a user replaces an existing calendar's weekly rules with valid non-overlapping rules
- **THEN** future scheduling validation SHALL use the replacement rules

### Requirement: Every project has one planning calendar
The system SHALL associate every project with exactly one planning calendar. Project create and update representations SHALL expose the associated calendar identifier. A project created without an explicit calendar SHALL be associated with the Default Calendar. The system SHALL reject changing a project's calendar when any of its explicitly scheduled tasks would be outside the target calendar's availability.

#### Scenario: Create a project with a selected calendar
- **WHEN** a user creates a project and supplies a planning calendar identifier
- **THEN** the created project SHALL be associated with that calendar

#### Scenario: Create a project without selecting a calendar
- **WHEN** a user creates a project without supplying a planning calendar identifier
- **THEN** the system SHALL associate the project with the Default Calendar

#### Scenario: Reject an incompatible project calendar change
- **WHEN** a user assigns a project with an existing scheduled task to a calendar that does not permit that task's scheduled time
- **THEN** the system SHALL reject the assignment and preserve the project's current calendar

### Requirement: Existing projects receive a non-restrictive Default Calendar
The system SHALL create a `Default Calendar` with full-day availability for every day of the week during migration and SHALL associate every existing project with it. The Default Calendar's 24/7 availability SHALL preserve the validity of existing scheduled tasks.

#### Scenario: Migrate existing projects
- **WHEN** the planning-calendar migration is applied to an installation containing projects and scheduled tasks
- **THEN** every existing project SHALL reference the Default Calendar and its scheduled tasks SHALL remain schedulable

### Requirement: Planning-calendar availability is visible and editable in the GUI
The GUI SHALL provide a Planning Calendars management view that lists calendars and visibly presents each calendar's weekly availability rules. The GUI SHALL allow users to create calendars, rename calendars, and edit rules. The project editor SHALL display and allow selection of the project's planning calendar.

#### Scenario: View work-calendar availability
- **WHEN** a user opens the Planning Calendars management view and selects the `Work` calendar
- **THEN** the user SHALL see its weekly availability rules grouped by weekday

#### Scenario: Assign a calendar while editing a project
- **WHEN** a user edits a project
- **THEN** the project editor SHALL display the currently assigned planning calendar and allow the user to select another available calendar
