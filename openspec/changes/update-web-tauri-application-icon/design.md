## Context

The approved Android icon refresh established exact sRGB `#14B37D` as Taska's icon background colour without redesigning its artwork. The web favicon and Tauri icon set share the same existing rounded-square/checkmark design but retain the obsolete orange treatment in vector and generated raster/container formats.

## Goals / Non-Goals

**Goals:**

- Give web, desktop, and generated Tauri platform icons the same signal-green treatment as Android.
- Preserve the existing geometry, composition, transparency, dimensions, and format coverage.
- Keep all checked-in representations synchronized.

**Non-Goals:**

- Redesign the icon or change its symbol.
- Change Android application resources outside the Tauri-generated icon set.
- Change runtime UI, application identity, or shared colour tokens.

## Decisions

### Use one exact brand colour

Replace the obsolete orange treatment with exact sRGB `#14B37D`, matching the approved Android launcher icon and shared visual-system token.

### Retain every existing delivery format

Keep the web SVG and multi-resolution ICO favicon and all existing Tauri PNG, ICO, ICNS, iOS, and Android generated outputs. Platforms select different representations, so every checked-in output must receive the same refresh.

### Preserve artwork rather than regenerate a new design

Only colour information changes. Existing dimensions, transparency, rounded-square silhouette, checkbox outline, and checkmark placement remain unchanged.

## Risks / Trade-offs

- [Generated formats drift] → Check every icon family for the new exact colour and absence of the obsolete orange.
- [Small sizes lose clarity] → Preserve the existing pixel dimensions and artwork placement and inspect representative favicon and desktop sizes.

## Migration Plan

1. Update the canonical web SVG treatment.
2. Recolour the web ICO and every checked-in Tauri icon representation.
3. Verify resource coverage and representative rendering.
4. Roll back by restoring the previous assets; no data migration is required.
