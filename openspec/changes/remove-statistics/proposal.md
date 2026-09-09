## Why

Taska no longer needs its aggregate statistics experience. Keeping the screen, API, computation code, and documentation would leave an unsupported product surface and unnecessary data scans in the maintained applications.

## What Changes

- **BREAKING** Remove the `/stats/overview` REST endpoint and its response contract.
- Delete the backend statistics controller, service, DTO, and statistics-only repository queries.
- Delete the Angular statistics screen, client service, models, route, navigation, command, and keyboard shortcut.
- Remove the Android project-level task-count and overdue-count summary that exposes statistics behavior.
- Remove statistics definitions and references from maintained OpenAPI and audit documentation.
- Preserve ordinary task and project listing, completion, scheduling, overdue display, and project views outside the retired aggregate statistics feature.

## Capabilities

### New Capabilities

- `statistics-removal`: Defines the permanent absence of aggregate statistics APIs and first-party statistics user interfaces.

### Modified Capabilities

None.

## Impact

This removes the Spring `domain.stats` package and stats-only task repository methods, Angular statistics feature files and entry points, the Android project summary counts, and current OpenAPI/audit material for statistics. There is no statistics entity, database table, migration, or Android statistics endpoint/client model to remove.
