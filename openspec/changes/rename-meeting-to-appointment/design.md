## Context

Taska currently represents scheduled work with the `MEETING` task-type enum value and meeting-specific copy in the backend, web client, Android client, APIs, tests, and priority-evaluation logic. The requested terminology changes the persisted and externally exposed value, so it must preserve the meaning of existing records while avoiding mixed vocabulary after deployment.

## Goals / Non-Goals

**Goals:**

- Replace `MEETING` with `APPOINTMENT` consistently in code, API contracts, UI copy, accessibility text, and task-type-dependent behavior.
- Migrate existing persisted `MEETING` values to `APPOINTMENT` before code that only recognizes the new value is deployed.
- Preserve the existing appointment indicator and priority-evaluation eligibility behavior.

**Non-Goals:**

- Change the visual design, interaction flow, or task-type selection model.
- Support `MEETING` as a backward-compatible public API value after the release.
- Alter the criteria or scheduling strategy used for priority evaluation.

## Decisions

### Use `APPOINTMENT` as the sole canonical task-type value

The backend enum, request/response DTOs, MCP contract, and client models will use `APPOINTMENT`; labels will use “Appointment” and the established French equivalent “Rendez-vous.” This makes terminology consistent at every boundary. Retaining `MEETING` as an API alias was considered, but rejected because it leaves a permanently ambiguous public contract and conflicts with the requested rename.

### Migrate stored values in the database

Add a versioned database migration that converts every persisted `MEETING` value to `APPOINTMENT`, coordinated with the enum/schema change. A data migration preserves classification for existing tasks. Resetting affected tasks to `TODO` was rejected because it changes user data, while a runtime dual-value compatibility layer was rejected because it prolongs legacy terminology and adds unnecessary branching.

### Preserve behavioral semantics under the new name

All checks that previously excluded or invalidated `MEETING` tasks—including priority evaluation—will instead use `APPOINTMENT`. Presentation keeps the existing non-color-only indicator/icon but changes its accessible and visible appointment terminology.

## Risks / Trade-offs

- [Clients deployed before the coordinated release can send `MEETING`] → Version the release deliberately and treat the enum rename as a breaking API change; update supported clients in the same change.
- [A migration or schema constraint is applied in the wrong order] → Validate the migration against representative existing data and ensure it executes before the old enum value is removed.
- [Residual user-facing or accessibility strings remain] → Search the full repository for both enum values and meeting terminology, then cover backend and client behavior with tests.

## Migration Plan

1. Add and test the data migration converting existing task types from `MEETING` to `APPOINTMENT`.
2. Update the backend model, persistence mapping, REST/MCP representations, clients, and priority-evaluation checks to use only `APPOINTMENT`.
3. Run backend, Android, and web test suites plus a repository-wide terminology search.
4. Deploy the migration and compatible application release together. Roll back application code only with a migration-aware compatibility release; do not revert converted records without an explicit reverse migration.

## Open Questions

None.
