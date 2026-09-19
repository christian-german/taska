## Context

Taska stores a recurring series as one task definition and persists only exceptional occurrence state. Changing the definition's `scheduledAt`, `recurrenceRule`, or recurring status can therefore change which historical occurrence identities exist. The dedicated following-series endpoint has an explicit cut identity, while the base replacement and unscoped MCP update contracts do not.

Truncation now preserves meaningful orphaned state as `detached`, but occurrence mutations still validate every identity by expanding the current RRULE. That validation necessarily rejects detached state even though the API returns it as a recurring occurrence.

## Goals / Non-Goals

**Goals:**

- Make non-retroactive series generation an application invariant across HTTP and MCP mutation paths.
- Preserve the documented exception that a series with no occurrence state can be corrected in place.
- Make detached occurrence state a supported lifecycle state rather than a read-only projection.
- Keep each rule in one authoritative service-layer mechanism.

**Non-Goals:**

- Introduce a server-selected implicit cut date for base replacement requests.
- Change the shape of existing HTTP or MCP requests.
- Add automatic reattachment when a later rule generates the detached anchor again.
- Copy comments, subtasks, or a series-root identifier to successor series.

## Decisions

### Reject unsafe in-place generator changes

The mutation boundary will ask `RecurringTaskSeriesService` whether a base update may change a recurring definition in place. A change to recurring status, normalized recurrence rule, or series schedule is allowed when the series has no persisted occurrence state. Once state exists, the base mutation is rejected before changing the task and the caller must use the existing following-series or truncation operation with an explicit occurrence identity.

Rejecting is preferred to selecting a cut instant in the backend because the base request carries no occurrence identity and an implicit "now" boundary would be timezone- and race-sensitive. It also avoids creating a successor whose supplied historical `scheduledAt` regenerates past occurrences.

The guard belongs to `RecurringTaskSeriesService`, which owns series topology and can consult the occurrence-state owner. `TaskMutationService` invokes it for both full replacement and unscoped partial updates so HTTP and MCP receive the same guarantee. Recurrence-rule normalization is shared before comparison so aliases that normalize to the stored rule are not mistaken for generator changes.

### Resolve persisted detached state before RRULE validation

`TaskOccurrenceService` will resolve the state for `(seriesId, occurrenceScheduledAt)` first. An existing detached state is a valid mutation target by persistence identity. Any absent or attached state must still pass RRULE validation, preserving rejection of fabricated occurrence identities.

This resolution mechanism is shared by update, replacement, skip, completion, and reopening.

### Preserve detached identity when reopening

Reopening an attached completed occurrence with no overrides continues to delete its sparse state and restore a virtual occurrence. Reopening a detached completed occurrence cannot do that because no virtual occurrence exists. It therefore clears completion, changes status to `MODIFIED`, and keeps the detached row even when all override columns are null.

Deleting an open detached occurrence removes its state. Persisting `SKIPPED` would be contradictory because detached state overlays no generated occurrence and detached states are independently displayed.

## Risks / Trade-offs

- [Existing clients can receive a 400 for a formerly accepted unsafe edit] → The error is intentional protection against rewriting history; the existing following-series and truncation operations remain available.
- [A detached reopened occurrence uses `MODIFIED` without a field override] → Its persisted row is the materialization that keeps the standalone occurrence visible; tests document this special state.
- [Concurrent creation of the first occurrence state can race with the in-place guard] → Both operations run transactionally; this change does not add pessimistic locking. A database-level cross-table constraint is not practical, so concurrency hardening can be added separately if observed.
