## Context

Both widgets currently bind a count-derived message into a status view. The Today widget renders up to eight fixed rows; seven rows contain a divider regardless of whether adjacent tasks belong to the overdue or current-day group. Overdue tasks are already ordered before today's tasks and use the established overdue classification.

## Goals / Non-Goals

**Goals:**
- Reclaim widget space used by routine task counts.
- Make the Today widget's overdue group explicit and consistent with the Week widget.
- Retain exactly one visual boundary when both overdue and current-day tasks are shown.

**Non-Goals:**
- Change empty/error behavior beyond removing count-derived status text.
- Change overdue classification, task ordering, or the eight-row Today capacity.
- Change the Week widget's existing collection grouping.

## Decisions

### Keep the status view for errors only

The renderers will hide the status view after a successful refresh and show it when refresh fails. This removes all scheduled-task count messages without discarding useful operational feedback.

### Derive Today grouping from task dates

The Today renderer will use the existing overdue predicate. It will show the established localized `Overdue` label when any rendered row is overdue. Since filtering already orders overdue rows first, the renderer can place the sole divider after the final overdue row only when a current-day row follows.

### Give each fixed row an addressable divider

Each Today row will retain a divider view, but it will be hidden by default and shown only at the overdue/current-day boundary. This preserves the fixed `RemoteViews` structure and existing click targets while removing separators between tasks.
