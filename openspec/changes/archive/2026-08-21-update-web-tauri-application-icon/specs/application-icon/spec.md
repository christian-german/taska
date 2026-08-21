## ADDED Requirements

### Requirement: Web favicon uses Taska signal green

The web application favicon SHALL use exact sRGB signal green `#14B37D` in every provided representation. It SHALL preserve the established rounded-square silhouette, checkbox outline, checkmark, composition, and transparency, and SHALL NOT expose the previous orange treatment.

#### Scenario: Browser displays the favicon

- **WHEN** a browser selects either the SVG or ICO Taska favicon
- **THEN** it SHALL display the established icon artwork with signal green `#14B37D`
- **AND** it SHALL NOT display the previous orange treatment

### Requirement: Tauri application icons use Taska signal green

Every checked-in Tauri application icon representation SHALL use exact sRGB signal green `#14B37D`, matching the Android application icon. Existing formats, dimensions, transparency, silhouette, and artwork composition SHALL remain available and unchanged apart from colour.

#### Scenario: Operating system displays a Tauri icon

- **WHEN** a supported desktop or generated mobile target selects a checked-in PNG, ICO, ICNS, adaptive, or density-specific Tauri icon representation
- **THEN** the icon SHALL display the established artwork with signal green `#14B37D`
- **AND** it SHALL NOT display the previous orange treatment

#### Scenario: Platform selects a different icon size

- **WHEN** a platform selects any checked-in size appropriate to its display context
- **THEN** the selected asset SHALL retain the same signal-green treatment, silhouette, and composition as the other representations
