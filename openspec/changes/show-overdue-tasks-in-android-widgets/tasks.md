## 1. Overdue task retrieval and classification

- [x] 1.1 Retrieve incomplete scheduled tasks through the local date before today for installed Android task widgets, while retaining each widget's existing current-range request.
- [x] 1.2 Combine and de-duplicate overdue and current-range task representations by task and recurring-occurrence identity.
- [x] 1.3 Classify overdue tasks from `scheduledAt` in the device time zone, excluding completed, unscheduled, and deadline-only tasks.

## 2. Widget ordering and presentation

- [x] 2.1 Order qualifying overdue tasks chronologically before the Today widget's tasks planned for the current local date, preserving its existing row presentation and capacity.
- [x] 2.2 Add one leading `Overdue` header to the Week widget when overdue tasks exist, place every overdue row beneath it without original-date headers, and retain normal date groups afterward.
- [x] 2.3 Preserve completion and detail actions, including recurring-occurrence identity, and keep existing refresh triggers and visual tokens unchanged.

## 3. Verification

- [x] 3.1 Add unit tests for local-date overdue boundaries, exclusion rules, chronological overdue-first ordering, response de-duplication, and recurring occurrences.
- [x] 3.2 Add Week collection tests for a single leading `Overdue` group, no empty overdue header, no original-date headers inside that group, and normal current-week groups after it.
- [x] 3.3 Add Today widget tests for overdue-first selection within its existing capacity and unchanged completed-today rendering and actions.
- [ ] 3.4 Run focused Android widget tests, the Android development build, and OpenSpec validation.
