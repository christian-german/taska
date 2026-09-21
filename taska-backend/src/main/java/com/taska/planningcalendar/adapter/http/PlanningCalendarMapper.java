package com.taska.planningcalendar.adapter.http;

import com.taska.planningcalendar.model.PlanningCalendarCreateParameters;
import com.taska.planningcalendar.model.PlanningCalendarDetails;
import com.taska.planningcalendar.model.PlanningCalendarRuleParameters;
import com.taska.planningcalendar.model.PlanningCalendarUpdateParameters;
import com.taska.platform.config.ApiMapperConfig;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(config = ApiMapperConfig.class)
public interface PlanningCalendarMapper {

    PlanningCalendarDto toDto(PlanningCalendarDetails planningCalendarDetails);

    PlanningCalendarDto.Rule toDto(PlanningCalendarRuleParameters planningCalendarRuleParameters);

    PlanningCalendarCreateParameters toParameters(PlanningCalendarCreateRequest planningCalendarCreateRequest);

    PlanningCalendarUpdateParameters toParameters(PlanningCalendarUpdateRequest planningCalendarUpdateRequest);

    PlanningCalendarRuleParameters toParameters(PlanningCalendarCreateRequest.Rule rule);

    PlanningCalendarRuleParameters toParameters(PlanningCalendarUpdateRequest.Rule rule);

    /** Absent rules on creation mean an empty calendar, never a null rule list. */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<PlanningCalendarRuleParameters> toParameters(List<PlanningCalendarCreateRequest.Rule> rules);
}
