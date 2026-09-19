## Why

The task backend now separates stored task definitions, recurring occurrences, recurring-series restructuring, and cross-cutting mutation dispatch, but its mostly flat `task.service` package still hides those ownership boundaries. The package layout should make the established responsibilities and allowed dependencies visible without changing runtime behavior.

## What Changes

- Reorganize the task backend into definition, occurrence, and recurring-series subfeatures, each with technical subpackages matching its responsibilities.
- Keep `TaskMutationService` and the application contracts shared by multiple task subfeatures at the task-level service boundary.
- Rename `TaskService` to `TaskDefinitionService` so its name describes its ownership of persisted non-recurring tasks and recurring-series definitions.
- Place JPA entities alongside their persistence interfaces in the owning `repository` package, including `Task` and `TaskOccurrenceState`; keep non-persistence task value types in their owning feature or subfeature root.
- Preserve the single HTTP task adapter and the task MCP adapter as entry points to the same application services.
- Update tests and automated architecture checks to enforce package placement and dependency direction.
- Preserve all REST, MCP, persistence-schema, transaction, validation, recurrence, notification, and change-publication behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `backend-application-boundaries`: Place JPA entities in their owning feature's `repository` package and allow nested subfeatures to own technical layers.
- `task-service-responsibility-separation`: Make the existing definition, occurrence, series, and mutation-orchestration ownership explicit in the task package structure and rename the stored-definition service accordingly.

## Impact

- Backend Java package declarations, imports, and source paths under `com.taska.domain.task`.
- Backend tests and architecture checks that reference task services or persistence entities.
- Other backend features that import task services or task persistence entities.
- No REST or MCP contract, database schema, persisted data, frontend, Android, or externally observable behavior changes.
