package com.taska.domain.planningcalendar.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Complete mutable representation used to replace a planning calendar. */
public record PlanningCalendarUpdateRequest(
        @JsonProperty(required = true) @NotBlank String name,
        @JsonProperty(required = true) @NotNull List<@Valid Rule> rules
) {
    public record Rule(int dayOfWeek, int startMinute, int endMinute) {}
}
