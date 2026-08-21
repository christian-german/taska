## Context

The Android day and week activities own both their calendar view models and the add-task bottom sheet. Each calendar view model already exposes `load()`, which reloads the range represented by its current offset. The successful creation callbacks currently only close the bottom sheet, unlike other screens that also reload their visible data.

## Goals / Non-Goals

**Goals:**

- Make a successfully created task available in the currently open day or week calendar without requiring navigation.
- Preserve the user's current day or week selection during the refresh.
- Keep dismissal and failed creation from issuing an unnecessary calendar reload.

**Non-Goals:**

- Optimistically insert a task before the create request succeeds.
- Change which tasks qualify for a day or week calendar.
- Change task creation, navigation, or other Android task-list screens.

## Decisions

### Reload the owning calendar view model from the success callback

Each calendar activity will invoke its calendar view model's existing `load()` operation after the add-task sheet reports successful creation. `load()` uses the view model's current offset, so this approach retains the selected range and applies the same server-backed filtering and layout logic as every other calendar reload.

Alternative considered: append the created task directly to UI state. The creation callback does not provide the created task, and duplicating range filtering, recurrence expansion, project lookup, and calendar layout in the activity would be more error-prone than reusing the canonical loader.

## Risks / Trade-offs

- [The new task does not belong to the visible range] → Reloading still produces the correct unchanged view according to the server query.
- [The refresh races with sheet dismissal] → The calendar view model owns its coroutine independently of the composable sheet, so closing the sheet does not cancel the reload.
- [Duplicate network requests] → Refresh only on successful creation; dismissal and failure retain their current behavior.

## Migration Plan

1. Wire both calendar success callbacks to close the sheet and reload the current range.
2. Add focused regression coverage for the callback behavior.
3. Run Android tests and strict OpenSpec validation.

No data migration is required. Rollback restores the previous callbacks.

## Open Questions

- None.
