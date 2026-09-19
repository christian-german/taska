## 1. Application Invariant

- [x] 1.1 Add one authoritative task-definition rule requiring a non-null schedule for recurring definitions.
- [x] 1.2 Apply the rule to creation, effective partial updates, complete replacement, and following-series replacement before any write-side effect.
- [x] 1.3 Add unit tests covering rejected recurring creation and mutations plus valid unscheduled non-recurring tasks.

## 2. Persistence And Contract

- [x] 2.1 Add a Flyway migration that converts unscheduled recurring rows to non-recurring tasks, clears recurrence metadata, and adds the recurring-schedule check constraint.
- [x] 2.2 Add persistence tests proving recurring rows require `scheduled_at` while non-recurring rows do not.
- [x] 2.3 Update the OpenAPI creation and replacement schemas to express the conditional non-null `scheduledAt` requirement.

## 3. Verification

- [x] 3.1 Run backend formatting and the complete backend test suite.
- [x] 3.2 Run relevant OpenAPI contract verification, strict OpenSpec validation, stale-reference scans, and repository diff checks.

Verification note: the focused frontend/Prism contract suite passes. The global positive-mode
Schemathesis run still reports 13 existing contract mismatches across unrelated endpoints and input
constraints; the recurring-schedule condition itself generated only requests containing a schedule.
