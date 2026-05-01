package com.taska.domain.filter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FilterMapper {
    @Mapping(target = "order", source = "position")
    FilterDto toDto(Filter filter);
}
