## ADDED Requirements

### Requirement: MCP follows simplified recurrence mutations
MCP task updates SHALL reject FROM_THIS edits and occurrence properties other than scheduling. Existing series generator changes SHALL be rejected with or without occurrence state. Supported single-occurrence scheduling updates SHALL reuse the same application behavior as REST, retaining stable identity, completion and detached classification.

#### Scenario: Tool attempts editing following occurrences
- **WHEN** update_task receives scope FROM_THIS
- **THEN** it SHALL return an error and neither modify the series nor create a successor

#### Scenario: Tool attempts an occurrence title override
- **WHEN** update_task receives scope THIS_ONLY and content
- **THEN** it SHALL return an error without modifying any state
