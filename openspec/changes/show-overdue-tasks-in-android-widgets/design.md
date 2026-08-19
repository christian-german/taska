## Context

The two Android widgets currently request bounded task ranges: the Today widget requests the device-local current date and the calendar-week widget requests Monday through Sunday. Their local filtering then excludes every task outside the requested range. The Week widget already renders a heterogeneous, scrollable collection of date headers and task rows; the Today widget uses its existing fixed task-row presentation.

An overdue task is an incomplete task with a non-null `scheduledAt` whose device-local scheduled date is earlier than the device-local current date. A `dueAt` value alone does not make a task eligible because both widgets are based on planned (`scheduledAt`) dates.

## Goals / Non-Goals

**Goals:**

- Retrieve and show overdue scheduled tasks in both widgets.
- Evaluate overdue status using the same device-local calendar semantics as the widgets' current ranges.
- Put every overdue row before non-overdue rows.
- Represent all overdue rows as one leading Week-widget group.
- Preserve exact task and recurring-occurrence actions.

**Non-Goals:**

- Include unscheduled or deadline-only tasks.
- Display completed overdue tasks.
- Change what counts as the current day or calendar week.
- Change task completion, reopening, navigation, refresh triggers, or non-widget task lists.
- Add age-based sections or one group per original overdue date.

## Decisions

### Fetch overdue tasks separately and combine them with each widget's current range

Request incomplete scheduled tasks ending on the local date immediately before today, then combine that result with the widget's existing current-range result. This avoids changing backend API semantics and permits the Today request to retain completed tasks for today without also retrieving completed historical tasks. De-duplicate combined representations by task identity and recurring-occurrence identity before rendering.

Alternative considered: extend each existing request's start date into the past. There is no fixed past boundary that represents all overdue tasks, and the Today widget would unnecessarily retrieve completed historical tasks.

### Classify overdue status by device-local scheduled date

Convert `scheduledAt` to the device's local date and compare it with the device-local current date. A task earlier than today is overdue; time-of-day does not make a task overdue while it remains on today's date. Completed tasks, unscheduled tasks, and deadline-only tasks are excluded from the overdue set.

Alternative considered: compare instants with the current clock time. That would classify earlier appointments today as overdue, contrary to the requested placement alongside date-grouped widget tasks and the widgets' established calendar-date behavior.

### Preserve chronological order inside the overdue section

Sort overdue rows by `scheduledAt` ascending, then append the widget's existing non-overdue ordering. The Week collection emits one `Overdue` header before the first overdue row and does not emit original-date headers between overdue rows. Normal current-week date headers follow afterward.

Alternative considered: show newest overdue tasks first. The request defines precedence over other tasks but not a reversal of the widgets' established chronological ordering.

### Keep widget-specific presentation behavior

The Week widget uses its scrollable collection and can expose all qualifying overdue and current-week tasks. The Today widget retains its existing row capacity and presentation; overdue tasks consume the leading available rows because they sort before today's tasks. Its status count describes the rendered scheduled-task selection consistently with existing behavior.

## Risks / Trade-offs

- [A large overdue backlog increases API and Week-widget collection size] → Reuse the supported task range endpoint and the Week widget's scrolling collection.
- [The Today widget can have more qualifying tasks than fixed rows] → Preserve its existing capacity and apply the required overdue-first ordering deterministically.
- [Two range responses could overlap because of server boundary semantics] → De-duplicate by task ID plus occurrence identity before filtering and rendering.
- [Time-zone boundaries can misclassify UTC timestamps] → Perform final classification from `scheduledAt` in the device time zone.

## Migration Plan

1. Add overdue retrieval and shared local-date classification.
2. Merge and de-duplicate overdue tasks with each widget's current-range tasks.
3. Add the Week collection's leading `Overdue` header and overdue-first ordering.
4. Verify both widgets' filtering, ordering, actions, and existing refresh behavior.
5. Roll back by restoring the original bounded requests and date-only Week grouping; no persisted-data migration is required.

## Open Questions

- None.
