package com.taska.domain.planningcalendar.service;

import com.taska.config.TaskaProperties;
import com.taska.domain.planningcalendar.repository.PlanningCalendar;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRule;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRepository;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRuleRepository;
import com.taska.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class PlanningCalendarService {

    public static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PlanningCalendarRepository planningCalendarRepository;
    private final PlanningCalendarRuleRepository planningCalendarRuleRepository;
    private final TaskaProperties taskaProperties;

    @Transactional(readOnly = true)
    public List<PlanningCalendarDetails> findAll() {
        return planningCalendarRepository.findAll().stream()
                .map(this::toDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanningCalendarDetails get(@NotNull UUID planningCalendarId) {
        return toDetails(planningCalendarRepository.findById(planningCalendarId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Planning calendar not found: " + planningCalendarId)));
    }

    public PlanningCalendarDetails create(@NotNull @Valid PlanningCalendarCreateParameters planningCalendarCreateParameters) {
        PlanningCalendar planningCalendar = new PlanningCalendar();
        planningCalendar.setName(planningCalendarCreateParameters.name());
        PlanningCalendar savedPlanningCalendar = planningCalendarRepository.save(planningCalendar);
        replaceRules(savedPlanningCalendar.getId(), planningCalendarCreateParameters.rules());
        return get(savedPlanningCalendar.getId());
    }

    public PlanningCalendarDetails update(@NotNull UUID planningCalendarId, @NotNull @Valid PlanningCalendarUpdateParameters planningCalendarUpdateParameters) {
        PlanningCalendar planningCalendar = planningCalendarRepository.findById(planningCalendarId)
                .orElseThrow(() -> new ResourceNotFoundException("Planning calendar not found: " + planningCalendarId));
        planningCalendar.setName(planningCalendarUpdateParameters.name());
        planningCalendarRepository.save(planningCalendar);
        replaceRules(planningCalendarId, planningCalendarUpdateParameters.rules());
        return get(planningCalendarId);
    }

    @Transactional(readOnly = true)
    public boolean allows(@NotNull UUID planningCalendarId, @NotNull Instant scheduledAt, boolean allDay) {

        ZoneId timeZone = taskaProperties.getCalendar().getTimeZone();
        ZonedDateTime scheduledDateTime = scheduledAt.atZone(timeZone);
        List<PlanningCalendarRule> calendarRules = planningCalendarRuleRepository
                .findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(planningCalendarId);
        if (allDay) {
            return calendarRules.stream()
                    .anyMatch(rule -> rule.getDayOfWeek() == scheduledDateTime.getDayOfWeek().getValue());
        }
        int scheduledMinute = scheduledDateTime.getHour() * 60 + scheduledDateTime.getMinute();
        return calendarRules.stream()
                .anyMatch(rule -> rule.getDayOfWeek() == scheduledDateTime.getDayOfWeek().getValue()
                        && rule.getStartMinute() <= scheduledMinute
                        && scheduledMinute < rule.getEndMinute());
    }

    private void replaceRules(UUID planningCalendarId, List<PlanningCalendarRuleParameters> requestedRules) {

        planningCalendarRuleRepository.deleteByCalendarId(planningCalendarId);
        planningCalendarRuleRepository.saveAll(requestedRules.stream()
                .map(requestedRule -> {
                    PlanningCalendarRule calendarRule = new PlanningCalendarRule();
                    calendarRule.setCalendarId(planningCalendarId);
                    calendarRule.setDayOfWeek(requestedRule.dayOfWeek());
                    calendarRule.setStartMinute(requestedRule.startMinute());
                    calendarRule.setEndMinute(requestedRule.endMinute());
                    return calendarRule;
                })
                .toList());
    }

    private PlanningCalendarDetails toDetails(PlanningCalendar planningCalendar) {

        return new PlanningCalendarDetails(
                planningCalendar.getId(),
                planningCalendar.getName(),
                planningCalendarRuleRepository
                        .findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(planningCalendar.getId())
                        .stream()
                        .map(rule -> new PlanningCalendarRuleParameters(
                                rule.getDayOfWeek(),
                                rule.getStartMinute(),
                                rule.getEndMinute()))
                        .toList());
    }
}
