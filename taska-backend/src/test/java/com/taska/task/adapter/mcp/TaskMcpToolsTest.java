package com.taska.task.adapter.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.task.application.TaskMutationService;
import com.taska.task.application.definition.TaskDefinitionService;
import com.taska.task.model.Task;
import com.taska.task.model.TaskCloseReopenParameters;
import com.taska.task.model.TaskCreateParameters;
import com.taska.task.model.TaskPatchParameters;
import com.taska.task.model.TaskResult;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TaskMcpToolsTest {

    @Mock
    private TaskDefinitionService taskService;
    @Mock
    private TaskMutationService taskMutationService;
    private TaskMcpTools taskMcpTools;

    @BeforeEach
    void authenticate() {
        taskMcpTools = new TaskMcpTools(taskService, taskMutationService, new TaskMcpMapperImpl());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("account-a", null));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listTasksUsesAnObjectAsStructuredContentRoot() {
        when(taskService.findAll(null, null, false)).thenReturn(List.of());

        McpSchema.CallToolResult callToolResult = taskMcpTools.listTasks(new TaskMcpTools.TaskListInput(null, null, false));

        assertThat(callToolResult.isError()).isFalse();
        assertThat(callToolResult.structuredContent()).isEqualTo(new TaskMcpTools.TaskListOutput(List.of()));
    }

    @Test
    void createTaskWithoutScheduleOrPriorityPreservesInboxAndNullableFields() {
        Task task = new Task();
        initializeTask(task);
        when(taskMutationService.create(any(), any())).thenReturn(task);

        McpSchema.CallToolResult callToolResult = taskMcpTools.createTask(
                new TaskMcpTools.TaskCreateInput(
                        "Write MCP tests",
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        ArgumentCaptor<TaskCreateParameters> taskRequestCaptor = ArgumentCaptor.forClass(TaskCreateParameters.class);
        verify(taskMutationService).create(taskRequestCaptor.capture(), any());
        assertThat(taskRequestCaptor.getValue().projectId()).isNull();
        assertThat(taskRequestCaptor.getValue().parentId()).isNull();
        assertThat(taskRequestCaptor.getValue().priority()).isNull();
        assertThat(taskRequestCaptor.getValue().scheduledAt()).isNull();
        assertThat(callToolResult.isError()).isFalse();
        assertThat(callToolResult.structuredContent()).isInstanceOf(TaskMcpOutput.class);
    }

    @Test
    void completeTaskPassesRecurringOccurrenceTimestampToTaskService() {
        UUID taskId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-07-29T09:00:00Z");
        TaskResult result = TaskResult.base(initializedTask());
        when(
                taskMutationService.close(
                        org.mockito.ArgumentMatchers.eq(taskId),
                        org.mockito.ArgumentMatchers.eq(new TaskCloseReopenParameters(occurrenceScheduledAt)),
                        any()))
                .thenReturn(result);

        McpSchema.CallToolResult callToolResult = taskMcpTools.completeTask(taskId, occurrenceScheduledAt);

        verify(taskMutationService).close(
                org.mockito.ArgumentMatchers.eq(taskId),
                org.mockito.ArgumentMatchers.eq(new TaskCloseReopenParameters(occurrenceScheduledAt)),
                any());
        assertThat(callToolResult.isError()).isFalse();
    }

    @Test
    void invalidPriorityIsReturnedAsSafeToolError() {
        McpSchema.CallToolResult callToolResult = taskMcpTools.createTask(
                new TaskMcpTools.TaskCreateInput("Bad priority", null, null, null, null, 5, null, null, null, null, null, null, null, null));

        assertThat(callToolResult.isError()).isTrue();
        assertThat(callToolResult.content().getFirst().toString()).contains("priority must be between 1 and 4");
    }

    @Test
    void updateTaskCanExplicitlyClearManualPriority() {
        UUID taskId = UUID.randomUUID();
        when(taskMutationService.update(any(), any(), org.mockito.ArgumentMatchers.eq(true), any())).thenReturn(TaskResult.base(initializedTask()));

        McpSchema.CallToolResult callToolResult = taskMcpTools.updateTask(
                taskId,
                new TaskMcpTools.TaskUpdateInput(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true));

        verify(taskMutationService)
                .update(org.mockito.ArgumentMatchers.eq(taskId), any(TaskPatchParameters.class), org.mockito.ArgumentMatchers.eq(true), any());
        assertThat(callToolResult.isError()).isFalse();
    }

    @Test
    void recurringSeriesOutputKeepsFlatDueAtFieldNull() {
        Task series = initializedTask();
        series.setIsRecurring(true);
        series.setDueAt(Instant.parse("2026-07-30T09:00:00Z"));

        TaskMcpOutput output = new TaskMcpMapperImpl().toOutput(series);

        assertThat(output.dueAt()).isNull();
    }

    private Task initializedTask() {
        Task task = new Task();
        initializeTask(task);
        return task;
    }

    private void initializeTask(Task task) {
        task.setId(UUID.randomUUID());
        task.setContent("Task");
        task.setPosition(0);
        task.setLabels(List.of());
        task.setIsCompleted(false);
        task.setIsRecurring(false);
    }
}
