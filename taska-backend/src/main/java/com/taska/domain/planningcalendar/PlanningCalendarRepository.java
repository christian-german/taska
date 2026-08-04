package com.taska.domain.planningcalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface PlanningCalendarRepository extends JpaRepository<PlanningCalendar, UUID> {}
