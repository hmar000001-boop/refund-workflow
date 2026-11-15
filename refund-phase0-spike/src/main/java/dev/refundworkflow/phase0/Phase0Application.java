package dev.refundworkflow.phase0;

import dev.refundworkflow.phase0.agentic.AgenticRefundReview;
import dev.refundworkflow.phase0.agentic.RefundEvidenceReviewer;
import dev.refundworkflow.phase0.agentic.RefundRequestInterpreter;
import dev.refundworkflow.phase0.graph.Phase0GraphState;
import dev.refundworkflow.phase0.graph.RefundReviewGraph;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;

import java.util.Map;

/** Runnable deterministic smoke application; no API key or network model is required. */
public final class Phase0Application {

    private Phase0Application() {
    }

    public static void main(String[] args) throws Exception {
        var interpreter = AgenticServices.agentBuilder(RefundRequestInterpreter.class)
                .chatModel(new FixedResponseChatModel("Interpretation produced by the deterministic test model."))
                .outputKey("interpretation")
                .build();

        var reviewer = AgenticServices.agentBuilder(RefundEvidenceReviewer.class)
                .chatModel(new FixedResponseChatModel("Reviewer note produced by the deterministic test model."))
                .outputKey("review")
                .build();

        var agenticReview = AgenticRefundReview.using(interpreter, reviewer);
        var graph = RefundReviewGraph.compile(agenticReview);

        var finalState = graph.invoke(
                        GraphInput.args(Map.of(
                                Phase0GraphState.REQUEST,
                                "Please review my refund request for transaction TX-100."
                        )),
                        RunnableConfig.empty()
                )
                .orElseThrow(() -> new IllegalStateException("Graph produced no final state"));

        System.out.println("Graph events: " + finalState.events());
        System.out.println("Agentic review: " + finalState.review());
    }

    private static final class FixedResponseChatModel implements ChatModel {
        private final String response;

        private FixedResponseChatModel(String response) {
            this.response = response;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(response))
                    .build();
        }
    }
}
