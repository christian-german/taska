## Why

Detached recurring occurrences are now valid, mutable backend resources, but the maintained web and Android clients neither model nor explain their detached status. They can consequently offer following-series actions that do not apply to an occurrence outside the generated series, and Android task detail currently reloads the backing series instead of the addressed occurrence.

## What Changes

- Add a read endpoint for one recurring occurrence identified by its series ID and stable `occurrenceScheduledAt`.
- Consume the required `isDetached` response property in Angular and Android occurrence models.
- Display a visible, accessible `Hors série` badge for detached occurrences in task rows and task detail.
- Route supported detached-occurrence mutations directly to the occurrence without displaying a recurrence-scope dialog.
- Limit detached editing to occurrence-supported fields; series-only fields remain visible but are not editable from detached occurrence detail.
- Load the actual occurrence representation in Android detail instead of reconstructing it from the backing series.

## Capabilities

### New Capabilities

- `detached-occurrence-client-experience`: Defines how maintained clients identify, present, and mutate detached occurrences.

### Modified Capabilities

- `task-representation-contract`: Adds direct retrieval of one recurring-occurrence representation by stable occurrence identity.

## Impact

- Backend occurrence query service, task controller, OpenAPI path contract, and controller/service tests.
- Angular task models, service, task rows, task detail, and component tests.
- Android task models, Retrofit API, repository, task-detail loading and mutation routing, calendar/detail presentation, and tests.
- No persistence migration and no change to recurrence-generation behavior.
