## 1. Architecture Contract

- [x] 1.1 Update backend architecture checks so JPA entities are required to live in their owning `repository` package and cohesive nested subfeatures may own technical subpackages.
- [x] 1.2 Add task-specific architecture assertions for definition, occurrence, series, adapter, and shared mutation-boundary package placement and dependency direction.

## 2. Task Definition Subfeature

- [x] 2.1 Create the `task.definition` package structure and move `TaskType`, `TaskDefinitionRules`, `Task`, and `TaskRepository` into their specified definition and repository packages.
- [x] 2.2 Move and rename `TaskService` to `task.definition.service.TaskDefinitionService` without changing its public behavior, transaction boundaries, validation, or persistence operations.
- [x] 2.3 Update production callers, cross-feature integrations, tests, fixtures, imports, and documentation references for the definition package and service rename.

## 3. Occurrence And Series Subfeatures

- [x] 3.1 Move `TaskOccurrenceState` beside `TaskOccurrenceStateRepository` in `task.occurrence.repository` and update all persistence, notification, service, and test references without changing JPA mappings.
- [x] 3.2 Move `TaskOccurrenceService`, `TaskRecurrenceService`, and `TaskOccurrenceUpdateParameters` into `task.occurrence.service`, retaining occurrence value types in the occurrence root.
- [x] 3.3 Move `RecurringTaskSeriesService` into `task.series.service` and update its definition and occurrence dependencies without introducing a reverse dependency.
- [x] 3.4 Update `TaskMutationService`, HTTP and MCP adapters, shared task contracts, and affected tests to use the reorganized subfeatures while preserving their existing routing and outputs.

## 4. Verification

- [x] 4.1 Move affected tests into matching packages where required for package-private access and run the backend formatter.
- [x] 4.2 Compile and run the complete backend test suite, including architecture, controller, MCP, persistence, occurrence, series, mutation, and notification coverage.
- [x] 4.3 Search production and test sources for stale task package names, validate this OpenSpec change and the complete OpenSpec repository, and run repository diff checks.
