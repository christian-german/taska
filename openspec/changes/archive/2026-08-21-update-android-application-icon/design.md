## Context

Taska's shared visual system defines signal green as `#14B37D`, while the checked-in Android adaptive-icon background is still white and the Android launcher assets predate that visual-system change. Android launchers can select adaptive resources on newer platform versions and density-specific legacy resources on older launchers. The project also provides a development-specific application icon so a development installation can be distinguished from the main application.

The requested change is a colour refresh, not an icon redesign. Both build variants therefore need a consistent update across every launcher resource path without altering the established artwork.

## Goals / Non-Goals

**Goals:**

- Make the Android launcher icon visibly use Taska's signal green.
- Preserve the existing icon design, silhouette, composition, and safe-area treatment.
- Cover both adaptive and legacy launcher rendering.
- Update the main and development-specific icons while retaining their existing variant distinction.

**Non-Goals:**

- Redesign the icon or introduce a new symbol.
- Change application names, package identifiers, build-variant behavior, or runtime UI.
- Change desktop, web, or iOS icon assets.
- Change Taska's approved colour tokens.

## Decisions

### Use the approved signal-green token as the icon's refreshed colour

Use exact sRGB `#14B37D`, the signal-green value already defined by Taska's cross-platform visual system. This gives implementation and visual review one objective colour target rather than an approximate green.

### Preserve artwork geometry and variant identity

Recolour the existing main and development artwork rather than redrawing it. The development icon keeps its existing distinguishing treatment; only the obsolete colour treatment changes. This satisfies the request without changing how users tell main and development installations apart.

### Update every Android launcher representation

Keep adaptive-icon background/foreground resources and density-specific legacy/round outputs visually consistent. A launcher may choose different resource forms based on Android version, shape, and device configuration, so updating only one representation could expose the old treatment.

## Risks / Trade-offs

- [Generated raster assets can drift from their source treatment] → Regenerate or update every Android density and shape variant together and verify their dimensions and resource coverage.
- [Adaptive launchers apply device-specific masks] → Preserve the current foreground geometry and safe area, and inspect the result under representative launcher masks.
- [The development variant could become indistinguishable] → Retain its existing variant marker while applying the same green colour update.

## Migration Plan

1. Update the main and development Android icon sources/resources to use `#14B37D` without changing their artwork geometry.
2. Regenerate or synchronize adaptive, legacy, round, and density-specific launcher resources.
3. Build both Android variants and visually verify their launcher icons, including continued development-variant identification.
4. Roll back by restoring the prior icon resources; no persisted-data migration is required.

## Open Questions

- None.
