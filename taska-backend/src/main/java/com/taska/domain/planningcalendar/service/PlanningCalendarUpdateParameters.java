package com.taska.domain.planningcalendar.service;

import java.util.List;

/** Application parameters for replacing a planning calendar's mutable state. */
public record PlanningCalendarUpdateParameters(
        String name,
        List<PlanningCalendarRuleParameters> rules
) {}
