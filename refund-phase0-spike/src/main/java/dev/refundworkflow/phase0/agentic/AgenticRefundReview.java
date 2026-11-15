package dev.refundworkflow.phase0.agentic;

import dev.langchain4j.agentic.UntypedAgent;

import java.util.Map;
import java.util.Objects;

/**
 * Owns the bounded Agentic sequence. It deliberately exposes a simple
 * application-facing method rather than leaking AgenticScope to callers.
 */
public final class AgenticRefundReview {

    private static final String REQUEST_KEY = "request";
    private static final String REVIEW_KEY = "review";

    private final UntypedAgent workflow;

    public AgenticRefundReview(UntypedAgent workflow) {
        this.workflow = Objects.requireNonNull(workflow, "workflow");
    }

    public String review(String request) {
        if (request == null || request.isBlank()) {
            throw new IllegalArgumentException("request must not be blank");
        }

        Object result = workflow.invoke(Map.of(REQUEST_KEY, request));
        if (!(result instanceof String review) || review.isBlank()) {
            throw new IllegalStateException("Agentic workflow returned no usable review");
        }
        return review;
    }

    public static AgenticRefundReview using(
            RefundRequestInterpreter interpreter,
            RefundEvidenceReviewer reviewer) {
        UntypedAgent sequence = dev.langchain4j.agentic.AgenticServices.sequenceBuilder()
                .subAgents(interpreter, reviewer)
                .outputKey(REVIEW_KEY)
                .build();
        return new AgenticRefundReview(sequence);
    }
}
