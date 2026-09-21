# Verification — 2026-09-21

Implementation is local; no GitHub operation, commit, push or database migration was performed.

## Successful checks

- Backend: `mvn -o -Dmaven.repo.local=/tmp/taska-m2 test` — 227 tests, no failures, errors or skips, including integration tests.
- Frontend: `npm test -- --watch=false` — 25 tests passed.
- Frontend: `npm run build` — passed; initial bundle approximately 577 kB, above the 500 kB warning threshold.
- Angular requests against OpenAPI/Prism: `./contract-tests/verify-frontend.sh` — 31 tests passed, no contract violations.
- Android: `./gradlew :app:testDevDebugUnitTest :app:compileDevDebugAndroidTestKotlin` — 138 unit tests passed; application and instrumentation test sources compile. Instrumented tests were not executed on a device.
- Project formatters: Maven fmt, Prettier and ktfmt executed. Unrelated Kotlin formatting was restored.
- `openspec validate simplify-recurring-task-mutations --strict` and `git diff --check` passed.

Coverage includes fixed series generators with and without persisted occurrence state, common series edits, removed following updates, schedule-only occurrence JSON, historical override preservation, completion/reopening, stopping without successor creation, detached history, moved-in completed occurrences and range-query deduplication. Client tests cover read-only occurrence fields and direct occurrence movement.

## Existing broad contract-suite failures

`./contract-tests/verify-contract.sh` ran against the local application and disposable PostgreSQL/Authentik containers: 34 test cases, 25 passed and 9 failed (10 unique generated failures). This suite is not green.

The checked-in baseline report already recorded 13 failing test cases. The observed failures concern schema/generator mismatches (invalid timezone offsets, an occurrence path parameter generated as `value`, overlapping planning rules, blank calendar names, recurring creation without a rule) and server errors involving invalid related-resource identifiers in tasks, comments and projects. The retired following endpoint is absent from the new run.

These broader failures were inspected but not repaired as part of recurrence simplification. Generated reports and caches were restored to their original versions to avoid unrelated, large artifact changes; this document records the current result.

Sandbox access was expanded for Docker, local integration-test ports, Gradle cache writes and Google Fonts downloads required by the frontend build.
