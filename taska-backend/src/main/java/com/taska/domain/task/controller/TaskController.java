package com.taska.domain.task.controller;

import com.taska.domain.priority.controller.TaskPriorityEvaluationDto;
import com.taska.domain.priority.controller.TaskPriorityEvaluationMapper;
import com.taska.domain.priority.service.TaskPriorityEvaluationService;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.service.TaskService;
import com.taska.domain.task.service.TaskUpdateParameters;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;
  private final TaskMutationService taskMutationService;
  private final TaskMapper taskMapper;
  private final TaskPriorityEvaluationService priorityEvaluationService;
  private final TaskPriorityEvaluationMapper priorityEvaluationMapper;

  /**
   * Lists tasks with optional filtering. When {@code date} is provided, returns all occurrences
   * (including recurring) for that single day. When {@code from} and {@code to} are both provided,
   * returns occurrences for that date range. Otherwise, delegates to the standard label/project
   * scoped query.
   *
   * @param projectId optional project filter
   * @param label optional label name filter
   * @param showCompleted include completed tasks when true
   * @param date single date for occurrence expansion (overrides other params)
   * @param from start of date range for occurrence expansion
   * @param to end-of-date range for occurrence expansion
   * @return list of task DTOs
   */
  @GetMapping
  public List<TaskDto> getAll(
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "label", required = false) String label,
      @RequestParam(name = "showCompleted", required = false, defaultValue = "false")
          boolean showCompleted,
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {

    if (date != null) {
      return taskService.findOccurrencesForDateRange(date, date, showCompleted).stream()
          .map(taskMapper::toDto)
          .toList();
    }
    if (from != null && to != null) {
      return taskService.findOccurrencesForDateRange(from, to, showCompleted).stream()
          .map(taskMapper::toDto)
          .toList();
    }
    return taskService.findAll(projectId, label, showCompleted).stream()
        .map(taskMapper::toDto)
        .toList();
  }

  /**
   * Creates a new task. Returns HTTP 201 with the created task DTO.
   *
   * @param taskRequest validated task creation payload
   * @return the created task DTO
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskDto create(
      @Valid @RequestBody TaskCreateRequest taskCreateRequest, @AuthenticationPrincipal Jwt jwt) {
    TaskCreateParameters taskCreateParameters = taskMapper.toParameters(taskCreateRequest);
    return taskMapper.toDto(taskMutationService.create(taskCreateParameters, jwt.getSubject()));
  }

  /**
   * Returns a single task by its UUID.
   *
   * @param taskId the task UUID
   * @return the task DTO, or 404 if not found
   */
  @GetMapping("/{taskId}")
  public TaskDto getById(@PathVariable UUID taskId) {
    return taskMapper.toDto(taskService.findById(taskId));
  }

  @GetMapping("/{taskId}/priority-evaluation")
  public ResponseEntity<TaskPriorityEvaluationDto> getPriorityEvaluation(
      @PathVariable UUID taskId) {
    return priorityEvaluationService
        .findForTask(taskId)
        .map(priorityEvaluationMapper::toDto)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PutMapping("/{taskId}")
  public TaskDto update(
      @PathVariable UUID taskId,
      @Valid @RequestBody TaskUpdateRequest taskUpdateRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskUpdateParameters taskUpdateParameters = taskMapper.toParameters(taskUpdateRequest);
    return taskMapper.toDto(
        taskMutationService.replace(taskId, taskUpdateParameters, jwt.getSubject()));
  }

  @PutMapping("/{taskId}/occurrences/{occurrenceScheduledAt}/following")
  public TaskDto replaceFollowing(
      @PathVariable UUID taskId,
      @PathVariable java.time.Instant occurrenceScheduledAt,
      @Valid @RequestBody TaskUpdateRequest taskUpdateRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskUpdateParameters taskUpdateParameters = taskMapper.toParameters(taskUpdateRequest);
    return taskMapper.toDto(
        taskMutationService.replaceFollowing(
            taskId, occurrenceScheduledAt, taskUpdateParameters, jwt.getSubject()));
  }

  @PutMapping("/{taskId}/occurrences/{occurrenceScheduledAt}")
  public TaskDto replaceOccurrence(
      @PathVariable UUID taskId,
      @PathVariable java.time.Instant occurrenceScheduledAt,
      @Valid @RequestBody OccurrenceUpdateRequest occurrenceUpdateRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskOccurrenceUpdateParameters taskOccurrenceUpdateParameters =
        taskMapper.toParameters(occurrenceUpdateRequest);
    return taskMapper.toDto(
        taskMutationService.replaceOccurrence(
            taskId, occurrenceScheduledAt, taskOccurrenceUpdateParameters, jwt.getSubject()));
  }

  /**
   * Deletes a task. For recurring tasks, an optional scope in the body controls whether only the
   * specific occurrence is skipped or the series is truncated from that point. Returns HTTP 204.
   *
   * @param taskId the task UUID
   * @param taskDeleteRequest optional delete scope for recurring tasks
   */
  @DeleteMapping("/{taskId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID taskId,
      @RequestBody(required = false) TaskDeleteRequest taskDeleteRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskDeleteParameters taskDeleteParameters =
        taskDeleteRequest == null ? null : taskMapper.toParameters(taskDeleteRequest);
    taskMutationService.delete(taskId, taskDeleteParameters, jwt.getSubject());
  }

  /**
   * Marks a task as completed. For recurring tasks, a {@code occurrenceScheduledAt} in the body
   * identifies which occurrence to close.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenRequest optional request with the scheduled occurrence instant
   * @return the updated task DTO
   */
  @PostMapping("/{taskId}/close")
  public TaskDto close(
      @PathVariable UUID taskId,
      @RequestBody(required = false) TaskCloseReopenRequest taskCloseReopenRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskCloseReopenParameters taskCloseReopenParameters =
        taskCloseReopenRequest == null ? null : taskMapper.toParameters(taskCloseReopenRequest);
    return taskMapper.toDto(
        taskMutationService.close(taskId, taskCloseReopenParameters, jwt.getSubject()));
  }

  /**
   * Reopens a previously completed task. For recurring tasks, a {@code occurrenceScheduledAt} in
   * the body identifies which occurrence to reopen by removing its DONE instance record.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenRequest optional request with the scheduled occurrence instant
   * @return the updated task DTO
   */
  @PostMapping("/{taskId}/reopen")
  public TaskDto reopen(
      @PathVariable UUID taskId,
      @RequestBody(required = false) TaskCloseReopenRequest taskCloseReopenRequest,
      @AuthenticationPrincipal Jwt jwt) {
    TaskCloseReopenParameters taskCloseReopenParameters =
        taskCloseReopenRequest == null ? null : taskMapper.toParameters(taskCloseReopenRequest);
    return taskMapper.toDto(
        taskMutationService.reopen(taskId, taskCloseReopenParameters, jwt.getSubject()));
  }

  /**
   * Returns all direct subtasks of the given parent task, ordered by position.
   *
   * @param taskId the parent task UUID
   * @return list of subtask DTOs
   */
  @GetMapping("/{taskId}/subtasks")
  public List<TaskDto> getSubtasks(@PathVariable UUID taskId) {
    return taskService.getSubtasks(taskId).stream().map(taskMapper::toDto).toList();
  }
}
