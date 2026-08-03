## 1. Configuration

- [x] 1.1 Add `taska.calendar.time-zone=Europe/Paris` to the backend `application.properties`.
- [x] 1.2 Extend `TaskaProperties` with a typed calendar `ZoneId` property so Spring validates the configured IANA time zone at startup.

## 2. Calendar availability

- [x] 2.1 Inject the configured calendar zone into `TaskService` and replace UTC local-day boundary calculations for the `today`, `overdue`, and `upcoming` filters.
- [x] 2.2 Use the configured calendar zone to derive inclusive start and exclusive end instants for explicit calendar date-range occurrence queries.
- [x] 2.3 Preserve `Instant` persistence and API representations; do not change database timestamp settings or task payload fields.

## 3. Verification

- [x] 3.1 Add unit tests proving named filters and calendar date-range queries include and exclude tasks at configured local-day boundaries.
- [x] 3.2 Add a daylight-saving-transition test confirming date boundaries are derived from successive local midnights rather than a fixed 24-hour duration.
- [x] 3.3 Run the relevant backend test suite and confirm valid configuration binding succeeds.
