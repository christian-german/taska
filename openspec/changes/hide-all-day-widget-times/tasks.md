## 1. Widget row formatting

- [ ] 1.1 Update calendar-week widget task rows to omit clock time when `allDay` is true and retain localized clock time when `allDay` is false.
- [ ] 1.2 Update Today widget task rows to omit clock time when `allDay` is true and retain the existing clock-time presentation when `allDay` is false.
- [ ] 1.3 Preserve task titles, date context, ordering, completion state and actions, recurring-occurrence identity, and task-detail navigation in both widgets.

## 2. Verification

- [ ] 2.1 Add focused calendar-week widget tests covering all-day and timed task row text without stray time separators or spacing.
- [ ] 2.2 Add focused Today widget tests covering all-day and timed task row text while preserving existing completed-task styling and actions.
- [ ] 2.3 Run relevant Android widget tests, the Android development build/static checks, and strict OpenSpec validation.
