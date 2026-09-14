package com.taska.domain.planningcalendar.service;

import com.taska.config.TaskaProperties;
import com.taska.domain.planningcalendar.PlanningCalendar;
import com.taska.domain.planningcalendar.PlanningCalendarRule;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRepository;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRuleRepository;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanningCalendarService {

    public static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PlanningCalendarRepository planningCalendarRepository;
    private final PlanningCalendarRuleRepository planningCalendarRuleRepository;
    private final TaskaProperties taskaProperties;

    @Transactional(readOnly = true)
    public List<PlanningCalendarDetails> all() {
        return planningCalendarRepository.findAll().stream()
                .map(this::toDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanningCalendarDetails get(UUID planningCalendarId) {
        return toDetails(findEntity(planningCalendarId));
    }

    public PlanningCalendarDetails create(PlanningCalendarCreateParameters planningCalendarCreateParameters) {
        PlanningCalendar planningCalendar = new PlanningCalendar();
        planningCalendar.setName(planningCalendarCreateParameters.name());
        PlanningCalendar savedPlanningCalendar = planningCalendarRepository.save(planningCalendar);
        replaceRules(savedPlanningCalendar.getId(), planningCalendarCreateParameters.rules());
        return get(savedPlanningCalendar.getId());
    }

    public PlanningCalendarDetails update(
            UUID planningCalendarId,
            PlanningCalendarUpdateParameters planningCalendarUpdateParameters) {
        PlanningCalendar planningCalendar = findEntity(planningCalendarId);
        planningCalendar.setName(planningCalendarUpdateParameters.name());
        planningCalendarRepository.save(planningCalendar);
        replaceRules(planningCalendarId, planningCalendarUpdateParameters.rules());
        return get(planningCalendarId);
    }

    @Transactional(readOnly = true)
    public boolean allows(UUID planningCalendarId, Instant scheduledAt, boolean allDay) {
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

    private void replaceRules(
            UUID planningCalendarId,
            List<PlanningCalendarRuleParameters> requestedRules) {
        validateRules(requestedRules);
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

    private void validateRules(List<PlanningCalendarRuleParameters> requestedRules) {
        for (PlanningCalendarRuleParameters rule : requestedRules) {
            if (rule.dayOfWeek() < 1
                    || rule.dayOfWeek() > 7
                    || rule.startMinute() < 0
                    || rule.startMinute() >= rule.endMinute()
                    || rule.endMinute() > 1_440) {
                throw new IllegalArgumentException("Invalid availability rule");
            }
        }
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            final int currentDayOfWeek = dayOfWeek;
            List<PlanningCalendarRuleParameters> rulesForDay = requestedRules.stream()
                    .filter(rule -> rule.dayOfWeek() == currentDayOfWeek)
                    .sorted(Comparator.comparingInt(PlanningCalendarRuleParameters::startMinute))
                    .toList();
            for (int ruleIndex = 1; ruleIndex < rulesForDay.size(); ruleIndex++) {
                if (rulesForDay.get(ruleIndex - 1).endMinute()
                        > rulesForDay.get(ruleIndex).startMinute()) {
                    throw new IllegalArgumentException("Availability rules overlap");
                }
            }
        }
    }

    private PlanningCalendar findEntity(UUID planningCalendarId) {
        return planningCalendarRepository.findById(planningCalendarId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Planning calendar not found: " + planningCalendarId));
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
