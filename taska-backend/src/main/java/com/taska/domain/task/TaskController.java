package com.taska.domain.task;

import com.taska.domain.priority.TaskPriorityEvaluationDto;
import com.taska.domain.priority.TaskPriorityEvaluationService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final TaskPriorityEvaluationService priorityEvaluationService;
    private final ObjectMapper objectMapper;

    /**
     * Lists tasks with optional filtering. When {@code date} is provided, returns all occurrences
     * (including recurring) for that single day. When {@code from} and {@code to} are both provided,
     * returns occurrences for that date range. Otherwise delegates to the standard filter/label/project
     * scoped query.
     *
     * @param project_id     optional project filter
     * @param section_id     optional section filter
     * @param label          optional label name filter
     * @param filter         optional named filter ("today", "overdue", "upcoming")
     * @param show_completed include completed tasks when true
     * @param date           single date for occurrence expansion (overrides other params)
     * @param from           start of date range for occurrence expansion
     * @param to             end of date range for occurrence expansion
     * @return list of task DTOs
     */
    @GetMapping
    public List<TaskDto> getAll(
            @RequestParam(required = false) UUID project_id,
            @RequestParam(required = false) UUID section_id,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "false") boolean show_completed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (date != null) {
            return taskService.findOccurrencesForDateRange(date, date);
        }
        if (from != null && to != null) {
            return taskService.findOccurrencesForDateRange(from, to);
        }
        return taskService.findAll(project_id, section_id, label, filter, show_completed)
                .stream().map(taskMapper::toDto).toList();
    }

    /**
     * Creates a new task. Returns HTTP 201 with the created task DTO.
     *
     * @param taskRequest validated task creation payload
     * @return the created task DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@Valid @RequestBody TaskRequest taskRequest) {
        return taskMapper.toDto(taskService.create(taskRequest));
    }

    /**
     * Returns a single task by its UUID.
     *
     * @param id the task UUID
     * @return the task DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable UUID id) {
        return taskMapper.toDto(taskService.findById(id));
    }

    @GetMapping("/{id}/priority-evaluation")
    public ResponseEntity<TaskPriorityEvaluationDto> getPriorityEvaluation(@PathVariable UUID id) {
        return priorityEvaluationService.findForTask(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Updates an existing task. Supports scope-based updates for recurring tasks via the request body.
     *
     * @param id          the task UUID
     * @param payload the update payload. Supplying {@code "priority": null} explicitly clears the
     *                manual priority; omitting {@code priority} leaves it unchanged.
     * @return the updated task DTO
     */
    @PutMapping("/{id}")
    public TaskDto update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        TaskRequest taskRequest = objectMapper.convertValue(payload, TaskRequest.class);
        return taskService.update(id, taskRequest, payload.has("priority"));
    }

    /**
     * Deletes a task. For recurring tasks, an optional scope in the body controls whether only the
     * specific occurrence is skipped or the series is truncated from that point. Returns HTTP 204.
     *
     * @param taskId the task UUID
     * @param body   optional delete scope for recurring tasks
     */
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID taskId,
                       @RequestBody(required = false) TaskDeleteRequest body) {
        taskService.delete(taskId, body);
    }

    /**
     * Marks a task as completed. For recurring tasks, a {@code occurrenceScheduledAt} in the body
     * identifies which occurrence to close.
     *
     * @param taskId the task UUID
     * @param body   optional request with the scheduled occurrence instant
     * @return the updated task DTO
     */
    @PostMapping("/{taskId}/close")
    public TaskDto close(@PathVariable UUID taskId,
                         @RequestBody(required = false) TaskCloseReopenRequest body) {
        return taskService.close(taskId, body);
    }

    /**
     * Reopens a previously completed task. For recurring tasks, a {@code occurrenceScheduledAt} in the body
     * identifies which occurrence to reopen by removing its DONE instance record.
     *
     * @param taskId the task UUID
     * @param body   optional request with the scheduled occurrence instant
     * @return the updated task DTO
     */
    @PostMapping("/{taskId}/reopen")
    public TaskDto reopen(@PathVariable UUID taskId,
                          @RequestBody(required = false) TaskCloseReopenRequest body) {
        return taskService.reopen(taskId, body);
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
