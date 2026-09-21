package com.taska.planningcalendar.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Application parameters for creating a planning calendar. */
public record PlanningCalendarCreateParameters(@NotBlank String name,
        @NotNull @NonOverlappingPlanningCalendarRules List<@NotNull @Valid PlanningCalendarRuleParameters> rules) {
}
