## Context

Android task detail renders a circular control immediately beside the task title, but the control has no click behavior and does not represent the task's completed state. Other Android task surfaces already complete or reopen tasks through `TaskRepository.closeTask` and `TaskRepository.reopenTask`. Those operations accept the optional recurring-occurrence timestamp and return the authoritative updated task.

## Goals / Non-Goals

**Goals:**

- Make the task-detail title control complete and reopen the displayed task.
- Represent active and completed state accessibly and consistently with established Android completion styling.
- Preserve recurring-occurrence identity when invoking the existing close or reopen operation.
- Update detail state only from a successful server response.

**Non-Goals:**

- Introduce optimistic completion in task detail.
- Change completion behavior in lists or widgets.
- Add or change backend endpoints, recurrence semantics, or navigation behavior.
- Change subtask completion behavior.

## Decisions

### Dispatch from the current authoritative task state

The view model will expose one task-completion toggle. It will inspect the task currently held in detail state, call reopen for a completed task and close for an active task, and pass the occurrence timestamp with which the detail activity was opened. This mirrors existing Android action selection while keeping the UI free of repository details.

### Adopt only the successful response

The control will remain server-confirmed rather than optimistic. On success, the view model will replace the displayed task with the returned task. On failure, it will retain the existing task and completion presentation. This avoids showing a completed or reopened state that the server did not accept.

### Make the title control stateful and accessible

The title control will be tappable and derive its checked/unchecked presentation from the displayed task's `isCompleted` value. Its accessibility semantics will identify the available completion or reopening action. The title will use the established completed-task treatment when the task is completed, allowing successful toggles to be visible without leaving detail.

## Risks / Trade-offs

- [Repeated taps could submit competing mutations] → Disable the completion action while its request is pending.
- [A recurring occurrence could be mistaken for the series definition] → Pass the detail activity's occurrence timestamp to the existing close/reopen operation.
- [A failed request could leave misleading local state] → Do not change the displayed task until a successful response is returned.

## Migration Plan

1. Add the view-model completion toggle and pending state.
2. Connect the title control to the toggle and render active/completed states.
3. Add focused view-model and UI tests for success, failure, repeat taps, accessibility, and recurring occurrences.
4. Roll back the Android client changes if necessary; no data migration is involved.

## Open Questions

- None.
