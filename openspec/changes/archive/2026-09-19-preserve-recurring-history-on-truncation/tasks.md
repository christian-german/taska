## 1. Recurring definition integrity

- [x] 1.1 Enforce the non-blank recurrence-rule invariant across task creation, partial update, complete replacement, successor creation, and database persistence.
- [x] 1.2 Migrate existing ruleless recurring rows to non-recurring tasks and clear their recurrence end.
- [x] 1.3 Isolate unusable recurrence rules per series during notification candidate expansion.
- [x] 1.4 Cover application validation, persistence constraints, and notification isolation with automated tests.

## 2. Detached occurrence history

- [x] 2.1 Persist detached occurrence classification and migrate existing orphaned DONE and MODIFIED state while removing orphaned SKIPPED state.
- [x] 2.2 Apply the same state detachment behavior to partial split, complete following replacement, and following deletion.
- [x] 2.3 Query detached state independently by effective date and bound attached moved-state overlays to the active series span.
- [x] 2.4 Preserve supplied successor scheduling independently from the cut identity.
- [x] 2.5 Expose detached status through OpenAPI, HTTP DTOs, and MCP-owned task outputs.
- [x] 2.6 Cover detachment, migration invariants, query results, successor scheduling, and transport mappings with automated tests.

## 3. Verification

- [x] 3.1 Run the backend test suite against the implemented behavior.
- [x] 3.2 Validate this change and the complete OpenSpec repository, then run repository diff checks.
