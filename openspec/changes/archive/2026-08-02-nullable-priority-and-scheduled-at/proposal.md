## Why

Tasks need to distinguish an unset user priority from an explicit priority value, while their planned time needs terminology that leaves room for a separate future due date. The REST and MCP contracts must make the same semantic change so every client can use the updated task model consistently.

## What Changes

- Allow a task's manual `priority` property to be absent (`null`) when no priority has been assigned.
- **BREAKING** Rename the task field `due_at` to `scheduled_at` throughout persisted task data and public task interfaces.
- Update task REST controllers and MCP task tools to accept and return nullable `priority` and `scheduled_at`.
- Retain the existing priority-evaluation behavior; an absent manual priority is not an evaluation score or an implicit zero.

## Capabilities

### New Capabilities
- `task-scheduling-and-priority-fields`: Defines task-level manual-priority nullability and the scheduled-time field terminology across task operations.

### Modified Capabilities
- `task-priority-evaluation`: Clarify that the optional task priority property is distinct from an optional current calculated evaluation.
- `taska-mcp-server`: Update MCP task-tool input and output contracts for nullable priority and `scheduled_at`.

## Impact

Affected task domain and persistence mappings, database migration(s), task request/response DTOs and REST controllers, task service and mapper logic, MCP task tool input/output models, API/MCP documentation, and their unit and integration tests. Existing API and MCP clients using `due_at` must migrate to `scheduled_at`.
