## Context

The backend persists scheduled times as UTC instants, but task calendar queries currently derive day boundaries with `ZoneOffset.UTC`. This makes availability for a requested `LocalDate` depend on UTC rather than the application's calendar locale. `TaskaProperties` is the existing typed configuration binding for `taska.*` properties and is the appropriate owner of an application-level calendar time zone.

## Goals / Non-Goals

**Goals:**

- Provide one explicit IANA `ZoneId` in `application.properties` for calendar availability.
- Apply that zone consistently to named date filters and date-range occurrence queries.
- Preserve existing persistence and API contracts based on `Instant`.
- Detect invalid configuration at application startup.

**Non-Goals:**

- Per-user or client-supplied time zones.
- Changing PostgreSQL, Hibernate, Jackson, or task timestamp storage settings.
- Reinterpreting existing timestamps or migrating task data.
- Changing recurrence rules beyond the calendar query boundaries supplied to them.

## Decisions

### Bind the value as `taska.calendar.time-zone`

Add `taska.calendar.time-zone=Europe/Paris` to the backend application properties and expose it through a `Calendar` section of `TaskaProperties` as a `ZoneId`. This keeps the property grouped with application behaviour rather than framework serialization settings, accepts standard IANA region IDs with daylight-saving rules, and lets Spring's conversion fail startup for invalid values.

Alternatives considered:

- `spring.jackson.time-zone`: rejected because it controls JSON serialization, not domain calendar availability.
- Fixed numeric offsets: rejected because they do not model daylight-saving changes.
- Reading a raw string at each call site: rejected because validation and use would be inconsistent.

### Convert local calendar boundaries only at the query boundary

Inject `TaskaProperties` into `TaskService`, then use `LocalDate.atStartOfDay(configuredZone).toInstant()` to form the inclusive start and exclusive end of date-based filters and calendar occurrence ranges. Repository and recurrence operations continue to receive `Instant` values.

This handles daylight-saving days correctly because a local day may not be exactly 24 hours. The range remains `[start, next-day start)`.

Alternatives considered:

- Continue using UTC and adjust results after querying: rejected because it can omit or include records at day edges and causes recurrence-range mismatches.
- Change persisted timestamps to local date-times: rejected because timestamps already represent absolute scheduled instants and would require data migration.

### Keep the setting application-wide

The configured zone applies to all task calendar availability paths in the backend, including the `today`, `overdue`, and `upcoming` named filters and explicit `date`/`from`/`to` occurrence requests. It does not alter timestamp payloads sent to clients.

## Risks / Trade-offs

- [Changing the configured zone changes which local date contains a boundary task] → Make the setting explicit, default it to the established `Europe/Paris` application locale, and cover boundary cases in tests.
- [DST transitions produce days shorter or longer than 24 hours] → Derive each boundary from `LocalDate.atStartOfDay(ZoneId)` rather than adding a fixed duration.
- [An invalid configured zone prevents startup] → Fail fast with Spring configuration binding so incorrect deployments do not silently calculate availability in an unintended zone.

## Migration Plan

1. Deploy the property with `Europe/Paris`, matching the historical timestamp migration assumption.
2. Deploy the typed configuration and calendar-query changes.
3. Validate calendar results at local midnight and across a DST transition.
4. To roll back, restore the previous application version; no data migration or data rollback is required.

## Open Questions

- None. A single deployment-configured application time zone is sufficient for this change.
