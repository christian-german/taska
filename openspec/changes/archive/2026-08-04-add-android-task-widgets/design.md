## Context

The Android client is a native Compose application with an authenticated Retrofit task client, Firebase Cloud Messaging (FCM), and existing day/week task screens. The task API already accepts `from` and `to` local dates and returns expanded scheduled occurrences in that range. It does not expose an Android launcher widget, and its current FCM device tokens are not linked to the authenticated account.

Widgets need a fast, compact view of the current calendar week and an action that remains correct for recurring occurrences. They must also update after a task is changed from the web without relying solely on Android's deferred background scheduling.

## Goals / Non-Goals

**Goals:**

- Provide a resizable Android launcher widget for incomplete, scheduled tasks in the current Monday-through-Sunday week.
- Allow one-tap completion of a task or a specific recurring occurrence.
- Keep widgets current after local task changes, day/week transitions, and account-relevant backend changes.
- Deliver task-change push signals only to devices registered by the authenticated account.

**Non-Goals:**

- Display inbox tasks, deadline-only tasks, unscheduled tasks, completed tasks, or tasks outside the current week.
- Create or edit tasks from the widget.
- Guarantee instantaneous delivery while a device has no network connection or FCM delivery is unavailable.
- Rework general task ownership or add cross-account task sharing.

## Decisions

### Use a single responsive calendar-week widget

The widget represents the current ISO-style calendar week (Monday through Sunday) and resizes its presentation: compact sizes show a limited ordered list, while larger sizes show more tasks and date grouping. It requests `GET /tasks?from=<monday>&to=<sunday>` and accepts only rows with a non-null `scheduledAt` and incomplete state.

This matches the existing Week screen and API range semantics, including recurring occurrence expansion. Separate Today, Week, and Upcoming widget providers were considered, but a single responsive week widget has a simpler launcher experience and matches the agreed scope.

### Build a native widget surface with an explicit refresh coordinator

The implementation will add an `AppWidgetProvider`-based widget surface and a shared refresh coordinator. The coordinator obtains the authenticated API client, fetches the current week's data, transforms it into widget display data, and updates all widget instances.

The existing Compose screens cannot be embedded as home-screen widgets. Glance was considered, but standard app-widget components keep the initial dependency and platform surface small while supporting the required list and pending-intent actions.

### Complete tasks through a widget broadcast action

Each checkbox sends a uniquely identified broadcast containing the task ID and, for recurring occurrences, `occurrenceScheduledAt`. The receiver invokes the existing close-task API, refreshes every widget on success, and exposes an error-safe state on failure. Task rows remain navigation actions that open the matching task detail.

Using the existing close endpoint preserves its recurrence semantics. Optimistic completion without server confirmation was rejected because a failed or unauthenticated request could make the widget misleading.

### Combine FCM invalidation with scheduled fallback refreshes

The backend publishes an opaque `tasks_changed` FCM data message after successful task creates, updates, deletes, closes, and reopens. The Android FCM service delegates that message to the widget refresh coordinator; no task title, description, or identifiers are carried in the push event.

The widget also refreshes after an in-widget completion, when Taska resumes after a local mutation, at the next local calendar-week boundary, and through periodic work as a fallback. FCM is the timely cross-client mechanism; periodic work handles missed pushes and date rollovers.

### Scope device tokens and push delivery to the authenticated account

Device registration derives an account subject from the authenticated JWT and persists it with the token. Re-registration updates the account association. Task-change events target tokens for the account that performed the authenticated mutation, never all registered tokens. Invalid or unregistered FCM tokens are removed or ignored according to Firebase delivery results.

The existing global token registry was rejected because it can deliver another account's event to an unrelated device. An opaque event limits payload exposure, while account-scoped targeting establishes the required delivery boundary.

## Risks / Trade-offs

- [Android may defer periodic work and FCM is not guaranteed during offline/device-restricted periods] → Refresh on every practical local trigger and on the next scheduled fallback; render a last-updated/error state without claiming real-time guarantees.
- [A widget receiver runs outside a visible Activity] → Initialize the authenticated API client from application context and handle absent/expired accounts without making a network action appear completed.
- [Recurring occurrences require their original occurrence timestamp] → Include `occurrenceScheduledAt` in the widget action identity and use it in the existing close request.
- [Calendar boundaries can differ across time zones] → Compute the widget's displayed week in the device's local zone and rely on the backend's configured planning-calendar range interpretation; validate this boundary with integration tests.
- [FCM token lifecycle is unreliable] → Register on token creation and app startup, update the stored owner on re-registration, and remove tokens rejected by FCM.

## Migration Plan

1. Add nullable account-subject storage to device tokens and backfill no existing owner; legacy unowned tokens receive no task-change events.
2. Deploy account-aware registration and mutation-triggered opaque FCM events.
3. Release the Android widget with local and FCM-driven refreshes.
4. Roll back by disabling task-change publication; widgets retain local and periodic refresh behavior. The token ownership column is backward-compatible.

## Open Questions

- None for the initial scope. The first implementation will use the existing single-task close semantics and current task authorization model.
