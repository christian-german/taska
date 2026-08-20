## 1. Android task-detail presentation

- [ ] 1.1 Refactor Android task-detail date formatting so an absent `scheduledAt` displays no assigned value and cannot synthesize a time from `allDay`.
- [ ] 1.2 Render an assigned all-day schedule as a localized date only and an assigned timed schedule as a localized date and time.
- [ ] 1.3 Render an assigned Android task-detail deadline as a localized date only, without changing its stored `dueAt` value.

## 2. Regression coverage

- [ ] 2.1 Add Android presentation tests for absent schedules, all-day schedules, timed schedules, and date-only deadlines, including non-UTC device time zones.
- [ ] 2.2 Add or update Android view-model tests proving a successful complete schedule removal adopts a response with `scheduledAt: null` while preserving the independent deadline.
- [ ] 2.3 Verify existing schedule and deadline editing, recurrence targeting, and failure behavior remain unchanged.

## 3. Verification

- [ ] 3.1 Run relevant Android unit tests and static checks.
- [ ] 3.2 Run strict OpenSpec validation for this change.
