## MODIFIED Requirements

### Requirement: Task type can be changed
The system SHALL allow a complete base-task or following-series replacement to set the task type to either `TODO` or `APPOINTMENT`. A valid `TaskUpdateRequest` SHALL contain the type being retained or selected; the system SHALL replace the stored type with that supplied value while replacing the other mutable task properties. A single-occurrence update SHALL NOT change the parent task's type.

#### Scenario: A to-do becomes an appointment
- **WHEN** a client sends a complete valid replacement with type `APPOINTMENT` for a `TODO` task or following series
- **THEN** the system SHALL persist and return the replacement as an `APPOINTMENT`

#### Scenario: An appointment becomes a to-do
- **WHEN** a client sends a complete valid replacement with type `TODO` for an `APPOINTMENT` task or following series
- **THEN** the system SHALL persist and return the replacement as a `TODO`

#### Scenario: Single occurrence retains the parent type
- **WHEN** a client updates one recurring occurrence through the occurrence endpoint
- **THEN** the occurrence representation SHALL retain its parent task's type
