package com.taska.domain.task;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RecurrenceService {

    /**
     * Expands the RRULE of a recurring task and returns all occurrence instants that fall within
     * the given period. Returns an empty list when the task has no due date or when the series has
     * already ended before {@code periodStart}.
     * <p>
     * Occurrences are post-filtered against {@code rruleEndsAt}: any occurrence on or after that
     * cutoff is excluded, regardless of what iCal4j returns.
     *
     * @param task        the recurring task — must have {@code isRecurring = true} and a non-null
     *                    {@code recurrenceRule}, otherwise an {@link IllegalStateException} is thrown
     * @param periodStart the inclusive start of the period to expand into
     * @param periodEnd   the inclusive end of the period to expand into
     * @return list of occurrence instants within the period, possibly empty
     * @throws IllegalStateException     if the task is not recurring or has no recurrence rule
     * @throws IllegalArgumentException  if the recurrence rule string cannot be parsed by iCal4j
     */
    public List<Instant> getOccurrencesInRange(Task task, Instant periodStart, Instant periodEnd) {

        // Validate input parameters.
        if (!Boolean.TRUE.equals(task.getIsRecurring()) || task.getRecurrenceRule() == null) {
            throw new IllegalStateException("Task " + task.getId() + " is_recurring=false or has no recurrence_rule");
        }
        if (task.getScheduledAt() == null || task.getScheduledAt().compareTo(periodEnd) >= 0) {
            return List.of();
        }
        if (task.getRruleEndsAt() != null && task.getRruleEndsAt().compareTo(periodStart) < 0) {
            return List.of();
        }

        Recur recur;
        try {
            recur = new Recur(task.getRecurrenceRule());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid recurrence_rule for task " + task.getId() + ": " + task.getRecurrenceRule(), e);
        }

        DateTime dtStart = new DateTime(Date.from(task.getScheduledAt()));
        DateTime from = new DateTime(Date.from(periodStart));
        DateTime to = new DateTime(Date.from(periodEnd));

        DateList dates = recur.getDates(dtStart, from, to, Value.DATE_TIME);

        List<Instant> result = new ArrayList<>(dates.size());
        for (net.fortuna.ical4j.model.Date d : dates) {
            result.add(Instant.ofEpochMilli(d.getTime()));
        }

        // Remove occurrences that are after the rrule end date.
        Instant cutoff = task.getRruleEndsAt();
        result.removeIf(occ -> cutoff != null && !occ.isBefore(cutoff));

        return result;
    }
}
