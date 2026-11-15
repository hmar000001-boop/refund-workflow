package dev.refundworkflow.application.port;

import dev.refundworkflow.domain.model.PaymentId;
import dev.refundworkflow.domain.model.PaymentSnapshot;

import java.util.Optional;

/** Outbound port for reading an authoritative payment snapshot. */
public interface PaymentQueryPort {

    Optional<PaymentSnapshot> findById(PaymentId paymentId);
}
