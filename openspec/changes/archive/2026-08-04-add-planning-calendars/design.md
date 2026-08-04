## Context

Taska projects currently have no planning context. Tasks store a `scheduledAt` instant, but the system cannot determine whether that instant is appropriate for the project. The backend already has one configured application calendar time zone, and projects, tasks, recurring-task instances, REST endpoints, MCP tools, and Angular clients all participate in scheduling writes.

## Goals / Non-Goals

**Goals:**

- Model named planning calendars and their recurring weekly availability.
- Associate every project with exactly one calendar, including the existing inbox and all migrated projects.
- Make calendar rules manageable and visible in the GUI.
- Reject explicit task scheduling writes outside the associated project's availability.
- Preserve all existing data by assigning it to a 24/7 default calendar.

**Non-Goals:**

- AI task selection, slot recommendations, or automatic rescheduling.
- Capacity calculation, overlap detection, or reserving a task's `estimateMinutes`.
- Date-specific exceptions, holidays, leave, or temporary availability overrides.
- Calendar deletion, cross-midnight availability intervals, and per-calendar time zones.
- Validating future instants generated from a recurring rule beyond an explicitly written base or occurrence schedule.

## Decisions

### Use one planning calendar per project

Add a `PlanningCalendar` aggregate and a required `planning_calendar_id` on projects. A calendar owns one or more weekly rules. The project reference is singular rather than a join table: the user has confirmed that a project belongs to only one calendar.

New projects default to the system Default Calendar when a calendar is not supplied. A project calendar change is rejected if it would leave an existing explicitly scheduled task outside the new calendar's rules.

Alternatives considered:

- Many-to-many project calendars: rejected as unnecessary ambiguity for scheduling authorization.
- Calendar on each task: rejected because project association is the requested policy boundary and prevents inconsistent tasks in one project.

### Model weekly intervals as same-day minute ranges

An availability rule stores a weekday plus an inclusive start and exclusive end minute in the configured application calendar time zone. Values must satisfy `0 <= start < end <= 1440`; `1440` represents `24:00`, allowing the Default Calendar to express a full local day without a special case. Rules must not overlap for a calendar and weekday. Intervals spanning midnight are represented by two adjacent day rules and are not accepted directly in this version.

This creates a simple, DST-safe authorization check: convert an instant to the configured zone, find the rule for its local weekday, and test its local minute. A timed task is valid when its start instant is within a rule. An all-day task is valid when the calendar has at least one rule on its local date; task-duration and capacity semantics are intentionally deferred.

Alternatives considered:

- Store UTC instants: rejected because weekly availability is a local civil-time concept and would shift across DST.
- Store arbitrary RRULE availability: rejected because it is unnecessarily complex before exceptions are introduced.

### Enforce availability at task scheduling writes

Centralize authorization in the task application service so REST, MCP, and GUI writes cannot bypass it. Validate an explicit non-null `scheduledAt` on task create and update, and an explicit occurrence schedule override. Existing scheduled values are left valid by the 24/7 default migration.

Recurring series keep their existing generation behaviour. This change validates explicitly written timestamps, not every future generated recurrence; a recurrence-aware policy can be added with capacity and AI planning later.

### Provide management-first visibility in the GUI

Add a Planning Calendars management view that lists calendars, creates and renames them, and presents weekly rules in a readable weekly grid/list. Add a calendar selector to the project editor and show the assigned calendar there. This makes rules visible and editable without expanding existing task calendar views into a capacity planner.

## Risks / Trade-offs

- [Changing a project's calendar can conflict with already scheduled tasks] → Reject the reassignment and report the affected tasks until they are rescheduled or a compatible calendar is chosen.
- [A 24/7 default provides no immediate behavioural restriction] → It preserves existing data and users can progressively move projects to purpose-specific calendars.
- [No exceptions means temporary unavailability cannot be represented] → Keep rule ownership and validation services extensible for a later date-override model.
- [Recurring tasks can produce future occurrences outside a weekly rule] → Explicitly scope recurrence-wide validation out of this first version and address it before AI scheduling is introduced.

## Migration Plan

1. Create planning-calendar and availability-rule tables and add a nullable project calendar reference.
2. Insert one `Default Calendar` with seven full-day (`00:00`–`24:00`) rules.
3. Update every existing project to reference that calendar.
4. Make the project calendar reference non-null and add its foreign key.
5. Deploy API and GUI changes. Rollback code if needed; retain the default calendar and project association because they preserve existing behaviour.

## Open Questions

- None for this first release. Exception rules and AI scheduling are explicitly follow-up work.
