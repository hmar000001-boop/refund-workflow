package dev.refundworkflow.application.service;

import dev.refundworkflow.application.model.RefundEvaluation;
import dev.refundworkflow.application.port.PaymentQueryPort;
import dev.refundworkflow.application.port.RefundHistoryQueryPort;
import dev.refundworkflow.domain.model.CustomerId;
import dev.refundworkflow.domain.model.Money;
import dev.refundworkflow.domain.model.PaymentId;
import dev.refundworkflow.domain.model.PaymentSnapshot;
import dev.refundworkflow.domain.model.PaymentStatus;
import dev.refundworkflow.domain.model.RefundEligibilityStatus;
import dev.refundworkflow.domain.model.RefundReasonCode;
import dev.refundworkflow.domain.model.RefundRequest;
import dev.refundworkflow.domain.model.RefundRequestId;
import dev.refundworkflow.domain.policy.RefundEligibilityPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultEvaluateRefundRequestServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T14:00:00Z");
    private static final PaymentId PAYMENT_ID = new PaymentId("payment-42");
    private static final CustomerId CUSTOMER_ID = new CustomerId("customer-7");

    @Test
    void evaluatesUsingReadPortsAndAnInjectedClock() {
        AtomicInteger paymentReads = new AtomicInteger();
        AtomicInteger refundHistoryReads = new AtomicInteger();
        var paymentPort = (PaymentQueryPort) id -> {
            assertEquals(PAYMENT_ID, id);
            paymentReads.incrementAndGet();
            return Optional.of(new PaymentSnapshot(
                    PAYMENT_ID,
                    CUSTOMER_ID,
                    Money.of("125.00", "CAD"),
                    PaymentStatus.CAPTURED,
                    Optional.of(NOW.minusSeconds(600))));
        };
        var historyPort = (RefundHistoryQueryPort) id -> {
            assertEquals(PAYMENT_ID, id);
            refundHistoryReads.incrementAndGet();
            return List.of();
        };

        RefundEvaluation evaluation = service(paymentPort, historyPort).evaluate(request());

        assertEquals(RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL, evaluation.decision().status());
        assertEquals(NOW, evaluation.evaluatedAt());
        assertEquals(DefaultEvaluateRefundRequestService.POLICY_VERSION, evaluation.policyVersion());
        assertEquals(1, paymentReads.get());
        assertEquals(1, refundHistoryReads.get());
    }

    @Test
    void doesNotQueryRefundHistoryWhenPaymentDoesNotExist() {
        AtomicInteger refundHistoryReads = new AtomicInteger();
        var paymentPort = (PaymentQueryPort) ignored -> Optional.empty();
        var historyPort = (RefundHistoryQueryPort) ignored -> {
            refundHistoryReads.incrementAndGet();
            return List.of();
        };

        RefundEvaluation evaluation = service(paymentPort, historyPort).evaluate(request());

        assertEquals(RefundEligibilityStatus.INELIGIBLE, evaluation.decision().status());
        assertEquals(0, refundHistoryReads.get());
    }

    @Test
    void rejectsNullRequestsAndNullAdapterResultsAtTheBoundary() {
        var emptyPaymentPort = (PaymentQueryPort) ignored -> Optional.empty();
        var emptyHistoryPort = (RefundHistoryQueryPort) ignored -> List.of();
        var useCase = service(emptyPaymentPort, emptyHistoryPort);
        assertThrows(NullPointerException.class, () -> useCase.evaluate(null));

        var brokenPaymentPort = (PaymentQueryPort) ignored -> null;
        assertThrows(NullPointerException.class, () -> service(brokenPaymentPort, emptyHistoryPort).evaluate(request()));
    }

    @Test
    void rejectsNullHistoryResultRatherThanTreatingItAsNoRefunds() {
        var paymentPort = (PaymentQueryPort) ignored -> Optional.of(new PaymentSnapshot(
                PAYMENT_ID, CUSTOMER_ID, Money.of("125.00", "CAD"), PaymentStatus.CAPTURED, Optional.of(NOW)));
        var brokenHistoryPort = (RefundHistoryQueryPort) ignored -> null;
        assertThrows(NullPointerException.class, () -> service(paymentPort, brokenHistoryPort).evaluate(request()));
    }

    private DefaultEvaluateRefundRequestService service(
            PaymentQueryPort paymentPort,
            RefundHistoryQueryPort historyPort) {
        return new DefaultEvaluateRefundRequestService(
                paymentPort,
                historyPort,
                new RefundEligibilityPolicy(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private RefundRequest request() {
        return new RefundRequest(
                RefundRequestId.newId(),
                PAYMENT_ID,
                CUSTOMER_ID,
                Money.of("25.00", "CAD"),
                RefundReasonCode.ITEM_DAMAGED,
                NOW.minusSeconds(30));
    }
}
