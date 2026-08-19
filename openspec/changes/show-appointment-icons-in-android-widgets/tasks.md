## 1. Shared appointment indicator

- [x] 1.1 Add a `RemoteViews`-compatible outlined calendar drawable matching the Android application's existing appointment icon and a localized Appointment content description.
- [x] 1.2 Add a compact appointment-icon slot to the reusable Today rows and the calendar-week collection task row without changing completion or task-open targets.

## 2. Widget binding

- [x] 2.1 Bind the calendar-week row's appointment icon and accessible description only for tasks whose type is `APPOINTMENT`.
- [x] 2.2 Bind the Today row's appointment icon and accessible description only for tasks whose type is `APPOINTMENT`, including completed and recurring task representations.
- [x] 2.3 Preserve the standard icon-free presentation for `TODO`, missing, and other non-appointment type values.

## 3. Verification

- [x] 3.1 Add focused tests that verify appointment-icon visibility, accessible description, and icon consistency for both Android widget render paths.
- [x] 3.2 Add focused tests that verify to-do rows do not expose the appointment icon or Appointment accessibility description.
- [ ] 3.3 Run the relevant Android widget tests, Android development build, and OpenSpec validation.
