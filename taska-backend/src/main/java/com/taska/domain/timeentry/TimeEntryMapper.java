package com.taska.domain.timeentry;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimeEntryMapper {
    TimeEntryDto toDto(TimeEntry entry);
}
