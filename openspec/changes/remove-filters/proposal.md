## Why

Taska no longer needs saved filters or the named task-filter shortcut. Keeping their persistence, APIs, clients, and documentation would leave an unsupported feature surface and redundant ways to express date-based task queries.

## What Changes

- **BREAKING** Remove saved-filter persistence and all `/filters` REST resources.
- **BREAKING** Remove the `filter` query parameter from task listing and the corresponding named-filter behavior.
- **BREAKING** Remove named filter input from MCP task listing.
- Delete saved-filter models, services, screens, routes, navigation, tests, OpenAPI definitions, and feature documentation.
- Add a forward database migration that removes existing saved-filter data.
- Preserve project, label, completion, explicit date/range, scheduling, search, and client-local collection filtering behavior.

## Capabilities

### New Capabilities

- `filter-removal`: Defines the permanent absence of saved filters and named task-filter shortcuts across persistence, APIs, and first-party clients.

### Modified Capabilities

- `taska-mcp-server`: Removes named filter input from MCP task-listing tools while preserving the remaining task query inputs.

## Impact

This removes the backend filter domain, its database table and REST routes, the task-list named-filter parameter, OpenAPI filter paths/schemas, Angular filter services and views, sidebar integration, MCP filter input, and related tests and documentation. Existing installations lose saved-filter records when the migration runs. Android has no saved-filter UI but any obsolete task-list filter parameter is removed from its API contract.
