## Context

Taska currently exposes one `AppWidgetProvider`: a responsive calendar-week widget. Its shared refresh coordinator queries the authenticated task API, renders RemoteViews, responds to task-change events, and schedules a refresh at the next week boundary with periodic fallback. The new provider must look like that widget while presenting the device-local current day and retaining completed tasks as visibly completed rows.

## Goals / Non-Goals

**Goals:**

- Provide an independently placeable, resizable Today app widget with the same Taska card, colors, typography, task-row treatment, and navigation as the calendar-week widget.
- Query and display all tasks planned for the current local date, including completed tasks and recurring occurrences returned for that date.
- Render completed tasks using strikethrough text and a checked circular control that reopens them; incomplete tasks retain the existing completion action.
- Refresh both widget types after relevant local mutations, push events, periodic fallback, and the next local day boundary.

**Non-Goals:**

- Change the existing week widget's task-selection semantics or make it show completed tasks.
- Add task creation, editing, filtering, or a new backend endpoint.

## Decisions

### Register a separate Today provider and metadata resource

Add a second `AppWidgetProvider` and its own `appwidget-provider` XML metadata so launchers expose separate Week and Today entries. This keeps independent widget instances and labels clear to users while allowing both providers to share refresh and rendering infrastructure.

Alternative considered: change the current widget's mode through configuration. This adds a configuration activity and makes the launcher choice less discoverable, without a need for per-instance customization.

### Generalize the refresh coordinator around widget definitions

Refactor the current refresh path so each provider supplies its component, local-date query range, header/status wording, and completed-row policy. One coordinated refresh invocation will enumerate IDs for each installed provider and fetch/render the appropriate range, avoiding duplicated authentication, error handling, task intents, and state views. The Today definition queries `from` and `to` as the same device-local current date; the Week definition keeps its existing Monday-through-Sunday range.

Alternative considered: duplicate the existing coordinator for Today. That would make refresh triggers and visual behavior drift over time.

### Toggle completion from the Today widget

Today rendering receives both complete and incomplete planned tasks. Completed rows use the existing circular affordance in a checked state and strike their title. The checked control sends a uniquely identified reopen broadcast containing the task ID and, for a recurring occurrence, its `occurrenceScheduledAt`; incomplete rows continue to send the existing completion broadcast. The shared receiver invokes the matching authenticated close or reopen API and refreshes all installed widget types only after success. Row and title taps remain task-detail navigation actions.

Alternative considered: make completed rows presentation-only. That makes the checked control look interactive while forcing users into task details for a basic correction.

### Refresh at the next local day boundary

Replace or extend the boundary scheduler so it runs at the next local midnight; that refreshes the Today widget immediately and also yields correct calendar-week data when Monday begins. Keep task-change, local-mutation, widget-lifecycle, and periodic fallback triggers shared across both providers.

Alternative considered: rely only on periodic work for Today. It can leave yesterday's content visible after midnight for an unacceptable interval.

## Risks / Trade-offs

- [Two providers add rendering and manifest surface] → Centralize shared RemoteViews resources and provider configuration; cover each provider's metadata and rendering with focused tests.
- [Completed tasks can increase Today list density] → Preserve existing bounded row capacity and deterministic ordering so small widget sizes remain legible.
- [Date boundaries vary by device time zone and daylight-saving transition] → Calculate range and next refresh in the device's local time zone using the existing date/time abstractions, and test midnight and Monday transitions.
- [An API response may not explicitly contain completed tasks in the requested range] → Verify the existing task query contract in integration tests and adjust only the client-side filtering for the Today provider.
- [A reopen request can fail after the widget is tapped] → Do not optimistically change the checked state; retain the completed rendering and expose a subsequent refresh path.

## Migration Plan

1. Ship the new provider alongside the existing Week provider; no data migration or widget replacement is needed.
2. Existing Week widget instances retain their provider and behavior; users add the Today widget independently from the launcher.
3. Roll back by removing the Today provider registration and its shared-definition entry. Existing Week widgets and backend APIs continue unaffected.

## Open Questions

- None; the existing task query is expected to return completed tasks for a date range, which implementation tests will confirm.
