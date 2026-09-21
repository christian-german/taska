package com.taska.priority.adapter.openai;

import com.taska.priority.application.PriorityAssessmentClient;
import com.taska.priority.model.PriorityAssessmentBatch;
import com.taska.priority.model.PriorityAssessmentBatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OpenAiPriorityAssessmentClient implements PriorityAssessmentClient {

    private static final String SYSTEM_PROMPT = """
            Assess each task independently. Return every supplied taskId exactly once. Urgency is LOW, MEDIUM, HIGH, or CRITICAL. Impact and risk are LOW, MEDIUM, or HIGH. Impact is the value of completion; risk is the consequence of delay. Estimate a positive durationMinutes. Give each assessment a confidence from 0 to 1 and a concise reason. Do not calculate points or total scores.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final JsonMapper jsonMapper;

    @Override
    public PriorityAssessmentBatchResult assess(PriorityAssessmentBatch batchRequest) {
        String prompt = jsonMapper.writeValueAsString(batchRequest);
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().entity(PriorityAssessmentBatchResult.class);
    }
}
