## MODIFIED Requirements

### Requirement: Widget uses the Taska visual design system
The Android task widget SHALL use the Taska web application's semantic visual language: navy primary ink, mint completion accent, neutral surface and divider colors, muted secondary text, and a sans-serif type hierarchy aligned to the web application. The widget SHALL provide corresponding light and dark color resources and SHALL NOT use hard-coded, generic black, gray, or white styling in its rendered task surface. The widget SHALL NOT display a scheduled-task count after a successful refresh, but SHALL retain visible refresh error feedback.

#### Scenario: Widget refresh succeeds
- **WHEN** either Android task widget successfully refreshes with any number of tasks
- **THEN** it SHALL NOT display a scheduled-task count

#### Scenario: Widget refresh fails
- **WHEN** either Android task widget cannot refresh its task data
- **THEN** it SHALL display refresh error feedback instead of silently hiding the failure

#### Scenario: Widget is rendered in light mode
- **WHEN** the launcher renders the widget in light system theme
- **THEN** the widget SHALL use the Taska light surface, navy primary text, muted slate secondary text, mint completion accent, and subtle neutral dividers

#### Scenario: Widget is rendered in dark mode
- **WHEN** the launcher renders the widget in dark system theme
- **THEN** the widget SHALL use the Taska dark canvas and surface colors with readable light primary text, muted secondary text, and a visible mint completion accent

#### Scenario: Task row is displayed
- **WHEN** the widget displays a task row
- **THEN** the row SHALL use Taska-aligned primary text, compact spacing, a restrained separator treatment, and a circular completion affordance rather than a font-dependent checkbox glyph
