package dev.refundworkflow.domain.model;

public enum PaymentStatus {
    AUTHORIZED,
    CAPTURED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    DISPUTED,
    VOIDED,
    FAILED
}
