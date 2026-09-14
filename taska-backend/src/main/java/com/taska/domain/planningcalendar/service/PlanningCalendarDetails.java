package com.taska.domain.planningcalendar.service;

import java.util.List;
import java.util.UUID;

/** Immutable application result containing a calendar and its ordered rules. */
public record PlanningCalendarDetails(
        UUID id,
        String name,
        List<PlanningCalendarRuleParameters> rules
) {}
