package com.taska.priority.application;

import com.taska.priority.model.PriorityAssessmentBatch;
import com.taska.priority.model.PriorityAssessmentBatchResult;

/**
 * Outbound port scoring a batch of tasks.
 *
 * <p>
 * Declared here rather than in the adapter that implements it: the evaluation service owns what it needs, and the language-model client is one
 * interchangeable way of providing it. Without this seam the service would depend on its own outbound adapter, which is the direction the layout
 * forbids.
 */
public interface PriorityAssessmentClient {

    /**
     * Assesses every task in the batch.
     *
     * @param batch tasks to assess
     * @return one assessment per submitted task
     */
    PriorityAssessmentBatchResult assess(PriorityAssessmentBatch batch);
}
