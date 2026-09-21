/**
 * Tasks, recurring series, and the occurrences a series generates.
 *
 * <p>
 * The largest module, and the only one split into functional sub-domains: {@code definition} owns the stored task row, {@code occurrence} owns what
 * happens to one generated occurrence, and {@code recurrence} owns RRULE expansion. {@code model} carries the vocabulary all of them share.
 *
 * <p>
 * Three modules read this one. None of them is read back: everything this module needs to tell the rest of the application travels as an event.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Task", allowedDependencies = {"platform :: config", "platform :: exception",
        "platform :: mcp", "planningcalendar :: application", "project :: application", "project :: model"})
package com.taska.task;
