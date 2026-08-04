## Why

Projects currently have no scheduling context, so Taska cannot distinguish work time from personal time or prevent tasks from being planned outside an appropriate period. Planning calendars provide a durable, visible source of scheduling authorization that future AI planning can rely on.

## What Changes

- Introduce planning calendars with a name and recurring weekly availability rules.
- Require every project to belong to exactly one planning calendar.
- Create a `Default Calendar` with 24/7 availability and associate all existing projects with it during migration.
- Provide UI and API support to create, rename, view, and edit planning calendars and their availability rules, and to select a calendar for a project.
- Enforce availability as a hard constraint whenever a task's scheduled time is created or changed.
- Defer date-specific exceptions, holidays, capacity/free-slot calculation, and AI-assisted scheduling to later changes.

## Capabilities

### New Capabilities

- `planning-calendar-management`: Planning calendar lifecycle, project association, weekly availability-rule management, migration, and GUI visibility.

### Modified Capabilities

- `task-scheduling-and-priority-fields`: Scheduled task writes must be authorized by the assigned project's planning calendar availability.
- `calendar-timezone-configuration`: The configured application calendar time zone defines the local interpretation of recurring planning-calendar availability rules.

## Impact

- Backend data model, Flyway migration, project and task services/controllers, and validation tests.
- Frontend models, API services, project editor, and a planning-calendar management UI.
- Existing projects receive a non-restrictive default calendar, so existing task timestamps remain valid.
- Existing task APIs keep their timestamp contract; invalid schedule writes gain a validation error.
