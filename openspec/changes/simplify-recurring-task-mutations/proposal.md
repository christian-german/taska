## Why

Recurring tasks must remain visible throughout any requested calendar period independently of completion. Simplifying the supported mutations removes series splitting and arbitrary occurrence overrides while preserving calendar scheduling and history.

## What Changes

- **BREAKING** Remove following-series replacement and successor creation from REST, MCP, and maintained clients.
- **BREAKING** Freeze existing series recurrence generators; stopping a series remains supported and creates no successor.
- **BREAKING** Restrict occurrence edits to rescheduling. Keep completion, reopening, skipping, detached history, and stable occurrence identity.
- Update frontend and Android actions, labels, and OpenAPI to match the reduced operations.
- Preserve calendar expansion, including moved-in occurrences and meaningful state surviving series stops.

## Capabilities

### New Capabilities

- `simplified-recurring-task-mutations`: Restricted recurrence operations and corresponding client experience.

### Modified Capabilities

- `explicit-task-mutations`: Remove following updates and restrict occurrence updates to scheduling.
- `task-service-responsibility-separation`: Series service owns stopping only, with no successor creation.
- `task-update-contract`: Restrict occurrence replacement and freeze series generators.
- `task-scheduling-and-priority-fields`: Preserve historical occurrence customizations read-only and distinguish schedule reset from removal.
- `recurring-series-history-preservation`: Preserve history on stopping without splitting.
- `taska-mcp-server`: Apply the same restrictions to tool mutations.

## Impact

Backend task services, REST and MCP adapters, HTTP request contracts, frontend task detail and calendar interactions, Android task detail and calendars, OpenAPI and contract tests. Work is local only, without GitHub operations, as requested. Existing recurrence data and identifiers remain in place.
