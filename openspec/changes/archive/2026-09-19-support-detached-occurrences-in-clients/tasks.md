## 1. Occurrence retrieval contract

- [x] 1.1 Add service-layer retrieval of one generated or detached occurrence with skipped and unavailable identity rejection.
- [x] 1.2 Expose the occurrence GET endpoint through the controller, mapper, and OpenAPI contract.
- [x] 1.3 Add backend service, controller representation, and contract tests for virtual, detached, skipped, and unavailable occurrences.

## 2. Angular detached occurrence experience

- [x] 2.1 Add required detached state to the Angular occurrence model and fixtures.
- [x] 2.2 Display the accessible `Hors série` badge in Angular task rows and task detail.
- [x] 2.3 Route supported detached edits and deletion directly to `THIS_ONLY`, suppress scope dialogs, and prevent series-only editing.
- [x] 2.4 Add Angular service and component tests for detached rendering and direct mutation routing.

## 3. Android detached occurrence experience

- [x] 3.1 Add detached state to Android occurrence models, factories, copies, and deserialization tests.
- [x] 3.2 Load an addressed occurrence through the new GET operation in Android task detail.
- [x] 3.3 Display `Hors série` in Android calendar rows and detail, suppress scope dialogs, and route supported mutations directly to the occurrence.
- [x] 3.4 Disable series-only editing for detached detail and add Android view-model/UI tests.

## 4. Verification

- [x] 4.1 Format Java, TypeScript, and Kotlin sources and run relevant backend, frontend, Android, and contract tests.
- [x] 4.2 Validate all OpenSpec artifacts and run repository diff checks.
