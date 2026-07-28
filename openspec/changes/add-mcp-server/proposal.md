## Why

Taska's task and project capabilities are available only through its REST API and user interfaces. Exposing a secure Model Context Protocol (MCP) endpoint lets compatible AI clients work with the same single-user Taska workspace through well-defined tools, without duplicating business logic or data access.

## What Changes

- Add a hosted Streamable HTTP MCP endpoint to the Spring Boot backend using Spring AI.
- Require the existing OAuth2 JWT bearer authentication for every MCP request; Taska remains a mono-user application and does not introduce tenant or ownership data.
- Expose first-release MCP tools for supported task and project listing, retrieval, creation, updates, and task completion/reopening operations.
- Route tool execution through the existing task and project application services so validation, recurrence behavior, inbox defaults, and persistence rules remain consistent with the REST API.
- Add automated coverage for MCP tool discovery, authenticated tool invocation, and unauthenticated request rejection.

## Capabilities

### New Capabilities

- `taska-mcp-server`: An authenticated Streamable HTTP MCP interface that exposes Taska task and project operations to MCP clients.

### Modified Capabilities

- None.

## Impact

- Affected backend: `taska-backend`, particularly Maven dependencies, application configuration, security configuration, and new MCP tool/configuration classes.
- Adds Spring AI's MCP server capability and a hosted `/mcp`-style endpoint.
- Existing REST endpoints and the single shared Taska data model remain unchanged.
