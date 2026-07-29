package com.taska.mcp;

import com.taska.domain.project.Project;
import com.taska.domain.project.ProjectMapper;
import com.taska.domain.project.ProjectService;
import com.taska.exception.ResourceNotFoundException;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMcpToolsTest {

    @Mock private ProjectService projectService;
    @Mock private ProjectMapper projectMapper;
    @InjectMocks private ProjectMcpTools tools;

    @Test
    void listProjectsDelegatesToProjectService() {
        when(projectService.findAll()).thenReturn(List.of());

        McpSchema.CallToolResult result = tools.listProjects();

        assertThat(result.isError()).isFalse();
        assertThat(result.structuredContent())
                .isEqualTo(new ProjectMcpTools.ProjectListOutput(List.of()));
        verify(projectService).findAll();
    }

    @Test
    void createProjectRejectsBlankNameWithoutCallingService() {
        McpSchema.CallToolResult result = tools.createProject(
                new ProjectMcpTools.ProjectCreateInput(" ", null, null, null, null, null));

        assertThat(result.isError()).isTrue();
        assertThat(result.content().getFirst().toString()).contains("must not be blank");
    }

    @Test
    void missingProjectProducesSafeToolError() {
        UUID projectId = UUID.randomUUID();
        when(projectService.findById(projectId)).thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

        McpSchema.CallToolResult result = tools.getProject(projectId);

        assertThat(result.isError()).isTrue();
        assertThat(result.content().getFirst().toString()).contains("Project not found");
    }
}
