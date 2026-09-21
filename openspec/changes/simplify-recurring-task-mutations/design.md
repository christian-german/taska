## Context

The user approved a reduced recurrence model in conversation and requested implementation across backend, OpenAPI, web/Tauri, and Android, without GitHub. Calendar expansion, occurrence movement, completion, skipping and detached history remain necessary. No database split or package reorganization is required.

## Goals / Non-Goals

Goals: remove successor-series creation and scope selection for edits, freeze recurrence generators, restrict occurrence requests to scheduling, preserve calendar identity and history.

Non-goals: change generation into completion-driven repetition; pre-materialize future occurrences; reorganize packages; publish Git changes.

## Decisions

- Keep PUT on an occurrence but reduce its body to required nullable `scheduledAt`. Null restores the original schedule; it does not unschedule a recurring occurrence. Keep the existing all-day classification and duration inherited from the series.
- Remove PUT `/tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following`. The existing DELETE scope FROM_THIS becomes explicitly described as stopping the series. Validate the cut as a generated occurrence and never extend an already stopped series.
- Keep state identities and detached handling. Rescheduling preserves completion and existing state; reopening preserves movement. Lookup includes moved-in states regardless of completion, without duplicates.
- Enforce restrictions in the application owners as well as transport contracts, including MCP scoped updates. Forbidden requests fail before persistence or publication.
- Clients move occurrences directly; no edit-scope dialog. Occurrence details expose scheduling, completion and removal only. Stop-series wording replaces deletion-of-following wording.

## Risks / Trade-offs

- Breaking write contracts → update all maintained adapters and OpenAPI together; add rejection tests.
- Existing personalized occurrence data → retain title, priority and deadline overrides read-only; scheduling and lifecycle operations preserve them. No destructive migration.
- Series common-field editing → common properties remain editable; recurrence generator is frozen once a series exists.
- Historical overrides or moved completed occurrences → verify query, reopening and stopping behavior explicitly.

## Resolved Product Decisions

The user confirmed common-series editing with fixed recurrence and start date, and preservation of historical occurrence overrides in read-only form.
