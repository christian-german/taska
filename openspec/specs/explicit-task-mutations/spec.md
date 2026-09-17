# explicit-task-mutations Specification

## Purpose
TBD - created by archiving change make-task-mutations-explicit. Update Purpose after archive.
## Requirements
### Requirement: Mutations identify their target explicitly

The application task service SHALL expose distinct operations for mutating a base task, one recurring occurrence, and the following part of a recurring series. Occurrence operations MUST receive both the series identifier and stable occurrence schedule identity.

#### Scenario: Close a base task
- **WHEN** the mutation boundary receives a close request without an occurrence identity for a non-recurring task
- **THEN** it invokes the base-task close operation

#### Scenario: Close one recurring occurrence
- **WHEN** the mutation boundary receives a close request with an occurrence identity for a recurring series
- **THEN** it invokes the occurrence close operation with the series identifier and occurrence identity

#### Scenario: Apply scoped recurring mutations
- **WHEN** an update or deletion uses `THIS_ONLY` or `FROM_THIS` for a recurring series
- **THEN** the mutation boundary invokes the explicitly named occurrence or following-series operation matching that scope

### Requirement: Operation names describe persisted effects

The application task service SHALL name a single-occurrence deletion as a skip operation and a following-occurrence deletion as a series truncation operation.

#### Scenario: Delete one generated occurrence
- **WHEN** deletion targets one generated recurring occurrence
- **THEN** the application records that occurrence as skipped through `skipOccurrence`

#### Scenario: Delete a recurring tail
- **WHEN** deletion targets a recurring series from an identified occurrence onward
- **THEN** the application truncates the series through `truncateSeriesFrom`

### Requirement: Existing transport behavior remains compatible

The HTTP and MCP adapters SHALL retain their existing paths, request and response contracts, target-selection precedence, validation behavior, and successful-mutation change publication.

#### Scenario: Existing ambiguous request is routed
- **WHEN** an existing HTTP or MCP request encodes its target through optional occurrence or scope fields
- **THEN** the mutation boundary translates it to exactly one explicit service operation without changing the externally observable result

#### Scenario: Successful mutation publishes a change
- **WHEN** any explicit mutation completes successfully through `TaskMutationService`
- **THEN** the account change notification is published exactly once

