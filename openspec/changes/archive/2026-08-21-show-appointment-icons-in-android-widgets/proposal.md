## Why

The Android Today and calendar-week widgets currently render appointments and to-dos identically, even though the rest of the Android application uses an outlined calendar icon to distinguish appointments. Users therefore cannot identify an appointment while scanning either home-screen widget.

## What Changes

- Show the Android application's existing outlined calendar appointment icon in each widget row whose task type is `APPOINTMENT`.
- Give the widget appointment icon an accessible Appointment description so the distinction is not visual-only.
- Keep `TODO` widget rows free of the appointment indicator and preserve all existing widget filtering, grouping, completion, navigation, and refresh behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Appointment rows in the calendar-week widget will use the application's appointment icon and accessible label.
- `android-today-task-widget`: Appointment rows in the Today widget will use the application's appointment icon and accessible label.

## Impact

- Android widget row layouts, `RemoteViews` binding, widget resources, and focused widget tests.
- No API, persistence, task-type, scheduling, completion, navigation, or refresh changes.
