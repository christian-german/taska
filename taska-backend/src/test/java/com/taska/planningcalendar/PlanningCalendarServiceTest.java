package com.taska.planningcalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.taska.planningcalendar.application.PlanningCalendarService;
import com.taska.planningcalendar.model.PlanningCalendar;
import com.taska.planningcalendar.model.PlanningCalendarCreateParameters;
import com.taska.planningcalendar.model.PlanningCalendarRule;
import com.taska.planningcalendar.model.PlanningCalendarRuleParameters;
import com.taska.planningcalendar.model.PlanningCalendarUpdateParameters;
import com.taska.planningcalendar.persistence.PlanningCalendarRepository;
import com.taska.planningcalendar.persistence.PlanningCalendarRuleRepository;
import com.taska.platform.config.TaskaProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanningCalendarServiceTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    PlanningCalendarRepository calendars;
    @Mock
    PlanningCalendarRuleRepository rules;
    @Mock
    TaskaProperties properties;
    @Mock
    TaskaProperties.Calendar calendarProperties;
    @InjectMocks
    PlanningCalendarService planningCalendarService;

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
    void validatesOverlappingRulesAtTheServiceBoundary() throws NoSuchMethodException {
        PlanningCalendarCreateParameters planningCalendarParameters = new PlanningCalendarCreateParameters(
                "Work",
                List.of(new PlanningCalendarRuleParameters(1, 540, 720), new PlanningCalendarRuleParameters(1, 600, 780)));
        Method createMethod = PlanningCalendarService.class.getMethod("create", PlanningCalendarCreateParameters.class);

        Set<ConstraintViolation<PlanningCalendarService>> violations = validator.forExecutables()
                .validateParameters(planningCalendarService, createMethod, new Object[]{planningCalendarParameters});

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("availability rules must not overlap");
        verifyNoInteractions(calendars, rules);
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

        var details = planningCalendarService.update(id, new PlanningCalendarUpdateParameters("Always", List.of()));

        assertThat(details.name()).isEqualTo("Always");
        assertThat(details.rules()).isEmpty();
        verify(rules).deleteByCalendarId(id);
        verify(rules).saveAll(List.of());
    }
}
