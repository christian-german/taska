## Context

Tasks expose two independent timestamps: `scheduledAt` places a task on the calendar, while `dueAt` records its deadline. Android and the shared Angular web/Tauri client display these values separately in task detail.

Android already offers a schedule-removal row, but the action does not successfully leave the task unscheduled. The Angular scheduled-date picker offers an `effacer` action only beside its time control. That action emits the selected date without a time, so it intentionally converts the schedule to all-day rather than removing the scheduled date. Although the Angular task-detail date row also contains a small removal control, complete removal must be explicit and must remain distinct from clearing only the time.

## Goals / Non-Goals

**Goals:**

- Make complete scheduled-date removal work in Android, web, and Tauri task detail.
- Persist complete removal as `scheduledAt: null`.
- Preserve the independent `dueAt` deadline and all unrelated task values.
- Keep time-only clearing available as a conversion of a timed schedule to an all-day schedule.
- Reflect the successful server response immediately in task detail.

**Non-Goals:**

- Change deadline creation, editing, or removal.
- Change scheduling time-zone interpretation, recurrence scope policy, calendar filtering, or notification behavior.
- Change backend, REST, or MCP contracts.
- Add schedule removal to task creation or views other than task detail.

## Decisions

### Clear the complete planned schedule explicitly

The complete-removal mutation will send `scheduledAt: null`. It will retain the current `dueAt` and all unrelated task values. The resulting task is unscheduled and therefore has no calendar date or scheduled time.

### Keep complete removal distinct from time-only clearing

Android will keep its existing schedule-removal affordance and connect it to a successful unscheduling mutation. The shared Angular detail view will retain an explicit complete-removal affordance whenever a schedule is assigned. Since the Angular application is shared, browser and Tauri clients receive the same behavior.

The date-time picker's `effacer` time action remains a time-only operation: it retains the selected scheduled date and makes that schedule all-day. Tests and accessible labels will distinguish that operation from removing the entire schedule.

### Adopt server-confirmed state

After a successful update, task detail will display the task returned by the server and show no assigned schedule. A failed request will leave the schedule represented as assigned and use the clients' existing failure handling.

### Preserve existing recurrence semantics

Schedule removal will use the same recurrence targeting and scope behavior as other schedule edits in each task-detail client. This change does not introduce a new recurrence policy.

## Risks / Trade-offs

- [Users may confuse removing time with removing the complete schedule] → Present distinct actions with labels that describe `time` versus the complete scheduled date.
- [A client may accidentally clear the deadline because schedule and deadline naming has changed historically] → Assert that `scheduledAt` becomes null while `dueAt` remains unchanged.
- [Displayed state may diverge after a failed update] → Update task-detail state only from a successful server response.

## Migration Plan

1. Correct and cover Android schedule removal.
2. Correct and cover complete removal in the shared Angular task-detail interface while retaining time-only clearing.
3. Validate both clients preserve `dueAt`, adopt successful server state, and retain existing recurrence behavior.
4. Roll back the client changes if necessary; no data migration is involved.

## Open Questions

- None.
