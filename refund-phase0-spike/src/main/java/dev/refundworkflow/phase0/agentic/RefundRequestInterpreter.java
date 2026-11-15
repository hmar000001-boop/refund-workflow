package dev.refundworkflow.phase0.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Phase 0 contract used only to prove that the experimental Agentic API
 * can create and invoke a declarative Java agent.
 */
public interface RefundRequestInterpreter {

    @UserMessage("""
            Interpret the refund request. Identify the request in neutral language.
            Do not decide eligibility, amount, approval, or execution.
            Request: {{request}}
            """)
    @Agent("Interprets a refund request without making financial decisions")
    String interpret(@V("request") String request);
}
