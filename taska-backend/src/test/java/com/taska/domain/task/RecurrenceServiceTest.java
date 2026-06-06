package com.taska.domain.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecurrenceServiceTest {

    private RecurrenceService service;

    @BeforeEach
    void setUp() {
        service = new RecurrenceService();
    }

    private Task taskWith(String rrule, String dueAt) {
        Task t = new Task();
        t.setIsRecurring(true);
        t.setRecurrenceRule(rrule);
        t.setDueAt(Instant.parse(dueAt));
        return t;
    }

    private Instant dayStart(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Test
    void daily_task_has_occurrence_every_day() {
        Task task = taskWith("FREQ=DAILY", "2026-05-01T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19"));
        assertThat(occ).hasSize(1);
        assertThat(occ.get(0)).isEqualTo(Instant.parse("2026-05-18T10:00:00Z"));
    }

    @Test
    void weekly_monday_task_not_on_tuesday() {
        Task task = taskWith("FREQ=WEEKLY;BYDAY=MO", "2026-05-04T09:00:00Z");

        // 2026-05-19 is a Tuesday
        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-19"), dayStart("2026-05-20"));
        assertThat(occ).isEmpty();
    }

    @Test
    void weekly_monday_task_on_monday() {
        Task task = taskWith("FREQ=WEEKLY;BYDAY=MO", "2026-05-04T09:00:00Z");

        // 2026-05-18 is a Monday
        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19"));
        assertThat(occ).hasSize(1);
    }

    @Test
    void no_occurrence_before_dtstart() {
        Task task = taskWith("FREQ=DAILY", "2026-05-18T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-17"), dayStart("2026-05-18"));
        assertThat(occ).isEmpty();
    }

    @Test
    void rrule_ends_at_stops_occurrences() {
        Task task = taskWith("FREQ=DAILY", "2026-05-01T10:00:00Z");
        task.setRruleEndsAt(Instant.parse("2026-05-15T23:59:59Z"));

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19"));
        assertThat(occ).isEmpty();
    }

    @Test
    void range_returns_multiple_occurrences() {
        Task task = taskWith("FREQ=DAILY", "2026-05-01T08:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-21"));
        assertThat(occ).hasSize(3);
    }

    @Test
    void non_recurring_task_throws_IllegalStateException() {
        Task task = new Task();
        task.setIsRecurring(false);

        assertThatThrownBy(() ->
                service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void monthly_first_day_of_month() {
        Task task = taskWith("FREQ=MONTHLY;BYMONTHDAY=1", "2026-05-01T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-06-01"), dayStart("2026-06-02"));
        assertThat(occ).hasSize(1);

        List<Instant> noOcc = service.getOccurrencesInRange(task, dayStart("2026-06-02"), dayStart("2026-06-03"));
        assertThat(noOcc).isEmpty();
    }

    // ── 1.2 ──────────────────────────────────────────────────────────────────

    @Test
    void daily_task_seven_day_window_returns_seven_occurrences() {
        Task task = taskWith("FREQ=DAILY", "2026-05-01T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-25"));

        assertThat(occ).hasSize(7);
    }

    // ── 1.3 ──────────────────────────────────────────────────────────────────

    @Test
    void weekly_monday_two_mondays_in_range_returns_two_occurrences() {
        Task task = taskWith("FREQ=WEEKLY;BYDAY=MO", "2026-05-04T09:00:00Z");
        // 2026-05-18 and 2026-05-25 are both Mondays
        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-26"));

        assertThat(occ).hasSize(2);
    }

    // ── 1.4 ──────────────────────────────────────────────────────────────────

    @Test
    void monthly_first_of_month_three_months_returns_three_occurrences() {
        Task task = taskWith("FREQ=MONTHLY;BYMONTHDAY=1", "2026-05-01T10:00:00Z");
        // June 1, July 1, August 1 all fall within the window
        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-06-01"), dayStart("2026-09-01"));

        assertThat(occ).hasSize(3);
    }

    // ── 1.6 ──────────────────────────────────────────────────────────────────
    // When rruleEndsAt < periodStart the service performs an early exit.
    // Within-window truncation is handled upstream by the repository query
    // (findActiveRecurringTasksForPeriod filters tasks whose rruleEndsAt
    // is before the period).

    @Test
    void rrule_ends_at_strictly_before_period_start_returns_empty() {
        Task task = taskWith("FREQ=DAILY", "2026-05-01T10:00:00Z");
        task.setRruleEndsAt(Instant.parse("2026-05-17T23:59:59Z")); // ends before period start

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19"));

        assertThat(occ).isEmpty();
    }

    // ── 1.8 ──────────────────────────────────────────────────────────────────

    @Test
    void single_day_window_equal_to_due_at_returns_one_occurrence() {
        Task task = taskWith("FREQ=DAILY", "2026-05-18T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19"));

        assertThat(occ).hasSize(1);
        assertThat(occ.get(0)).isEqualTo(Instant.parse("2026-05-18T10:00:00Z"));
    }

    // ── 1.9 ──────────────────────────────────────────────────────────────────
    // A recurring task with a null RRULE is a programming error: the service
    // throws IllegalStateException rather than silently returning empty.

    @Test
    void null_recurrence_rule_on_recurring_task_throws_IllegalStateException() {
        Task task = new Task();
        task.setIsRecurring(true);
        task.setRecurrenceRule(null);
        task.setDueAt(Instant.parse("2026-05-01T10:00:00Z"));

        assertThatThrownBy(() ->
                service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19")))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── 1.10 ─────────────────────────────────────────────────────────────────
    // A malformed RRULE string is a caller error: the service throws
    // IllegalArgumentException to surface the parse failure explicitly.

    @Test
    void malformed_recurrence_rule_throws_IllegalArgumentException() {
        Task task = taskWith("NOT_A_VALID_RRULE_STRING", "2026-05-01T10:00:00Z");

        assertThatThrownBy(() ->
                service.getOccurrencesInRange(task, dayStart("2026-05-18"), dayStart("2026-05-19")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 6.2 ──────────────────────────────────────────────────────────────────

    // dueAt is in May 2024 (same DST context as the May 2026 window), so the
    // projected UTC time is stable across the two dates.
    @Test
    void due_at_in_far_past_future_window_still_generates_occurrences() {
        Task task = taskWith("FREQ=DAILY", "2024-05-01T10:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-20"), dayStart("2026-05-21"));

        assertThat(occ).hasSize(1);
        assertThat(occ.get(0)).isEqualTo(Instant.parse("2026-05-20T10:00:00Z"));
    }

    // ── 6.3 ──────────────────────────────────────────────────────────────────

    @Test
    void large_daily_window_one_year_returns_three_hundred_sixty_five_occurrences() {
        Task task = taskWith("FREQ=DAILY", "2026-01-01T08:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-01-01"), dayStart("2027-01-01"));

        assertThat(occ).hasSize(365);
    }

    // ── 6.4 ──────────────────────────────────────────────────────────────────
    // 2026-05-05 is a Tuesday. Bi-weekly from that date: May 5, May 19.
    // The window ends on June 2 (exclusive midnight) so the June 2 occurrence
    // falls outside the window, giving exactly 2 results.

    @Test
    void biweekly_tuesday_four_week_window_returns_two_occurrences() {
        Task task = taskWith("FREQ=WEEKLY;INTERVAL=2;BYDAY=TU", "2026-05-05T09:00:00Z");

        List<Instant> occ = service.getOccurrencesInRange(task, dayStart("2026-05-05"), dayStart("2026-06-02"));

        assertThat(occ).hasSize(2);
        assertThat(occ.get(0)).isEqualTo(Instant.parse("2026-05-05T09:00:00Z"));
        assertThat(occ.get(1)).isEqualTo(Instant.parse("2026-05-19T09:00:00Z"));
    }
}
