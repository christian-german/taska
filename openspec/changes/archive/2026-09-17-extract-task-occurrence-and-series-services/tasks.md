## 1. Occurrence service

- [x] 1.1 Create `TaskOccurrenceService` and move occurrence expansion, validation, override, skip, close, and reopen behavior into it.
- [x] 1.2 Remove occurrence persistence and recurrence-expansion dependencies from `TaskService`.
- [x] 1.3 Route date-range occurrence queries through `TaskOccurrenceService`.

## 2. Recurring-series service

- [x] 2.1 Create `RecurringTaskSeriesService` and move following-series update, replacement, and truncation behavior into it.
- [x] 2.2 Reuse authoritative task-definition mapping and occurrence validation without duplicating business rules.

## 3. Mutation boundary and tests

- [x] 3.1 Update `TaskMutationService` to delegate each explicit target operation to its owning service while preserving one change publication.
- [x] 3.2 Reassign and extend unit tests for task, occurrence, series, routing, and controller-query ownership.
- [x] 3.3 Run formatting, focused tests, the full backend suite, OpenSpec validation, and API contract checks.
