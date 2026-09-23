## 1. Backend overdue query

- [x] 1.1 Add the application operation that selects the open non-recurring history and expands overdue recurring occurrences in the configured calendar zone.
- [x] 1.2 Expose `GET /tasks/overdue` and return only existing displayable task representations, sorted by effective `scheduledAt`.
- [x] 1.3 Add backend tests for an open daily series, a rescheduled occurrence, completed or skipped state, and the HTTP representation contract.

## 2. Android clients

- [x] 2.1 Add the Retrofit and repository call for the overdue query.
- [x] 2.2 Replace the widgets' generic historical query with the dedicated query while preserving groups, order, Today widget capacity, and occurrence identity.
- [x] 2.3 Add focused tests for loading and displaying overdue recurring occurrences in both widgets.

## 3. Web Today view

- [x] 3.1 Add the Angular service method for `GET /tasks/overdue` with its HTTP contract test.
- [x] 3.2 Load overdue work and the today/tomorrow range together, then render the `En retard` group from the authoritative response without losing occurrence actions.
- [x] 3.3 Add component tests for the web `En retard` group, including a recurring occurrence and an empty response.

## 4. Verification

- [x] 4.1 Format modified Kotlin and TypeScript code, then run relevant backend, Android, and frontend tests.
- [x] 4.2 Validate the OpenSpec change strictly and check for Git whitespace errors.
