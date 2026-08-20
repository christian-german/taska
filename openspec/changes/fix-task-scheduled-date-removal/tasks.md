## 1. Android task-detail schedule removal

- [x] 1.1 Make the existing Android task-detail complete-removal action persist `scheduledAt: null` and adopt the successful update response.
- [x] 1.2 Preserve `dueAt`, unrelated task values, and the existing recurrence targeting behavior during complete schedule removal.
- [x] 1.3 Add Android tests covering scheduled tasks with and without deadlines, successful visible removal, preserved fields, recurrence behavior, and update failure.

## 2. Web and Tauri task-detail schedule removal

- [x] 2.1 Make the shared Angular task-detail complete-removal affordance explicit and available whenever `scheduledAt` is assigned.
- [x] 2.2 Persist `scheduledAt: null`, preserve `dueAt` and unrelated task values, retain existing recurrence targeting behavior, and adopt only a successful update response.
- [x] 2.3 Keep the date-time picker's time-clear action distinct and accessible, retaining the scheduled date as an all-day schedule.
- [x] 2.4 Add Angular tests covering scheduled tasks with and without deadlines, successful visible removal, preserved fields, recurrence behavior, time-only clearing, and update failure.

## 3. Verification

- [x] 3.1 Run relevant Android unit tests and static checks.
- [x] 3.2 Run relevant Angular tests, static checks, and browser/Tauri build checks.
- [x] 3.3 Run strict OpenSpec validation for this change.
