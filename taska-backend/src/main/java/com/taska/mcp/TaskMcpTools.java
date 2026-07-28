package com.taska.mcp;

import com.taska.domain.task.RecurrenceScope;
import com.taska.domain.task.TaskCloseReopenRequest;
import com.taska.domain.task.TaskDto;
import com.taska.domain.task.TaskMapper;
import com.taska.domain.task.TaskRequest;
import com.taska.domain.task.TaskService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** MCP transport adapters for the supported task operations. */
@Component
@RequiredArgsConstructor
public class TaskMcpTools {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @McpTool(name = "list_tasks", description = "List Taska tasks with optional project, section, label, completion, or named date filters.", generateOutputSchema = true)
    public McpSchema.CallToolResult listTasks(
            @McpToolParam(required = true, description = "Optional filters. Use null for filters not needed.") TaskListInput input) {
        return McpToolResponses.execute(() -> taskService.findAll(
                        input.projectId(), input.sectionId(), input.label(), input.filter(), input.showCompleted())
                .stream().map(taskMapper::toDto).map(TaskOutput::from).toList());
    }

    @McpTool(name = "get_task", description = "Get a Taska task by its UUID.", generateOutputSchema = true)
    public McpSchema.CallToolResult getTask(
            @McpToolParam(required = true, description = "Task UUID.") UUID taskId) {
        return McpToolResponses.execute(() -> TaskOutput.from(taskMapper.toDto(taskService.findById(taskId))));
    }

    @McpTool(name = "create_task", description = "Create a Taska task. Omit projectId and parentId to create it in Inbox.", generateOutputSchema = true)
    public McpSchema.CallToolResult createTask(
            @McpToolParam(required = true, description = "New task details.") TaskCreateInput input) {
        return McpToolResponses.execute(() -> {
            requireContent(input.content());
            validatePriority(input.priority());
            validateEstimate(input.estimateMinutes());
            TaskDto task = taskMapper.toDto(taskService.create(toRequest(input)));
            return TaskOutput.from(task);
        });
    }

    @McpTool(name = "update_task", description = "Update fields on a Taska task. For recurring task occurrences, provide scope and scheduledAt.", generateOutputSchema = true)
    public McpSchema.CallToolResult updateTask(
            @McpToolParam(required = true, description = "Task UUID.") UUID taskId,
            @McpToolParam(required = true, description = "Task fields to update. Omitted fields are unchanged.") TaskUpdateInput input) {
        return McpToolResponses.execute(() -> {
            if (input.content() != null) requireContent(input.content());
            validatePriority(input.priority());
            validateEstimate(input.estimateMinutes());
            return TaskOutput.from(taskService.update(taskId, toRequest(input)));
        });
    }

    @McpTool(name = "complete_task", description = "Complete a Taska task. scheduledAt is required for recurring task occurrences.", generateOutputSchema = true)
    public McpSchema.CallToolResult completeTask(
            @McpToolParam(required = true, description = "Task UUID.") UUID taskId,
            @McpToolParam(required = false, description = "Scheduled occurrence timestamp for recurring tasks, in ISO-8601 UTC format.") Instant scheduledAt) {
        return McpToolResponses.execute(() -> TaskOutput.from(taskService.close(taskId, new TaskCloseReopenRequest(scheduledAt))));
    }

    @McpTool(name = "reopen_task", description = "Reopen a Taska task. scheduledAt identifies a recurring task occurrence.", generateOutputSchema = true)
    public McpSchema.CallToolResult reopenTask(
            @McpToolParam(required = true, description = "Task UUID.") UUID taskId,
            @McpToolParam(required = false, description = "Scheduled occurrence timestamp for recurring tasks, in ISO-8601 UTC format.") Instant scheduledAt) {
        return McpToolResponses.execute(() -> TaskOutput.from(taskService.reopen(taskId, new TaskCloseReopenRequest(scheduledAt))));
    }

    private static TaskRequest toRequest(TaskInput input) {
        return new TaskRequest(input.content(), input.description(), input.projectId(), input.sectionId(), input.parentId(),
                input.order(), input.priority(), input.labels(), input.dueAt(), input.allDay(), input.isRecurring(),
                input.estimateMinutes(), input.mentionContext(), input.recurrenceRule(), input.scope(), input.scheduledAt());
    }

    private static void requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Task content must not be blank.");
        }
    }

    private static void validatePriority(Integer priority) {
        if (priority != null && (priority < 1 || priority > 4)) {
            throw new IllegalArgumentException("Task priority must be between 1 and 4.");
        }
    }

    private static void validateEstimate(Integer estimateMinutes) {
        if (estimateMinutes != null && estimateMinutes <= 0) {
            throw new IllegalArgumentException("Task estimateMinutes must be positive.");
        }
    }

    public sealed interface TaskInput permits TaskCreateInput, TaskUpdateInput {
        String content();
        String description();
        UUID projectId();
        UUID sectionId();
        UUID parentId();
        Integer order();
        Integer priority();
        List<String> labels();
        Instant dueAt();
        Boolean allDay();
        Boolean isRecurring();
        Integer estimateMinutes();
        String mentionContext();
        String recurrenceRule();
        RecurrenceScope scope();
        Instant scheduledAt();
    }

    public record TaskListInput(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
    }

    public record TaskCreateInput(String content, String description, UUID projectId, UUID sectionId, UUID parentId,
                                  Integer order, Integer priority, List<String> labels, Instant dueAt, Boolean allDay,
                                  Boolean isRecurring, Integer estimateMinutes, String mentionContext,
                                  String recurrenceRule, RecurrenceScope scope, Instant scheduledAt) implements TaskInput {
    }

    public record TaskUpdateInput(String content, String description, UUID projectId, UUID sectionId, UUID parentId,
                                  Integer order, Integer priority, List<String> labels, Instant dueAt, Boolean allDay,
                                  Boolean isRecurring, Integer estimateMinutes, String mentionContext,
                                  String recurrenceRule, RecurrenceScope scope, Instant scheduledAt) implements TaskInput {
    }

    public record TaskOutput(UUID id, String content, String description, UUID projectId, UUID sectionId, UUID parentId,
                             Integer order, Integer priority, List<String> labels, Boolean isCompleted, Instant dueAt,
                             Boolean allDay, Boolean isRecurring, Integer estimateMinutes, String mentionContext,
                             String recurrenceRule, Instant createdAt, Instant updatedAt, Instant completedAt,
                             UUID instanceId, Instant scheduledAt, Boolean isVirtual, Instant rruleEndsAt) {
        static TaskOutput from(TaskDto task) {
            return new TaskOutput(task.id(), task.content(), task.description(), task.projectId(), task.sectionId(), task.parentId(),
                    task.order(), task.priority(), task.labels(), task.isCompleted(), task.dueAt(), task.allDay(),
                    task.isRecurring(), task.estimateMinutes(), task.mentionContext(), task.recurrenceRule(),
                    task.createdAt(), task.updatedAt(), task.completedAt(), task.instanceId(), task.scheduledAt(),
                    task.isVirtual(), task.rruleEndsAt());
        }
    }
}
