## Why

Clients that display overdue tasks currently reconstruct the view from generic lists. This approach does not expand historical recurring occurrences and lets clients apply different date rules.

## What Changes

- Add `GET /tasks/overdue`, returning the complete unpaginated set of unfinished scheduled tasks whose effective date is before the configured calendar's current date.
- Return expanded recurring occurrences, including rescheduled occurrences, instead of series definitions.
- Make Android widgets and the web Today view consume this query.

## Capabilities

### New Capabilities
- `overdue-task-query`: Defines the HTTP query and authoritative selection of overdue tasks and occurrences.
- `web-today-overdue-tasks`: Defines display of overdue work retrieved by the web Today view.

### Modified Capabilities
- `android-task-widgets`: Widgets retrieve overdue tasks from the dedicated query.
- `android-today-task-widget`: The Today widget retrieves overdue tasks from the dedicated query.
- `task-representation-contract`: The overdue query returns displayable representations, never recurring series definitions.

## Impact

- Spring backend: controller and occurrence service, with HTTP and query tests.
- Android Kotlin client: Retrofit contract, repository, and widget refresh.
- Angular frontend: task service and Today view loading.
