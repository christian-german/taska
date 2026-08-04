## 1. Account-scoped device synchronization

- [x] 1.1 Add a database migration and persistence fields that associate each Firebase device token with its authenticated account subject.
- [x] 1.2 Update device registration to require authentication and upsert the token's account association from the JWT subject.
- [x] 1.3 Add a backend task-change publisher that emits an opaque `tasks_changed` FCM data message after successful authenticated task creation, update, deletion, completion, and reopening.
- [x] 1.4 Target task-change messages only to tokens owned by the mutating account and retire permanently invalid Firebase tokens.
- [ ] 1.5 Add backend tests for account-scoped registration, mutation event coverage, payload opacity, cross-account isolation, and invalid-token handling.

## 2. Android widget data and actions

- [x] 2.1 Add the Android app-widget provider, provider metadata, launcher resources, and any required widget dependencies.
- [x] 2.2 Implement a widget refresh coordinator that fetches the current local Monday-through-Sunday range through the authenticated task API and updates every widget instance.
- [x] 2.3 Transform widget data to include only incomplete tasks with non-null `scheduledAt` inside the current week, including expanded recurring occurrences.
- [x] 2.4 Implement widget task-row navigation to the matching task detail and a uniquely identified one-tap completion broadcast action.
- [x] 2.5 Implement the completion receiver using the existing close-task API, including `occurrenceScheduledAt` for recurring occurrences, and refresh widgets only after success.
- [x] 2.6 Handle missing authentication, API failures, and empty weekly schedules without incorrectly rendering a task as completed.

## 3. Refresh orchestration

- [x] 3.1 Add refresh triggers for successful local task mutations, widget completion, widget lifecycle updates, and the next local calendar-week transition.
- [x] 3.2 Add periodic fallback work that refreshes all installed widget instances within Android background-execution constraints.
- [x] 3.3 Extend Firebase message handling to recognize the opaque `tasks_changed` event and invoke widget refresh without displaying a notification.
- [x] 3.4 Register refreshed Firebase tokens after token rotation and application startup so account-scoped sync remains available.

## 4. Verification

- [x] 4.1 Add Android unit tests for calendar-week range calculation, scheduled-only filtering, task/occurrence completion request construction, and push-event dispatch.
- [x] 4.2 Add Android widget/instrumentation tests for rendering, row navigation, direct completion, refresh behavior, and error states.
- [x] 4.3 Run the focused Android and backend test suites, then build the Android development variant.
