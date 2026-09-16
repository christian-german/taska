package com.taska.domain.project.controller;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.project.repository.Project;
import com.taska.domain.project.service.ProjectCreateParameters;
import com.taska.domain.project.service.ProjectReorderParameters;
import com.taska.domain.project.service.ProjectUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
public interface ProjectMapper {
  @Mapping(target = "order", source = "position")
  ProjectDto toDto(Project project);

  @Mapping(target = "color", defaultValue = "#808080")
  @Mapping(target = "position", source = "order", defaultValue = "0")
  @Mapping(target = "favorite", source = "isFavorite", defaultValue = "false")
  @Mapping(target = "viewStyle", defaultValue = "LIST")
  ProjectCreateParameters toParameters(ProjectCreateRequest projectCreateRequest);

  @Mapping(target = "position", source = "order")
  @Mapping(target = "favorite", source = "isFavorite")
  ProjectUpdateParameters toParameters(ProjectUpdateRequest projectUpdateRequest);

  @Mapping(target = "position", source = "order")
  @Mapping(target = "projectId", source = "id")
  ProjectReorderParameters toParameters(ProjectReorderRequest projectReorderRequest);
}
