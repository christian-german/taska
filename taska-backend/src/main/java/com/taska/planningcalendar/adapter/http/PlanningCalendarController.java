package com.taska.planningcalendar.adapter.http;

import com.taska.planningcalendar.application.PlanningCalendarService;
import com.taska.planningcalendar.model.PlanningCalendarCreateParameters;
import com.taska.planningcalendar.model.PlanningCalendarUpdateParameters;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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

@RestController
@RequestMapping("/planning-calendars")
@RequiredArgsConstructor
public class PlanningCalendarController {

    private final PlanningCalendarService planningCalendarService;
    private final PlanningCalendarMapper planningCalendarMapper;

    @GetMapping
    public List<PlanningCalendarDto> getAll() {
        return planningCalendarService.findAll().stream().map(planningCalendarMapper::toDto).toList();
    }

    @GetMapping("/{planningCalendarId}")
    public PlanningCalendarDto getById(@PathVariable UUID planningCalendarId) {
        return planningCalendarMapper.toDto(planningCalendarService.findById(planningCalendarId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningCalendarDto create(@Valid @RequestBody PlanningCalendarCreateRequest planningCalendarCreateRequest) {
        PlanningCalendarCreateParameters planningCalendarCreateParameters = planningCalendarMapper.toParameters(planningCalendarCreateRequest);
        return planningCalendarMapper.toDto(planningCalendarService.create(planningCalendarCreateParameters));
    }

    @PutMapping("/{planningCalendarId}")
    public PlanningCalendarDto update(
            @PathVariable UUID planningCalendarId,
            @Valid @RequestBody PlanningCalendarUpdateRequest planningCalendarUpdateRequest) {
        PlanningCalendarUpdateParameters planningCalendarUpdateParameters = planningCalendarMapper.toParameters(planningCalendarUpdateRequest);
        return planningCalendarMapper.toDto(planningCalendarService.replace(planningCalendarId, planningCalendarUpdateParameters));
    }
}
