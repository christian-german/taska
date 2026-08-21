## Why

The Android launcher icon still uses the application's previous colour treatment, so it no longer matches Taska's approved visual system or the application users see after launch.

## What Changes

- Recolour the existing Android application icon with Taska's signal green (`#14B37D`).
- Preserve the icon's existing design, silhouette, and composition.
- Apply the refreshed treatment to both the main Android launcher icon and the development-specific Android launcher icon.
- Keep the main and development installations identifiable as their respective application variants.

## Capabilities

### New Capabilities

- `android-application-icon`: Defines the visual treatment and variant coverage of Taska's Android launcher icons.

### Modified Capabilities

None.

## Impact

- Android launcher icon source assets, adaptive-icon resources, generated density variants, and development-variant resources.
- Visual verification of main and development Android builds on launchers that render adaptive and legacy icons.
- No changes to the icon design, in-application user interface, application behavior, or non-Android application icons.
