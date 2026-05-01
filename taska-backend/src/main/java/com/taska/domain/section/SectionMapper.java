package com.taska.domain.section;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SectionMapper {
    @Mapping(target = "order", source = "position")
    SectionDto toDto(Section section);
}
