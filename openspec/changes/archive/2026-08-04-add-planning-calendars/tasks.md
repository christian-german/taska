## 1. Persistence and migration

- [x] 1.1 Add planning-calendar and weekly availability-rule persistence models, repositories, and a Flyway migration.
- [x] 1.2 Add the required project-to-planning-calendar reference and migrate all existing projects to a `Default Calendar` with seven `00:00`–`24:00` rules.
- [x] 1.3 Enforce rule interval validity and non-overlap for each calendar weekday.

## 2. Backend calendar and project APIs

- [x] 2.1 Implement planning-calendar list, retrieve, create, rename, and availability-rule update services and REST endpoints.
- [x] 2.2 Extend project create, update, and response contracts with the single planning-calendar association; assign the Default Calendar when absent.
- [x] 2.3 Preserve the calendar association in project representations exposed through existing REST and MCP project paths without adding AI scheduling tools.
- [x] 2.4 Reject project calendar reassignment when the project's existing explicit scheduled tasks are not authorized by the target calendar.

## 3. Scheduling authorization

- [x] 3.1 Implement a calendar-availability authorization service that evaluates local weekday and minute ranges using `taska.calendar.time-zone`.
- [x] 3.2 Enforce authorization for explicit `scheduledAt` task create and update writes, including explicit recurring-occurrence schedule overrides.
- [x] 3.3 Return clear validation errors for schedule writes outside availability while retaining existing `Instant` API and persistence contracts.

## 4. Frontend management

- [x] 4.1 Add planning-calendar frontend models and API service methods.
- [x] 4.2 Create a Planning Calendars management view for listing calendars and creating, renaming, and editing visible weekly availability rules.
- [x] 4.3 Add navigation to the management view and a planning-calendar selector to the project create/edit UI.

## 5. Verification

- [x] 5.1 Add backend tests for default-calendar migration, rule validation, calendar CRUD, project assignment, and incompatible reassignment rejection.
- [x] 5.2 Add task-service tests for allowed and rejected scheduled times in the configured time zone, including DST-local-time boundaries and occurrence overrides.
- [x] 5.3 Add frontend tests for calendar rule visibility/editing and project calendar selection.
- [x] 5.4 Run the relevant backend and frontend test suites and strict OpenSpec validation.
