## ADDED Requirements

### Requirement: Widget uses the Taska visual design system
The Android task widget SHALL use the Taska web application's semantic visual language: navy primary ink, mint completion accent, neutral surface and divider colors, muted secondary text, and a sans-serif type hierarchy aligned to the web application. The widget SHALL provide corresponding light and dark color resources and SHALL NOT use hard-coded, generic black, gray, or white styling in its rendered task surface.

#### Scenario: Widget is rendered in light mode
- **WHEN** the launcher renders the widget in light system theme
- **THEN** the widget SHALL use the Taska light surface, navy primary text, muted slate secondary text, mint completion accent, and subtle neutral dividers

#### Scenario: Widget is rendered in dark mode
- **WHEN** the launcher renders the widget in dark system theme
- **THEN** the widget SHALL use the Taska dark canvas and surface colors with readable light primary text, muted secondary text, and a visible mint completion accent

#### Scenario: Task row is displayed
- **WHEN** the widget displays a task row
- **THEN** the row SHALL use Taska-aligned primary text, compact spacing, a restrained separator treatment, and a circular completion affordance rather than a font-dependent checkbox glyph

### Requirement: Widget is rendered as a rounded Taska card
The Android task widget SHALL render its outer surface as an opaque rounded card with a 10dp corner radius. Its background and child content SHALL be clipped or inset so that no task row, divider, or status area visibly reaches a square outer corner, regardless of the widget's supported size.

#### Scenario: Widget is placed on the home screen
- **WHEN** the launcher displays the widget at its default size
- **THEN** all four visible outer corners SHALL be rounded with the Taska card treatment

#### Scenario: Widget is resized
- **WHEN** the user resizes the widget horizontally or vertically within its supported bounds
- **THEN** its outer surface SHALL retain rounded corners and its content SHALL remain inside the rounded card boundary
