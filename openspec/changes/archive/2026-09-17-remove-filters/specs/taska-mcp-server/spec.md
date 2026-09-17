## MODIFIED Requirements

### Requirement: Task management tools

The MCP server SHALL expose tools to list tasks, retrieve a task by identifier, create a task, update a task, complete a task, and reopen a task. Each task tool SHALL delegate to Taska's task application service and preserve existing task validation, inbox defaults, and recurring-task semantics. MCP task tool inputs and outputs SHALL NOT contain section identifiers, and task-list input SHALL NOT contain a named filter shortcut.

#### Scenario: Client lists scoped tasks
- **WHEN** an authenticated MCP client invokes the task-listing tool with supported project, label, or completion inputs
- **THEN** the server SHALL return tasks matching the same semantics as Taska's task service without section data or named filter input

#### Scenario: Client creates an inbox task
- **WHEN** an authenticated MCP client invokes task creation without a project identifier or parent task
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
