package dev.refundworkflow.domain.model;

import java.util.Objects;

public record PaymentId(String value) {

    public PaymentId {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("payment id must not be blank");
        }
    }
}
