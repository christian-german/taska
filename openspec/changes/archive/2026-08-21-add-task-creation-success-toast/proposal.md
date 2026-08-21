## Why

Task creation currently closes or resets the creation interface without a consistent, explicit confirmation. On Android, web, and the Tauri desktop client, users can therefore be unsure whether a submission completed successfully.

## What Changes

- Show a transient success toast after a task-creation request succeeds in the Android, web, and Tauri desktop clients.
- Make the confirmation available across each client's user-facing task-creation entry points.
- Do not show the success toast while creation is pending or after a failed creation request.
- Keep task creation, refresh, navigation, and error behavior otherwise unchanged.

## Capabilities

### New Capabilities

- `task-creation-feedback`: Defines consistent success feedback for user-initiated task creation across the supported graphical clients.

### Modified Capabilities

None.

## Impact

- Android Compose task-creation presentation and tests.
- Shared Angular presentation used by the web and Tauri desktop clients, including task-creation entry points and tests.
- No backend API, persistence, task data, MCP, notification-reminder, or synchronization changes.
