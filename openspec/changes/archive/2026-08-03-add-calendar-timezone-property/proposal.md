## Why

Calendar availability is currently calculated against fixed UTC day boundaries, so a task can appear on the wrong calendar date for the application's operating locale. The backend needs one explicit, configurable time zone so calendar-oriented queries consistently evaluate local days.

## What Changes

- Add a dedicated TimeZone property to the backend `application.properties` configuration.
- Use the configured IANA time zone when translating calendar date ranges and named calendar filters into instant boundaries.
- Keep persisted task timestamps and API timestamp values as instants; only calendar-day availability calculations become time-zone-aware.
- Validate the configured time zone during application startup so invalid values fail fast.

## Capabilities

### New Capabilities

- `calendar-timezone-configuration`: Configures the application time zone used to determine calendar-day availability.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Date-based task filtering and calendar occurrence queries use the configured calendar time zone rather than UTC day boundaries.

## Impact

- Backend configuration in `taska-backend/src/main/resources/application.properties`.
- Backend task query and calendar-occurrence services, plus their unit tests.
- No database migration, API field change, or client-side time-zone setting is required.
