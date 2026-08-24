## Why

The web client currently marks up to two tasks on the Today screen with a yellow
"suggéré" chip derived from their priority and estimate. This unsolicited marker
adds visual noise to task rows and should no longer be presented.

## What Changes

- Remove the "suggéré" chip from task rows in the web client.
- Stop deriving and passing suggestion state solely for that chip.
- Preserve task content, task ordering, and all other task-row metadata and actions.

## Capabilities

### New Capabilities

- `web-task-row-presentation`: Defines which task information and metadata the web
  client presents in task-list rows.

### Modified Capabilities

None.

## Impact

- Shared Angular Today, task-list, and task-row presentation code and tests.
- No backend API, persistence, scheduling, priority, task mutation, Android, or
  recommendation behavior changes outside removal of the web-client marker.
