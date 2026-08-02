## Purpose

Provide a JWT-protected Model Context Protocol server embedded in Taska so compatible clients can work with projects and tasks.

## Requirements

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

### Requirement: Hosted Streamable HTTP MCP endpoint
The backend SHALL host a Streamable HTTP MCP server within the existing Spring Boot application at a documented endpoint, defaulting to `/mcp`. The server SHALL use Spring AI MCP server capabilities and SHALL operate without server-held MCP client session state.

#### Scenario: MCP client initializes the server
- **WHEN** a compatible client sends an authenticated Streamable HTTP MCP initialization request to the configured endpoint
- **THEN** the backend SHALL complete MCP initialization and advertise the Taska tools it exposes

#### Scenario: Tool discovery uses the hosted endpoint
- **WHEN** a compatible authenticated MCP client requests the available tools
- **THEN** the backend SHALL return the Taska task and project tools without requiring a separate MCP process

### Requirement: JWT-protected MCP access
The MCP endpoint SHALL require a bearer JWT accepted by Taska's existing OAuth2 resource-server configuration for every MCP operation. Taska SHALL treat all authenticated MCP access as access to its single shared workspace and SHALL NOT introduce tenant or record-ownership behavior in this change.

#### Scenario: Authenticated client invokes a tool
- **WHEN** an MCP client sends a tool request with a valid bearer JWT
- **THEN** the backend SHALL process the request under the existing Spring Security authentication configuration

#### Scenario: Unauthenticated client is rejected
- **WHEN** an MCP client requests MCP initialization, tool discovery, or tool invocation without a valid bearer JWT
- **THEN** the backend SHALL reject the request without exposing task or project data

### Requirement: Project management tools
The MCP server SHALL expose tools to list projects, retrieve a project by identifier, create a project, and update a project. Each project tool SHALL delegate to Taska's project application service and return a project representation containing its identifier and current state.

#### Scenario: Client lists projects
- **WHEN** an authenticated MCP client invokes the project-listing tool
- **THEN** the server SHALL return the projects in the same ordering and form produced by the Taska project service

#### Scenario: Client creates a project
- **WHEN** an authenticated MCP client invokes the project-creation tool with valid project input
- **THEN** the server SHALL create the project using Taska's existing project rules and return the created project

#### Scenario: Client updates a project
- **WHEN** an authenticated MCP client invokes the project-update tool with a valid project identifier and update input
- **THEN** the server SHALL apply the update using Taska's existing project rules and return the updated project

#### Scenario: Client requests a missing project
- **WHEN** an authenticated MCP client invokes a project retrieval or update tool for an identifier that does not exist
- **THEN** the server SHALL return an actionable tool error without exposing internal implementation details

### Requirement: Task management tools
The MCP server SHALL expose tools to list tasks, retrieve a task by identifier, create a task, update a task, complete a task, and reopen a task. Each task tool SHALL delegate to Taska's task application service and preserve existing task validation, inbox defaults, and recurring-task semantics.

#### Scenario: Client lists filtered tasks
- **WHEN** an authenticated MCP client invokes the task-listing tool with supported project, section, label, completion, or named date-filter inputs
- **THEN** the server SHALL return tasks matching the same semantics as Taska's task service

#### Scenario: Client creates an inbox task
- **WHEN** an authenticated MCP client invokes the task-creation tool without a project identifier or parent task
- **THEN** the server SHALL create the task in Taska's existing inbox project according to current task-service behavior

#### Scenario: Client completes a task
- **WHEN** an authenticated MCP client invokes the task-completion tool for an incomplete task
- **THEN** the server SHALL apply Taska's existing completion behavior and return the updated task

#### Scenario: Client reopens a task
- **WHEN** an authenticated MCP client invokes the task-reopen tool for a completed task
- **THEN** the server SHALL apply Taska's existing reopen behavior and return the updated task

#### Scenario: Client mutates a recurring task occurrence
- **WHEN** an authenticated MCP client updates, completes, or reopens a recurring task occurrence with the required recurrence scope and scheduled occurrence input
- **THEN** the server SHALL preserve the corresponding existing recurrence behavior

### Requirement: Safe MCP tool failures
The MCP server SHALL return actionable tool-level error content when a task or project tool receives invalid input or targets a missing resource. Error content SHALL NOT include stack traces, database details, or bearer-token contents.

#### Scenario: Client supplies invalid task input
- **WHEN** an authenticated MCP client invokes a task tool with input that violates Taska validation rules
- **THEN** the server SHALL return a safe error describing the invalid input and SHALL NOT persist an invalid task

#### Scenario: Unexpected tool failure occurs
- **WHEN** an unexpected exception occurs while processing an MCP tool call
- **THEN** the server SHALL return a generic safe tool error and log diagnostic detail only on the server
