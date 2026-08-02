## 1. Persisted task model and migration

- [x] 1.1 Add a Flyway migration that removes the task-priority default and renames `tasks.due_at` and recurring task-instance `due_at` columns to `scheduled_at`, preserving existing instants and database indexes.
- [x] 1.2 Rename the planned-time properties and mappings in `Task` and `TaskInstance` to `scheduledAt`, and make manual task priority nullable without a default value.
- [x] 1.3 Rename affected task and task-instance repository methods, JPQL queries, recurrence logic, date filtering, and notification scheduling to use `scheduledAt`.

## 2. Task application and REST contracts

- [x] 2.1 Rename `dueAt` to `scheduledAt` in task request/response DTOs, mappers, controller-facing JSON contracts, and API documentation; rename the distinct recurring-occurrence selector to `occurrenceScheduledAt`; ensure `due_at`/`dueAt` is no longer accepted or emitted.
- [x] 2.2 Update task creation, update, recurrence-scope, clone, and occurrence-override logic so manual priority can remain or become `null` without being defaulted to `4`, while retaining range validation for supplied values.
- [x] 2.3 Preserve the existing scheduled-time behavior for task creation, mutation, recurrence generation, date filters, and notifications under the renamed property.
- [x] 2.4 Confirm priority evaluation remains independent of nullable manual priority and continues to invalidate and retrieve evaluations under its existing rules.

## 3. MCP task tools

- [x] 3.1 Rename MCP task-tool input and output fields from `dueAt` to `scheduledAt` and update tool descriptions/schema metadata accordingly.
- [x] 3.2 Update MCP task create and update mapping so nullable priority is passed through, including clearing an assigned priority where supported by the task contract.
- [x] 3.3 Add MCP contract tests for omitted/cleared priority, `scheduledAt` round-tripping, and rejection or non-interpretation of legacy `dueAt` input.
- [x] 3.4 Check if a task can be created without a `scheduledAt` property by using the MCP Server. If not, add a contract test for this scenario and update the code accordingly.

## 4. Client contract migration

- [x] 4.1 Rename the web client task model, request payloads, date components, filters, recurrence handling, and display copy from `dueAt` to `scheduledAt` without changing planned-time behavior.
- [x] 4.2 Rename the Android task DTO/request models, view models, scheduling/notification code, screens, and user-facing planned-time terminology to `scheduledAt`.
- [x] 4.3 Update web and Android priority presentation/editing to represent an unassigned manual priority safely and without implying an explicit priority `4`.

## 5. Verification and documentation

- [x] 5.1 Add backend unit and integration coverage for the database migration, nullable priority create/update/recurrence paths, scheduled-time REST serialization, date filters, and notifications.
- [x] 5.2 Update task mapper, service, controller, and priority-evaluation tests for renamed fields and independent manual-priority/evaluation absence.
- [x] 5.3 Update MCP tests plus web and Android tests/build checks for the renamed API field and nullable-priority UI behavior.
- [x] 5.4 Update README and API/MCP usage documentation to describe `scheduled_at`/`scheduledAt`, nullable priority, and the breaking removal of `due_at`/`dueAt`.
