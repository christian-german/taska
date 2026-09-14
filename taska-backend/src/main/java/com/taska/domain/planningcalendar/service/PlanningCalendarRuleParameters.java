package com.taska.domain.planningcalendar.service;

/** Application representation of one weekly planning-calendar window. */
public record PlanningCalendarRuleParameters(int dayOfWeek, int startMinute, int endMinute) {}
