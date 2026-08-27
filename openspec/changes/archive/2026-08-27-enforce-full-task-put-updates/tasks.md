## 1. Backend replacement contract

- [x] 1.1 Replace the partial-update `TaskRequest` use at the task update boundary with a typed complete `TaskUpdateRequest` that contains every mutable base-task property and validates required field presence.
- [x] 1.2 Implement full replacement for `PUT /tasks/{id}`, including explicit nullable replacements and removal of `JsonNode` field-presence handling.
- [x] 1.3 Add the following-series endpoint that validates the target occurrence, ends the original series, and creates the replacement series entirely from `TaskUpdateRequest`.
- [x] 1.4 Add the single-occurrence endpoint and narrow `OccurrenceUpdateRequest` for title, priority, schedule, and deadline overrides only.
- [x] 1.5 Preserve planning-calendar validation, notification-reset behavior, task-change publication, authorization, and existing response representations for each applicable update operation.

## 2. Client migration

- [x] 2.1 Update the web task service and every editing caller to construct and submit a complete mutable task representation for base-task and following-series replacements.
- [x] 2.2 Update web recurring-task interactions to use the occurrence-specific endpoint for single-occurrence edits and prevent unsupported occurrence fields from being submitted.
- [x] 2.3 Extend Android request models and Retrofit APIs for complete base/following replacements and occurrence-specific updates.
- [x] 2.4 Update Android task-detail, day, week, and snooze flows to retain complete task state and call the correct endpoint for each recurrence scope.

## 3. Verification

- [x] 3.1 Add backend controller and service tests for complete replacement, rejected partial payloads, explicit null priority/schedule/deadline clearing, and preservation of output-only fields.
- [x] 3.2 Add backend recurrence tests for following-series replacement of every mutable property and isolated supported single-occurrence overrides.
- [x] 3.3 Add web tests for complete replacement payload construction, schedule removal, and occurrence endpoint selection.
- [x] 3.4 Add Android tests for complete replacement payload construction, nullable clears, and recurrence endpoint selection.
- [x] 3.5 Run relevant backend, web, and Android test suites plus strict OpenSpec validation.
