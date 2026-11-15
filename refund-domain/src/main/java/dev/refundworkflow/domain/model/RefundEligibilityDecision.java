package dev.refundworkflow.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RefundEligibilityDecision(
        RefundEligibilityStatus status,
        List<RefundDecisionCode> decisionCodes,
        Optional<Money> remainingRefundableAmount) {

    public RefundEligibilityDecision {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(decisionCodes, "decisionCodes must not be null");
        Objects.requireNonNull(remainingRefundableAmount, "remainingRefundableAmount must not be null");
        decisionCodes = List.copyOf(decisionCodes);
        if (decisionCodes.isEmpty()) {
            throw new IllegalArgumentException("at least one decision code is required");
        }
        if (decisionCodes.stream().distinct().count() != decisionCodes.size()) {
            throw new IllegalArgumentException("decision codes must be unique");
        }
        if (status == RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL
                && !decisionCodes.equals(List.of(RefundDecisionCode.ELIGIBLE))) {
            throw new IllegalArgumentException("eligible decision must contain only ELIGIBLE code");
        }
        if (status != RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL
                && decisionCodes.contains(RefundDecisionCode.ELIGIBLE)) {
            throw new IllegalArgumentException("non-eligible decision must not contain ELIGIBLE code");
        }
    }

    public boolean isEligibleForApproval() {
        return status == RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL;
    }
}
