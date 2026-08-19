## Context

The Android task model already exposes the `allDay` flag. The calendar-week widget and Today widget use different rendering paths, but both currently derive and display a clock time from `scheduledAt` for every task. For an all-day task, `scheduledAt` identifies its calendar placement; its time component is not a user-selected task time and must not be presented as one.

## Goals / Non-Goals

**Goals:**

- Make all-day tasks visibly date-based rather than time-specific in both Android widgets.
- Retain time text for tasks whose `allDay` flag is false.
- Preserve all existing widget selection, ordering, status, navigation, and completion behavior.

**Non-Goals:**

- Change the meaning or persistence of `allDay` or `scheduledAt`.
- Change date headers, widget titles, task titles, or non-widget task presentation.
- Infer all-day status from a timestamp value or time of day.

## Decisions

### Use the task's explicit all-day flag

Each widget rendering path will conditionally include a formatted time only when `allDay` is false. The explicit flag is the approved task-domain distinction and avoids treating genuine midnight tasks as all-day tasks.

Alternative considered: omit midnight times. This would hide valid times from timed tasks scheduled at midnight and would infer product meaning from an implementation timestamp.

### Apply the rule to both Android task widgets

The calendar-week and Today widgets will follow the same semantic formatting rule even though their row layouts differ. In the Week widget, the surrounding date header remains unchanged; in the Today widget, the existing row prefix and title treatment remain unchanged except for removal of the all-day task's clock time.

## Risks / Trade-offs

- [Removing the time leaves awkward spacing or separators] → Build each row's visible text from semantic components and add focused assertions for the complete all-day and timed labels.
- [Separate renderers drift] → Cover both Week and Today paths with tests for `allDay = true` and `allDay = false`.

## Migration Plan

1. Update both Android widget row formatters to conditionally include time text.
2. Add focused formatting/rendering tests for all-day and timed tasks in each widget.
3. Validate the Android checks and this OpenSpec change.

No data or API migration is required. Rollback restores unconditional time formatting.

## Open Questions

- None.
