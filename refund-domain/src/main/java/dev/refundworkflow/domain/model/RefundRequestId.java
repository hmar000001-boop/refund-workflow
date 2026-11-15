package dev.refundworkflow.domain.model;

import java.util.Objects;
import java.util.UUID;

public record RefundRequestId(UUID value) {

    public RefundRequestId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RefundRequestId newId() {
        return new RefundRequestId(UUID.randomUUID());
    }
}
