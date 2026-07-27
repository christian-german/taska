## Context

Taska's web UI is Angular CSS-token based and its desktop build packages the same UI with Tauri. Android is a native Jetpack Compose client with a minimal light-only `MaterialTheme`. The current web system uses warm cream/orange tokens and three typefaces (Inter, Caveat, JetBrains Mono); Android uses a different warm palette. The redesign must apply Christian German EI's approved navy/green palette and Archivo typeface throughout while preserving dense task-management usability.

## Goals / Non-Goals

**Goals:**

- Provide one documented semantic visual language across web, desktop, and Android.
- Make the existing application chrome feel frosted while maintaining clear, mostly opaque work surfaces.
- Preserve readable, keyboard- and touch-friendly controls in light, dark, high-contrast, and reduced-transparency contexts.
- Share decisions at the semantic-token level even where platform rendering differs.

**Non-Goals:**

- Rebuild Angular views, change task workflows, or alter backend APIs.
- Achieve pixel-identical rendering between browser/Tauri and Android.
- Apply blur to task rows, text fields, or all content cards.
- Use the brand green as a general-purpose background for small white text.

## Decisions

### Use semantic tokens backed by the approved palette

The UI SHALL use named semantic tokens rather than raw brand values in components. The source palette is: navy `#17233D`, light navy `#2A3D63`, signal green `#14B37D`, dark green `#0E7A55`, pale green `#E3F5EE`, text `#10151C`, muted `#78828F`, canvas `#F6F8FA`, and white `#FFFFFF`.

This lets the same intent—such as `surface-glass`, `action-primary`, or `text-secondary`—map to CSS custom properties on web/desktop and Compose colour roles on Android. Direct component-by-component recolouring was rejected because it will drift between platforms and makes dark mode difficult to maintain.

### Make Archivo the single UI family

Archivo 400 and 700 will provide body and hierarchy respectively; numeric labels will use Archivo rather than a monospaced exception. The frontend will load the font once through its normal asset strategy, and Android will package the corresponding family resource. A system-font-only approach was rejected because brand consistency is a stated requirement.

### Scope glass to chrome and transient elevated surfaces

Web and desktop will use a translucent neutral surface, subtle navy border, soft shadow, and `backdrop-filter` where supported for sidebars, top/bottom navigation, floating action controls, dialogs, and sheets. Content-dense task lists, forms, and editable details remain opaque white or canvas surfaces.

Android will use translucent/tinted containers, borders, and elevation to convey the same hierarchy. It will only use platform blur if the API level and performance budget safely allow it; visual parity does not require an actual blur implementation.

### Keep green semantic and accessible

Signal green is reserved for small interaction and state signals—completion, selection, focus, progress, icons, and decorative rules. Navy remains the filled primary action colour. Text on light surfaces uses dark green when a green text treatment is required; small white text on signal green is prohibited.

### Support dark mode and reduced transparency

Both platforms will define dark semantic tokens derived from the navy family. When transparency is unavailable, disabled by user preference, or fails contrast checks, glass surfaces fall back to opaque semantic surfaces with the same border/elevation and content hierarchy. This is preferred over disabling the new navigation treatment entirely.

## Risks / Trade-offs

- [Browser and GPU blur can reduce scrolling performance] → Restrict blur to a small number of non-scrolling chrome surfaces and use opaque fallbacks.
- [Transparent surfaces can make text contrast depend on the backdrop] → Keep body content opaque; verify text and controls against every supported background and expose reduced-transparency fallback.
- [Archivo availability can cause layout shift or Android/browser differences] → Bundle/pin the family assets, define metric-compatible fallback stacks, and verify key responsive layouts.
- [Dark-mode palette has no explicit brand specification] → Derive it from semantic roles, validate contrast, and keep the approved brand values for identity and emphasis.
- [Native Android lacks consistent cross-device blur] → Require visual hierarchy equivalence, not identical blur physics.

## Migration Plan

1. Introduce tokens and Archivo without changing task data or APIs.
2. Migrate shared web/desktop primitives and shell, then feature surfaces.
3. Migrate Android theme and shared Compose components before individual screens.
4. Test light, dark, reduced-transparency, keyboard, touch, and narrow-screen variants before release.
5. Roll back by retaining the prior token theme behind the release branch if a visual regression prevents normal task management; no data migration is needed.

## Open Questions

- Whether dark mode is selected from the system, from the existing user preference, or both.
- Whether the Tauri window itself should use a transparent native titlebar where the target OS supports it.
- Whether the ambient background is limited to CSS/Compose gradients or includes a branded raster illustration.
