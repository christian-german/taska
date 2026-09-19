## 1. Non-retroactive series mutations

- [x] 1.1 Add an occurrence-state existence query and central recurring-generator comparison.
- [x] 1.2 Guard full and unscoped partial base mutations before any stateful series generator change.
- [x] 1.3 Add service tests for rejected stateful changes, allowed stateless corrections, normalized equivalent rules, and allowed non-generator changes.

## 2. Detached occurrence lifecycle

- [x] 2.1 Resolve existing detached state as a valid occurrence mutation target before RRULE validation.
- [x] 2.2 Preserve detached state when completing, replacing, updating, and reopening occurrences, including reopening without overrides.
- [x] 2.3 Remove open detached state when it is deleted instead of persisting a detached skip.
- [x] 2.4 Add lifecycle tests covering detached update, completion, reopening, deletion, and rejection of unknown non-generated identities.

## 3. Verification

- [x] 3.1 Format Java sources and run the backend test suite.
- [x] 3.2 Run OpenSpec validation and repository diff checks.
