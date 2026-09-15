package com.taska.domain.planningcalendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanningCalendarRepository extends JpaRepository<PlanningCalendar, UUID> {
}
