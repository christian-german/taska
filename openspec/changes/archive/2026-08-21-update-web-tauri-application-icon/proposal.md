## Why

The Android launcher icon now uses Taska's approved signal green, but the web favicon and Tauri application icons still use the previous orange treatment. The inconsistent assets present different brand colours depending on how Taska is opened.

## What Changes

- Recolour the existing web favicon with exact signal green (`#14B37D`).
- Recolour the existing Tauri application icon outputs with the same signal green.
- Preserve the established rounded-square, checkbox, and checkmark artwork across all formats and sizes.

## Capabilities

### New Capabilities

- `application-icon`: Defines the consistent visual treatment of Taska's web and Tauri application icons.

### Modified Capabilities

None.

## Impact

- Web SVG and ICO favicon assets.
- Tauri desktop and generated platform icon assets.
- No changes to application behavior, names, identifiers, or icon artwork.
