package com.taska.domain.planningcalendar.controller;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PlanningCalendarCreateRequest(@NotBlank String name, List<Rule> rules) {

    public record Rule(int dayOfWeek, int startMinute, int endMinute) {
    }
}
