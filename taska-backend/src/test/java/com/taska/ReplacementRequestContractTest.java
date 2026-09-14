package com.taska;

import com.taska.domain.comment.controller.CommentUpdateRequest;
import com.taska.domain.planningcalendar.controller.PlanningCalendarUpdateRequest;
import com.taska.domain.project.controller.ProjectUpdateRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplacementRequestContractTest {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void commentReplacementRequiresContent() {
        assertThatThrownBy(() -> jsonMapper.readValue("{}", CommentUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void planningCalendarReplacementRequiresRules() {
        assertThatThrownBy(() -> jsonMapper.readValue(
                "{\"name\":\"Work\"}", PlanningCalendarUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void projectReplacementRequiresEveryMutableProperty() {
        assertThatThrownBy(() -> jsonMapper.readValue(
                "{\"name\":\"Work\"}", ProjectUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void projectReplacementUsesExplicitNullToClearParent() throws Exception {
        ProjectUpdateRequest request = jsonMapper.readValue("""
                {
                  "name":"Work",
                  "color":"blue",
                  "parentId":null,
                  "order":2,
                  "isFavorite":true,
                  "viewStyle":"LIST",
                  "planningCalendarId":"00000000-0000-0000-0000-000000000001"
                }
                """, ProjectUpdateRequest.class);

        assertThat(request.parentId()).isNull();
    }

    @Test
    void replacementRequestsRejectUnknownProperties() {
        assertThatThrownBy(() -> jsonMapper.readValue(
                "{\"content\":\"Updated\",\"unexpected\":true}", CommentUpdateRequest.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }
}
