## ADDED Requirements

### Requirement: Saved filters are absent from Taska

The system SHALL NOT expose saved-filter resources, saved-filter task collections, saved-filter persistence, or saved-filter controls in first-party clients. The maintained database schema SHALL NOT contain a `filters` table.

#### Scenario: User navigates maintained clients
- **WHEN** a user opens Taska after the removal
- **THEN** no saved-filter list, editor, favorite, navigation item, or saved-filter task view SHALL be available

#### Scenario: Caller requests a former saved-filter endpoint
- **WHEN** a caller requests a former `/filters` endpoint
- **THEN** the system SHALL NOT route the request to saved-filter application behavior

### Requirement: Named task-filter shortcuts are absent

The task-list REST and MCP contracts SHALL NOT accept or advertise a named `filter` input. The task service SHALL NOT implement the former `today`, `overdue`, or `upcoming` named-filter branch.

#### Scenario: Client inspects task-list contracts
- **WHEN** a client inspects the maintained REST or MCP task-list input
- **THEN** no named filter parameter SHALL be present

### Requirement: Explicit task query capabilities remain available

Removing the filter feature SHALL NOT remove project, label, completion, explicit single-date, date-range, or search behavior.

#### Scenario: Client queries tasks after filter removal
- **WHEN** a client lists tasks using a remaining supported query input
- **THEN** the system SHALL preserve that input's existing behavior

### Requirement: Upgrade removes saved-filter data

The database upgrade SHALL remove all persisted saved-filter records. Historical immutable migration files MAY remain as deployment history, but the post-upgrade schema and live application SHALL contain no saved-filter capability.

#### Scenario: Existing installation is upgraded
- **WHEN** Flyway applies the removal migration to a database containing saved filters
- **THEN** it SHALL remove the saved-filter table and its records
