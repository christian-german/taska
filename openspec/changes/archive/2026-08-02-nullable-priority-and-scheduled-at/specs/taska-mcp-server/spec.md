## ADDED Requirements

### Requirement: MCP task tools use nullable priority and scheduled-at fields
The MCP server's task create, update, retrieval, completion, reopen, and listing representations SHALL use nullable `priority` and `scheduledAt` fields for the task's manual priority and planned timestamp. The MCP server SHALL preserve a `null` manual priority without substituting priority `4`, and it SHALL NOT accept or return `dueAt` for the planned timestamp.

#### Scenario: Create a task through MCP without a priority
- **WHEN** an authenticated MCP client invokes task creation without assigning `priority`
- **THEN** the returned task representation SHALL contain `priority: null`

#### Scenario: Create a scheduled task through MCP
- **WHEN** an authenticated MCP client invokes task creation with `scheduledAt`
- **THEN** the returned task representation SHALL expose that same planned timestamp as `scheduledAt`

#### Scenario: Use legacy due-at input through MCP
- **WHEN** an authenticated MCP client supplies `dueAt` to an MCP task tool after this change
- **THEN** the tool SHALL not treat it as the task's scheduled time
