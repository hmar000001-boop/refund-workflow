package dev.refundworkflow.domain.model;

import java.time.Instant;
import java.util.Objects;

public record RefundRequest(
        RefundRequestId id,
        PaymentId paymentId,
        CustomerId customerId,
        Money requestedAmount,
        RefundReasonCode reasonCode,
        Instant requestedAt) {

    public RefundRequest {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(requestedAmount, "requestedAmount must not be null");
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        if (!requestedAmount.isPositive()) {
            throw new IllegalArgumentException("requested refund amount must be positive");
        }
    }
}
