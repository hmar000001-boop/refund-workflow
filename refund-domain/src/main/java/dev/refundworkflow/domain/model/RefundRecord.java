package dev.refundworkflow.domain.model;

import java.time.Instant;
import java.util.Objects;

/** A previously recorded refund; pending and successful records reserve refundable balance. */
public record RefundRecord(
        RefundId id,
        RefundRequestId requestId,
        PaymentId paymentId,
        Money amount,
        RefundStatus status,
        Instant createdAt) {

    public RefundRecord {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("refund record amount must be positive");
        }
    }
}
