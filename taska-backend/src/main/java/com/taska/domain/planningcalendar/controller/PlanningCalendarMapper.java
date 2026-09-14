package com.taska.domain.planningcalendar.controller;

import com.taska.domain.planningcalendar.service.PlanningCalendarCreateParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarDetails;
import com.taska.domain.planningcalendar.service.PlanningCalendarRuleParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlanningCalendarMapper {

    default PlanningCalendarDto toDto(PlanningCalendarDetails planningCalendarDetails) {
        return new PlanningCalendarDto(
                planningCalendarDetails.id(),
                planningCalendarDetails.name(),
                planningCalendarDetails.rules().stream()
                        .map(rule -> new PlanningCalendarDto.Rule(
                                rule.dayOfWeek(), rule.startMinute(), rule.endMinute()))
                        .toList());
    }

    default PlanningCalendarCreateParameters toParameters(
            PlanningCalendarCreateRequest planningCalendarCreateRequest) {
        return new PlanningCalendarCreateParameters(
                planningCalendarCreateRequest.name(),
                planningCalendarCreateRequest.rules() == null
                        ? java.util.List.of()
                        : planningCalendarCreateRequest.rules().stream()
                                .map(rule -> new PlanningCalendarRuleParameters(
                                        rule.dayOfWeek(), rule.startMinute(), rule.endMinute()))
                                .toList());
    }

    default PlanningCalendarUpdateParameters toParameters(
            PlanningCalendarUpdateRequest planningCalendarUpdateRequest) {
        return new PlanningCalendarUpdateParameters(
                planningCalendarUpdateRequest.name(),
                planningCalendarUpdateRequest.rules().stream()
                        .map(rule -> new PlanningCalendarRuleParameters(
                                rule.dayOfWeek(), rule.startMinute(), rule.endMinute()))
                        .toList());
    }
}
