package com.taska.project.adapter.mcp;

import com.taska.platform.config.ApiMapperConfig;
import com.taska.project.model.Project;
import com.taska.project.model.ProjectCreateParameters;
import com.taska.project.model.ProjectUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps MCP tool inputs to the project service contract. */
@Mapper(config = ApiMapperConfig.class)
public interface ProjectMcpMapper {

    /**
     * The tool contract exposes no planning calendar, so a created project has none.
     */
    @Mapping(target = "color", defaultValue = "#808080")
    @Mapping(target = "position", source = "order", defaultValue = "0")
    @Mapping(target = "favorite", source = "isFavorite", defaultValue = "false")
    @Mapping(target = "viewStyle", defaultValue = "LIST")
    @Mapping(target = "planningCalendarId", ignore = true)
    ProjectCreateParameters toParameters(ProjectMcpTools.ProjectCreateInput projectCreateInput);

    /**
     * Builds a full replacement from a partial tool input by falling back to the persisted project for every omitted property. Two sources with a
     * per-property fallback have no declarative equivalent, so the merge is written out here rather than forced into a mapping expression.
     *
     * @param projectUpdateInput the properties the tool caller chose to change
     * @param existingProject the project as currently persisted
     * @return parameters carrying every mutable property of the project
     */
    default ProjectUpdateParameters toParameters(ProjectMcpTools.ProjectUpdateInput projectUpdateInput, Project existingProject) {
        return new ProjectUpdateParameters(
                projectUpdateInput.name() == null ? existingProject.getName() : projectUpdateInput.name(),
                projectUpdateInput.color() == null ? existingProject.getColor() : projectUpdateInput.color(),
                resolveParentId(projectUpdateInput, existingProject),
                projectUpdateInput.order() == null ? existingProject.getPosition() : projectUpdateInput.order(),
                projectUpdateInput.isFavorite() == null ? existingProject.getIsFavorite() : projectUpdateInput.isFavorite(),
                projectUpdateInput.viewStyle() == null ? existingProject.getViewStyle() : projectUpdateInput.viewStyle(),
                existingProject.getPlanningCalendarId());
    }

    private static java.util.UUID resolveParentId(ProjectMcpTools.ProjectUpdateInput projectUpdateInput, Project existingProject) {
        if (Boolean.TRUE.equals(projectUpdateInput.clearParent())) {
            return null;
        }
        return projectUpdateInput.parentId() == null ? existingProject.getParentId() : projectUpdateInput.parentId();
    }
}
