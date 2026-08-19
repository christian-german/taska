## 1. Weekly collection model

- [ ] 1.1 Build an ordered Week-widget item model that inserts one date header before each represented device-local scheduled date and retains chronological task order.
- [ ] 1.2 Remove the fixed eight-task truncation from the Week widget while preserving the Today widget's existing capacity and completed-task behavior.
- [ ] 1.3 Format Week date headers with the locale-aware abbreviated weekday and two-digit day/month, and format Week task rows with time and title only.

## 2. Weekly widget presentation and actions

- [ ] 2.1 Add a vertically scrollable `RemoteViews` collection for the Week widget with distinct centered date-header and compact task-row layouts that use the existing Taska visual tokens.
- [ ] 2.2 Bind every weekly task's existing detail-open and completion actions through collection-item intents, including recurring occurrence identity.
- [ ] 2.3 Refresh collection data after normal widget refresh triggers and task completion, while leaving the Today widget's layout and behavior unchanged.

## 3. Verification

- [ ] 3.1 Add unit tests for multi-day grouping, single-header same-day groups, local-date boundaries, locale-aware labels, chronological ordering, and weeks containing more than eight tasks.
- [ ] 3.2 Add resource or instrumentation tests for centered date headers, time-only Week task prefixes, vertical scrolling, task actions, and unchanged Today-widget rendering.
- [ ] 3.3 Run focused Android widget tests, the Android development build, and OpenSpec validation.
