package dev.refundworkflow.application.model;

import dev.refundworkflow.domain.model.RefundEligibilityDecision;
import dev.refundworkflow.domain.model.RefundRequest;

import java.time.Instant;
import java.util.Objects;

/** Immutable, auditable result of one eligibility-policy evaluation. */
public record RefundEvaluation(
        RefundRequest request,
        RefundEligibilityDecision decision,
        Instant evaluatedAt,
        String policyVersion) {

    public RefundEvaluation {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");
        Objects.requireNonNull(policyVersion, "policyVersion must not be null");
        policyVersion = policyVersion.trim();
        if (policyVersion.isEmpty()) {
            throw new IllegalArgumentException("policyVersion must not be blank");
        }
    }
}
