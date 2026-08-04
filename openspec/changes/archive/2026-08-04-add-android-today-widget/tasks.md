## 1. Provider and presentation setup

- [x] 1.1 Add the Today app-widget provider, launcher label, manifest declaration, and provider metadata while retaining the existing Week provider.
- [x] 1.2 Extract or extend shared widget layout/resources so Today uses the existing Taska rounded card, light/dark colors, typography, row spacing, and circular completion controls.
- [x] 1.3 Add completed-row styling with a checked control and struck-through title, preserving task-detail navigation for every Today row.

## 2. Data and refresh behavior

- [x] 2.1 Generalize widget refresh orchestration to enumerate and render each installed Week and Today widget provider independently.
- [x] 2.2 Implement the Today local-date query and filtering so it includes completed and incomplete scheduled tasks plus recurring occurrences, while excluding tasks outside today and unscheduled/deadline-only tasks.
- [x] 2.3 Attach one-tap completion broadcasts only to incomplete Today rows, using the existing task and occurrence identity contract.
- [x] 2.4 Update boundary scheduling to refresh at the next device-local day transition, retaining local-mutation, push-event, lifecycle, and periodic fallback refreshes for both providers.
- [x] 2.5 Add a uniquely identified reopen broadcast for checked Today rows that invokes the existing reopen API with the task and occurrence identity, refreshing widgets only after success.

## 3. Verification

- [x] 3.1 Add unit tests for Today date-range selection, completed/incomplete filtering, row state, and completion intent behavior.
- [x] 3.2 Add Android/instrumentation tests for the Today provider registration, Taska visual parity, checked/struck completed rows, task navigation, and incomplete-task completion.
- [x] 3.3 Test refresh behavior across a local midnight and Monday boundary, push/local mutation triggers, and no-installed-Today-widget state.
- [x] 3.4 Run focused Android widget tests and build the Android development variant.
- [x] 3.5 Add focused tests for reopening completed non-recurring and recurring Today rows, including rejected or unauthenticated reopen behavior.
