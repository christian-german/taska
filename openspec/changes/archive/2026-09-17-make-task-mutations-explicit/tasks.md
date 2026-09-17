## 1. Explicit service operations

- [x] 1.1 Split task, occurrence, and following-series update paths into explicitly named `TaskService` operations.
- [x] 1.2 Split task deletion, occurrence skipping, and following-series truncation into explicitly named `TaskService` operations.
- [x] 1.3 Split task and occurrence close/reopen paths into explicitly named `TaskService` operations.

## 2. Mutation routing

- [x] 2.1 Route existing update and delete parameter contracts to the matching explicit operation in `TaskMutationService`.
- [x] 2.2 Route existing close and reopen parameter contracts to the matching explicit operation while preserving validation and change publication.

## 3. Tests and verification

- [x] 3.1 Update service and adapter tests to use and verify the explicit operations and unchanged external behavior.
- [x] 3.2 Run formatting, focused tests, the full backend suite, OpenSpec validation, and API contract checks.
