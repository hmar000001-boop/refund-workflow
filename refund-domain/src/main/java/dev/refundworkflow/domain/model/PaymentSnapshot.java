package dev.refundworkflow.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Immutable payment facts obtained from a trusted payment query adapter. */
public record PaymentSnapshot(
        PaymentId paymentId,
        CustomerId customerId,
        Money transactionAmount,
        PaymentStatus status,
        Optional<Instant> capturedAt) {

    public PaymentSnapshot {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(transactionAmount, "transactionAmount must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");

        if (!transactionAmount.isPositive()) {
            throw new IllegalArgumentException("transaction amount must be positive");
        }

        boolean captureExpected = switch (status) {
            case CAPTURED, PARTIALLY_REFUNDED, REFUNDED, DISPUTED -> true;
            case AUTHORIZED, VOIDED, FAILED -> false;
        };
        if (captureExpected && capturedAt.isEmpty()) {
            throw new IllegalArgumentException("capturedAt is required for payment status " + status);
        }
    }
}
