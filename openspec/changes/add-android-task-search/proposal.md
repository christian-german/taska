## Why

Task search is available in the browser and Tauri clients, but the native Android client has no way to search across tasks. Android users need a discoverable mobile search that also finds completed tasks.

## What Changes

- Add a search action to the top bar of every primary Android task screen.
- Open a dedicated full-screen, task-only search experience from that action.
- Match a query case-insensitively against task content and include both active and completed tasks.
- Reuse Android's task presentation and open the existing task-detail experience when a result is selected.
- Keep the browser and Tauri command palette, backend API, and matching of fields other than task content unchanged.

## Capabilities

### New Capabilities

- `android-task-search`: Defines how users discover, perform, and act on task searches in the native Android client.

### Modified Capabilities

None.

## Impact

- Android primary task-screen top bars, navigation, and application manifest.
- Android search presentation, state management, task retrieval, and automated tests.
- Existing Android task-row and task-detail integrations may be reused.
- No backend, database, web, Tauri, MCP, or synchronization changes.
