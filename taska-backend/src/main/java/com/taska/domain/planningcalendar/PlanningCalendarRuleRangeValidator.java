package com.taska.domain.planningcalendar;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlanningCalendarRuleRangeValidator
        implements ConstraintValidator<ValidPlanningCalendarRuleRange, PlanningCalendarRuleValue> {

    @Override
    public boolean isValid(
            PlanningCalendarRuleValue planningCalendarRule,
            ConstraintValidatorContext constraintValidatorContext) {
        if (planningCalendarRule == null
                || planningCalendarRule.startMinute() == null
                || planningCalendarRule.endMinute() == null) {
            return true;
        }
        return planningCalendarRule.startMinute() < planningCalendarRule.endMinute();
    }
}
