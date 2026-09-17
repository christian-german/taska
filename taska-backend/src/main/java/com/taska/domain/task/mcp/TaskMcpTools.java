package com.taska.domain.task.mcp;

import com.taska.domain.task.occurrence.RecurrenceScope;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskService;
import com.taska.mcp.McpToolResponses;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** MCP transport adapters for the supported task operations. */
@Component
@RequiredArgsConstructor
public class TaskMcpTools {

  private final TaskService taskService;
  private final TaskMutationService taskMutationService;
  private final TaskMcpMapper taskMcpMapper;

  @McpTool(
      name = "list_tasks",
      description = "List Taska tasks with optional project, label, or completion criteria.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult listTasks(
      @McpToolParam(description = "Optional filters. Use null for filters not needed.")
          TaskListInput taskListInput) {
    return McpToolResponses.execute(
        () ->
            new TaskListOutput(
                taskService
                    .findAll(
                        taskListInput.projectId(),
                        taskListInput.label(),
                        taskListInput.showCompleted())
                    .stream()
                    .map(taskMcpMapper::toOutput)
                    .toList()));
  }

  @McpTool(
      name = "get_task",
      description = "Get a Taska task by its UUID.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult getTask(@McpToolParam(description = "Task UUID.") UUID taskId) {
    return McpToolResponses.execute(() -> taskMcpMapper.toOutput(taskService.findById(taskId)));
  }

  @McpTool(
      name = "create_task",
      description = "Create a Taska task. Omit projectId and parentId to create it in Inbox.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult createTask(
      @McpToolParam(description = "New task details.") TaskCreateInput taskCreateInput) {
    return McpToolResponses.execute(
        () -> {
          requireContent(taskCreateInput.content());
          validatePriority(taskCreateInput.priority());
          validateEstimate(taskCreateInput.estimateMinutes());
          return taskMcpMapper.toOutput(
              taskMutationService.create(
                  taskMcpMapper.toParameters(taskCreateInput), accountSubject()));
        });
  }

  @McpTool(
      name = "update_task",
      description =
          "Update fields on a Taska task. For recurring task occurrences, provide scope and occurrenceScheduledAt. Set clearPriority to true to remove a manual priority.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult updateTask(
      @McpToolParam(description = "Task UUID.") UUID taskId,
      @McpToolParam(description = "Task fields to update. Omitted fields are unchanged.")
          TaskUpdateInput taskUpdateInput) {
    return McpToolResponses.execute(
        () -> {
          if (taskUpdateInput.content() != null) {
            requireContent(taskUpdateInput.content());
          }
          validatePriority(taskUpdateInput.priority());
          validateEstimate(taskUpdateInput.estimateMinutes());
          boolean priorityProvided =
              taskUpdateInput.priority() != null
                  || Boolean.TRUE.equals(taskUpdateInput.clearPriority());
          return taskMcpMapper.toOutput(
              taskMutationService.update(
                  taskId,
                  taskMcpMapper.toParameters(taskUpdateInput),
                  priorityProvided,
                  accountSubject()));
        });
  }

  @McpTool(
      name = "complete_task",
      description =
          "Complete a Taska task. occurrenceScheduledAt is required for recurring task occurrences.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult completeTask(
      @McpToolParam(description = "Task UUID.") UUID taskId,
      @McpToolParam(
              required = false,
              description =
                  "Scheduled occurrence timestamp for recurring tasks, in ISO-8601 UTC format.")
          Instant occurrenceScheduledAt) {
    return McpToolResponses.execute(
        () ->
            taskMcpMapper.toOutput(
                taskMutationService.close(
                    taskId,
                    new TaskCloseReopenParameters(occurrenceScheduledAt),
                    accountSubject())));
  }

  @McpTool(
      name = "reopen_task",
      description =
          "Reopen a Taska task. occurrenceScheduledAt identifies a recurring task occurrence.",
      generateOutputSchema = true)
  public McpSchema.CallToolResult reopenTask(
      @McpToolParam(description = "Task UUID.") UUID taskId,
      @McpToolParam(
              required = false,
              description =
                  "Scheduled occurrence timestamp for recurring tasks, in ISO-8601 UTC format.")
          Instant occurrenceScheduledAt) {
    return McpToolResponses.execute(
        () ->
            taskMcpMapper.toOutput(
                taskMutationService.reopen(
                    taskId,
                    new TaskCloseReopenParameters(occurrenceScheduledAt),
                    accountSubject())));
  }

