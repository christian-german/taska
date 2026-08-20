## 1. Completion action identity and presentation

- [x] 1.1 Include the initiating widget instance and task-row control identity in incomplete-task completion actions for both the calendar-week and Today widgets, while retaining task and recurring-occurrence identity.
- [x] 1.2 Add a `RemoteViews`-compatible update that checks only the invoked completion control immediately, without changing row visibility, title styling, grouping, ordering, status, or cached task data.

## 2. Request reconciliation

- [x] 2.1 Keep the optimistic checked state while the completion request is pending and retain the existing authoritative widget refresh after success.
- [x] 2.2 Restore the invoked completion control to unchecked when initialization, authentication, transport, or API completion fails, without requiring a network refresh or discarding the displayed tasks.
- [x] 2.3 Preserve completed-task reopening in the Today widget, task-detail navigation, recurrence targeting, and all non-widget task behavior.

## 3. Verification

- [x] 3.1 Add focused calendar-week widget tests proving immediate checked feedback, successful refresh reconciliation, failed-request rollback, and precise recurring-occurrence targeting.
- [x] 3.2 Add focused Today-widget tests proving the same incomplete-task completion behavior and unchanged completed-task reopen behavior.
- [x] 3.3 Run focused Android widget tests, Android development build/static checks, and strict OpenSpec validation.
