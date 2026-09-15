package com.taska.domain.planningcalendar;

import static org.assertj.core.api.Assertions.assertThat;

import com.taska.domain.planningcalendar.controller.PlanningCalendarCreateRequest;
import com.taska.domain.planningcalendar.controller.PlanningCalendarUpdateRequest;
import com.taska.domain.planningcalendar.service.PlanningCalendarCreateParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarRuleParameters;
import com.taska.domain.planningcalendar.service.PlanningCalendarUpdateParameters;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlanningCalendarValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void createRequestValidatesNestedRulePresenceAndBounds() {
    PlanningCalendarCreateRequest request =
        new PlanningCalendarCreateRequest(
            "Work", List.of(new PlanningCalendarCreateRequest.Rule(8, null, 1_441)));

    assertThat(validator.validate(request))
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("rules[0].dayOfWeek", "rules[0].startMinute", "rules[0].endMinute");
  }

  @Test
  void updateRequestValidatesRuleRangeAndOverlap() {
    PlanningCalendarUpdateRequest request =
        new PlanningCalendarUpdateRequest(
            "Work",
            List.of(
                new PlanningCalendarUpdateRequest.Rule(1, 540, 720),
                new PlanningCalendarUpdateRequest.Rule(1, 600, 600)));

    assertThat(validator.validate(request))
        .extracting(ConstraintViolation::getMessage)
        .contains("startMinute must be less than endMinute", "availability rules must not overlap");
  }

  @Test
  void createRequestAllowsItsOptionalRulesCollection() {
    PlanningCalendarCreateRequest request = new PlanningCalendarCreateRequest("Work", null);

    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void createParametersIndependentlyValidateTheirContract() {
    PlanningCalendarCreateParameters parameters =
        new PlanningCalendarCreateParameters(
            " ", List.of(new PlanningCalendarRuleParameters(0, 720, 540)));

    assertThat(validator.validate(parameters))
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("name", "rules[0].dayOfWeek", "rules[0]");
  }

  @Test
  void updateParametersRequireRulesAndAllowTouchingWindows() {
    PlanningCalendarUpdateParameters missingRules =
        new PlanningCalendarUpdateParameters("Work", null);
    PlanningCalendarUpdateParameters touchingRules =
        new PlanningCalendarUpdateParameters(
            "Work",
            List.of(
                new PlanningCalendarRuleParameters(1, 540, 720),
                new PlanningCalendarRuleParameters(1, 720, 780)));

    assertThat(validator.validate(missingRules))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactly("rules");
    assertThat(validator.validate(touchingRules)).isEmpty();
  }
}
