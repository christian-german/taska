## 1. Android task-detail deadline removal

- [ ] 1.1 Update the Android task-detail deadline-removal action to persist `dueAt: null` rather than clearing calendar scheduling fields.
- [ ] 1.2 Preserve the task's current `scheduledAt` and `allDay` values during deadline removal and refresh the detail state from the successful update response.
- [ ] 1.3 Add Android tests covering a task with both timestamps, a deadline-only task, the successful visible state, and update failure behavior.

## 2. Web and Tauri task-detail deadline removal

- [ ] 2.1 Add a dedicated full-deadline removal affordance to the shared Angular task-detail view whenever `dueAt` is assigned.
- [ ] 2.2 Persist `dueAt: null`, preserve `scheduledAt` and `allDay`, and update the visible detail state only after a successful response.
- [ ] 2.3 Keep the date-time picker's time-clear behavior distinct from complete deadline removal.
- [ ] 2.4 Add Angular tests covering tasks with both timestamps, deadline-only tasks, successful visible removal, preserved scheduling values, and update failure behavior.

## 3. Verification

- [ ] 3.1 Run relevant Android unit tests and static checks.
- [ ] 3.2 Run relevant Angular tests, static checks, and browser/Tauri build checks.
- [ ] 3.3 Run strict OpenSpec validation for this change.
