package dev.refundworkflow.domain.model;

public enum RefundStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELLED;

    public boolean consumesRefundableBalance() {
        return this == PENDING || this == SUCCEEDED;
    }
}
