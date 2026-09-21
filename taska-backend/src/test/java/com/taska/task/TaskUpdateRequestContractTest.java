package com.taska.task;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taska.task.adapter.http.TaskUpdateRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

class TaskUpdateRequestContractTest {

    @Test
    void taskUpdateRequest_rejectsPayloadsThatOmitMutableProperties() {
        assertThatThrownBy(() -> new JsonMapper().readValue("{\"content\":\"Only title\"}", TaskUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }
}
