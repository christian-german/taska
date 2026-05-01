package com.taska.domain.label;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
    @Mapping(target = "order", source = "position")
    LabelDto toDto(Label label);
}
