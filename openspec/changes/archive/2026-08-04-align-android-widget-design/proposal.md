## Why

The Android widget currently uses generic, disconnected styling, so it does not feel like a Taska surface. Aligning it with the web application makes the widget immediately recognizable while preserving native widget readability and interaction.

## What Changes

- Apply the Taska web design system's navy, mint accent, neutral surfaces, typography hierarchy, row treatment, and status colors to Android widgets.
- Replace the square white widget container with a rounded-card treatment that clearly clips all content to rounded corners.
- Redesign task rows and completion affordances to echo the web application's circular check controls and restrained separators.
- Define light and dark widget color behavior from the web application themes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Widget presentation requirements will adopt the Taska visual system, including rounded corners, colors, typography, task rows, and light/dark rendering.

## Impact

- Android widget layout XML, drawable/color/font resources, AppWidget provider rendering, and widget visual tests.
- No task API, task filtering, completion, or synchronization behavior changes.
