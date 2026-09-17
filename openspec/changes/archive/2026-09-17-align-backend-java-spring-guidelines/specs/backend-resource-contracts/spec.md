## ADDED Requirements

### Requirement: Resource mutations use operation-specific request contracts
The REST API SHALL define operation-specific creation and replacement request schemas for labels, comments, projects, planning calendars, and tasks. Creation contracts SHALL contain only properties accepted during creation, replacement contracts SHALL contain every mutable property covered by that resource's PUT operation, and writable contracts SHALL exclude identifiers, generated timestamps, computed values, recurrence selectors used by dedicated operations, and other server-controlled state.

#### Scenario: Client inspects resource mutation schemas
- **WHEN** a client inspects the maintained OpenAPI contract
- **THEN** label, comment, project, planning-calendar, and task POST and PUT operations SHALL reference their operation-specific request schemas
- **AND** no writable schema SHALL expose server-controlled properties

### Requirement: PUT operations replace complete mutable resource state
Label, project, comment, and planning-calendar PUT operations SHALL apply every property in their replacement contract. The backend SHALL reject a replacement that omits any required property, including required properties that permit `null`; an explicit `null` SHALL be applied according to the property's documented meaning, and an empty collection SHALL clear that collection.

#### Scenario: Replace a label
- **WHEN** a client sends a label replacement containing name, color, order, and favorite state
- **THEN** the backend SHALL persist and return every supplied mutable value

#### Scenario: Reject an incomplete replacement
- **WHEN** a client omits any property required by a resource replacement schema
- **THEN** the backend SHALL reject the request without changing the resource

#### Scenario: Apply explicit nullable project relationships
- **WHEN** a client sends a complete project replacement with `parentId` set to `null`
- **THEN** the backend SHALL remove the project's parent relationship
- **AND** the contract SHALL NOT require or accept `clearParent`

#### Scenario: Clear planning-calendar rules
- **WHEN** a client sends a complete planning-calendar replacement with an empty `rules` array
- **THEN** the backend SHALL remove all existing availability rules from that calendar

### Requirement: Writable JSON contracts are strict
The backend SHALL reject unknown JSON properties for maintained REST creation, replacement, and dedicated-operation request bodies. API-facing paths, parameters, and JSON properties SHALL use the names defined by OpenAPI, with camelCase dynamic identifiers and kebab-case multi-word static path segments.

#### Scenario: Request contains an unknown property
- **WHEN** a client sends an otherwise valid mutation request with a property absent from that operation's schema
- **THEN** the backend SHALL return a client error without invoking the application mutation

### Requirement: First-party clients send the maintained contract
The Angular and Android applications SHALL use the operation-specific REST payloads defined by OpenAPI. A first-party PUT caller SHALL construct the complete replacement from the resource state being retained plus the user's requested changes.

#### Scenario: Web client edits a project
- **WHEN** the Angular project editor saves an existing project
- **THEN** it SHALL send every mutable project replacement property
- **AND** it SHALL express a removed parent as `parentId: null` without `clearParent`
