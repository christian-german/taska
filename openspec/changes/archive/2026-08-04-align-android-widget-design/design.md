## Context

The existing Android widget is a plain white `RemoteViews` layout with black and gray text, square corners, and text-glyph checkboxes. The web application already defines a coherent Taska design system: Archivo typography; navy `#17233D`; mint accent `#14B37D`; light neutral canvas/surfaces; muted slate text; soft dividers; circular completion controls; and a 10px base radius. The widget must express that language using Android widget-compatible layouts and drawables, without changing its existing task data or actions.

## Goals / Non-Goals

**Goals:**

- Make the widget recognizable as a Taska surface in both light and dark system themes.
- Use the web design system's semantic color roles, type hierarchy, and completion affordance rather than a one-off palette.
- Render every widget size with a visibly rounded outer card and clipping that prevents rows or backgrounds from appearing square at its edges.
- Preserve current task filtering, task-opening, and one-tap completion behavior.

**Non-Goals:**

- Recreate web-only glass, hover, animation, sidebar, or responsive-layout effects in `RemoteViews`.
- Change task data, widget refresh policy, navigation, or API behavior.
- Introduce a custom widget-rendering framework or a downloaded font dependency solely for this visual refresh.

## Decisions

### Define Android semantic resources from the web tokens

Create named light and night color resources for the widget instead of scattering hex values through its XML or provider. The mapping will use navy as the primary heading/ink, mint as the completion accent, white/navy surfaces, slate muted text, and subtle neutral dividers. Archivo is the web font, but the widget will use the closest available Android system sans-serif family unless Archivo is already bundled; this avoids a new font payload and keeps rendering reliable in `RemoteViews`.

Alternative considered: copy web CSS colors directly into the layout. This would drift across themes and make future design-system changes difficult to audit.

### Use a layered rounded-card background

Apply an opaque rounded-rectangle drawable to the widget root with a 10dp corner radius (or a dimension token resolving to 10dp). The root will have internal padding and child backgrounds will remain transparent or be inset, so no task row can paint into the rounded outer corners. Use a solid surface rather than web-style blur because App Widgets do not reliably expose the live content behind them.

Alternative considered: rely on the launcher or Android 12 widget-host masking. That is host-dependent and does not guarantee rounded corners on older devices or across launchers.

### Echo web task-row and checkbox primitives within RemoteViews limits

Render task rows with compact vertical rhythm, muted dividers, navy primary text, and circular mint completion controls backed by drawable resources. Preserve full-row tap and separate completion tap targets, with sufficient contrast and a minimum practical touch target. Rows should be visually restrained rather than carding each item, matching the web's dashed/divider-based task list.

Alternative considered: retain Unicode checkbox glyphs. They vary by system font and cannot consistently reproduce the circular Taska control.

### Adapt by available size and night mode

Keep the same semantic tokens at all widget sizes, but make headers/status text and row capacity degrade gracefully when the host supplies limited height. Provide `values-night` resources so system dark mode maps to the web dark palette (`#101827` canvas and `#17233D` surface) without provider-side theme branching.

Alternative considered: force a permanent light widget. That conflicts with the web's supported dark theme and is visually disruptive on dark launchers.

## Risks / Trade-offs

- [Launcher implementations may add their own padding or mask] → Keep the rounded background self-contained and validate on the supported API/emulator range.
- [RemoteViews lacks CSS capabilities] → Prioritize color, typography, spacing, dividers, and drawables over blur, hover, and animated feedback.
- [System fonts differ from Archivo] → Use a specified system sans-serif hierarchy and validate truncation/legibility with long task titles.
- [Strong navy/mint contrast can fail in night mode if reused unchanged] → Provide separate night semantic colors and add contrast-focused rendering tests.

## Migration Plan

1. Add semantic color, dimension, and rounded/circular drawable resources, including night variants.
2. Update the widget layout and provider bindings to use the new resources without changing intent or data contracts.
3. Extend visual/resource and instrumentation coverage for light mode, dark mode, rounded outer corners, task rows, and completion controls.
4. Roll back by restoring the existing layout and resource references; widget data and update mechanisms remain unchanged.

## Open Questions

- None. The existing app’s Archivo font will be reused only if it is already available to the Android module; otherwise the chosen system sans-serif fallback is intentional.
