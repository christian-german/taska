package com.taska.project.model;

import java.util.UUID;

/**
 * Signals the intent to move a project onto another planning calendar, before it is applied.
 *
 * <p>
 * Listeners run synchronously inside the publishing transaction and may reject the change by throwing. The project module owns the calendar
 * assignment but not the rule that scheduled work must fit its calendar; whoever owns that rule vetoes here.
 *
 * @param projectId project whose planning calendar would change
 * @param planningCalendarId planning calendar the project would move to
 */
public record ProjectPlanningCalendarChangeRequested(UUID projectId, UUID planningCalendarId) {
}
