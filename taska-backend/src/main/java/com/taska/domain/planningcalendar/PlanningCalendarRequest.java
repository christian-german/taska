package com.taska.domain.planningcalendar;
import jakarta.validation.constraints.NotBlank; import java.util.List;
public record PlanningCalendarRequest(@NotBlank String name, List<Rule> rules) { public record Rule(int dayOfWeek, int startMinute, int endMinute) {} }
