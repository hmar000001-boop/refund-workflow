package dev.refundworkflow.phase0.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Phase 0 contract used to prove sequential agent composition.
 */
public interface RefundEvidenceReviewer {

    @UserMessage("""
            Review this interpretation as an advisory input only.
            Do not authorize or execute any refund.
            Interpretation: {{interpretation}}
            Return a concise reviewer note.
            """)
    @Agent("Produces an advisory note about the interpreted refund request")
    String review(@V("interpretation") String interpretation);
}