  private static String accountSubject() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
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

    UUID parentId();

    Integer order();

    Integer priority();

    List<String> labels();

    Instant scheduledAt();

    Instant dueAt();

    Boolean allDay();

    Boolean isRecurring();

    Integer estimateMinutes();

    String mentionContext();

    String recurrenceRule();
  }

  public record TaskListInput(
      @McpToolParam(required = false) UUID projectId,
      @McpToolParam(required = false) String label,
      @McpToolParam(required = false) boolean showCompleted) {}

  /** Object-root structured result required by current MCP clients. */
  public record TaskListOutput(List<TaskMcpOutput> tasks) {}

  public record TaskCreateInput(
      @McpToolParam(description = "Task title.") String content,
      @McpToolParam(required = false, description = "Optional longer task description.")
          String description,
      @McpToolParam(
              required = false,
              description = "Project UUID. Omit with parentId to create the task in Inbox.")
          UUID projectId,
      @McpToolParam(required = false, description = "Parent task UUID for a subtask.")
          UUID parentId,
      @McpToolParam(required = false, description = "Display position.") Integer order,
      @McpToolParam(required = false, description = "Manual priority from 1 (urgent) through 4.")
          Integer priority,
      @McpToolParam(required = false, description = "Label names to attach.") List<String> labels,
      @McpToolParam(required = false, description = "Planned timestamp in ISO-8601 UTC format.")
          Instant scheduledAt,
      @McpToolParam(required = false, description = "Deadline timestamp in ISO-8601 UTC format.")
          Instant dueAt,
      @McpToolParam(required = false, description = "Whether scheduledAt is all-day.")
          Boolean allDay,
      @McpToolParam(required = false, description = "Whether the task recurs.") Boolean isRecurring,
      @McpToolParam(required = false, description = "Positive time estimate in minutes.")
          Integer estimateMinutes,
      @McpToolParam(required = false, description = "Context captured from an @-mention.")
          String mentionContext,
      @McpToolParam(required = false, description = "Recurrence rule.") String recurrenceRule)
      implements TaskInput {}

  public record TaskUpdateInput(
      @McpToolParam(required = false, description = "Replacement task title.") String content,
      @McpToolParam(required = false, description = "Replacement longer task description.")
          String description,
      @McpToolParam(required = false, description = "Project UUID.") UUID projectId,
      @McpToolParam(required = false, description = "Parent task UUID.") UUID parentId,
      @McpToolParam(required = false, description = "Display position.") Integer order,
      @McpToolParam(required = false, description = "Manual priority from 1 (urgent) through 4.")
          Integer priority,
      @McpToolParam(required = false, description = "Replacement label names.") List<String> labels,
      @McpToolParam(required = false, description = "Planned timestamp in ISO-8601 UTC format.")
          Instant scheduledAt,
      @McpToolParam(required = false, description = "Deadline timestamp in ISO-8601 UTC format.")
          Instant dueAt,
      @McpToolParam(required = false, description = "Whether scheduledAt is all-day.")
          Boolean allDay,
      @McpToolParam(required = false, description = "Whether the task recurs.") Boolean isRecurring,
      @McpToolParam(required = false, description = "Positive time estimate in minutes.")
          Integer estimateMinutes,
      @McpToolParam(required = false, description = "Context captured from an @-mention.")
          String mentionContext,
      @McpToolParam(required = false, description = "Recurrence rule.") String recurrenceRule,
      @McpToolParam(required = false, description = "Scope for a recurring occurrence update.")
          RecurrenceScope scope,
      @McpToolParam(
              required = false,
              description = "Recurring occurrence timestamp in ISO-8601 UTC format.")
          Instant occurrenceScheduledAt,
      @McpToolParam(required = false, description = "Remove the manual priority.")
          Boolean clearPriority)
      implements TaskInput {}
}
