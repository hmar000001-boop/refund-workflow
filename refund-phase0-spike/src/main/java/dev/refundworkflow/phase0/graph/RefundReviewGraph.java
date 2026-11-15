package dev.refundworkflow.phase0.graph;

import dev.refundworkflow.phase0.agentic.AgenticRefundReview;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;
import java.util.Objects;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/** Builds a two-node LangGraph4j workflow whose second node invokes LangChain4j Agentic. */
public final class RefundReviewGraph {

    private RefundReviewGraph() {
    }

    public static CompiledGraph<Phase0GraphState> compile(AgenticRefundReview refundReview)
            throws GraphStateException {
        Objects.requireNonNull(refundReview, "refundReview");

        NodeAction<Phase0GraphState> prepare = state ->
                Map.of(Phase0GraphState.EVENTS, "graph-started");

        NodeAction<Phase0GraphState> agenticReview = state -> Map.of(
                Phase0GraphState.EVENTS, "agentic-review-completed",
                Phase0GraphState.REVIEW, refundReview.review(state.request())
        );

        return new StateGraph<>(Phase0GraphState.SCHEMA, Phase0GraphState::new)
                .addNode("prepare", node_async(prepare))
                .addNode("agentic-review", node_async(agenticReview))
                .addEdge(START, "prepare")
                .addEdge("prepare", "agentic-review")
                .addEdge("agentic-review", END)
                .compile();
    }
}
