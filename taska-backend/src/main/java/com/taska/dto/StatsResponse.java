package com.taska.dto;

import java.util.List;
import java.util.UUID;

public record StatsResponse(
        long totalCompleted,
        long totalActive,
        long overdue,
        int streakDays,
        long completedThisWeek,
        int remainingMinutes,
        List<DailyCount> last14Days,
        List<ProjectStat> byProject
) {
    public record DailyCount(String date, long count) {}
    public record ProjectStat(UUID projectId, String name, String color, long total, long done) {}
}
