## Why

The current Android launcher widget only shows the current week and hides completed work, so it cannot give users a concise view of what they planned to accomplish today. A dedicated Today widget makes that daily view available from the launcher while preserving the established Taska widget experience.

## What Changes

- Add a second, resizable Android home-screen widget that presents tasks planned for the device's local current day.
- Include both incomplete and completed planned tasks in the Today widget; render completed rows with struck-through text and a checked circular completion control that reopens the task or occurrence when tapped.
- Reuse the existing Taska widget visual system, task navigation, and task-completion behavior, adapting refresh scheduling to the local day boundary.

## Capabilities

### New Capabilities

- `android-today-task-widget`: Defines the Today launcher widget's task selection, completed-task presentation, actions, and refresh behavior.

### Modified Capabilities

- None.

## Impact

- Android manifest, app-widget provider metadata, layouts, rendering, refresh coordination, and widget tests.
- Existing authenticated close/reopen task APIs and task-change refresh triggers; no backend API or dependency changes are expected.
