## 1. Visual foundation

- [x] 1.1 Inventory current web/desktop CSS tokens, fonts, shared primitives, and Android Compose colour/type usage against the new semantic-token contract.
- [x] 1.2 Define light and dark semantic tokens from the approved Christian German EI palette for web/desktop CSS and Android Compose.
- [x] 1.3 Add Archivo font assets/loading and replace Inter, Caveat, and JetBrains Mono usage with accessible Archivo weight and size rules.
- [x] 1.4 Implement shared reduced-transparency and contrast-safe opaque surface tokens.

## 2. Web and desktop chrome

- [x] 2.1 Rework the Angular application shell, sidebar, navigation controls, and responsive mobile navigation with the neutral frosted-chrome treatment.
- [x] 2.2 Rebrand shared Angular primitives—buttons, chips, checkboxes, section headers, task rows, inputs, and focus states—with semantic navy/green tokens.
- [x] 2.3 Apply frosted treatment to Angular dialogs, modal panels, and floating controls while retaining opaque task lists, forms, and detail work areas.
- [ ] 2.4 Update Tauri-specific window/application chrome where supported without making transparency a functional requirement.
- [ ] 2.5 Migrate remaining web feature views to the shared surfaces and verify narrow responsive layouts.

## 3. Android native experience

- [x] 3.1 Replace the Compose theme's current colours and typography with the shared semantic palette and Archivo resources, including dark mode.
- [x] 3.2 Implement reusable Compose opaque content, glass-equivalent chrome, state accent, and reduced-transparency surface primitives.
- [ ] 3.3 Apply the primitives to bottom navigation, project drawer, task items, add-task sheets, dialogs, and task-detail surfaces.
- [ ] 3.4 Migrate Today, Inbox, Project, Day, Week, Tracker, and authentication screens to the new native visual system.

## 4. Validation and release readiness

- [ ] 4.1 Verify primary-action, green-text, focus, completion, and selected-state contrast in light and dark themes; confirm no small white text appears on signal green.
- [ ] 4.2 Verify opaque fallback behaviour with transparency disabled or unsupported on web, desktop, and Android.
- [ ] 4.3 Run web/desktop build and automated tests, Android unit tests/build, and targeted manual checks for keyboard, touch, screen-reader labels, and responsive layouts.
- [ ] 4.4 Capture before/after screenshots of representative web, desktop, and Android views and resolve visual regressions before release.
