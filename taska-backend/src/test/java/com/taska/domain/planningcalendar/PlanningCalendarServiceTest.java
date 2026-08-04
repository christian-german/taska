package com.taska.domain.planningcalendar;

import com.taska.config.TaskaProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningCalendarServiceTest {
    @Mock PlanningCalendarRepository calendars;
    @Mock PlanningCalendarRuleRepository rules;
    @Mock TaskaProperties properties;
    @Mock TaskaProperties.Calendar calendarProperties;
    @InjectMocks PlanningCalendarService service;

    @Test void allowsAnInstantWithinItsWeeklyRule() {
        when(properties.getCalendar()).thenReturn(calendarProperties);
        when(calendarProperties.getTimeZone()).thenReturn(ZoneId.of("Europe/Paris"));
        UUID id = UUID.randomUUID();
        PlanningCalendarRule rule = new PlanningCalendarRule();
        rule.setDayOfWeek(1); rule.setStartMinute(9 * 60); rule.setEndMinute(17 * 60);
        when(rules.findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(id)).thenReturn(List.of(rule));

        assertThat(service.allows(id, Instant.parse("2026-05-18T08:00:00Z"), false)).isTrue();
        assertThat(service.allows(id, Instant.parse("2026-05-18T16:00:00Z"), false)).isFalse();
    }

    @Test void rejectsOverlappingRulesOnCreate() {
        PlanningCalendar calendar = new PlanningCalendar(); calendar.setId(UUID.randomUUID());
        when(calendars.save(org.mockito.ArgumentMatchers.any())).thenReturn(calendar);
        PlanningCalendarRequest request = new PlanningCalendarRequest("Work", List.of(
                new PlanningCalendarRequest.Rule(1, 540, 720),
                new PlanningCalendarRequest.Rule(1, 600, 780)));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlap");
    }
}
