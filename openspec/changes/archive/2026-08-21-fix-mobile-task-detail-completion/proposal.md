## Why

The circular completion control beside the title in Android task detail is currently decorative. Tapping it does not complete an active task or reopen a completed task, so task detail behaves differently from the Android task lists and cannot perform the action the control communicates.

## What Changes

- Make the title-adjacent completion control in Android task detail interactive.
- Complete an active task and reopen a completed task through the existing task actions.
- Target the displayed recurring occurrence when task detail was opened for an occurrence.
- Adopt the task returned by a successful action so the completion control and title presentation reflect the new state.
- Keep task details unchanged when the completion or reopen action fails.

## Capabilities

### New Capabilities

- `android-task-detail-completion`: Defines completion and reopening behavior for the title control in Android task detail.

### Modified Capabilities

None.

## Impact

- Android task-detail Compose UI, view-model behavior, and focused tests.
- Existing Android task repository close/reopen operations and widget refresh side effects are reused.
- No backend API, persistence, web/Tauri client, task-list, widget, or recurrence-policy changes.
