package dev.refundworkflow.application.service;

import dev.refundworkflow.application.inbound.EvaluateRefundRequestUseCase;
import dev.refundworkflow.application.model.RefundEvaluation;
import dev.refundworkflow.application.port.PaymentQueryPort;
import dev.refundworkflow.application.port.RefundHistoryQueryPort;
import dev.refundworkflow.domain.model.RefundRequest;
import dev.refundworkflow.domain.model.RefundRecord;
import dev.refundworkflow.domain.policy.RefundEligibilityPolicy;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates the read ports and pure domain policy. This service has no dependency on
 * LangGraph4j, LangChain4j, persistence technology, transport framework or system clock singleton.
 */
public final class DefaultEvaluateRefundRequestService implements EvaluateRefundRequestUseCase {

    public static final String POLICY_VERSION = "refund-eligibility-v1";

    private final PaymentQueryPort paymentQuery;
    private final RefundHistoryQueryPort refundHistoryQuery;
    private final RefundEligibilityPolicy eligibilityPolicy;
    private final Clock clock;

    public DefaultEvaluateRefundRequestService(
            PaymentQueryPort paymentQuery,
            RefundHistoryQueryPort refundHistoryQuery,
            RefundEligibilityPolicy eligibilityPolicy,
            Clock clock) {
        this.paymentQuery = Objects.requireNonNull(paymentQuery, "paymentQuery must not be null");
        this.refundHistoryQuery = Objects.requireNonNull(refundHistoryQuery, "refundHistoryQuery must not be null");
        this.eligibilityPolicy = Objects.requireNonNull(eligibilityPolicy, "eligibilityPolicy must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public RefundEvaluation evaluate(RefundRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var payment = Objects.requireNonNull(
                paymentQuery.findById(request.paymentId()),
                "PaymentQueryPort returned null instead of Optional");
        List<RefundRecord> history = payment.isPresent()
                ? List.copyOf(Objects.requireNonNull(
                        refundHistoryQuery.findByPaymentId(request.paymentId()),
                        "RefundHistoryQueryPort returned null instead of a list"))
                : List.of();

        var decision = eligibilityPolicy.evaluate(request, payment, history);
        return new RefundEvaluation(request, decision, clock.instant(), POLICY_VERSION);
    }
}
