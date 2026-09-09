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
    @InjectMocks private TaskMcpTools taskMcpTools;

    @Test
    void listTasksUsesAnObjectAsStructuredContentRoot() {
        when(taskService.findAll(null, null, false)).thenReturn(List.of());

        McpSchema.CallToolResult callToolResult = taskMcpTools.listTasks(
                new TaskMcpTools.TaskListInput(null, null, false));

        assertThat(callToolResult.isError()).isFalse();
        assertThat(callToolResult.structuredContent())
                .isEqualTo(new TaskMcpTools.TaskListOutput(List.of()));
    }

    @Test
    void createTaskWithoutScheduleOrPriorityPreservesInboxAndNullableFields() {
        Task task = new Task();
        TaskDto taskDto = taskDto();
        when(taskService.create(any())).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        McpSchema.CallToolResult callToolResult = taskMcpTools.createTask(new TaskMcpTools.TaskCreateInput(
                "Write MCP tests", null, null, null, null, null, List.of(), null,
                null, null, null, null, null, null, null));

        ArgumentCaptor<TaskRequest> taskRequestCaptor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(taskService).create(taskRequestCaptor.capture());
        assertThat(taskRequestCaptor.getValue().projectId()).isNull();
        assertThat(taskRequestCaptor.getValue().parentId()).isNull();
        assertThat(taskRequestCaptor.getValue().priority()).isNull();
        assertThat(taskRequestCaptor.getValue().scheduledAt()).isNull();
        assertThat(callToolResult.isError()).isFalse();
        assertThat(callToolResult.structuredContent()).isInstanceOf(TaskMcpTools.TaskOutput.class);
    }

    @Test
    void completeTaskPassesRecurringOccurrenceTimestampToTaskService() {
        UUID taskId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-07-29T09:00:00Z");
        when(taskService.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt))).thenReturn(taskDto());

        McpSchema.CallToolResult callToolResult = taskMcpTools.completeTask(taskId, occurrenceScheduledAt);

        verify(taskService).close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt));
        assertThat(callToolResult.isError()).isFalse();
    }

    @Test
    void invalidPriorityIsReturnedAsSafeToolError() {
        McpSchema.CallToolResult callToolResult = taskMcpTools.createTask(new TaskMcpTools.TaskCreateInput(
                "Bad priority", null, null, null, null, 5, null, null,
                null, null, null, null, null, null, null));

        assertThat(callToolResult.isError()).isTrue();
        assertThat(callToolResult.content().getFirst().toString()).contains("priority must be between 1 and 4");
    }

    @Test
    void updateTaskCanExplicitlyClearManualPriority() {
        UUID taskId = UUID.randomUUID();
        when(taskService.update(any(), any(), org.mockito.ArgumentMatchers.eq(true))).thenReturn(taskDto());

        McpSchema.CallToolResult callToolResult = taskMcpTools.updateTask(taskId, new TaskMcpTools.TaskUpdateInput(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true));

        verify(taskService).update(org.mockito.ArgumentMatchers.eq(taskId), any(TaskRequest.class),
                org.mockito.ArgumentMatchers.eq(true));
        assertThat(callToolResult.isError()).isFalse();
    }

    private TaskDto taskDto() {
        return new TaskDto(UUID.randomUUID(), "Task", null, null, null, 0, 4, List.of(),
                false, null, false, false, null, null, null, null, null, null, null, null, false, null);
    }
}
