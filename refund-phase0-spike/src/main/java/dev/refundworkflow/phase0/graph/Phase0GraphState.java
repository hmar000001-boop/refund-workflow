package dev.refundworkflow.phase0.graph;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Minimal typed state for the Phase 0 cross-library proof. */
public final class Phase0GraphState extends AgentState {

    public static final String REQUEST = "request";
    public static final String EVENTS = "events";
    public static final String REVIEW = "review";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            REQUEST, Channels.base(() -> ""),
            EVENTS, Channels.appender(ArrayList::new),
            REVIEW, Channels.base(() -> "")
    );

    public Phase0GraphState(Map<String, Object> initialData) {
        super(initialData);
    }

    public String request() {
        return this.<String>value(REQUEST).orElse("");
    }

    public List<String> events() {
        return this.<List<String>>value(EVENTS).orElse(List.of());
    }

    public String review() {
        return this.<String>value(REVIEW).orElse("");
    }
}
