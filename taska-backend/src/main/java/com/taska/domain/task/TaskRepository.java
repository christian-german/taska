package com.taska.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    /** Returns all incomplete tasks in the given project, ordered by position. */
    List<Task> findByProjectIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    /** Returns all incomplete tasks in the given section, ordered by position. */
    List<Task> findBySectionIdAndIsCompletedFalseOrderByPositionAsc(UUID sectionId);

    /** Returns all incomplete tasks belonging to both the given project and section, ordered by position. */
    List<Task> findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId, UUID sectionId);

    /** Returns all incomplete tasks in the given project that have no section assigned, ordered by position. */
    List<Task> findByProjectIdAndSectionIdIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    /** Returns all incomplete tasks whose due date falls within [from, to), ordered by due date. */
    List<Task> findByDueAtBetweenAndIsCompletedFalseOrderByDueAtAsc(Instant from, Instant to);

    /** Returns all incomplete tasks with a due date strictly before the given instant, ordered by due date. */
    List<Task> findByDueAtBeforeAndIsCompletedFalseOrderByDueAtAsc(Instant before);

    /** Returns all incomplete, non-recurring overdue tasks (due before the given instant), ordered by due date. */
    List<Task> findByDueAtBeforeAndIsCompletedFalseAndIsRecurringFalseOrderByDueAtAsc(Instant before);

    /** Returns all tasks (including completed) in the given project, ordered by position. */
    List<Task> findByProjectIdOrderByPositionAsc(UUID projectId);

    /** Returns all tasks (including completed) in the given section, ordered by position. */
    List<Task> findBySectionIdOrderByPositionAsc(UUID sectionId);

    /** Returns all tasks (including completed) in the given project and section, ordered by position. */
    List<Task> findByProjectIdAndSectionIdOrderByPositionAsc(UUID projectId, UUID sectionId);

    /** Returns all tasks (including completed) in the given project that have no section, ordered by position. */
    List<Task> findByProjectIdAndSectionIdIsNullOrderByPositionAsc(UUID projectId);

    /** Returns all direct subtasks of the given parent task, ordered by position. */
    List<Task> findByParentIdOrderByPositionAsc(UUID parentId);

    /** Deletes all tasks belonging to the given project. */
    void deleteByProjectId(UUID projectId);

    /** Returns all incomplete tasks carrying the given label, ordered by position. */
    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label AND t.isCompleted = false ORDER BY t.position ASC")
    List<Task> findByLabelAndIsCompletedFalse(@Param("label") String label);

    /** Returns all tasks (including completed) carrying the given label, ordered by position. */
    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label ORDER BY t.position ASC")
    List<Task> findByLabel(@Param("label") String label);

    /** Returns all incomplete tasks that have a due date, ordered by due date. */
    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueAt IS NOT NULL ORDER BY t.dueAt ASC")
    List<Task> findAllWithDueAtNotCompleted();

    /** Returns all incomplete tasks that have no due date, ordered by position. */
    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueAt IS NULL ORDER BY t.position ASC")
    List<Task> findAllWithNoDueAtNotCompleted();

    /** Returns all incomplete tasks in the given project that have a due date, ordered by due date. */
    List<Task> findByProjectIdAndDueAtIsNotNullAndIsCompletedFalseOrderByDueAtAsc(UUID projectId);

    /** Returns all incomplete tasks in the given project that have no due date, ordered by position. */
    List<Task> findByProjectIdAndDueAtIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    /** Returns the total number of completed tasks. */
    long countByIsCompletedTrue();

    /** Returns the total number of incomplete tasks. */
    long countByIsCompletedFalse();

    /** Returns the count of incomplete tasks whose due date is strictly before the given instant. */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.isCompleted = false AND t.dueAt < :before")
    long countByIsCompletedFalseAndDueAtBefore(@Param("before") Instant before);

    /** Returns all tasks completed after the given instant, ordered chronologically by completion date. */
    List<Task> findByCompletedAtAfterOrderByCompletedAtAsc(Instant since);

    /** Returns all completed tasks ordered by completion date descending. */
    List<Task> findByIsCompletedTrueOrderByCompletedAtDesc();

    /**
     * Returns incomplete, non-notified, non-all-day tasks with a due date on or before {@code in15min}.
     * Used by the notification scheduler to identify tasks due in approximately 15 minutes.
     */
    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.isNotified = false AND t.allDay = false AND t.dueAt IS NOT NULL AND t.dueAt <= :in15min")
    List<Task> findTasksDueAround(@Param("in15min") Instant in15min);

    /**
     * Returns all incomplete, non-recurring tasks whose due date falls within [start, end).
     * Ordered by due date ascending. Used to build the occurrence list for a date range.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.dueAt >= :start AND t.dueAt < :end
            AND t.isCompleted = false
            AND t.isRecurring = false
            ORDER BY t.dueAt ASC
            """)
    List<Task> findNonRecurringTasksInPeriod(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Returns all recurring tasks whose series overlaps the given period.
     * A task is included when its first due date is before the period end and its
     * {@code rruleEndsAt} (if set) has not yet passed the period start.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.isRecurring = true
            AND t.recurrenceRule IS NOT NULL
            AND t.dueAt < :periodEnd
            AND (t.rruleEndsAt IS NULL OR t.rruleEndsAt >= :periodStart)
            """)
    List<Task> findActiveRecurringTasksForPeriod(
            @Param("periodStart") Instant periodStart,
            @Param("periodEnd") Instant periodEnd);
}
