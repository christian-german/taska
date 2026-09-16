package com.taska.domain.planningcalendar.controller;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.planningcalendar.service.PlanningCalendarCreateParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarDetails;
import com.taska.domain.planningcalendar.service.PlanningCalendarRuleParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarUpdateParameters;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(config = ApiMapperConfig.class)
public interface PlanningCalendarMapper {

  PlanningCalendarDto toDto(PlanningCalendarDetails planningCalendarDetails);

  PlanningCalendarDto.Rule toDto(PlanningCalendarRuleParameters planningCalendarRuleParameters);

  PlanningCalendarCreateParameters toParameters(
      PlanningCalendarCreateRequest planningCalendarCreateRequest);

  PlanningCalendarUpdateParameters toParameters(
      PlanningCalendarUpdateRequest planningCalendarUpdateRequest);

  PlanningCalendarRuleParameters toParameters(PlanningCalendarCreateRequest.Rule rule);

  PlanningCalendarRuleParameters toParameters(PlanningCalendarUpdateRequest.Rule rule);

  /** Absent rules on creation mean an empty calendar, never a null rule list. */
  @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
  List<PlanningCalendarRuleParameters> toParameters(List<PlanningCalendarCreateRequest.Rule> rules);
}
