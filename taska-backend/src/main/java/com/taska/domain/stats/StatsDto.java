package com.taska.domain.stats;

import java.util.List;
import java.util.UUID;

/**
 * Aggregated statistics snapshot returned by {@code GET /stats/overview}.
 *
 * @param totalCompleted   total number of tasks marked as completed across all projects
 * @param totalActive      total number of incomplete tasks across all projects
 * @param overdue          number of incomplete tasks whose due date is in the past
 * @param streakDays       current completion streak: the number of consecutive days (ending yesterday
 *                         or today) on which at least one task was completed; today is skipped in the
 *                         count if no tasks have been completed yet
 * @param completedThisWeek number of tasks completed in the last 7 days (including today)
 * @param remainingMinutes  sum of {@code estimateMinutes} across all incomplete tasks; tasks without
 *                          an estimate contribute 0
 * @param last14Days        per-day completed task counts for the 14 days ending today, ordered
 *                          chronologically from oldest to newest
 * @param byProject         per-project task totals; projects with no tasks are excluded
 */
public record StatsDto(
        long totalCompleted,
        long totalActive,
        long overdue,
        int streakDays,
        long completedThisWeek,
        int remainingMinutes,
        List<DailyCount> last14Days,
        List<ProjectStat> byProject
) {
    /**
     * Completed task count for a single calendar day.
     *
     * @param date  ISO-8601 date string ({@code YYYY-MM-DD})
     * @param count number of tasks completed on that day
     */
    public record DailyCount(String date, long count) {}

    /**
     * Task totals for a single project.
     *
     * @param projectId UUID of the project
     * @param name      display name of the project
     * @param color     color identifier of the project
     * @param total     total number of tasks (both completed and active) in the project
     * @param done      number of completed tasks in the project
     */
    public record ProjectStat(UUID projectId, String name, String color, long total, long done) {}
}
