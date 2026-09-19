## Why

Truncating or splitting a recurring series can leave completed and deliberately modified occurrences outside the series' new rule or time range. Those occurrences are historical facts that must remain visible, while invalid recurring definitions must not corrupt persistence or prevent unrelated series from receiving notifications.

This change records the behavior already introduced by the recurrence-invariant and detached-occurrence work so it can be reviewed, validated, and eventually synchronized into the canonical specifications.

## What Changes

- Require every persisted recurring series to have a non-blank recurrence rule across all application write paths and at the database boundary.
- Repair pre-existing recurring rows without a rule during migration instead of leaving definitions that recurrence expansion cannot process.
- Preserve completed and modified occurrence state beyond a truncation boundary as explicitly detached state, while removing skipped state that no longer overlays a generated occurrence.
- Include detached occurrences independently in calendar-range results and expose their status through the HTTP and MCP task representations.
- Isolate recurrence-expansion failures per series so one malformed definition cannot suppress notifications or calendar results for valid series.
- Keep following-series replacement anchored to the replacement's supplied `scheduledAt` rather than silently resetting it to the cut occurrence.

## Capabilities

### New Capabilities

- `recurring-series-integrity`: Defines the recurrence-rule invariant and per-series failure isolation for recurrence expansion.
- `recurring-series-history-preservation`: Defines how truncation classifies, preserves, removes, migrates, and queries occurrence state that falls outside the resulting series.

### Modified Capabilities

- `task-occurrence-state-persistence`: Persists the detached classification and migrates pre-existing orphaned occurrence state.
- `task-representation-contract`: Exposes detached status on recurring-occurrence HTTP representations.
- `task-notification-scheduling`: Prevents one invalid recurring definition from aborting notification processing for other series.
- `taska-mcp-server`: Exposes detached status through the MCP-owned task output contract.

## Impact

- Backend task definition validation, recurring-series truncation, occurrence-state persistence, calendar occurrence queries, and notification candidate expansion.
- Flyway migrations for recurrence integrity and detached occurrence state.
- HTTP OpenAPI and MCP output schemas for recurring occurrences.
- Existing clients receive a new required HTTP `isDetached` occurrence field; client-specific presentation remains outside this change.
