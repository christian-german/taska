## 1. Design resources

- [x] 1.1 Add named light and night widget color resources that map Taska navy, mint, neutral surfaces, muted text, and dividers to Android semantic roles.
- [x] 1.2 Add shared dimension and drawable resources for the 10dp rounded widget card, circular completion control, and row/divider treatment.
- [x] 1.3 Configure the widget's type styles with an Android-safe sans-serif hierarchy aligned to Taska's Archivo-based web typography.

## 2. Widget presentation

- [x] 2.1 Replace the widget root's square white background with the opaque rounded Taska card and ensure content is padded or clipped inside every corner.
- [x] 2.2 Update header, status, and task-row bindings to use semantic Taska colors and the new type hierarchy.
- [x] 2.3 Replace Unicode checkbox glyphs with circular drawable-backed completion controls while retaining separate task-open and completion actions.
- [x] 2.4 Apply the same visual hierarchy to empty, loading, authentication-error, and API-error widget states.
- [x] 2.5 Verify the layout remains legible and correctly bounded at the widget's supported minimum size and after horizontal or vertical resizing.

## 3. Verification

- [x] 3.1 Add or update resource/layout tests that assert Taska semantic tokens, rounded-card background, and circular completion control references.
- [x] 3.2 Add Android rendering or instrumentation coverage for light mode, dark mode, rounded corners, long task titles, and task-row interactions.
- [x] 3.3 Run focused Android widget tests and build the Android development variant.
