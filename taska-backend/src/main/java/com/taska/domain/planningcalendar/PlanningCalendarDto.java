package com.taska.domain.planningcalendar;
import java.util.List; import java.util.UUID;
public record PlanningCalendarDto(UUID id, String name, List<Rule> rules) { public record Rule(int dayOfWeek, int startMinute, int endMinute) {} }
