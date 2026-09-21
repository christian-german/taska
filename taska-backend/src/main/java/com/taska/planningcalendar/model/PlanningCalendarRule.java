package com.taska.planningcalendar.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "planning_calendar_rules")
public class PlanningCalendarRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "calendar_id", nullable = false)
    private UUID calendarId;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_minute", nullable = false)
    private int startMinute;

    @Column(name = "end_minute", nullable = false)
    private int endMinute;

    public UUID getId() {
        return this.id;
    }

    public UUID getCalendarId() {
        return this.calendarId;
    }

    public int getDayOfWeek() {
        return this.dayOfWeek;
    }

    public int getStartMinute() {
        return this.startMinute;
    }

    public int getEndMinute() {
        return this.endMinute;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setCalendarId(final UUID calendarId) {
        this.calendarId = calendarId;
    }

    public void setDayOfWeek(final int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartMinute(final int startMinute) {
        this.startMinute = startMinute;
    }

    public void setEndMinute(final int endMinute) {
        this.endMinute = endMinute;
    }
}
