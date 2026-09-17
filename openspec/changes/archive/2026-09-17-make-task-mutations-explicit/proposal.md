## Why

Task mutations currently infer whether they target a base task, a recurring occurrence, or the following part of a series from nullable parameters and recurrence scopes. This implicit dispatch keeps unrelated mutation rules coupled inside `TaskService` and prevents a clean extraction of occurrence behavior.

## What Changes

- Replace mixed service methods for update, delete, close, and reopen with operations whose names and parameters identify the target explicitly.
- Represent occurrence deletion as skipping an occurrence and following-series deletion as truncating a series, matching the behavior already implemented.
- Route existing HTTP and MCP requests to the explicit operations without changing their paths, payloads, responses, or business behavior.
- Preserve the existing change-publication side effect for every successful mutation.

## Capabilities

### New Capabilities

- `explicit-task-mutations`: Defines explicit application-service operations for base tasks, recurring occurrences, and following-series mutations.

### Modified Capabilities

None.

## Impact

- Affects task mutation services, HTTP and MCP adapters, and their tests.
- Does not change the OpenAPI or MCP contracts, persistence schema, or recurrence semantics.
- Prepares occurrence-specific behavior for a later extraction into `TaskOccurrenceService`.
