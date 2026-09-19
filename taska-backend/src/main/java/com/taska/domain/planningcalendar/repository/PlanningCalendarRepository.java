package com.taska.domain.planningcalendar.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanningCalendarRepository extends JpaRepository<PlanningCalendar, UUID> {}
