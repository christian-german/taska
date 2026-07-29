## ADDED Requirements

### Requirement: Frosted chrome surface placement
The applications SHALL apply the frosted treatment only to application chrome and transient elevated surfaces, including navigation, sidebars, floating controls, dialogs, modal panels, and bottom sheets. The treatment SHALL use a neutral translucent tint, subtle navy border, and soft elevation where platform capabilities allow.

#### Scenario: Desktop navigation is displayed
- **WHEN** the web or desktop application displays its main navigation sidebar
- **THEN** the sidebar uses the frosted chrome treatment while preserving readable navigation labels and active state

### Requirement: Opaque productivity surfaces
The applications SHALL render dense task lists, text-entry controls, forms, and task-detail work areas on predominantly opaque white or canvas surfaces rather than a blurred/translucent background.

#### Scenario: A user edits a task
- **WHEN** a user opens a task detail view and edits text fields
- **THEN** the text, controls, and task content are presented on an opaque high-legibility surface

### Requirement: Native-equivalent Android treatment
Android SHALL reproduce the frosted surface hierarchy through translucent or tinted Compose containers, border, and elevation. Actual backdrop blur SHALL be optional and SHALL NOT be required for design conformance.

#### Scenario: Android displays a modal sheet
- **WHEN** an Android modal sheet is opened on a device without safe blur support
- **THEN** it uses the native-equivalent tinted surface treatment and remains visually distinct from the underlying task content

### Requirement: Accent restraint on glass surfaces
Frosted surfaces SHALL remain neutral; signal green SHALL be used on them only for compact state and interaction accents.

#### Scenario: A glass navigation item is active
- **WHEN** a navigation item is selected on a frosted surface
- **THEN** the selected state uses a restrained green indicator or icon accent without making the entire glass surface green
