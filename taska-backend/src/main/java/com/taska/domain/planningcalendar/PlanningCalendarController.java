package com.taska.domain.planningcalendar;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/planning-calendars")
@RequiredArgsConstructor
public class PlanningCalendarController {

    private final PlanningCalendarService planningCalendarService;

    @GetMapping
    public List<PlanningCalendarDto> all() {
        return planningCalendarService.all();
    }

    @GetMapping("/{planningCalendarId}")
    public PlanningCalendarDto get(@PathVariable UUID planningCalendarId) {
        return planningCalendarService.get(planningCalendarId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningCalendarDto create(@Valid @RequestBody PlanningCalendarRequest planningCalendarRequest) {
        return planningCalendarService.create(planningCalendarRequest);
    }

    @PutMapping("/{planningCalendarId}")
    public PlanningCalendarDto update(
            @PathVariable UUID planningCalendarId,
            @Valid @RequestBody PlanningCalendarRequest planningCalendarRequest) {
        return planningCalendarService.update(planningCalendarId, planningCalendarRequest);
    }
}
