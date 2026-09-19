## ADDED Requirements

### Requirement: MCP recurring occurrences expose detached status

The MCP-owned task output SHALL include a boolean `isDetached` for recurring occurrences. The value SHALL reflect the persisted detached classification without requiring MCP clients to consume HTTP DTO types.

#### Scenario: MCP returns a detached occurrence
- **WHEN** an MCP task tool returns recurring-occurrence state classified as detached
- **THEN** its task output SHALL contain `isDetached: true`
- **AND** it SHALL preserve the occurrence's task and stable schedule identities

#### Scenario: MCP returns an attached or virtual occurrence
- **WHEN** an MCP task tool returns an occurrence that is not detached
- **THEN** its task output SHALL contain `isDetached: false`

