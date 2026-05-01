package com.taska.domain.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    @Mapping(target = "order", source = "position")
    ProjectDto toDto(Project project);
}
