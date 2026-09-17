## 1. Backend application and HTTP contract

- [x] 1.1 Replace nullable `TaskResult` state with exhaustive non-recurring, recurring-series, and recurring-occurrence variants.
- [x] 1.2 Add the three discriminated HTTP DTO variants and update `TaskMapper` and task endpoints to return the correct variants.
- [x] 1.3 Give the MCP adapter an independent task output mapping that preserves its existing schema.
- [x] 1.4 Add or update backend tests for result classification, mapper fields, and endpoint-specific task variants.

## 2. Maintained OpenAPI contract

- [x] 2.1 Replace the flat task response schema with a discriminated `oneOf` union and concrete variant schemas.
- [x] 2.2 Constrain and document undated, dated, series, and occurrence endpoint responses and update contract fixtures/tests.

## 3. Angular client

- [x] 3.1 Replace the flat Angular task interface with discriminated task representation types and type guards.
- [x] 3.2 Update Angular services, components, and tests to use variant-specific recurrence and occurrence fields.
- [x] 3.3 Format, build, and test the Angular client.

## 4. Android client

- [x] 4.1 Replace the flat Android task data class with sealed task representation types and shared derived accessors.
- [x] 4.2 Add strict Gson discriminator deserialization and reuse it in Retrofit and widget persistence.
- [x] 4.3 Update Android application code and tests for variant-aware copying and recurrence behavior.
- [x] 4.4 Format, compile, and test the Android client.

## 5. Verification

- [x] 5.1 Format and run backend tests and static checks.
- [x] 5.2 Validate the OpenAPI and OpenSpec contracts and run repository contract tests.
- [x] 5.3 Review the final diff for obsolete flat DTO assumptions and mark all completed tasks.

The targeted frontend/OpenAPI contract suite passes. The full positive-mode backend contract suite
was also run and reported no violation of the new discriminated response schemas; it remains red on
pre-existing fuzz cases such as out-of-range Java timestamps, generated foreign keys, and blank
strings accepted by older request schemas.
