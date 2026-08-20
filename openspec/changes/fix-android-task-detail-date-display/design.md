## Context

Taska stores both `scheduledAt` and `dueAt` as nullable instants. `scheduledAt` is paired with `allDay`, which distinguishes an all-day calendar placement from a timed placement. The Android task-detail deadline picker, however, accepts a date only and does not expose a deadline-time control.

The Android detail screen currently uses one formatter for both properties. It correctly passes the task's `allDay` value for a schedule, but forces timed formatting for every `dueAt`. A schedule-removal response can also contain schedule-related metadata that no longer describes a visible value; presentation must be driven first by whether `scheduledAt` exists.

## Goals / Non-Goals

**Goals:**

- Give each visible Android task-detail date property a presentation matching what the user selected.
- Never derive a visible schedule time from `allDay` when `scheduledAt` is absent.
- Preserve localized time display for genuinely timed schedules.
- Cover the response state produced by schedule removal.

**Non-Goals:**

- Change timestamp persistence, API shapes, or the meaning of `allDay`.
- Add a due-time picker or a separate due-date all-day field.
- Repair historical timestamps or modify web/Tauri presentation.
- Change overdue calculation, recurrence behavior, or schedule-removal mutation semantics.

## Decisions

### Treat schedule presence as the first presentation condition

Android task detail will show no scheduled value when `scheduledAt` is null. The `allDay` flag cannot cause a date or time to be synthesized from a missing timestamp. This also makes the view robust to stale or reset schedule metadata returned after removal.

### Use `allDay` only for an assigned schedule

When `scheduledAt` exists, Android task detail will show only its localized calendar date if `allDay` is true. If `allDay` is false, it will show the localized date and time as it does today.

### Present mobile deadlines as date-only

The Android deadline control currently captures only a date. Its detail row will therefore show the localized calendar date without a clock time. The underlying `dueAt` instant remains unchanged, preserving the cross-client contract and leaving any future due-time feature to a separate change.

### Keep formatting logic independently testable

Date formatting will be structured so regression tests can verify absent schedule, all-day schedule, timed schedule, and date-only deadline output without requiring a backend mutation. View-model coverage will additionally verify that successful schedule removal adopts the returned unscheduled task.

## Risks / Trade-offs

- [A precise `dueAt` supplied by another client will not show its time on Android] → This matches the current Android date-only deadline editor; adding due-time support requires an explicit product change.
- [UTC offsets can make midnight instants look like arbitrary clock times] → Date-only properties omit clock output while continuing to use the established localized calendar-date conversion.
- [Schedule metadata can remain false after removal] → Gate schedule formatting on non-null `scheduledAt` before consulting `allDay`.

## Migration Plan

1. Separate date-only and timed presentation paths in Android task detail.
2. Add regression tests for all specified display states and the successful removal response.
3. Run Android tests and strict OpenSpec validation.
4. Roll back the Android presentation change if necessary; no data migration is involved.

## Open Questions

- None.
