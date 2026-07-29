## 1. Spring AI MCP server setup

- [x] 1.1 Select and pin a Spring AI release compatible with Spring Boot 4.0.6, then add the WebMVC MCP server starter and required BOM/dependency configuration to `taska-backend/pom.xml`.
- [x] 1.2 Configure the embedded MCP server for stateless Streamable HTTP at `/mcp`, enabling only the capabilities needed for the initial tool release.
- [x] 1.3 Update the existing Spring Security configuration so every MCP protocol request, including initialization and tool discovery, requires the existing valid bearer JWT.

## 2. Project MCP tools

- [x] 2.1 Define stable MCP input and output records for supported project operations without exposing persistence entities directly.
- [x] 2.2 Implement annotated Spring AI tools to list and retrieve projects by delegating to `ProjectService` and the existing mapper.
- [x] 2.3 Implement annotated Spring AI tools to create and update projects, preserving existing validation and project-service rules.

## 3. Task MCP tools

- [x] 3.1 Define stable MCP input and output records for supported task list, retrieval, creation, update, completion, and reopen operations.
- [x] 3.2 Implement annotated Spring AI tools to list and retrieve tasks, supporting the approved filters and existing task-service semantics.
- [x] 3.3 Implement annotated Spring AI tools to create and update tasks through `TaskService`, including inbox defaults and recurring-task scope inputs.
- [x] 3.4 Implement annotated Spring AI tools to complete and reopen tasks through `TaskService`, including recurring occurrence inputs.

## 4. Error handling and verification

- [x] 4.1 Translate validation, missing-resource, and unexpected failures from MCP tools into safe, actionable MCP tool errors without leaking internals.
- [x] 4.2 Add integration tests for authenticated MCP initialization and tool discovery at `/mcp`.
- [x] 4.3 Add integration tests covering authenticated project and task tool calls, including inbox creation behavior and recurring-task mutation behavior.
- [x] 4.4 Add security tests that verify invalid or absent bearer tokens cannot initialize MCP, discover tools, or access task/project data.
- [x] 4.5 Document the endpoint URL, bearer-JWT requirement, supported tools, and mono-user access model in the project documentation.
