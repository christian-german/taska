package com.taska.mcp;

import com.taska.domain.project.Project;
import com.taska.domain.project.ViewStyle;
import com.taska.domain.project.service.ProjectCreateParameters;
import com.taska.domain.project.service.ProjectService;
import com.taska.domain.project.service.ProjectUpdateParameters;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
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

  @McpTool(
      name = "list_projects",
      description = "List all Taska projects in their display order.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult listProjects() {
    return McpToolResponses.execute(
        () ->
            new ProjectListOutput(
                projectService.findAll().stream().map(ProjectOutput::from).toList()));
  }

  @McpTool(
      name = "get_project",
      description = "Get a Taska project by its UUID.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult getProject(
      @McpToolParam(required = true, description = "Project UUID.") UUID projectId) {
    return McpToolResponses.execute(() -> ProjectOutput.from(projectService.findById(projectId)));
  }

  @McpTool(
      name = "create_project",
      description = "Create a Taska project.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult createProject(
      @McpToolParam(required = true, description = "New project details.")
          ProjectCreateInput projectCreateInput) {
    return McpToolResponses.execute(
        () -> {
          requireName(projectCreateInput.name());
          Project project =
              projectService.create(
                  new ProjectCreateParameters(
                      projectCreateInput.name(),
                      projectCreateInput.color() == null ? "#808080" : projectCreateInput.color(),
                      projectCreateInput.parentId(),
                      projectCreateInput.order() == null ? 0 : projectCreateInput.order(),
                      Boolean.TRUE.equals(projectCreateInput.isFavorite()),
                      projectCreateInput.viewStyle() == null
                          ? ViewStyle.LIST
                          : projectCreateInput.viewStyle(),
                      null));
          return ProjectOutput.from(project);
        });
  }

  @McpTool(
      name = "update_project",
      description = "Update fields on an existing Taska project.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult updateProject(
      @McpToolParam(required = true, description = "Project UUID.") UUID projectId,
      @McpToolParam(
              required = true,
              description = "Project fields to update. Omitted fields are unchanged.")
          ProjectUpdateInput projectUpdateInput) {
    return McpToolResponses.execute(
        () -> {
          Project existingProject = projectService.findById(projectId);
          String name =
              projectUpdateInput.name() == null
                  ? existingProject.getName()
                  : projectUpdateInput.name();
          requireName(name);
          Project project =
              projectService.update(
                  projectId,
                  new ProjectUpdateParameters(
                      name,
                      projectUpdateInput.color() == null
                          ? existingProject.getColor()
                          : projectUpdateInput.color(),
                      Boolean.TRUE.equals(projectUpdateInput.clearParent())
                          ? null
                          : projectUpdateInput.parentId() == null
                              ? existingProject.getParentId()
                              : projectUpdateInput.parentId(),
                      projectUpdateInput.order() == null
                          ? existingProject.getPosition()
                          : projectUpdateInput.order(),
                      projectUpdateInput.isFavorite() == null
                          ? existingProject.getIsFavorite()
                          : projectUpdateInput.isFavorite(),
                      projectUpdateInput.viewStyle() == null
                          ? existingProject.getViewStyle()
                          : projectUpdateInput.viewStyle(),
                      existingProject.getPlanningCalendarId()));
          return ProjectOutput.from(project);
        });
  }

  private static void requireName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Project name must not be blank.");
    }
  }

  public record ProjectCreateInput(
      String name,
      String color,
      UUID parentId,
      Integer order,
      Boolean isFavorite,
      ViewStyle viewStyle) {}

  public record ProjectUpdateInput(
      String name,
      String color,
      UUID parentId,
      Boolean clearParent,
      Integer order,
      Boolean isFavorite,
      ViewStyle viewStyle) {}

  /** Object-root structured result required by current MCP clients. */
  public record ProjectListOutput(List<ProjectOutput> projects) {}

  public record ProjectOutput(
      UUID id,
      String name,
      String color,
      UUID parentId,
      Integer order,
      Boolean isFavorite,
      ViewStyle viewStyle,
      Boolean isInboxProject,
      UUID planningCalendarId,
      Instant createdAt,
      Instant updatedAt) {
    static ProjectOutput from(Project project) {
      return new ProjectOutput(
          project.getId(),
          project.getName(),
          project.getColor(),
          project.getParentId(),
          project.getPosition(),
          project.getIsFavorite(),
          project.getViewStyle(),
          project.getIsInboxProject(),
          project.getPlanningCalendarId(),
          project.getCreatedAt(),
          project.getUpdatedAt());
    }
  }
}
