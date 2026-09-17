## ADDED Requirements

### Requirement: MCP task outputs remain adapter-owned

The MCP task adapter SHALL expose task outputs defined within the MCP adapter and SHALL NOT reuse HTTP task DTO types. The introduction of discriminated HTTP task representations SHALL NOT require MCP clients to consume the HTTP discriminator or its variant-specific schemas.

#### Scenario: MCP task tool returns a task after the HTTP contract changes
- **WHEN** an authenticated MCP client invokes a task tool after discriminated HTTP task representations are introduced
- **THEN** the MCP server SHALL return its MCP-owned task output with the existing MCP task field semantics
- **AND** it SHALL not expose an HTTP transport DTO type as its tool schema
