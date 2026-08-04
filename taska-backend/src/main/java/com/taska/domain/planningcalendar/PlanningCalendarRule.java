package com.taska.domain.planningcalendar;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "planning_calendar_rules") @Getter @Setter
public class PlanningCalendarRule {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "calendar_id", nullable = false) private UUID calendarId;
    @Column(name = "day_of_week", nullable = false) private int dayOfWeek;
    @Column(name = "start_minute", nullable = false) private int startMinute;
    @Column(name = "end_minute", nullable = false) private int endMinute;
}
