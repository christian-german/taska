package com.taska.domain.planningcalendar;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlanningCalendarRulesOverlapValidator.class)
public @interface NonOverlappingPlanningCalendarRules {

    String message() default "availability rules must not overlap";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
