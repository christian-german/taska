## Context

Tasks expose two independent timestamps: `scheduledAt` places a task on a calendar, while `dueAt` records its deadline. Both Android and the shared Angular web/Tauri client display and edit these values in separate task-detail controls.

The Android detail view already presents a deadline-removal action, but its update currently clears `scheduledAt` and the associated all-day state instead of `dueAt`. The Angular deadline picker provides a time-clearing action; that action emits the selected calendar date and therefore retains a date-only deadline. The Angular task-detail deadline row has no separate action that clears the complete `dueAt` value.

## Goals / Non-Goals

**Goals:**

- Make complete deadline removal available from Android, web, and Tauri task-detail screens.
- Persist deadline removal as `dueAt: null`.
- Preserve `scheduledAt` and `allDay` exactly when removing a deadline.
- Preserve date/time editing and the distinction between removing only a deadline time and removing the whole deadline.

**Non-Goals:**

- Change how deadlines are created, formatted, or interpreted across time zones.
- Change calendar scheduling, recurrence scopes, notifications, filtering, or priority evaluation.
- Change backend or MCP task contracts.
- Add deadline removal to task-creation interfaces or other views.

## Decisions

### Clear only the deadline field

The removal mutation will explicitly set `dueAt` to `null`. It will send or retain the current scheduling fields unchanged, including both `scheduledAt` and `allDay`. This follows the existing contract that deadline and calendar placement are independently writable and prevents deadline removal from unscheduling a task.

### Expose a complete-removal action in each task-detail interface

Android will connect its existing deadline-removal affordance to the corrected mutation. The shared Angular detail view will expose a dedicated removal affordance whenever a deadline is assigned. Because the same Angular application is packaged for browser and Tauri use, this supplies identical behavior in both clients.

The complete-removal action is distinct from the date-time picker's existing time-clear action. Clearing only the time may retain the chosen deadline date; choosing the deadline-removal action removes the entire deadline.

### Reflect the persisted task returned by the server

After successful removal, each detail view will use the task returned by the update operation so the deadline is visibly absent without requiring navigation or a manual refresh. Existing failure handling remains unchanged and must not falsely display a successful removal.

## Risks / Trade-offs

- [Deadline and scheduled-date actions remain easy to confuse internally] → Name and test mutations around the specific `dueAt` field and assert scheduling fields remain unchanged.
- [Web users may confuse clearing time with clearing the deadline] → Keep a distinct full-removal affordance visible on the deadline row when a deadline exists.
- [Client state could diverge from persistence after a failed update] → Continue updating displayed task state only from a successful server response.

## Migration Plan

1. Correct and cover Android deadline removal.
2. Add and cover the shared Angular deadline-removal action.
3. Verify both clients preserve independent scheduling values and validate this OpenSpec change.
4. Roll back the client presentation and mutation changes if necessary; no data migration is involved.

## Open Questions

- None.
