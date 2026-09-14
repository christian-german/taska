package com.taska.domain.planningcalendar.repository;

import com.taska.domain.planningcalendar.PlanningCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanningCalendarRepository extends JpaRepository<PlanningCalendar, UUID> {
}
