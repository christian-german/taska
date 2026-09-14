package com.taska.domain.planningcalendar.service;

import java.util.List;

/** Application parameters for creating a planning calendar. */
public record PlanningCalendarCreateParameters(
        String name,
        List<PlanningCalendarRuleParameters> rules
) {}
