package dev.refundworkflow.application.port;

import dev.refundworkflow.domain.model.PaymentId;
import dev.refundworkflow.domain.model.RefundRecord;

import java.util.List;

/** Outbound port for reading all refund records that reserve a payment's refundable balance. */
public interface RefundHistoryQueryPort {

    List<RefundRecord> findByPaymentId(PaymentId paymentId);
}
