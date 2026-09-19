## ADDED Requirements

### Requirement: Maintained clients model detached occurrence state

The Angular and Android task models SHALL consume the required `isDetached` property on recurring-occurrence responses and SHALL preserve it through client-side copies and state updates. Non-recurring tasks and recurring-series definitions SHALL NOT be treated as detached occurrences.

#### Scenario: Client receives a detached occurrence
- **WHEN** a maintained client deserializes a recurring occurrence with `isDetached: true`
- **THEN** its occurrence model SHALL retain detached status

#### Scenario: Client receives an attached occurrence
- **WHEN** a maintained client deserializes a recurring occurrence with `isDetached: false`
- **THEN** its occurrence model SHALL retain attached status

### Requirement: Detached occurrences are visibly identified

Angular and Android SHALL display the non-color-only text `Hors série` for every detached occurrence in supported calendar task rows and task detail. They SHALL NOT display that badge for attached or virtual occurrences.

#### Scenario: Detached occurrence appears in a task row
- **WHEN** a calendar task row represents a detached occurrence
- **THEN** the row SHALL display `Hors série`

#### Scenario: Detached occurrence detail opens
- **WHEN** task detail displays a detached occurrence
- **THEN** the detail view SHALL display `Hors série`

#### Scenario: Attached occurrence is displayed
- **WHEN** a task row or detail view represents an occurrence with `isDetached: false`
- **THEN** it SHALL NOT display the detached badge

### Requirement: Detached mutations never request recurrence scope

When a detached occurrence is edited, completed, reopened, or deleted, the client SHALL target its series ID and stable `occurrenceScheduledAt` directly and SHALL NOT display a `THIS_ONLY` versus `FROM_THIS` choice. Editable detached properties SHALL be limited to the content, priority, schedule, and deadline fields supported by occurrence state. Series-only properties SHALL not be editable from detached occurrence detail.

#### Scenario: Edit a supported detached property
- **WHEN** a user changes the content, priority, schedule, or deadline of a detached occurrence
- **THEN** the client SHALL invoke the single-occurrence operation directly
- **AND** it SHALL NOT display a recurrence-scope dialog

#### Scenario: Delete a detached occurrence
- **WHEN** a user confirms deletion of a detached occurrence
- **THEN** the client SHALL request deletion with `THIS_ONLY` and the stable occurrence identity
- **AND** it SHALL NOT display a recurrence-scope dialog

#### Scenario: Complete or reopen a detached occurrence
- **WHEN** a user completes or reopens a detached occurrence
- **THEN** the client SHALL include the stable occurrence identity in the operation
- **AND** it SHALL NOT request recurrence scope

#### Scenario: View a series-only property on detached detail
- **WHEN** detached occurrence detail shows type, description, project, labels, estimate, or recurrence metadata
- **THEN** the client SHALL NOT offer an edit action for that property

### Requirement: Android detail loads the addressed occurrence

When Android task detail receives both a task ID and `occurrenceScheduledAt`, it SHALL retrieve that occurrence resource rather than reconstructing an occurrence from the base series. The loaded representation SHALL preserve effective overrides, completion, virtual state, and detached state.

#### Scenario: Open detached occurrence detail on Android
- **WHEN** Android opens task detail with a series ID and detached occurrence identity
- **THEN** it SHALL retrieve the occurrence resource
- **AND** it SHALL render the server-returned detached occurrence

#### Scenario: Open base task detail on Android
- **WHEN** Android opens task detail without an occurrence identity
- **THEN** it SHALL continue retrieving the base task resource

