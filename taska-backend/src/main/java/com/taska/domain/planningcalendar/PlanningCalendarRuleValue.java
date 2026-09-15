package com.taska.domain.planningcalendar;

/** Values required to validate a weekly planning-calendar window. */
public interface PlanningCalendarRuleValue {

    Integer dayOfWeek();

    Integer startMinute();

    Integer endMinute();
}
