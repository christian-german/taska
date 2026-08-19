## Context

The shared Android widget renderer currently binds at most eight tasks into fixed rows. A week task row combines weekday, time, and title, while the Today widget uses the same row layout. The selected product direction is to give the calendar-week widget a centered header for each represented date, reduce its task rows to time and title, and keep all weekly tasks accessible even when the content is taller than the widget.

Android home-screen widgets implement scrolling content through a `RemoteViews` collection rather than an ordinary application `ScrollView`. The collection must retain the existing per-task open and completion actions and must refresh its data alongside the widget.

## Goals / Non-Goals

**Goals:**

- Make boundaries between represented days visually explicit in the calendar-week widget.
- Format each day header as an abbreviated weekday followed by numeric day and month, using the device locale and local time zone.
- Keep all current-week tasks accessible by vertical scrolling when they do not fit at once.
- Preserve chronological ordering and existing task actions.
- Keep the Today widget unchanged.

**Non-Goals:**

- Add empty headers for days that have no scheduled tasks.
- Change which tasks belong to the current week or how recurring occurrences are obtained.
- Change task completion, task-detail navigation, refresh triggers, or Taska visual-system requirements.
- Add horizontal scrolling or pagination.

## Decisions

### Model the week body as a heterogeneous collection

Represent the calendar-week body as an ordered sequence of date-header items and task-row items. Emit one header immediately before the first task for each represented local date. This directly expresses the requested grouping and permits collection-backed vertical scrolling without a fixed row cap.

Alternative considered: continue binding a fixed set of predeclared views. That cannot guarantee access to every weekly task and makes the number of headers compete with the existing eight-row limit.

### Derive grouping and labels in device-local calendar terms

Group by the local date derived from each task's `scheduledAt`, matching the current week-range semantics. Render headers with the device locale's abbreviated weekday plus two-digit day and month (the locale-equivalent pattern of `Wed 17/06`). Task rows render scheduled time and title without repeating the weekday.

Alternative considered: group by the UTC date encoded in the API timestamp. That can place tasks under the wrong day near midnight and would contradict the device-local week definition.

### Scope collection rendering to the Week widget

Use a Week-specific body/layout or rendering path so the Today widget retains its current fixed-row appearance and completed-task presentation. Shared visual resources and task-action construction may remain shared internal details.

Alternative considered: apply date headers to both widgets. A date header is redundant in a widget already titled “Today” and was not requested.

## Risks / Trade-offs

- [Launcher collection behavior varies] → Use Android's supported app-widget collection APIs and cover service/provider metadata and refresh notifications with focused tests.
- [Headers consume vertical space] → Keep header styling compact and rely on vertical scrolling rather than dropping tasks.
- [Locale-specific text may be wider than the example] → Use locale-aware abbreviated weekdays, centered single-line labels, and bounded text treatment.
- [Collection pending-intent handling differs from fixed rows] → Preserve unique task and occurrence identity in fill-in intents and test open/completion actions for ordinary and recurring tasks.

## Migration Plan

1. Introduce Week-specific collection item layouts and collection service/factory data.
2. Bind grouped local-date headers and task rows without truncating the filtered week data.
3. Preserve task-open and completion actions and notify collection data changes during refresh.
4. Add focused unit, resource, and instrumentation coverage for grouping, formatting, scrolling, actions, and Today-widget isolation.
5. Roll back by restoring fixed Week rows; no persisted data or API migration is required.

## Open Questions

- None.
