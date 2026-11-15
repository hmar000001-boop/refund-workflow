package dev.refundworkflow.domain.model;

import java.util.Objects;
import java.util.UUID;

public record RefundId(UUID value) {

    public RefundId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RefundId newId() {
        return new RefundId(UUID.randomUUID());
    }
}
