package com.taska.domain.planningcalendar.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taska.domain.planningcalendar.NonOverlappingPlanningCalendarRules;
import com.taska.domain.planningcalendar.PlanningCalendarRuleValue;
import com.taska.domain.planningcalendar.ValidPlanningCalendarRuleRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Complete mutable representation used to replace a planning calendar. */
public record PlanningCalendarUpdateRequest(
    @JsonProperty(required = true) @NotBlank String name,
    @JsonProperty(required = true) @NotNull @NonOverlappingPlanningCalendarRules
        List<@NotNull @Valid Rule> rules) {
  @ValidPlanningCalendarRuleRange
  public record Rule(
      @NotNull @Min(1) @Max(7) Integer dayOfWeek,
      @NotNull @Min(0) @Max(1_439) Integer startMinute,
      @NotNull @Min(1) @Max(1_440) Integer endMinute)
      implements PlanningCalendarRuleValue {}
}
