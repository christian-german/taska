## MODIFIED Requirements

### Requirement: Mutations identify their target explicitly
The application SHALL distinguish base-task mutations, occurrence rescheduling and lifecycle operations, and series stopping. Occurrence and stop operations MUST receive the series identifier and stable occurrence identity. Following-series editing SHALL be rejected without creating a successor or modifying the original series.

#### Scenario: Move one occurrence
- **WHEN** a recurring occurrence schedule changes
- **THEN** the mutation boundary SHALL invoke the occurrence rescheduling owner with the original identity

#### Scenario: Stop a series
- **WHEN** deletion uses FROM_THIS for an attached recurring occurrence
- **THEN** the mutation boundary SHALL stop the series before that occurrence without creating a successor

### Requirement: Existing transport behavior remains compatible
Read representations and successful-mutation publication SHALL remain compatible. Occurrence writes SHALL accept only scheduling, and following-series updates SHALL no longer be supported. Successful supported mutations SHALL publish exactly one account change signal; rejected mutations SHALL publish none.

#### Scenario: Retired mutation is rejected
- **WHEN** REST or MCP attempts a following-series update
- **THEN** it SHALL fail without a persistent change or change publication
