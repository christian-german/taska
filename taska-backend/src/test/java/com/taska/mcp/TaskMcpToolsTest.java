package com.taska.mcp;

import com.taska.domain.task.Task;
import com.taska.domain.task.TaskCloseReopenRequest;
import com.taska.domain.task.TaskDto;
import com.taska.domain.task.TaskMapper;
import com.taska.domain.task.TaskRequest;
import com.taska.domain.task.TaskService;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMcpToolsTest {

    @Mock private TaskService taskService;
    @Mock private TaskMapper taskMapper;
    @InjectMocks private TaskMcpTools tools;

    @Test
    void listTasksUsesAnObjectAsStructuredContentRoot() {
        when(taskService.findAll(null, null, null, null, false)).thenReturn(List.of());

        McpSchema.CallToolResult result = tools.listTasks(
                new TaskMcpTools.TaskListInput(null, null, null, null, false));

        assertThat(result.isError()).isFalse();
        assertThat(result.structuredContent())
                .isEqualTo(new TaskMcpTools.TaskListOutput(List.of()));
    }

    @Test
    void createTaskWithoutScheduleOrPriorityPreservesInboxAndNullableFields() {
        Task task = new Task();
        TaskDto taskDto = taskDto();
        when(taskService.create(any())).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        McpSchema.CallToolResult result = tools.createTask(new TaskMcpTools.TaskCreateInput(
                "Write MCP tests", null, null, null, null, null, null, List.of(), null,
                null, null, null, null, null, null, null));

        ArgumentCaptor<TaskRequest> request = ArgumentCaptor.forClass(TaskRequest.class);
        verify(taskService).create(request.capture());
        assertThat(request.getValue().projectId()).isNull();
        assertThat(request.getValue().parentId()).isNull();
        assertThat(request.getValue().priority()).isNull();
        assertThat(request.getValue().scheduledAt()).isNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.structuredContent()).isInstanceOf(TaskMcpTools.TaskOutput.class);
    }

    @Test
    void completeTaskPassesRecurringOccurrenceTimestampToTaskService() {
        UUID taskId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-07-29T09:00:00Z");
        when(taskService.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt))).thenReturn(taskDto());

        McpSchema.CallToolResult result = tools.completeTask(taskId, occurrenceScheduledAt);

        verify(taskService).close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt));
        assertThat(result.isError()).isFalse();
    }

    @Test
    void invalidPriorityIsReturnedAsSafeToolError() {
        McpSchema.CallToolResult result = tools.createTask(new TaskMcpTools.TaskCreateInput(
                "Bad priority", null, null, null, null, null, 5, null, null,
                null, null, null, null, null, null, null));

        assertThat(result.isError()).isTrue();
        assertThat(result.content().getFirst().toString()).contains("priority must be between 1 and 4");
    }

    @Test
    void updateTaskCanExplicitlyClearManualPriority() {
        UUID taskId = UUID.randomUUID();
        when(taskService.update(any(), any(), org.mockito.ArgumentMatchers.eq(true))).thenReturn(taskDto());

        McpSchema.CallToolResult result = tools.updateTask(taskId, new TaskMcpTools.TaskUpdateInput(
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true));

        verify(taskService).update(org.mockito.ArgumentMatchers.eq(taskId), any(TaskRequest.class),
                org.mockito.ArgumentMatchers.eq(true));
        assertThat(result.isError()).isFalse();
    }

    private TaskDto taskDto() {
        return new TaskDto(UUID.randomUUID(), "Task", null, null, null, null, 0, 4, List.of(),
                false, null, false, false, null, null, null, null, null, null, null, null, false, null);
    }
}
