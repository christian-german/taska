package com.taska.planningcalendar.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PlanningCalendarRulesOverlapValidator
        implements
            ConstraintValidator<NonOverlappingPlanningCalendarRules, Collection<? extends PlanningCalendarRuleValue>> {

    @Override
    public boolean isValid(
            Collection<? extends PlanningCalendarRuleValue> planningCalendarRules,
            ConstraintValidatorContext constraintValidatorContext) {
        if (planningCalendarRules == null) {
            return true;
        }

        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            int currentDayOfWeek = dayOfWeek;
            List<? extends PlanningCalendarRuleValue> rulesForDay = planningCalendarRules.stream()
                    .filter(
                            rule -> rule != null && rule.dayOfWeek() != null && rule.startMinute() != null && rule.endMinute() != null
                                    && rule.dayOfWeek() == currentDayOfWeek)
                    .sorted(Comparator.comparingInt(PlanningCalendarRuleValue::startMinute))
                    .toList();
            for (int ruleIndex = 1; ruleIndex < rulesForDay.size(); ruleIndex++) {
                if (rulesForDay.get(ruleIndex - 1).endMinute() > rulesForDay.get(ruleIndex).startMinute()) {
                    return false;
                }
            }
        }
        return true;
    }
}
