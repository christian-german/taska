package com.taska.domain.task;

import com.taska.domain.project.ProjectRepository;
import com.taska.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;

    public TaskService(TaskRepository taskRepo, ProjectRepository projectRepo) {
        this.taskRepo = taskRepo;
        this.projectRepo = projectRepo;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
        if (filter != null) {
            LocalDate today = LocalDate.now();
            return switch (filter) {
                case "today" -> taskRepo.findByDueDateAndIsCompletedFalseOrderByPositionAsc(today)
                        .stream().map(this::toResponse).toList();
                case "overdue" -> taskRepo.findByDueDateBeforeAndIsCompletedFalseOrderByDueDateAsc(today)
                        .stream().map(this::toResponse).toList();
                case "upcoming" -> taskRepo.findByDueDateBetweenAndIsCompletedFalseOrderByDueDateAsc(
                        today.plusDays(1), today.plusDays(14))
                        .stream().map(this::toResponse).toList();
                default -> taskRepo.findAll().stream().map(this::toResponse).toList();
            };
        }
        if (label != null) {
            var tasks = showCompleted
                    ? taskRepo.findByLabel(label)
                    : taskRepo.findByLabelAndIsCompletedFalse(label);
            return tasks.stream().map(this::toResponse).toList();
        }
        if (projectId != null && sectionId != null) {
            var tasks = showCompleted
                    ? taskRepo.findByProjectIdAndSectionIdOrderByPositionAsc(projectId, sectionId)
                    : taskRepo.findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(projectId, sectionId);
            return tasks.stream().map(this::toResponse).toList();
        }
        if (projectId != null) {
            var tasks = showCompleted
                    ? taskRepo.findByProjectIdOrderByPositionAsc(projectId)
                    : taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
            return tasks.stream().map(this::toResponse).toList();
        }
        if (sectionId != null) {
            var tasks = showCompleted
                    ? taskRepo.findBySectionIdOrderByPositionAsc(sectionId)
                    : taskRepo.findBySectionIdAndIsCompletedFalseOrderByPositionAsc(sectionId);
            return tasks.stream().map(this::toResponse).toList();
        }
        return taskRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public TaskResponse create(TaskRequest req) {
        Task t = new Task();
        t.setContent(req.content());
        t.setDescription(req.description());
        t.setSectionId(req.sectionId());
        t.setParentId(req.parentId());
        t.setPosition(req.order() != null ? req.order() : 0);
        t.setPriority(req.priority() != null ? req.priority() : 1);
        t.setLabels(req.labels() != null ? req.labels() : new ArrayList<>());
        t.setDueDate(req.dueDate());
        t.setDueDateTime(req.dueDateTime());
        t.setIsRecurring(req.isRecurring() != null ? req.isRecurring() : false);
        t.setEstimateMinutes(req.estimateMinutes());
        t.setMentionContext(req.mentionContext());
        t.setRecurrenceRule(req.recurrenceRule());

        UUID projectId = req.projectId();
        if (projectId == null && req.parentId() == null) {
            projectId = projectRepo.findByIsInboxProjectTrue()
                    .orElseThrow(() -> new ResourceNotFoundException("Inbox project not found"))
                    .getId();
        }
        t.setProjectId(projectId);

        return toResponse(taskRepo.save(t));
    }

    public TaskResponse update(UUID id, TaskRequest req) {
        Task t = getOrThrow(id);
        if (req.content() != null) t.setContent(req.content());
        if (req.description() != null) t.setDescription(req.description());
        if (req.projectId() != null) t.setProjectId(req.projectId());
        if (req.sectionId() != null) t.setSectionId(req.sectionId());
        if (req.parentId() != null) t.setParentId(req.parentId());
        if (req.order() != null) t.setPosition(req.order());
        if (req.priority() != null) t.setPriority(req.priority());
        if (req.labels() != null) t.setLabels(req.labels());
        if (req.dueDate() != null) t.setDueDate(req.dueDate());
        if (req.dueDateTime() != null) t.setDueDateTime(req.dueDateTime());
        if (req.isRecurring() != null) t.setIsRecurring(req.isRecurring());
        if (req.estimateMinutes() != null) t.setEstimateMinutes(req.estimateMinutes());
        if (req.mentionContext() != null) t.setMentionContext(req.mentionContext());
        if (req.recurrenceRule() != null) t.setRecurrenceRule(req.recurrenceRule());
        return toResponse(taskRepo.save(t));
    }

    public void delete(UUID id) {
        taskRepo.delete(getOrThrow(id));
    }

    public TaskResponse close(UUID id) {
        Task t = getOrThrow(id);
        t.setIsCompleted(true);
        t.setCompletedAt(Instant.now());
        return toResponse(taskRepo.save(t));
    }

    public TaskResponse reopen(UUID id) {
        Task t = getOrThrow(id);
        t.setIsCompleted(false);
        t.setCompletedAt(null);
        return toResponse(taskRepo.save(t));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(UUID parentId) {
        return taskRepo.findByParentIdOrderByPositionAsc(parentId).stream().map(this::toResponse).toList();
    }

    private Task getOrThrow(UUID id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getContent(), t.getDescription(), t.getProjectId(),
                t.getSectionId(), t.getParentId(), t.getPosition(), t.getPriority(),
                t.getLabels(), t.getIsCompleted(), t.getDueDate(), t.getDueDateTime(),
                t.getIsRecurring(), t.getEstimateMinutes(), t.getMentionContext(),
                t.getRecurrenceRule(),
                t.getCreatedAt(), t.getUpdatedAt(), t.getCompletedAt());
    }
}
