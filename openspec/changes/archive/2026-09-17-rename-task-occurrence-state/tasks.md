## 1. Persistence schema

- [x] 1.1 Add a forward migration renaming the occurrence-state table, series column, constraints, and indexes without copying data.
- [x] 1.2 Rename the recurring notification marker association and database objects to `seriesId` terminology.

## 2. Java occurrence-state vocabulary

- [x] 2.1 Rename `TaskInstance`, its status enum, and repository to occurrence-state names and map them to the renamed schema.
- [x] 2.2 Replace occurrence-state `taskId` fields, accessors, repository methods, parameters, and local variables with `seriesId`.
- [x] 2.3 Update recurrence results, task services, mappers, and notification discovery to use the new occurrence-state vocabulary without changing behavior.

## 3. Tests and verification

- [x] 3.1 Rename and update unit tests while preserving coverage for expansion, overrides, close/reopen, and notifications.
- [x] 3.2 Extend PostgreSQL integration coverage to verify the renamed schema and atomic notification claims.
- [x] 3.3 Run formatting, focused tests, the full backend suite, OpenSpec validation, and API contract checks.
