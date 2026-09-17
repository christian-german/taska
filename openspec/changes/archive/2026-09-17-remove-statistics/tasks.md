## 1. Backend removal

- [x] 1.1 Delete the Spring statistics controller, service, and DTO
- [x] 1.2 Remove statistics-only task repository queries and verify remaining backend references

## 2. First-party client removal

- [x] 2.1 Delete the Angular statistics screen, HTTP service, and models
- [x] 2.2 Remove Angular statistics routing, navigation, command-palette, and keyboard-shortcut entry points
- [x] 2.3 Remove the Android project statistics summary and verify no other Android statistics references exist

## 3. Documentation and specifications

- [x] 3.1 Remove statistics paths, schemas, and tags from the maintained OpenAPI contract
- [x] 3.2 Remove retired statistics implementation and endpoint findings from maintained audit documentation
- [x] 3.3 Synchronize the statistics-removal delta into canonical OpenSpec specifications

## 4. Verification

- [x] 4.1 Run residual feature scans across maintained sources
- [x] 4.2 Run backend tests and OpenAPI contract validation
- [x] 4.3 Run the frontend build and Android unit tests
- [x] 4.4 Validate OpenSpec and review scoped changes without disturbing concurrent work
