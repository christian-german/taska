package com.taska.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.project.service.ProjectService;
import com.taska.exception.ResourceNotFoundException;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectMcpToolsTest {

  @Mock private ProjectService projectService;
  @InjectMocks private ProjectMcpTools projectMcpTools;

  @Test
  void listProjectsDelegatesToProjectService() {
    when(projectService.findAll()).thenReturn(List.of());

    McpSchema.CallToolResult callToolResult = projectMcpTools.listProjects();

    assertThat(callToolResult.isError()).isFalse();
    assertThat(callToolResult.structuredContent())
        .isEqualTo(new ProjectMcpTools.ProjectListOutput(List.of()));
    verify(projectService).findAll();
  }

  @Test
  void createProjectRejectsBlankNameWithoutCallingService() {
    McpSchema.CallToolResult callToolResult =
        projectMcpTools.createProject(
            new ProjectMcpTools.ProjectCreateInput(" ", null, null, null, null, null));

    assertThat(callToolResult.isError()).isTrue();
    assertThat(callToolResult.content().getFirst().toString()).contains("must not be blank");
  }

  @Test
  void missingProjectProducesSafeToolError() {
    UUID projectId = UUID.randomUUID();
    when(projectService.findById(projectId))
        .thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

    McpSchema.CallToolResult callToolResult = projectMcpTools.getProject(projectId);

    assertThat(callToolResult.isError()).isTrue();
    assertThat(callToolResult.content().getFirst().toString()).contains("Project not found");
  }
}
