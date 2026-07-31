package com.taska.domain.priority;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OpenAiPriorityAssessmentClient {

    private static final String SYSTEM_PROMPT = """
            Assess each task independently. Return every supplied taskId exactly once. Urgency is LOW, MEDIUM, HIGH, or CRITICAL. Impact and risk are LOW, MEDIUM, or HIGH. Impact is the value of completion; risk is the consequence of delay. Estimate a positive durationMinutes. Give each assessment a confidence from 0 to 1 and a concise reason. Do not calculate points or total scores.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final JsonMapper jsonMapper;

    public PriorityEvaluationBatchResponse assess(PriorityEvaluationBatchRequest request) {
        String prompt = jsonMapper.writeValueAsString(request);
        return chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .entity(PriorityEvaluationBatchResponse.class);
    }
}
