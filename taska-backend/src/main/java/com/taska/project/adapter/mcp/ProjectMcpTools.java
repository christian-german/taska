package com.taska.project.adapter.mcp;

import com.taska.platform.mcp.McpToolResponses;
import com.taska.project.adapter.http.ProjectDto;
import com.taska.project.adapter.http.ProjectMapper;
import com.taska.project.application.ProjectService;
import com.taska.project.model.Project;
import com.taska.project.model.ProjectUpdateParameters;
import com.taska.project.model.ViewStyle;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/** MCP transport adapters for the supported project operations. */
@Component
@RequiredArgsConstructor
public class ProjectMcpTools {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final ProjectMcpMapper projectMcpMapper;

    @McpTool(name = "list_projects", description = "List all Taska projects in their display order.", generateOutputSchema = true)
    public McpSchema.CallToolResult listProjects() {
        return McpToolResponses.execute(() -> new ProjectListOutput(projectService.findAll().stream().map(projectMapper::toDto).toList()));
    }

    @McpTool(name = "get_project", description = "Get a Taska project by its UUID.", generateOutputSchema = true)
    public McpSchema.CallToolResult getProject(@McpToolParam(required = true, description = "Project UUID.") UUID projectId) {
        return McpToolResponses.execute(() -> projectMapper.toDto(projectService.findById(projectId)));
    }

    @McpTool(name = "create_project", description = "Create a Taska project.", generateOutputSchema = true)
    public McpSchema.CallToolResult createProject(
            @McpToolParam(required = true, description = "New project details.") ProjectCreateInput projectCreateInput) {
        return McpToolResponses.execute(() -> {
            requireName(projectCreateInput.name());
            return projectMapper.toDto(projectService.create(projectMcpMapper.toParameters(projectCreateInput)));
        });
    }

    @McpTool(name = "update_project", description = "Update fields on an existing Taska project.", generateOutputSchema = true)
    public McpSchema.CallToolResult updateProject(
            @McpToolParam(required = true, description = "Project UUID.") UUID projectId,
            @McpToolParam(required = true, description = "Project fields to update. Omitted fields are unchanged.") ProjectUpdateInput projectUpdateInput) {
        return McpToolResponses.execute(() -> {
            Project existingProject = projectService.findById(projectId);
            ProjectUpdateParameters projectUpdateParameters = projectMcpMapper.toParameters(projectUpdateInput, existingProject);
            requireName(projectUpdateParameters.name());
            return projectMapper.toDto(projectService.replace(projectId, projectUpdateParameters));
        });
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank.");
        }
    }

    public record ProjectCreateInput(String name, String color, UUID parentId, Integer order, Boolean isFavorite, ViewStyle viewStyle) {
    }

    public record ProjectUpdateInput(String name, String color, UUID parentId, Boolean clearParent, Integer order, Boolean isFavorite,
            ViewStyle viewStyle) {
    }

    /** Object-root structured result required by current MCP clients. */
    public record ProjectListOutput(List<ProjectDto> projects) {
    }
}
