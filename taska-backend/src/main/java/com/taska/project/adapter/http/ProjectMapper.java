package com.taska.project.adapter.http;

import com.taska.platform.config.ApiMapperConfig;
import com.taska.project.model.Project;
import com.taska.project.model.ProjectCreateParameters;
import com.taska.project.model.ProjectReorderParameters;
import com.taska.project.model.ProjectUpdateParameters;
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
