## ADDED Requirements

### Requirement: Existing occurrence customizations remain readable
Existing title, priority and deadline overrides SHALL remain persisted and readable. New occurrence edits SHALL NOT write these properties. Rescheduling, stopping, completion and reopening SHALL preserve existing customizations. Series common properties SHALL remain editable, while its recurrence generator and start date remain fixed.

#### Scenario: Move a historically customized occurrence
- **WHEN** an occurrence with historical title, priority or deadline overrides is moved
- **THEN** those overrides SHALL remain unchanged and visible in its response

#### Scenario: Edit a series title
- **WHEN** a series title is changed without changing its generator
- **THEN** the change SHALL succeed and existing occurrence title overrides SHALL remain authoritative

### Requirement: Calendar recurrence remains independent of completion
Calendar queries SHALL expand series throughout the requested inclusive date range independently of prior completion. A moved occurrence SHALL replace its original position, retain its original identity, and appear exactly once at its effective schedule, including when its original position lies outside the query range. Completed moved occurrences SHALL be discoverable using the same range rules as other completed recurring occurrences.

#### Scenario: Next week is visible before this week is completed
- **WHEN** a weekly task is still open this week and the client queries next week
- **THEN** next week's generated occurrence SHALL be returned

#### Scenario: Moved occurrence enters a query range
- **WHEN** an occurrence originally outside the requested range was moved into it
- **THEN** it SHALL appear once at the effective schedule even when completed

### Requirement: Clients expose only supported occurrence operations
Web, Tauri and Android SHALL allow moving, completing, reopening and removing an occurrence. They SHALL NOT expose occurrence title, priority, deadline, description, project, label, type, duration or recurrence editing. Moving SHALL directly address that occurrence without asking for edit scope. Schedule reset SHALL be labelled as returning to the original date, not removing the schedule. Series generator controls SHALL be read-only for existing series. Creation retains recurrence configuration.

#### Scenario: Drag a recurring calendar item
- **WHEN** a user moves a recurring calendar item
- **THEN** the client SHALL send only the new occurrence schedule with its stable identity
- **AND** no following-series edit option SHALL be shown

#### Scenario: Stop from an attached occurrence
- **WHEN** a user opens the occurrence removal choices
- **THEN** the choices SHALL distinguish removing this occurrence from stopping the series from this occurrence
- **AND** stopping SHALL create no replacement series

#### Scenario: Detached occurrence actions
- **WHEN** a user views a detached occurrence
- **THEN** movement, completion, reopening and individual removal SHALL remain supported
- **AND** the client SHALL not offer stopping the series from its detached identity
