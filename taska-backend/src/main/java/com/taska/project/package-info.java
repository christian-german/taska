/**
 * Projects grouping tasks, and their assignment to a planning calendar.
 *
 * <p>
 * The module owns which calendar a project uses, but not the rule that scheduled work must fit that calendar: it announces an intended change through
 * {@link com.taska.project.model.ProjectPlanningCalendarChangeRequested} and lets the rule's owner veto it. That is what keeps this module
 * independent of tasks.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Project", allowedDependencies = {"platform :: config", "platform :: exception",
        "platform :: mcp", "planningcalendar :: application"})
package com.taska.project;
