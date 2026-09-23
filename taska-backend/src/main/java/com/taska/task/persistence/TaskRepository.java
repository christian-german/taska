package com.taska.task.persistence;

import com.taska.task.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT t FROM Task t WHERE t.isCompleted = false AND t.isRecurring = false AND t.type = com.taska.task.model.TaskType.TODO
            AND NOT EXISTS (SELECT e FROM TaskPriorityEvaluation e WHERE e.taskId = t.id) ORDER BY t.createdAt ASC
            """)
    List<Task> findEligibleTasksWithoutPriorityEvaluation(org.springframework.data.domain.Pageable pageable);

    /** Returns all incomplete tasks in the given project, ordered by position. */
    List<Task> findByProjectIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    /**
     * Returns all tasks (including completed) in the given project, ordered by position.
     */
    List<Task> findByProjectIdOrderByPositionAsc(UUID projectId);

    /**
     * Returns all direct subtasks of the given parent task, ordered by position.
     */
    List<Task> findByParentIdOrderByPositionAsc(UUID parentId);

    /**
     * Returns all incomplete tasks carrying the given label, ordered by position.
     */
    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label AND t.isCompleted = false ORDER BY t.position ASC")
    List<Task> findByLabelAndIsCompletedFalse(@Param("label") String label);

    /**
     * Returns all tasks (including completed) carrying the given label, ordered by position.
     */
    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label ORDER BY t.position ASC")
    List<Task> findByLabel(@Param("label") String label);

    /**
     * Returns all incomplete tasks in the given project that have a scheduled time, ordered by scheduled time.
     */
    List<Task> findByProjectIdAndScheduledAtIsNotNullAndIsCompletedFalseOrderByScheduledAtAsc(UUID projectId);

    /**
     * Returns incomplete, non-recurring, non-notified, non-all-day tasks scheduled on or before {@code in15min}. Recurring series are expanded
     * independently into occurrence candidates.
     */
    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.isRecurring = false AND t.isNotified = false AND t.allDay = false AND t.scheduledAt IS NOT NULL AND t.scheduledAt <= :in15min")
    List<Task> findTasksDueAround(@Param("in15min") Instant in15min);

    /**
     * Returns all incomplete, non-recurring tasks whose due date falls within [start, end). Ordered by due date ascending. Used to build the
     * occurrence list for a date range.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.scheduledAt >= :start AND t.scheduledAt < :end
            AND t.isCompleted = false
            AND t.isRecurring = false
            ORDER BY t.scheduledAt ASC
            """)
    List<Task> findNonRecurringTasksInPeriod(@Param("start") Instant start, @Param("end") Instant end);

    /** Returns incomplete, scheduled non-recurring tasks before an exclusive calendar boundary. */
    @Query("""
            SELECT t FROM Task t
            WHERE t.scheduledAt < :periodEnd
            AND t.isCompleted = false
            AND t.isRecurring = false
            ORDER BY t.scheduledAt ASC
            """)
    List<Task> findNonRecurringTasksBefore(@Param("periodEnd") Instant periodEnd);

    /**
     * Returns all non-recurring tasks whose scheduled date falls within [start, end), including completed tasks. Used by date-range views that
     * explicitly request them.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.scheduledAt >= :start AND t.scheduledAt < :end
            AND t.isRecurring = false
            ORDER BY t.scheduledAt ASC
            """)
    List<Task> findNonRecurringTasksIncludingCompletedInPeriod(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Returns all recurring tasks whose series overlaps the given period. A task is included when its first due date is before the period end and its
     * {@code rruleEndsAt} (if set) has not yet passed the period start.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.isRecurring = true
            AND t.recurrenceRule IS NOT NULL
            AND t.scheduledAt < :periodEnd
            AND (t.rruleEndsAt IS NULL OR t.rruleEndsAt >= :periodStart)
            """)
    List<Task> findActiveRecurringTasksForPeriod(@Param("periodStart") Instant periodStart, @Param("periodEnd") Instant periodEnd);

    /** Returns every recurring series that can have an occurrence before an exclusive calendar boundary. */
    @Query("""
            SELECT t FROM Task t
            WHERE t.isRecurring = true
            AND t.recurrenceRule IS NOT NULL
            AND t.scheduledAt < :periodEnd
            """)
    List<Task> findRecurringTasksBefore(@Param("periodEnd") Instant periodEnd);
}
