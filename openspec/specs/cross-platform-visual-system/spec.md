## Purpose

Define the shared visual language and accessibility variants for the web, desktop, and Android applications.

## Requirements

### Requirement: Shared Christian German EI visual tokens
The applications SHALL expose semantic colour, type, surface, border, elevation, radius, and state tokens. In light mode, the tokens SHALL be derived from the approved palette: `#17233D`, `#2A3D63`, `#14B37D`, `#0E7A55`, `#E3F5EE`, `#10151C`, `#78828F`, `#F6F8FA`, and `#FFFFFF`.

#### Scenario: A UI component consumes a colour role
- **WHEN** a web, desktop, or Android component renders a shared visual role
- **THEN** it uses a semantic token mapped to the approved palette rather than a component-local raw colour value

### Requirement: Archivo-only UI typography
The applications SHALL use Archivo as the sole UI typeface. Titles and section headings SHALL use Archivo 700, and body and secondary text SHALL use Archivo 400 unless a different weight is required for an accessible interaction state.

#### Scenario: A task screen renders text hierarchy
- **WHEN** a task screen displays a title, body text, and secondary metadata
- **THEN** all text uses Archivo with title weight 700, body weight 400 at 15–16 px equivalent with approximately 1.6 line height, and secondary text at 13 px equivalent using the secondary-text token

### Requirement: Accessible action and accent colour use
The applications SHALL render filled primary actions with navy `#17233D` and white text. They SHALL reserve signal green `#14B37D` for graphical or state accents and SHALL NOT use small white text on that colour. Green text or links on light surfaces SHALL use dark green `#0E7A55`.

#### Scenario: A user encounters a primary action and completion state
- **WHEN** a primary action and a completed-task indicator are both visible
- **THEN** the action uses the navy filled-action treatment and the completion indicator uses signal green without relying on small white text over it

### Requirement: Theme and accessibility variants
The web, desktop, and Android applications SHALL provide dark semantic token mappings and an opaque fallback for users or environments that reduce, disable, or cannot render transparency. All text and controls SHALL retain sufficient contrast in each supported variant.

#### Scenario: Transparency is unavailable
- **WHEN** a platform cannot render backdrop translucency or the user requests reduced transparency
- **THEN** glass-designated surfaces render as opaque semantic surfaces while retaining their border, elevation, and interactive state hierarchy
