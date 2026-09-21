package com.taska;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taska.comment.adapter.http.CommentUpdateRequest;
import com.taska.planningcalendar.adapter.http.PlanningCalendarUpdateRequest;
import com.taska.project.adapter.http.ProjectUpdateRequest;
import com.taska.task.adapter.http.OccurrenceUpdateRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.json.JsonMapper;

class ReplacementRequestContractTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    @Test
    void commentReplacementRequiresContent() {
        assertThatThrownBy(() -> jsonMapper.readValue("{}", CommentUpdateRequest.class)).isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void planningCalendarReplacementRequiresRules() {
        assertThatThrownBy(() -> jsonMapper.readValue("{\"name\":\"Work\"}", PlanningCalendarUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void projectReplacementRequiresEveryMutableProperty() {
        assertThatThrownBy(() -> jsonMapper.readValue("{\"name\":\"Work\"}", ProjectUpdateRequest.class))
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
        assertThatThrownBy(() -> jsonMapper.readValue("{\"content\":\"Updated\",\"unexpected\":true}", CommentUpdateRequest.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }

    @Test
    void occurrenceReschedulingRequiresScheduleButAcceptsNullToRestoreOriginalDate() {
        assertThatThrownBy(() -> jsonMapper.readValue("{}", OccurrenceUpdateRequest.class)).isInstanceOf(MismatchedInputException.class);
        assertThat(jsonMapper.readValue("{\"scheduledAt\":null}", OccurrenceUpdateRequest.class).scheduledAt()).isNull();
    }

    @Test
    void occurrenceReschedulingRejectsHistoricalOverrideFields() {
        for (String field : new String[]{"content", "priority", "dueAt"}) {
            assertThatThrownBy(() -> jsonMapper.readValue("{\"scheduledAt\":null,\"" + field + "\":null}", OccurrenceUpdateRequest.class))
                    .isInstanceOf(UnrecognizedPropertyException.class);
        }
    }
}
