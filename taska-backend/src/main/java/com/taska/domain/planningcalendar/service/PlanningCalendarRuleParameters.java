package com.taska.domain.planningcalendar.service;

import com.taska.domain.planningcalendar.PlanningCalendarRuleValue;
import com.taska.domain.planningcalendar.ValidPlanningCalendarRuleRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Application representation of one weekly planning-calendar window. */
@ValidPlanningCalendarRuleRange
public record PlanningCalendarRuleParameters(
    @NotNull @Min(1) @Max(7) Integer dayOfWeek,
    @NotNull @Min(0) @Max(1_439) Integer startMinute,
    @NotNull @Min(1) @Max(1_440) Integer endMinute)
    implements PlanningCalendarRuleValue {}
