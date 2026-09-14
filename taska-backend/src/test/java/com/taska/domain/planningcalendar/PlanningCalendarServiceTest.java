package com.taska.domain.planningcalendar;

import com.taska.config.TaskaProperties;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRepository;
import com.taska.domain.planningcalendar.repository.PlanningCalendarRuleRepository;
import com.taska.domain.planningcalendar.service.PlanningCalendarCreateParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarRuleParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.planningcalendar.service.PlanningCalendarUpdateParameters;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlanningCalendarServiceTest {
    @Mock PlanningCalendarRepository calendars;
    @Mock PlanningCalendarRuleRepository rules;
    @Mock TaskaProperties properties;
    @Mock TaskaProperties.Calendar calendarProperties;
    @InjectMocks PlanningCalendarService planningCalendarService;

    @Test
    void allowsAnInstantWithinItsWeeklyRule() {
        when(properties.getCalendar()).thenReturn(calendarProperties);
        when(calendarProperties.getTimeZone()).thenReturn(ZoneId.of("Europe/Paris"));
        UUID id = UUID.randomUUID();
        PlanningCalendarRule rule = new PlanningCalendarRule();
        rule.setDayOfWeek(1);
        rule.setStartMinute(9 * 60);
        rule.setEndMinute(17 * 60);
        when(rules.findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(id)).thenReturn(List.of(rule));

        assertThat(planningCalendarService.allows(id, Instant.parse("2026-05-18T08:00:00Z"), false)).isTrue();
        assertThat(planningCalendarService.allows(id, Instant.parse("2026-05-18T16:00:00Z"), false)).isFalse();
    }

    @Test
    void rejectsOverlappingRulesOnCreate() {
        PlanningCalendar calendar = new PlanningCalendar();
        calendar.setId(UUID.randomUUID());
        when(calendars.save(org.mockito.ArgumentMatchers.any())).thenReturn(calendar);
        PlanningCalendarCreateParameters planningCalendarRequest = new PlanningCalendarCreateParameters("Work", List.of(
                new PlanningCalendarRuleParameters(1, 540, 720),
                new PlanningCalendarRuleParameters(1, 600, 780)));

        assertThatThrownBy(() -> planningCalendarService.create(planningCalendarRequest)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    void updateWithAnEmptyRuleListClearsExistingRules() {
        UUID id = UUID.randomUUID();
        PlanningCalendar calendar = new PlanningCalendar();
        calendar.setId(id);
        calendar.setName("Old");
        when(calendars.findById(id)).thenReturn(java.util.Optional.of(calendar));
        when(calendars.save(calendar)).thenReturn(calendar);
        when(rules.findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(id)).thenReturn(List.of());

        var details = planningCalendarService.update(
                id, new PlanningCalendarUpdateParameters("Always", List.of()));

        assertThat(details.name()).isEqualTo("Always");
        assertThat(details.rules()).isEmpty();
        verify(rules).deleteByCalendarId(id);
        verify(rules).saveAll(List.of());
    }
}
