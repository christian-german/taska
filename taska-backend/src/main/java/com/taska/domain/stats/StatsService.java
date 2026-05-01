package com.taska.domain.stats;

import com.taska.domain.project.Project;
import com.taska.domain.project.ProjectRepository;
import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;

    public StatsService(TaskRepository taskRepo, ProjectRepository projectRepo) {
        this.taskRepo = taskRepo;
        this.projectRepo = projectRepo;
    }

    public StatsResponse compute() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        long completed = taskRepo.countByIsCompletedTrue();
        long active = taskRepo.countByIsCompletedFalse();
        long overdue = taskRepo.countByIsCompletedFalseAndDueDateBefore(today);

        Instant since = today.minusDays(14).atStartOfDay(zone).toInstant();
        List<Task> recentDone = taskRepo.findByCompletedAtAfterOrderByCompletedAtAsc(since);

        Map<LocalDate, Long> doneByDay = new HashMap<>();
        for (Task t : recentDone) {
            if (t.getCompletedAt() == null) continue;
            LocalDate d = t.getCompletedAt().atZone(zone).toLocalDate();
            doneByDay.merge(d, 1L, Long::sum);
        }

        List<StatsResponse.DailyCount> last14 = new ArrayList<>();
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last14.add(new StatsResponse.DailyCount(d.format(iso), doneByDay.getOrDefault(d, 0L)));
        }

        long completedThisWeek = 0;
        for (int i = 6; i >= 0; i--) {
            completedThisWeek += doneByDay.getOrDefault(today.minusDays(i), 0L);
        }

        int streak = 0;
        LocalDate cursor = today;
        while (true) {
            long n = doneByDay.getOrDefault(cursor, 0L);
            if (n == 0) {
                if (cursor.equals(today)) {
                    cursor = cursor.minusDays(1);
                    continue;
                }
                break;
            }
            streak++;
            cursor = cursor.minusDays(1);
            if (streak >= 365) break;
        }

        int remainingMinutes = taskRepo.findAll().stream()
                .filter(t -> Boolean.FALSE.equals(t.getIsCompleted()))
                .mapToInt(t -> t.getEstimateMinutes() == null ? 0 : t.getEstimateMinutes())
                .sum();

        Map<java.util.UUID, long[]> byProject = new HashMap<>();
        for (Task t : taskRepo.findAll()) {
            if (t.getProjectId() == null) continue;
            long[] arr = byProject.computeIfAbsent(t.getProjectId(), k -> new long[2]);
            arr[0]++;
            if (Boolean.TRUE.equals(t.getIsCompleted())) arr[1]++;
        }

        List<StatsResponse.ProjectStat> projectStats = new ArrayList<>();
        for (Project p : projectRepo.findAll()) {
            long[] arr = byProject.get(p.getId());
            if (arr == null || arr[0] == 0) continue;
            projectStats.add(new StatsResponse.ProjectStat(p.getId(), p.getName(), p.getColor(), arr[0], arr[1]));
        }

        return new StatsResponse(completed, active, overdue, streak, completedThisWeek,
                remainingMinutes, last14, projectStats);
    }
}
