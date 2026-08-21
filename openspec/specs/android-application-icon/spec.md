## Purpose

Define the Android launcher icon brand treatment, resource variants, and visual consistency requirements.

## Requirements

### Requirement: Android launcher icon uses Taska signal green

The Android application launcher icon SHALL use exact sRGB signal green `#14B37D` as its refreshed brand colour. The icon SHALL retain the existing design, silhouette, composition, and foreground safe-area treatment; the colour refresh SHALL NOT introduce a new symbol or rearrange the existing artwork.

#### Scenario: Main application icon is displayed

- **WHEN** an Android launcher displays the installed main Taska application
- **THEN** the launcher icon SHALL use `#14B37D` as its Taska green
- **AND** its design, silhouette, composition, and foreground placement SHALL match the established icon

#### Scenario: Launcher applies an adaptive mask

- **WHEN** a compatible Android launcher applies any supported adaptive-icon mask to Taska's icon
- **THEN** the visible icon SHALL retain the established artwork within its safe area and expose the refreshed green treatment without clipping essential artwork

#### Scenario: Legacy launcher displays the icon

- **WHEN** an Android launcher resolves a density-specific legacy or round icon instead of the adaptive icon
- **THEN** the displayed icon SHALL present the same established design and signal-green treatment as the adaptive icon

### Requirement: Android launcher icon covers main and development variants

The main and development Android application variants SHALL each provide a complete launcher icon using the refreshed signal-green treatment. The development icon SHALL retain its existing visual distinction from the main icon so installed main and development applications remain identifiable. No supported adaptive, legacy, round, or density-specific launcher representation for either variant SHALL retain the previous colour treatment.

#### Scenario: Main and development applications are installed together

- **WHEN** an Android launcher displays the main and development Taska applications together
- **THEN** both icons SHALL use the refreshed `#14B37D` treatment
- **AND** the development application SHALL remain visually distinguishable from the main application by its existing variant treatment

#### Scenario: Android selects a variant-specific launcher resource

- **WHEN** either application variant resolves an adaptive, legacy, round, or density-specific launcher icon resource
- **THEN** that resource SHALL use the refreshed green treatment and SHALL NOT expose the previous colour treatment
