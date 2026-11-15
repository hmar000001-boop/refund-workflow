package dev.refundworkflow.phase0;

import dev.refundworkflow.phase0.agentic.AgenticRefundReview;
import dev.refundworkflow.phase0.agentic.RefundEvidenceReviewer;
import dev.refundworkflow.phase0.agentic.RefundRequestInterpreter;
import dev.refundworkflow.phase0.graph.Phase0GraphState;
import dev.refundworkflow.phase0.graph.RefundReviewGraph;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameworkCompatibilityTest {

    @Test
    void langchain4jAgenticSequenceRunsWithoutExternalModelCredentials() {
        var interpreterModel = new RecordingChatModel(
                "Interpretation: customer requests a refund for transaction TX-100.");
        var reviewerModel = new RecordingChatModel(
                "Review: further deterministic eligibility assessment is required.");

        var interpreter = AgenticServices.agentBuilder(RefundRequestInterpreter.class)
                .chatModel(interpreterModel)
                .outputKey("interpretation")
                .build();
        var reviewer = AgenticServices.agentBuilder(RefundEvidenceReviewer.class)
                .chatModel(reviewerModel)
                .outputKey("review")
                .build();

        AgenticRefundReview workflow = AgenticRefundReview.using(interpreter, reviewer);
        String result = workflow.review("Please refund TX-100.");

        assertEquals("Review: further deterministic eligibility assessment is required.", result);
        assertEquals(1, interpreterModel.requests().size());
        assertEquals(1, reviewerModel.requests().size());
        assertTrue(reviewerModel.requests().getFirst().toString().contains(
                "Interpretation: customer requests a refund for transaction TX-100."));
    }

    @Test
    void langgraph4jNodeInvokesAgenticSequenceAndReturnsFinalState() throws Exception {
        var interpreter = AgenticServices.agentBuilder(RefundRequestInterpreter.class)
                .chatModel(new RecordingChatModel("Interpretation: refund request received."))
                .outputKey("interpretation")
                .build();
        var reviewer = AgenticServices.agentBuilder(RefundEvidenceReviewer.class)
                .chatModel(new RecordingChatModel("Review: no execution was performed."))
                .outputKey("review")
                .build();

        var graph = RefundReviewGraph.compile(AgenticRefundReview.using(interpreter, reviewer));
        var finalState = graph.invoke(
                        GraphInput.args(Map.of(
                                Phase0GraphState.REQUEST,
                                "Please review a refund request for TX-100."
                        )),
                        RunnableConfig.empty()
                )
                .orElseThrow(() -> new AssertionError("Expected final graph state"));

        assertEquals(
                List.of("graph-started", "agentic-review-completed", "graph-finished"),
                finalState.events());
        assertEquals("Review: no execution was performed.", finalState.review());
        assertFalse(finalState.review().isBlank());
    }

    private static final class RecordingChatModel implements ChatModel {
        private final String response;
        private final List<ChatRequest> requests = new CopyOnWriteArrayList<>();

        private RecordingChatModel(String response) {
            this.response = response;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            requests.add(request);
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(response))
                    .build();
        }

        private List<ChatRequest> requests() {
            return List.copyOf(requests);
        }
    }
}
