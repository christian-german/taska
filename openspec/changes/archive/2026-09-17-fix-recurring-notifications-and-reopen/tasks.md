## 1. Recurring occurrence notification state

- [x] 1.1 Add a migration and notification-owned persistence model with a unique `(taskId, occurrenceScheduledAt)` delivery marker.
- [x] 1.2 Add repository tests covering unique occurrence claims and marker removal.

## 2. Notification candidate scheduling

- [x] 2.1 Introduce a common notification candidate model for non-recurring tasks and resolved recurring occurrences.
- [x] 2.2 Discover eligible recurring occurrences in the upcoming scheduler window, including moved occurrences and excluding completed, skipped, and all-day occurrences.
- [x] 2.3 Refactor the scheduler to claim and dispatch each recurring occurrence once without changing occurrence business state, while preserving non-recurring behavior.
- [x] 2.4 Clear an occurrence notification marker only when its effective schedule changes.
- [x] 2.5 Add scheduler and service tests for virtual, modified, completed, skipped, all-day, duplicate, concurrent-claim, no-token, and rescheduled occurrences.

## 3. Occurrence completion and reopening

- [x] 3.1 Make recurring close and reopen reject missing or invalid occurrence identity without mutating the series.
- [x] 3.2 Preserve occurrence overrides when completing and reopening a modified occurrence.
- [x] 3.3 Restore an unmodified completed occurrence to virtual state and reject reopening an occurrence that is not completed.
- [x] 3.4 Add service and adapter tests for recurring close/reopen invariants and preserved non-recurring behavior.

## 4. Verification

- [x] 4.1 Run the Java formatter, focused backend tests, and the full backend test suite.
- [x] 4.2 Validate the OpenSpec change and review the final diff for unintended API or client changes.
