## 1. Persisted domain invariant

- [x] 1.1 Add a Flyway migration that clears legacy recurring-series deadlines and constrains recurring rows to `due_at IS NULL`.
- [x] 1.2 Reject explicit recurring-series deadlines and clear an inherited deadline when a partial mutation converts a task to recurring.
- [x] 1.3 Stop successor-series operations from copying or accepting `dueAt`.

## 2. Occurrence and transport semantics

- [x] 2.1 Resolve recurring-occurrence `dueAt` only from occurrence state, never from the series definition.
- [x] 2.2 Remove `dueAt` from `RecurringTaskSeriesDto` and refactor the HTTP/OpenAPI common task shape into variant-specific deadline fields.
- [x] 2.3 Keep flat MCP series outputs compatible with `dueAt: null` while preserving explicit occurrence deadlines.

## 3. Maintained clients

- [x] 3.1 Update Angular task models and deadline interactions so recurring-series resources have no `dueAt` property.
- [x] 3.2 Update Android task models, deserialization, and task-detail behavior so recurring-series resources have no wire-level `dueAt` property.

## 4. Verification

- [x] 4.1 Add or update backend unit, representation, migration, and contract tests for the recurring-series deadline invariant.
- [x] 4.2 Update Angular and Android tests for the variant-specific deadline shape.
- [x] 4.3 Run formatters, focused tests, full relevant suites, OpenSpec validation, and API contract checks.
