package com.taska.domain.planningcalendar.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanningCalendarRuleRepository extends JpaRepository<PlanningCalendarRule, UUID> {

  List<PlanningCalendarRule> findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(UUID calendarId);

  void deleteByCalendarId(UUID calendarId);
}
