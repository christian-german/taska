## Why

Sections and time tracking are no longer part of Taska's intended product. Keeping their storage, public contracts, and client surfaces creates unsupported behavior and unnecessary maintenance, so both capabilities must be removed end to end.

## What Changes

- **BREAKING** Remove section resources, section persistence, project-section relationships, and section identifiers/filters from task, project, REST, and MCP contracts.
- **BREAKING** Remove time-entry resources, persistence, REST endpoints, web time-tracker navigation and UI, and Android timer controls and networking.
- Add a forward-only database migration that drops section and time-entry data structures from existing installations after removing the task-to-section relationship.
- Remove documentation, OpenAPI paths/schemas, models, services, tests, and archived visual assets whose purpose is either removed feature.
- Preserve task scheduling, deadlines, recurrence, notifications, and task duration estimates because they are separate from elapsed-time tracking.

## Capabilities

### New Capabilities
- `section-and-time-tracking-removal`: Defines the observable absence of section and elapsed-time tracking behavior across storage, APIs, clients, and documentation.

### Modified Capabilities
- `task-update-contract`: Removes `sectionId` from mutable task representations while preserving full-replacement semantics.
- `taska-mcp-server`: Removes section-based task inputs and output data from MCP task tools.

## Impact

Affected areas include the PostgreSQL/Flyway schema; Spring section, time-entry, task, project, and MCP code; Angular routes, navigation, project views, services, models, and time-tracker UI; Android task-detail UI, view models, API/repository models, and tests; REST/OpenAPI documentation; repository documentation; and feature-specific design assets. Existing section assignments and time-entry records are intentionally deleted by the migration.
