## Why

The task API currently uses one nullable-field-heavy `TaskDto` for persisted non-recurring tasks, recurring-series definitions, and expanded recurring occurrences. Clients must infer which concept they received from combinations of `isRecurring`, `occurrenceScheduledAt`, and `isVirtual`, allowing invalid states and obscuring occurrence identity.

## What Changes

- **BREAKING** Replace the single task response shape with explicitly discriminated non-recurring-task, recurring-series, and recurring-occurrence representations.
- **BREAKING** Return only non-recurring tasks and recurring occurrences from date-range task queries, represented as an OpenAPI `oneOf` union with a required `kind` discriminator.
- Make occurrence identity and persistence metadata explicit while preserving the distinction between immutable `occurrenceScheduledAt` and mutable effective `scheduledAt`.
- Represent application-layer task results as exhaustive variants instead of a record whose nullable members encode its meaning.
- Update the Angular and Android clients to consume and branch on the discriminated task representations.
- Preserve MCP task-tool behavior through an MCP-owned output contract rather than exposing HTTP DTOs through the MCP adapter.

## Capabilities

### New Capabilities

- `task-representation-contract`: Defines the three task response representations, their discriminator and field invariants, endpoint-specific allowed variants, and client handling.

### Modified Capabilities

- `task-update-contract`: Requires base-task, following-series, and single-occurrence mutations to return the corresponding discriminated representation.
- `task-scheduling-and-priority-fields`: Defines how scheduling, priority, and due-date values appear in each discriminated task representation.
- `taska-mcp-server`: Separates MCP task outputs from the breaking HTTP response representation while retaining existing MCP semantics.

## Impact

- Backend task service results, HTTP DTOs, mappers, controllers, MCP output mapping, and related tests.
- Maintained OpenAPI task schemas and response definitions.
- Angular task models and consumers.
- Android task models, JSON deserialization, repositories, view models, UI, widgets, and tests.
- Contract tests and generated/static API assumptions that currently reference `TaskDto`.
