## Context

The task feature exposes one HTTP adapter and one MCP adapter, with `TaskMutationService` as the transport-independent mutation boundary. Its application logic is now split among stored task-definition operations, generated or persisted occurrence operations, and recurring-series restructuring, but those services and their shared helpers still mostly occupy one flat `task.service` package.

The persisted `Task` row represents either a non-recurring task or the definition of a recurring series. It is therefore misleading to call this area `classic`. Occurrences have a separate sparse-state entity, while series restructuring changes persisted definitions rather than one occurrence. Existing backend conventions also place JPA entities in `repository` packages in practice, despite the canonical architecture spec currently saying otherwise.

## Goals / Non-Goals

**Goals:**

- Make task-definition, occurrence, recurring-series, and mutation-orchestration ownership visible from package names.
- Retain one HTTP adapter, one MCP adapter, and one shared mutation boundary for the task feature.
- Keep JPA entities with their owning repositories.
- Preserve the current service responsibilities and dependency direction.
- Make package-placement regressions detectable through automated architecture checks.

**Non-Goals:**

- Changing REST, MCP, database, recurrence, notification, transaction, or validation behavior.
- Introducing a query facade whose only role is forwarding existing reads.
- Splitting the task feature into Maven modules or independently deployed components.
- Renaming database tables, columns, entity names, API types, or serialized properties.
- Reorganizing unrelated backend feature packages beyond correcting the shared architecture rule for their already-established JPA entity placement.

## Decisions

### Use definition, occurrence, and series subfeatures

The target production layout is:

```text
com.taska.domain.task
├── controller/                 HTTP adapter and HTTP contracts
├── mcp/                        MCP adapter and MCP contracts
├── service/                    shared application boundary
│   ├── TaskMutationService
│   ├── shared mutation parameter records
│   └── TaskResult and its result variants
├── definition/
│   ├── TaskType
│   ├── TaskDefinitionRules
│   ├── repository/
│   │   ├── Task
│   │   └── TaskRepository
│   └── service/
│       └── TaskDefinitionService
├── occurrence/
│   ├── RecurrenceScope
│   ├── TaskOccurrenceStatus
│   ├── repository/
│   │   ├── TaskOccurrenceState
│   │   └── TaskOccurrenceStateRepository
│   └── service/
│       ├── TaskOccurrenceService
│       ├── TaskRecurrenceService
│       └── TaskOccurrenceUpdateParameters
└── series/
    └── service/
        └── RecurringTaskSeriesService
```

`definition` is used instead of `classic` because the persisted `Task` model owns both non-recurring tasks and recurring-series definitions. `series` remains distinct because splitting and truncation alter series topology and coordinate definition and occurrence behavior. Keeping all services in one flat package was considered, but rejected because it conceals the responsibility separation already established in code and specification.

### Keep shared orchestration and contracts at the task service boundary

`TaskMutationService` remains in `com.taska.domain.task.service` because it dispatches across all three subfeatures and publishes the shared account-change signal. Parameter and result types used across more than one subfeature remain beside that boundary. The occurrence-only replacement parameters move with the occurrence service.

The HTTP controller continues to call the definition or occurrence service directly for reads and the mutation boundary for writes. A new `TaskQueryService` is not introduced because it would currently add delegation without owning behavior.

### Keep JPA entities in repository packages

`Task` moves with `TaskRepository` into `task.definition.repository`. `TaskOccurrenceState` moves with `TaskOccurrenceStateRepository` into `task.occurrence.repository`. JPA entities are treated as persistence-owned models, while non-JPA enums and value types remain in the relevant subfeature root.

This deliberately updates the canonical backend package rule. Moving JPA entities to feature roots was considered, but rejected because it contradicts the repository's established persistence organization and the requested ownership model.

### Rename the stored-definition service

`TaskService` becomes `TaskDefinitionService`. Its responsibility and methods do not change: it owns stored task and recurring-series definition queries and mutations that neither target one occurrence nor restructure a series tail. The new name avoids implying ownership of all task behavior.

### Enforce direction without forbidding domain collaboration

Adapters may depend on the task-level service boundary and read-owning subfeature services. `TaskMutationService` may depend on all task subfeature services. The series service may depend on definition and occurrence services because series restructuring coordinates both. The occurrence service may read stored definitions and their repository but must not depend on the series service. The definition service must remain independent of occurrence persistence and recurrence expansion.

Architecture tests will verify the resulting package placement and the existing transport/repository dependency restrictions. Tests for moved classes will use matching packages where package-private access requires it; shared fixtures may remain at the task test root.

## Risks / Trade-offs

- [Mechanical package moves can leave stale imports, string-based class references, or test paths] → Search all production and test sources for old qualified names, then compile and run the complete backend test suite.
- [Nested packages can accidentally introduce circular service dependencies] → Preserve the specified dependency direction and add focused architecture assertions for the task subfeatures.
- [Moving shared rules across package boundaries can require broader Java visibility] → Expose only the minimum type or operation required by the series service and keep it internal to the task feature by convention.
- [A structural refactor can be mistaken for a behavior change] → Avoid changes to method semantics and verify existing controller, MCP, persistence, occurrence, notification, and mutation tests unchanged except for package references.

## Migration Plan

1. Update architecture rules and tests for nested subfeatures and repository-owned JPA entities.
2. Move the definition persistence types, definition rules, and renamed definition service together; update all imports and references.
3. Move occurrence persistence and service types together; update all imports and references.
4. Move the recurring-series service and update the mutation boundary dependencies.
5. Mirror affected test packages, run formatting, compile and execute the backend tests, validate OpenSpec, and scan for stale package names.

No deployment or data migration is required. Rollback consists of reverting the package-only commit because persistence metadata and external contracts remain unchanged.

## Open Questions

None.
