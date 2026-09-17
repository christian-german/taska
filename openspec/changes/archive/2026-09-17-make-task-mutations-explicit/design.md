## Context

`TaskService` currently exposes four mixed mutations (`update`, `delete`, `close`, and `reopen`). Each method decides at runtime whether it is mutating a stored task row, one generated occurrence, or the following part of a recurring series. The existing HTTP and MCP contracts encode that target through nullable occurrence identity and recurrence-scope fields, so those adapters must remain compatible while the application layer becomes explicit.

## Goals / Non-Goals

**Goals:**

- Give every task-layer mutation a name and parameter list that identifies its target.
- Centralize translation of the existing transport contracts in `TaskMutationService`.
- Preserve validation, persistence, result types, notification resets, priority invalidation, and change publication.
- Prepare occurrence operations for a later move to `TaskOccurrenceService`.

**Non-Goals:**

- Changing HTTP paths, request bodies, MCP tool inputs, or response representations.
- Extracting a new occurrence or recurring-series service in this change.
- Changing recurrence, deletion, completion, or notification behavior.

## Decisions

### Keep compatibility dispatch at the mutation boundary

`TaskMutationService` will continue to accept the existing transport-independent parameter records and will inspect the stored task plus the requested scope. It will then call one explicit `TaskService` operation. This keeps transport adapters stable and moves target selection out of the implementation of each mutation.

The alternative of branching in each controller and MCP tool was rejected because it would duplicate business dispatch and require adapters to inspect persistence state.

### Name operations after their actual effects

The service operations will be `updateTask`, `updateOccurrence`, `updateSeriesFrom`, `deleteTask`, `skipOccurrence`, `truncateSeriesFrom`, `closeTask`, `closeOccurrence`, `reopenTask`, and `reopenOccurrence`. In particular, an occurrence is skipped rather than deleted because generated occurrences have no row to delete, and a `FROM_THIS` deletion truncates a series.

The alternative of retaining `deleteOccurrence` was rejected because it would hide the persisted `SKIPPED` state and encourage incorrect assumptions during the later service extraction.

### Keep explicit operations in `TaskService` for now

This change splits methods without moving dependencies or business logic. A later change can move occurrence operations and their collaborators to `TaskOccurrenceService` mechanically once call sites no longer depend on mixed signatures.

## Risks / Trade-offs

- [Compatibility routing could drift from the former branch conditions] → Preserve the exact precedence rules and update existing tests to call through the new explicit operations and routing boundary.
- [More public methods temporarily enlarge `TaskService`] → Treat this as an intermediate refactor whose method boundaries match the planned service extraction.
- [Loading a task for routing can add a repository lookup] → Prefer correctness and clear ownership now; the later service split can optimize the boundary without mixing target behaviors again.
