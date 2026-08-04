package com.taska.domain.planningcalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PlanningCalendarRuleRepository extends JpaRepository<PlanningCalendarRule, UUID> {
    List<PlanningCalendarRule> findByCalendarIdOrderByDayOfWeekAscStartMinuteAsc(UUID calendarId);
    void deleteByCalendarId(UUID calendarId);
}
