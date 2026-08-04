## Why

Taska users cannot see their planned work from the Android launcher, and a widget can become stale when tasks are created or changed from another client. A calendar-week widget with secure push-driven refresh makes the current schedule visible and actionable without opening the app.

## What Changes

- Add a resizable Android home-screen widget that displays only incomplete tasks with a `scheduledAt` value in the current Monday-through-Sunday calendar week.
- Allow a user to complete an individual task or recurring occurrence from the widget with one tap, and open its details by tapping the task row.
- Refresh widget content after local mutations, at calendar boundaries, periodically as a fallback, and when the backend signals a task change.
- Add account-scoped device registration and task-change push messages so a task changed through the web or another client refreshes only that account's Android widgets.

## Capabilities

### New Capabilities

- `android-task-widgets`: Android launcher widgets that show calendar-week scheduled tasks and support one-tap completion.
- `device-scoped-task-sync`: Secure, account-targeted task-change push events that cause Android widgets to refresh.

### Modified Capabilities

- None.

## Impact

- Affects the Android app's manifest, widget UI/data refresh path, task API client, and Firebase message handling.
- Affects backend device-token persistence, registration, task mutation flows, and Firebase Cloud Messaging delivery.
- Adds Android widget dependencies and requires widget and backend integration tests.
