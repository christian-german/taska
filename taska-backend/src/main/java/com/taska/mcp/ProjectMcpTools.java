package com.taska.mcp;

import com.taska.domain.project.Project;
import com.taska.domain.project.ProjectMapper;
import com.taska.domain.project.ProjectRequest;
import com.taska.domain.project.ProjectService;
import com.taska.domain.project.ViewStyle;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** MCP transport adapters for the supported project operations. */
@Component
@RequiredArgsConstructor
public class ProjectMcpTools {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @McpTool(name = "list_projects", description = "List all Taska projects in their display order.", generateOutputSchema = true)
    public McpSchema.CallToolResult listProjects() {
        return McpToolResponses.execute(() -> projectService.findAll().stream()
                .map(projectMapper::toDto)
                .map(ProjectOutput::from)
                .toList());
    }

    @McpTool(name = "get_project", description = "Get a Taska project by its UUID.", generateOutputSchema = true)
    public McpSchema.CallToolResult getProject(
            @McpToolParam(required = true, description = "Project UUID.") UUID projectId) {
        return McpToolResponses.execute(() -> ProjectOutput.from(projectMapper.toDto(projectService.findById(projectId))));
    }

    @McpTool(name = "create_project", description = "Create a Taska project.", generateOutputSchema = true)
    public McpSchema.CallToolResult createProject(
            @McpToolParam(required = true, description = "New project details.") ProjectCreateInput input) {
        return McpToolResponses.execute(() -> {
            requireName(input.name());
            Project project = projectService.create(new ProjectRequest(
                    input.name(), input.color(), input.parentId(), false, input.order(), input.isFavorite(), input.viewStyle()));
            return ProjectOutput.from(projectMapper.toDto(project));
        });
    }

    @McpTool(name = "update_project", description = "Update fields on an existing Taska project.", generateOutputSchema = true)
    public McpSchema.CallToolResult updateProject(
            @McpToolParam(required = true, description = "Project UUID.") UUID projectId,
            @McpToolParam(required = true, description = "Project fields to update. Omitted fields are unchanged.") ProjectUpdateInput input) {
        return McpToolResponses.execute(() -> {
            if (input.name() != null) requireName(input.name());
            Project project = projectService.update(projectId, new ProjectRequest(
                    input.name(), input.color(), input.parentId(), input.clearParent(), input.order(), input.isFavorite(), input.viewStyle()));
            return ProjectOutput.from(projectMapper.toDto(project));
        });
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank.");
        }
    }

    public record ProjectCreateInput(String name, String color, UUID parentId, Integer order,
                                     Boolean isFavorite, ViewStyle viewStyle) {
    }

    public record ProjectUpdateInput(String name, String color, UUID parentId, Boolean clearParent,
                                     Integer order, Boolean isFavorite, ViewStyle viewStyle) {
    }

    public record ProjectOutput(UUID id, String name, String color, UUID parentId, Integer order,
                                Boolean isFavorite, ViewStyle viewStyle, Boolean isInboxProject,
                                Instant createdAt, Instant updatedAt) {
        static ProjectOutput from(com.taska.domain.project.ProjectDto project) {
            return new ProjectOutput(project.id(), project.name(), project.color(), project.parentId(), project.order(),
                    project.isFavorite(), project.viewStyle(), project.isInboxProject(), project.createdAt(), project.updatedAt());
        }
    }
}
