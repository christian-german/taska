## 1. Persistence and backend domain

- [x] 1.1 Add a Flyway migration that introduces nullable `due_at` columns for tasks and recurring task-instance overrides without modifying existing `scheduled_at` values.
- [x] 1.2 Add nullable `dueAt` support to task and task-instance entities, request/response DTOs, mapper logic, and task-service create, patch, retrieval, and recurrence flows.
- [x] 1.3 Implement recurrence due-date inheritance and `THIS_ONLY`/`FROM_THIS` update semantics so occurrence overrides take precedence over the series deadline.
- [x] 1.4 Keep date filters, calendar scheduling, all-day behavior, and notifications exclusively bound to `scheduledAt`.

## 2. Public task interfaces

- [x] 2.1 Extend REST task request and response contracts to accept and return nullable `dueAt` independently from `scheduledAt`.
- [x] 2.2 Extend MCP task tool input/output schemas and mappings with nullable `dueAt`, keeping its meaning distinct from calendar scheduling.
- [x] 2.3 Update frontend task models and Quick Add/create/edit/detail flows to preserve and present the due date independently from the scheduled date.
- [x] 2.4 Update Android task models and create/edit/detail flows to preserve and present the due date independently from the scheduled date.

## 3. Documentation and verification

- [x] 3.1 Update REST, MCP, and user-facing task documentation to define `dueAt`/`due_at` as a deadline and `scheduledAt`/`scheduled_at` as calendar placement.
- [x] 3.2 Add backend unit and integration coverage for migration, task create/update/retrieval, independent schedule/deadline values, and unchanged scheduling behavior.
- [x] 3.3 Add recurrence coverage for inherited and overridden occurrence due dates across scoped updates.
- [x] 3.4 Verify frontend task-detail compilation and add Android model coverage for serialization and presentation of due dates distinct from scheduled dates.
