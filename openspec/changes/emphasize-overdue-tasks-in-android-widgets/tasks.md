## 1. Shared overdue presentation

- [ ] 1.1 Add semantic, legible overdue-red widget colors for light and dark themes.
- [ ] 1.2 Provide a `RemoteViews`-compatible way to apply both the overdue color and bold weight to widget text without changing the text content.

## 2. Widget rendering

- [ ] 2.1 Style the Week widget's `Overdue` header and every task row in that group with bold overdue-red text, while leaving ordinary date headers and non-overdue task rows unchanged.
- [ ] 2.2 Style every overdue Today-widget task row with bold overdue-red text, while leaving today's task rows and completed-today presentation unchanged.
- [ ] 2.3 Preserve task selection, overdue classification, ordering, grouping, capacity, time/title formatting, appointment indicators, completion and detail actions, and refresh behavior.

## 3. Verification

- [ ] 3.1 Add focused Week-widget tests for the overdue header and overdue task text color/weight, plus unchanged ordinary date-header and non-overdue row styles.
- [ ] 3.2 Add focused Today-widget tests for overdue task text color/weight, plus unchanged today and completed-today row styles.
- [ ] 3.3 Verify the overdue text remains legible in both light and dark widget themes.
- [ ] 3.4 Run focused Android widget tests, the Android development build/static checks, and strict OpenSpec validation.
