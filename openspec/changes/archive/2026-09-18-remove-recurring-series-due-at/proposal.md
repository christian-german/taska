## Why

A recurring-series definition currently stores one absolute `dueAt` instant and exposes it as the deadline of every generated occurrence. That value becomes stale immediately across later occurrences and has no coherent business meaning, so the model and contracts should prevent series-level deadlines.

## What Changes

- **BREAKING** Remove `dueAt` from recurring-series HTTP and client representations while retaining it for non-recurring tasks and individual recurring occurrences.
- Stop generated occurrences from inheriting `dueAt` from their series; a recurring occurrence has a deadline only when its occurrence state contains an explicit override.
- Reject creation and complete replacement requests that combine a recurring series with a non-null `dueAt`.
- Clear legacy series deadlines through a database migration and enforce that recurring task rows cannot persist a non-null `due_at`.
- Keep flat MCP compatibility by returning `dueAt: null` for recurring series while preserving occurrence-specific deadlines.
- Update backend, OpenAPI, web, and Android models and tests to reflect the new invariant.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: restrict deadlines to non-recurring tasks and explicit recurring-occurrence overrides.
- `task-update-contract`: define validation of `dueAt` for recurring creation, replacement, conversion, and following-series operations.
- `task-representation-contract`: remove `dueAt` from the recurring-series HTTP representation and align client discriminated models.

## Impact

- Backend task persistence, series and occurrence result mapping, request validation, Flyway migrations, and tests.
- OpenAPI task schemas and contract expectations.
- Angular and Android discriminated task models and affected UI/update behavior.
- MCP retains its flat output shape but recurring-series deadlines become permanently null.
