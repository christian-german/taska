## Why

The application uses “meeting” for a task type that represents scheduled commitments more broadly. Renaming it to “appointment” gives users clearer, more inclusive terminology across every client and API.

## What Changes

- Replace the `MEETING` task-type value with `APPOINTMENT` in persisted task data, backend and client APIs, and integrations.
- Update all user-visible labels, accessible labels, and identifiers from “Meeting” to “Appointment” (including localized French copy).
- Migrate existing `MEETING` tasks so their classification is retained as `APPOINTMENT`.
- **BREAKING**: Clients and integrations must use `APPOINTMENT` instead of `MEETING` when reading or writing task types.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-type-classification`: Rename the non-to-do task classification and its client-facing presentation from meeting to appointment.
- `task-priority-evaluation`: Apply priority-evaluation exclusions and invalidation rules to appointment tasks.

## Impact

The backend task model, persistence migration, REST/MCP representations, Android and web clients, tests, and OpenSpec requirements will be updated. No new dependencies are required.
