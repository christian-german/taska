## Why

Taska currently has a warm, paper-like web and desktop interface and a separate, minimal Android theme. The applications need a cohesive, accessible visual identity based on Christian German EI's navy, green, and Archivo charter, with a carefully applied frosted-glass treatment across all supported platforms.

## What Changes

- Establish a shared visual-token contract for colours, typography, spacing, radii, borders, elevations, and interaction states across web, desktop, and Android.
- Replace the current Inter, Caveat, and JetBrains Mono typography with Archivo as the sole UI typeface.
- Rebrand web and desktop from their cream/orange palette to the approved navy, signal-green, and neutral palette.
- Introduce frosted-glass chrome for navigation, floating controls, modals, dialogs, and sheets while retaining mostly opaque, high-legibility task and form surfaces.
- Apply equivalent native Android surfaces rather than relying on costly or inconsistent backdrop blur.
- Define accessible light and dark theme behaviour, including reduced-transparency fallbacks and minimum contrast requirements.

## Capabilities

### New Capabilities

- `cross-platform-visual-system`: Defines the shared visual identity, token values, typography, and accessibility rules for Taska's web, desktop, and Android applications.
- `frosted-glass-ui-surfaces`: Defines when frosted-glass chrome is used and the fallback behaviour for unsupported or reduced-transparency environments.

### Modified Capabilities

- None.

## Impact

- Web and desktop Angular styles, shell, sidebar, shared components, feature views, and theme service.
- Tauri desktop window and application chrome where supported.
- Android Compose theme, shared UI components, navigation, sheets, and screen-level surfaces.
- Frontend font-loading and build assets; no backend APIs or data contracts change.
