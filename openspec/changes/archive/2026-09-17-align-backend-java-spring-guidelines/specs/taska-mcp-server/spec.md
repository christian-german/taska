## MODIFIED Requirements

### Requirement: Project management tools
The MCP server SHALL expose tools to list projects, retrieve a project by identifier, create a project, and update a project. Each project tool SHALL map its transport input to Taska's transport-independent project application parameters, delegate to the same project application service behavior used by REST, and return a project representation containing its identifier and current state.

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
- **THEN** the tool SHALL return an actionable not-found error without exposing backend implementation details

### Requirement: Task management tools
The MCP server SHALL expose tools to list tasks, retrieve a task by identifier, create a task, update a task, complete a task, and reopen a task. Each task tool SHALL map its transport input to Taska's transport-independent task application parameters, delegate to the same authoritative task application behavior used by REST, and preserve existing task validation, inbox defaults, mutation side effects, and recurring-task semantics. MCP task tool inputs and outputs SHALL NOT contain section identifiers, and task-list input SHALL NOT contain a named filter shortcut.

#### Scenario: Client lists scoped tasks
- **WHEN** an authenticated MCP client invokes the task-listing tool with supported project, label, or completion inputs
- **THEN** the server SHALL return only tasks matching those inputs using the task application's query behavior

#### Scenario: Client creates an inbox task
- **WHEN** an authenticated MCP client invokes task creation without a project identifier or parent task
- **THEN** the server SHALL create the task in Taska's existing inbox project according to current task-service behavior

#### Scenario: Client completes a task
- **WHEN** an authenticated MCP client invokes the task-completion tool for an incomplete task
- **THEN** the server SHALL apply the same completion transition and side effects as the equivalent REST operation

#### Scenario: Client reopens a task
- **WHEN** an authenticated MCP client invokes the task-reopen tool for a completed task
- **THEN** the server SHALL apply the same reopen transition and side effects as the equivalent REST operation

#### Scenario: Client mutates a recurring task occurrence
- **WHEN** an authenticated MCP client updates, completes, or reopens a recurring task occurrence with the required recurrence scope and scheduled occurrence input
- **THEN** the server SHALL preserve the existing recurring-task semantics implemented by the task application service
