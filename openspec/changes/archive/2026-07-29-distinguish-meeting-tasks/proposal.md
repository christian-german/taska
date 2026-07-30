## Why

Tasks that require completing work and commitments to attend meetings are currently indistinguishable. This makes it harder to scan a task list, plan available focus time, and understand what kind of commitment an item represents.

## What Changes

- Add a task type that lets each task be identified as either a to-do item or a meeting to attend.
- Let people choose the task type when creating or editing a task, defaulting to a to-do item for backward compatibility.
- Clearly communicate a meeting task's type in task-list and task-detail views, including mobile daily and weekly views and the web tracker view.

## Capabilities

### New Capabilities
- `task-type-classification`: Classifies tasks as to-do work or meetings and exposes that classification throughout task creation, editing, and presentation.

### Modified Capabilities

- None.

## Impact

- Task domain model and persistence/migration handling.
- Task creation and editing interfaces.
- Task list, daily, weekly, tracker, and detail presentation across supported clients, reusing the established Today-view meeting icon.
- Existing tasks must remain usable and be treated as to-do items when no type is stored.
