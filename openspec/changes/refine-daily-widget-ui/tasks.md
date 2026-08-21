## 1. Widget chrome

- [ ] 1.1 Remove successful-refresh scheduled-task count text from the Week and Today widgets while preserving refresh error feedback.

## 2. Today overdue grouping

- [ ] 2.1 Show the established `Overdue` label before Today-widget overdue rows and hide it when no overdue row is displayed.
- [ ] 2.2 Remove separators between Today task rows and show one separator only between overdue and current-day groups when both groups are present.
- [ ] 2.3 Preserve Today task selection, overdue classification, overdue-first ordering, capacity, text styling, appointment indicators, completion/detail actions, and refresh behavior.

## 3. Verification

- [ ] 3.1 Add focused tests for hidden count status, retained error status, conditional overdue labeling, and the single group-boundary separator.
- [ ] 3.2 Run focused Android widget tests, the Android development build/static checks, and strict OpenSpec validation.
