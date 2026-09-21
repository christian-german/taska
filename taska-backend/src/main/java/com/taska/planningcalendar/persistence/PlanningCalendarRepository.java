package com.taska.planningcalendar.persistence;

import com.taska.planningcalendar.model.PlanningCalendar;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanningCalendarRepository extends JpaRepository<PlanningCalendar, UUID> {
}
