## 1. Data and backend contract

- [x] 1.1 Add a versioned Flyway migration that converts persisted task types from `MEETING` to `APPOINTMENT` while preserving all other task data.
- [x] 1.2 Replace the backend `MEETING` enum value, domain terminology, DTO/request handling, and API/MCP task-type representations with `APPOINTMENT`.
- [x] 1.3 Update task-priority evaluation eligibility, invalidation, and stale-result handling to recognize `APPOINTMENT` as ineligible.
- [x] 1.4 Update backend unit and integration tests for appointment creation, retrieval, mutation, persistence migration, and priority evaluation.

## 2. Client terminology and behavior

- [x] 2.1 Replace the web `TaskType` contract and all `MEETING` comparisons with `APPOINTMENT`, including task detail, quick-add, task rows, and time tracker presentation.
- [x] 2.2 Update web task-type labels from French “Réunion” to “Rendez-vous” and update relevant web tests.
- [x] 2.3 Replace Android task-type values, helper names, selection toggles, and appointment-specific presentation checks with `APPOINTMENT` terminology.
- [x] 2.4 Update Android visible and accessible labels from “Réunion” to “Rendez-vous”, preserving the existing appointment indicator/icon across Today, daily, weekly, and detail views.
- [x] 2.5 Update Android model and UI tests for appointment payloads, labels, and presentation behavior.

## 3. Verification and documentation

- [x] 3.1 Update affected OpenSpec requirements and project documentation to use appointment terminology.
- [x] 3.2 Run the backend test suite, the relevant Android tests, and the web test/build checks.
- [x] 3.3 Search production code, tests, and user-facing strings to confirm no `MEETING` value or meeting terminology remains except the one-time database migration.
